package kr.howlens.app.ui

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import kr.howlens.app.data.*

enum class Phase { INPUT, LOADING, RESULT, ERROR }
data class AnalysisUiState(
    val deviceId: String = "server", val question: String = "", val photo: Photo? = null,
    val photoLoading: Boolean = false, val offline: Boolean = true,
    val scenario: FakeScenario = FakeScenario.MORE_INFORMATION,
    val baseUrl: String = "http://10.0.2.2:8000/", val phase: Phase = Phase.INPUT,
    val analysis: Analysis? = null, val error: String? = null, val retryable: Boolean = false,
    val visualLoading: Boolean = false, val visualAttempts: Int = 0,
    val visualPanels: List<Panel> = emptyList(), val visualImages: Map<Int, ByteArray> = emptyMap(),
    val visualError: String? = null, val verificationPhoto: Photo? = null,
    val verificationPhotoLoading: Boolean = false, val verificationBusy: Boolean = false,
    val verification: Verification? = null, val verificationError: String? = null,
    val confirmation: String = ""
)
class AnalysisViewModel(initialState: AnalysisUiState = AnalysisUiState()) : ViewModel() {
    private val mutable = MutableStateFlow(initialState)
    val state = mutable.asStateFlow()
    private var analysisJob: Job? = null
    private var visualTask: Job? = null
    private var verificationTask: Job? = null
    private var workGeneration = 0L
    private var visualGeneration = 0L
    private var verificationGeneration = 0L

    private fun invalidateWork() {
        workGeneration++
        visualGeneration++
        verificationGeneration++
        analysisJob?.cancel(); visualTask?.cancel(); verificationTask?.cancel()
        analysisJob = null; visualTask = null; verificationTask = null
    }

    private fun edit(change: (AnalysisUiState) -> AnalysisUiState) {
        invalidateWork()
        mutable.update { change(it).copy(phase = Phase.INPUT, analysis = null, error = null, retryable = false,
            visualLoading = false, visualAttempts = 0, visualPanels = emptyList(), visualImages = emptyMap(),
            visualError = null, verificationPhoto = null, verificationPhotoLoading = false,
            verificationBusy = false, verification = null, verificationError = null) }
    }
    fun device(value: String) = edit { it.copy(deviceId = value) }
    fun question(value: String) = edit { it.copy(question = value) }
    fun offline(value: Boolean) = edit { it.copy(offline = value) }
    fun scenario(value: FakeScenario) = edit { it.copy(scenario = value) }
    fun baseUrl(value: String) = edit { it.copy(baseUrl = value) }
    fun showInput() { mutable.update { if (it.analysis != null) it.copy(phase = Phase.INPUT) else it } }
    fun showResult() { mutable.update { if (it.analysis != null) it.copy(phase = Phase.RESULT) else it } }
    fun clearPhoto() = edit { it.copy(photo = null, photoLoading = false) }
    fun photoLoading() = edit { it.copy(photo = null, photoLoading = true) }
    fun photo(value: Photo) = edit { it.copy(photo = value, photoLoading = false) }
    fun inputError(message: String) {
        edit { it.copy(photo = null, photoLoading = false) }
        mutable.update { it.copy(error = message) }
    }
    fun selectPhoto(resolver: ContentResolver, uri: Uri, deleteFileAfterRead: Boolean = false) {
        photoLoading()
        val generation = workGeneration
        analysisJob = viewModelScope.launch {
            try {
                val loaded = PhotoLoader.load(resolver, uri)
                if (generation == workGeneration) mutable.update { it.copy(photo = loaded, photoLoading = false) }
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                if (generation == workGeneration) mutable.update { it.copy(photo = null, photoLoading = false, error = e.message ?: "사진을 읽을 수 없습니다.") }
            } finally {
                if (deleteFileAfterRead && uri.scheme == "file") uri.path?.let { File(it).delete() }
            }
        }
    }
    fun selectVerificationPhoto(resolver: ContentResolver, uri: Uri) {
        val generation = workGeneration
        val analysisId = state.value.analysis?.analysisId ?: return
        val request = ++verificationGeneration
        verificationTask?.cancel()
        mutable.update { it.copy(verificationPhoto = null, verificationPhotoLoading = true,
            verification = null, verificationError = null) }
        verificationTask = viewModelScope.launch {
            try {
                val loaded = PhotoLoader.load(resolver, uri)
                updateVerification(generation, request, analysisId) { it.copy(verificationPhoto = loaded, verificationPhotoLoading = false) }
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                updateVerification(generation, request, analysisId) { it.copy(verificationPhotoLoading = false, verificationError = e.message ?: "사진을 읽을 수 없습니다.") }
            }
        }
    }
    fun confirmation(value: String) { mutable.update { it.copy(confirmation = value.take(2000)) } }
    fun analyze() {
        if (state.value.phase == Phase.LOADING || state.value.photoLoading) return
        val input = state.value
        val invalid = InputRules.validate(input.deviceId, input.question, input.photo)
        if (invalid != null) { mutable.update { it.copy(error = invalid, retryable = false) }; return }
        invalidateWork()
        val generation = workGeneration
        mutable.update { it.copy(phase = Phase.LOADING, error = null, analysis = null, retryable = false,
            visualLoading = false, visualPanels = emptyList(), visualImages = emptyMap(), visualError = null,
            verificationPhotoLoading = false, verificationBusy = false, verification = null, verificationError = null) }
        analysisJob = viewModelScope.launch {
            try {
                val repository: AnalysisRepository = if (input.offline) FakeAnalysisRepository(input.scenario)
                    else HttpAnalysisRepository(input.baseUrl.trim())
                val result = repository.analyze(input.deviceId, input.question, requireNotNull(input.photo))
                if (generation == workGeneration) mutable.update { it.copy(phase = Phase.RESULT, analysis = result, visualAttempts = 0,
                    visualPanels = emptyList(), visualImages = emptyMap(), visualError = null,
                    verificationPhoto = null, verification = null, verificationError = null) }
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                if (generation == workGeneration) mutable.update { it.copy(phase = Phase.ERROR,
                    error = if (e is ApiFailure) e.message else "설정 또는 응답을 확인하세요: ${e.message.orEmpty()}",
                    retryable = (e as? ApiFailure)?.retryable ?: false) }
            }
        }
    }
    fun requestVisual() {
        val input = state.value
        val analysis = input.analysis ?: return
        if (!analysis.canRequestVisual || input.offline || input.visualLoading || input.visualAttempts >= 2) return
        val attempt = input.visualAttempts + 1
        val work = workGeneration
        val request = ++visualGeneration
        val analysisId = analysis.analysisId
        visualTask?.cancel()
        visualTask = viewModelScope.launch {
            updateVisual(work, request, analysisId) { it.copy(visualLoading = true, visualAttempts = attempt, visualError = null,
                visualPanels = emptyList(), visualImages = emptyMap()) }
            try {
                val repo = HttpAnalysisRepository(input.baseUrl.trim())
                var visual = repo.createVisual(analysis)
                require(visual.analysisId == analysis.analysisId && visual.mode == Mode.LIVE) {
                    "서버 이미지 작업이 요청 분석과 일치하지 않습니다. 텍스트 안내는 유지됩니다."
                }
                var polls = 0
                while (visual.status == VisualStatus.QUEUED || visual.status == VisualStatus.RUNNING) {
                    if (polls++ >= MAX_VISUAL_POLLS) throw ApiFailure("visual_timeout",
                        "시각 안내 응답을 기다리지 못했어요. 승인된 텍스트 단계는 유지됩니다.", true)
                    delay(1200)
                    visual = repo.visualJob(analysis, visual.jobId)
                    require(visual.analysisId == analysis.analysisId && visual.mode == Mode.LIVE) {
                        "서버 이미지 작업 응답이 요청 분석과 일치하지 않습니다."
                    }
                }
                if (visual.status == VisualStatus.FAILED) throw ApiFailure("visual_failed",
                    visual.error ?: "이미지를 만들지 못했습니다. 문서 근거 텍스트를 확인하세요.", true)
                val expected = analysis.visibleSteps.map { it.stepId }.toSet()
                require(visual.status == VisualStatus.COMPLETED && visual.panels.size == 9 &&
                    visual.panels.map { it.index } == (0..8).toList() && visual.panels.all { it.stepId in expected }) {
                    "패널 9개 또는 단계 연결을 검증할 수 없습니다. 텍스트 안내는 유지됩니다."
                }
                updateVisual(work, request, analysisId) { it.copy(visualPanels = visual.panels) }
                val images = coroutineScope {
                    visual.panels.map { panel -> async { panel.index to repo.visualAsset(analysis, panel.imageUrl) } }.awaitAll().toMap()
                }
                updateVisual(work, request, analysisId) { it.copy(visualImages = images, visualLoading = false) }
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                updateVisual(work, request, analysisId) { it.copy(visualLoading = false,
                    visualPanels = emptyList(), visualImages = emptyMap(),
                    visualError = if (e is ApiFailure) e.message else "시각 안내를 표시하지 못했습니다: ${e.message ?: "응답 확인 오류"}. 문서 근거 텍스트는 유지됩니다.") }
            }
        }
    }
    fun cancelVisual() {
        visualGeneration++
        visualTask?.cancel(); visualTask = null
        mutable.update { it.copy(visualLoading = false, visualError = "시각 이미지 요청을 취소했습니다. 기존 텍스트 안내는 유지됩니다.") }
    }
    fun verify() {
        val input = state.value
        val analysis = input.analysis ?: return
        val photo = input.verificationPhoto ?: run { mutable.update { it.copy(verificationError = "비교할 전후 사진을 선택하세요.") }; return }
        if (!analysis.canRequestVisual || input.offline || input.verificationBusy) return
        val issue = InputRules.validate(analysis.deviceId, "verification", photo)
        if (issue != null) { mutable.update { it.copy(verificationError = issue) }; return }
        val work = workGeneration
        val request = ++verificationGeneration
        val analysisId = analysis.analysisId
        verificationTask?.cancel()
        verificationTask = viewModelScope.launch {
            updateVerification(work, request, analysisId) { it.copy(verificationBusy = true, verificationError = null, verification = null) }
            try {
                val result = HttpAnalysisRepository(input.baseUrl.trim()).verify(analysis, photo, input.confirmation)
                require(result.mode == Mode.LIVE && result.analysisId == analysis.analysisId &&
                    result.evidenceIds.all { id -> analysis.evidence.any { it.evidenceId == id } }) {
                    "비교 응답을 원 분석 근거와 연결할 수 없습니다."
                }
                updateVerification(work, request, analysisId) { it.copy(verificationBusy = false, verification = result) }
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) { updateVerification(work, request, analysisId) { it.copy(verificationBusy = false,
                verificationError = if (e is ApiFailure) e.message else "전후 사진을 비교하지 못했습니다: ${e.message ?: "응답 확인 오류"}") } }
        }
    }
    private fun updateVisual(work: Long, request: Long, analysisId: String, transform: (AnalysisUiState) -> AnalysisUiState) {
        if (work != workGeneration || request != visualGeneration || mutable.value.analysis?.analysisId != analysisId) return
        mutable.update { current -> if (work == workGeneration && request == visualGeneration && current.analysis?.analysisId == analysisId) transform(current) else current }
    }
    private fun updateVerification(work: Long, request: Long, analysisId: String, transform: (AnalysisUiState) -> AnalysisUiState) {
        if (work != workGeneration || request != verificationGeneration || mutable.value.analysis?.analysisId != analysisId) return
        mutable.update { current -> if (work == workGeneration && request == verificationGeneration && current.analysis?.analysisId == analysisId) transform(current) else current }
    }
    fun cancelVerification() {
        verificationGeneration++
        verificationTask?.cancel(); verificationTask = null
        mutable.update { it.copy(verificationBusy = false, verificationError = "전후 사진 비교를 취소했습니다.") }
    }
    fun cancel() = edit { it }

    private companion object { const val MAX_VISUAL_POLLS = 60 }
}
