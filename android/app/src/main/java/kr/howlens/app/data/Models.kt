package kr.howlens.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable enum class Decision {
    @SerialName("guide") GUIDE,
    @SerialName("needs_more_information") NEEDS_MORE_INFORMATION,
    @SerialName("stop") STOP
}
@Serializable enum class Mode { @SerialName("live") LIVE, @SerialName("mock") MOCK }
@Serializable enum class ConditionStatus {
    @SerialName("satisfied") SATISFIED,
    @SerialName("unsatisfied") UNSATISFIED,
    @SerialName("unknown") UNKNOWN
}
@Serializable data class Evidence(
    @SerialName("evidence_id") val evidenceId: String,
    @SerialName("document_id") val documentId: String,
    @SerialName("document_version") val documentVersion: String,
    @SerialName("pdf_page") val pdfPage: Int,
    @SerialName("printed_page") val printedPage: String?,
    val section: String, val quote: String,
    @SerialName("source_url") val sourceUrl: String
)
@Serializable data class Precondition(
    @SerialName("precondition_id") val preconditionId: String,
    val description: String, val status: ConditionStatus, val required: Boolean,
    @SerialName("evidence_ids") val evidenceIds: List<String>
)
@Serializable data class Step(
    @SerialName("step_id") val stepId: String, val description: String,
    @SerialName("evidence_ids") val evidenceIds: List<String>,
    @SerialName("visual_hint") val visualHint: String
)
@Serializable data class Analysis(
    @SerialName("analysis_id") val analysisId: String,
    @SerialName("device_id") val deviceId: String,
    val decision: Decision, val observations: List<String>, val evidence: List<Evidence>,
    val preconditions: List<Precondition>, val steps: List<Step>, val warnings: List<String>,
    @SerialName("missing_information") val missingInformation: List<String>, val mode: Mode
) {
    // Never promote a server decision. Defense in depth also hides malformed guide responses.
    val canShowSteps: Boolean get() = mode == Mode.LIVE && decision == Decision.GUIDE && steps.size in 1..9 &&
        preconditions.none { it.required && it.status != ConditionStatus.SATISFIED } &&
        steps.all { step -> step.evidenceIds.isNotEmpty() && step.evidenceIds.all { id ->
            evidence.any { it.evidenceId == id && it.pdfPage > 0 }
        } }
    val visibleSteps: List<Step> get() = if (canShowSteps) steps else emptyList()
    val canRequestVisual: Boolean get() = canShowSteps && mode == Mode.LIVE
}
@Serializable data class Health(val status: String, val mode: Mode)
@Serializable enum class VisualStatus {
    @SerialName("queued") QUEUED, @SerialName("running") RUNNING,
    @SerialName("completed") COMPLETED, @SerialName("failed") FAILED
}
@Serializable data class Panel(val index: Int, @SerialName("step_id") val stepId: String,
    @SerialName("image_url") val imageUrl: String)
@Serializable data class VisualJob(
    @SerialName("job_id") val jobId: String, @SerialName("analysis_id") val analysisId: String,
    val status: VisualStatus, @SerialName("image_url") val imageUrl: String?,
    val panels: List<Panel>, val error: String?, val mode: Mode
)
@Serializable enum class VerificationResult {
    @SerialName("observed_change") OBSERVED_CHANGE,
    @SerialName("issue_remaining") ISSUE_REMAINING, @SerialName("inconclusive") INCONCLUSIVE
}
@Serializable data class Verification(
    @SerialName("analysis_id") val analysisId: String, val result: VerificationResult,
    val observations: List<String>, @SerialName("evidence_ids") val evidenceIds: List<String>,
    @SerialName("missing_information") val missingInformation: List<String>,
    val limitations: List<String>, val mode: Mode
)

/** Evidence-only user-reviewed handoff; arbitrary text from photos/models is omitted. */
object SharePreview {
    private val credentialKeys = setOf(
        "token", "access_token", "auth", "authorization", "signature", "sig", "key", "password", "secret"
    )

    private fun containsCredentialParameter(raw: String?): Boolean {
        if (raw.isNullOrEmpty()) return false
        return raw.split('&', ';', '?').any { parameter ->
            var key = parameter.substringBefore('=')
            for (attempt in 0..8) {
                if (key.lowercase(java.util.Locale.ROOT) in credentialKeys) return true
                val decoded = runCatching {
                    java.net.URLDecoder.decode(key, Charsets.UTF_8.name())
                }.getOrNull() ?: return@any true
                if (decoded == key) return@any false
                if (attempt == 8) return@any true
                key = decoded
            }
            false
        }
    }

    fun build(analysis: Analysis): String = buildString {
        appendLine("HowLens 장비 참고 요약")
        appendLine("장비: ${InputRules.devices[analysis.deviceId] ?: "알 수 없는 장비"}")
        appendLine("판정: ${when (analysis.decision) {
            Decision.GUIDE -> "문서 근거 안내"
            Decision.NEEDS_MORE_INFORMATION -> "추가 정보 필요"
            Decision.STOP -> "작업 중단"
        }}")
        appendLine("모드: ${if (analysis.mode == Mode.LIVE) "LIVE 서버 응답" else "MOCK 합성 예시"}")
        val publicEvidence = analysis.evidence.filter { evidence ->
            runCatching { java.net.URI(evidence.sourceUrl).let { uri ->
                uri.scheme == "https" && !uri.host.isNullOrBlank() && uri.userInfo == null &&
                    !containsCredentialParameter(uri.rawQuery) && !containsCredentialParameter(uri.rawFragment)
            } }.getOrDefault(false)
        }
        if (publicEvidence.isNotEmpty()) {
            appendLine("공개 문서 출처")
            publicEvidence.forEach { evidence ->
                appendLine("• ${evidence.documentId} · 버전 ${evidence.documentVersion} · PDF ${evidence.pdfPage}쪽 · 인쇄 ${evidence.printedPage ?: "표기 없음"}")
                appendLine(evidence.sourceUrl)
            }
        }
        append("한계: 사진과 문서 참고만 제공하며 안전, 작업 성공, 정상 동작을 보증하지 않습니다. 별도 기능 시험과 담당자 확인이 필요합니다.")
    }
}

data class Photo(val bytes: ByteArray, val mime: String, val width: Int, val height: Int)
object InputRules {
    const val MAX_BYTES = 10 * 1024 * 1024
    val devices = linkedMapOf("server" to "Dell PowerEdge R750", "cobot" to "UR5e", "ups" to "APC Smart-UPS")
    fun questionLength(question: String): Int = question.trim().let { it.codePointCount(0, it.length) }
    fun validate(device: String, question: String, photo: Photo?): String? = when {
        device !in devices -> "장비를 선택하세요."
        questionLength(question) !in 1..2000 -> "질문은 공백 제거 후 1–2,000자여야 합니다."
        photo == null -> "JPEG 또는 PNG 사진을 선택하세요."
        photo.mime !in setOf("image/jpeg", "image/png") -> "JPEG/PNG만 사용할 수 있습니다."
        photo.bytes.isEmpty() || photo.bytes.size > MAX_BYTES -> "사진은 0 초과, 10 MiB 이하여야 합니다."
        photo.width <= 0 || photo.height <= 0 || photo.width.toLong() * photo.height > 20_000_000 -> "디코딩 가능한 20MP 이하 사진이 필요합니다."
        else -> null
    }
}
