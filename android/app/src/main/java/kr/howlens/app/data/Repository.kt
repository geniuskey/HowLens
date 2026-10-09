package kr.howlens.app.data

import java.io.IOException
import java.io.InterruptedIOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

interface AnalysisRepository {
    suspend fun analyze(deviceId: String, question: String, photo: Photo): Analysis
}
class ApiFailure(val code: String, override val message: String, val retryable: Boolean) : IOException(message)

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

    private suspend inline fun <reified T> request(path: String, body: RequestBody? = null): T = withContext(Dispatchers.IO) {
        val builder = Request.Builder().url(base.newBuilder().encodedPath(path).build())
        if (body != null) builder.post(body)
        try {
            client.newCall(builder.build()).execute().use { response ->
                val text = response.body?.string().orEmpty()
                if (!response.isSuccessful) throw ApiErrors.parse(response.code, text)
                json.decodeFromString<T>(text)
            }
        } catch (e: SocketTimeoutException) {
            throw ApiFailure("timeout", "요청 시간이 초과되었습니다. 다시 시도할 수 있습니다.", true)
        } catch (e: InterruptedIOException) {
            throw ApiFailure("timeout", "요청 시간이 초과되었습니다. 다시 시도할 수 있습니다.", true)
        } catch (e: SerializationException) {
            throw ApiFailure("invalid_response", "서버 응답 형식을 확인할 수 없습니다.", false)
        } catch (e: IOException) {
            if (e is ApiFailure) throw e
            throw ApiFailure("network", "서버에 연결할 수 없습니다. 주소와 연결을 확인하세요.", true)
        }
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
    // W1 has no visual-generation UI. These guarded contract methods are ready for integration.
    suspend fun createVisual(analysis: Analysis): VisualJob {
        require(analysis.canRequestVisual) { "검증된 live guide만 이미지 요청 가능" }
        return request("/analyses/${safeId(analysis.analysisId)}/visual", ByteArray(0).toRequestBody())
    }
    suspend fun visualJob(analysis: Analysis, jobId: String): VisualJob {
        require(analysis.canRequestVisual)
        return request("/visual-jobs/${safeId(jobId)}")
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
