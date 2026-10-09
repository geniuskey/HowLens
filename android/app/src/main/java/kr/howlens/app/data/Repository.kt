package kr.howlens.app.data

import java.io.IOException
import java.io.InterruptedIOException
import java.io.ByteArrayOutputStream
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resumeWithException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response

interface AnalysisRepository {
    suspend fun analyze(deviceId: String, question: String, photo: Photo): Analysis
}
class ApiFailure(val code: String, override val message: String, val retryable: Boolean) : IOException(message)

/** Validates downloaded panels with Android's decoder while bounding native bitmap memory. */
internal object PngAssetValidator {
    private const val MAX_PIXELS = 20_000_000L
    private const val MAX_DECODE_PIXELS = 4_000_000L

    fun requireDecodable(bytes: ByteArray) {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        val width = bounds.outWidth
        val height = bounds.outHeight
        if (width <= 0 || height <= 0 || width.toLong() * height.toLong() > MAX_PIXELS) {
            throw invalidImage()
        }

        var sampleSize = 1
        fun sampledPixels(sample: Int): Long {
            val divisor = sample.toLong()
            val sampledWidth = (width.toLong() + divisor - 1) / divisor
            val sampledHeight = (height.toLong() + divisor - 1) / divisor
            return sampledWidth * sampledHeight
        }
        while (sampledPixels(sampleSize) > MAX_DECODE_PIXELS) {
            sampleSize *= 2
        }
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = false
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.RGB_565
        }
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options) ?: throw invalidImage()
        bitmap.recycle()
    }

    private fun invalidImage() = ApiFailure(
        "invalid_image", "서버 패널 응답이 유효한 PNG가 아닙니다. 텍스트 안내는 유지됩니다.", false
    )
}

object ApiErrors {
    fun parse(status: Int, body: String): ApiFailure {
        val fallback = ApiFailure("http_$status", "서버 오류 ($status)", status in setOf(408, 429, 503, 504))
        return runCatching {
            when (val detail = Json.parseToJsonElement(body).jsonObject["detail"]) {
                is JsonObject -> ApiFailure(
                    detail["code"]?.jsonPrimitive?.content ?: fallback.code,
                    detail["message"]?.jsonPrimitive?.content ?: fallback.message,
                    detail["retryable"]?.jsonPrimitive?.booleanOrNull ?: fallback.retryable
                )
                is JsonArray -> ApiFailure("validation", detail.joinToString("\n") {
                    it.jsonObject["msg"]?.jsonPrimitive?.content ?: "입력 필드를 확인하세요."
                }, false)
                else -> fallback
            }
        }.getOrDefault(fallback)
    }
}

class HttpAnalysisRepository(baseUrl: String, private val client: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS).readTimeout(45, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS).callTimeout(60, TimeUnit.SECONDS)
    .retryOnConnectionFailure(false).followRedirects(false).followSslRedirects(false).build()) : AnalysisRepository {
    private val base = baseUrl.toHttpUrl().also {
        require(it.encodedPath == "/" && it.query == null && it.fragment == null && it.username.isEmpty() && it.password.isEmpty()) {
            "API 주소는 인증정보·쿼리 없는 서버 루트여야 합니다."
        }
    }
    private val json = Json { ignoreUnknownKeys = true }
    companion object { const val MAX_RESPONSE_BYTES = 2 * 1024 * 1024 }

    private suspend inline fun <reified T> request(path: String, body: RequestBody? = null): T = withContext(Dispatchers.IO) {
        val builder = Request.Builder().url(base.newBuilder().encodedPath(path).build())
        if (body != null) builder.post(body)
        try {
            val text = cancellableTextCall(client.newCall(builder.build()))
            json.decodeFromString<T>(text)
        } catch (e: InterruptedIOException) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            throw ApiFailure("timeout", "요청 시간이 초과되었습니다. 다시 시도할 수 있습니다.", true)
        } catch (e: SerializationException) {
            throw ApiFailure("invalid_response", "서버 응답 형식을 확인할 수 없습니다.", false)
        } catch (e: ApiFailure) {
            throw e
        } catch (e: IOException) {
            throw ApiFailure("network", "서버에 연결할 수 없습니다. 주소와 연결을 확인하세요.", true)
        }
    }
    private suspend fun cancellableTextCall(call: Call): String = suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation { call.cancel() }
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (continuation.isActive) continuation.resumeWithException(e)
            }
            override fun onResponse(call: Call, response: Response) {
                try {
                    response.use {
                        val bytes = readBounded(it.body?.source(), MAX_RESPONSE_BYTES, "서버 응답이 너무 큽니다. 요청을 줄이거나 서버 응답을 확인하세요.")
                        val text = bytes.toString(Charsets.UTF_8)
                        if (!it.isSuccessful) throw ApiErrors.parse(it.code, text)
                        if (continuation.isActive) continuation.resumeWith(Result.success(text))
                    }
                } catch (e: Exception) {
                    if (continuation.isActive) continuation.resumeWithException(e)
                }
            }
        })
    }
    private fun readBounded(source: okio.BufferedSource?, limit: Int, error: String): ByteArray {
        if (source == null) return byteArrayOf()
        val output = ByteArrayOutputStream()
        val chunk = ByteArray(8192)
        while (true) {
            val count = source.read(chunk)
            if (count < 0) break
            if (output.size() + count > limit) throw ApiFailure("response_too_large", error, false)
            output.write(chunk, 0, count)
        }
        return output.toByteArray()
    }
    suspend fun health(): Health = request("/health")
    override suspend fun analyze(deviceId: String, question: String, photo: Photo): Analysis {
        require(InputRules.validate(deviceId, question, photo) == null)
        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("device_id", deviceId).addFormDataPart("question", question.trim())
            .addFormDataPart("photo", if (photo.mime == "image/png") "photo.png" else "photo.jpg",
                photo.bytes.toRequestBody(photo.mime.toMediaType())).build()
        return request<Analysis>("/analyses", body).also {
            if (it.deviceId != deviceId) throw ApiFailure("invalid_response", "요청 장비와 응답 장비가 다릅니다.", false)
        }
    }
    suspend fun createVisual(analysis: Analysis): VisualJob {
        require(analysis.canRequestVisual) { "검증된 live guide만 이미지 요청 가능" }
        return request("/analyses/${safeId(analysis.analysisId)}/visual", ByteArray(0).toRequestBody())
    }
    suspend fun visualJob(analysis: Analysis, jobId: String): VisualJob {
        require(analysis.canRequestVisual)
        require(jobId.matches(Regex("[A-Za-z0-9_-]+")))
        return request("/visual-jobs/${safeId(jobId)}")
    }
    suspend fun visualAsset(analysis: Analysis, path: String): ByteArray {
        require(analysis.canRequestVisual)
        val url = assetUrl(path)
        return withContext(Dispatchers.IO) {
            try {
                val call = client.newCall(Request.Builder().url(url).build())
                val bytes = cancellableBytesCall(call)
                bytes
            } catch (e: InterruptedIOException) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                throw ApiFailure("timeout", "이미지 다운로드 시간이 초과되었습니다.", true)
            } catch (e: ApiFailure) { throw e
            } catch (e: IOException) { throw ApiFailure("network", "서버 이미지에 연결할 수 없습니다.", true) }
        }
    }
    private suspend fun cancellableBytesCall(call: Call): ByteArray = suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation { call.cancel() }
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { if (continuation.isActive) continuation.resumeWithException(e) }
            override fun onResponse(call: Call, response: Response) {
                try { response.use {
                    if (!it.isSuccessful) throw ApiErrors.parse(it.code, "")
                    val contentType = it.body?.contentType()?.let { type -> "${type.type}/${type.subtype}" }
                    if (contentType != "image/png") throw ApiFailure("invalid_image", "서버 패널은 PNG 이미지여야 합니다.", false)
                    val bytes = readBounded(it.body?.source(), MAX_RESPONSE_BYTES, "서버 이미지가 너무 큽니다.")
                    val pngSignature = byteArrayOf(0x89.toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a)
                    if (bytes.size < pngSignature.size || !pngSignature.indices.all { index -> bytes[index] == pngSignature[index] }) {
                        throw ApiFailure("invalid_image", "서버 패널 응답이 유효한 PNG가 아닙니다. 텍스트 안내는 유지됩니다.", false)
                    }
                    PngAssetValidator.requireDecodable(bytes)
                    if (continuation.isActive) continuation.resumeWith(Result.success(bytes))
                } } catch (e: Exception) { if (continuation.isActive) continuation.resumeWithException(e) }
            }
        })
    }
    suspend fun verify(analysis: Analysis, photo: Photo, confirmation: String = ""): Verification {
        require(analysis.canRequestVisual)
        require(InputRules.validate(analysis.deviceId, "verification", photo) == null)
        require(confirmation.codePointCount(0, confirmation.length) <= 2000)
        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("photo", if (photo.mime == "image/png") "photo.png" else "photo.jpg",
                photo.bytes.toRequestBody(photo.mime.toMediaType()))
            .addFormDataPart("user_confirmation", confirmation).build()
        return request<Verification>("/analyses/${safeId(analysis.analysisId)}/verification", body).also { result ->
            require(result.analysisId == analysis.analysisId && result.evidenceIds.all { id -> analysis.evidence.any { it.evidenceId == id } })
        }
    }
    fun assetUrl(path: String): String {
        require(path.startsWith("/visual-assets/") && !path.contains("..") && !path.contains('\\') && !path.contains('%') && !path.contains('?') && !path.contains('#'))
        return base.newBuilder().encodedPath(path).build().toString()
    }
    private fun safeId(id: String): String = id.also { require(it.matches(Regex("[A-Za-z0-9_-]+"))) }
}

enum class FakeScenario { MORE_INFORMATION, STOP }
class FakeAnalysisRepository(private val scenario: FakeScenario) : AnalysisRepository {
    override suspend fun analyze(deviceId: String, question: String, photo: Photo): Analysis {
        require(InputRules.validate(deviceId, question, photo) == null)
        delay(450)
        return Analysis("offline-synthetic", deviceId,
            if (scenario == FakeScenario.STOP) Decision.STOP else Decision.NEEDS_MORE_INFORMATION,
            listOf("합성 UI 예시입니다. 실제 사진이나 질문을 분석하지 않았습니다."), emptyList(),
            listOf(Precondition("identity", "장비 모델과 작업 환경 확인", ConditionStatus.UNKNOWN, true, emptyList())),
            emptyList(), listOf("이 결과는 작업 허가나 안전·정상 동작 보증이 아닙니다."),
            if (scenario == FakeScenario.STOP) listOf("작업을 중단하고 담당자에게 확인하세요. (합성 예시)")
            else listOf("모델 식별 사진과 검증된 문서 근거가 필요합니다. (합성 예시)"), Mode.MOCK)
    }
}
