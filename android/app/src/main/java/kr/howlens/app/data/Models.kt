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
