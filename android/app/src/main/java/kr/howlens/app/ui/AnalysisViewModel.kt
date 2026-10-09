package kr.howlens.app.ui

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.howlens.app.data.*

enum class Phase { INPUT, LOADING, RESULT, ERROR }
data class AnalysisUiState(
    val deviceId: String = "server", val question: String = "", val photo: Photo? = null,
    val photoLoading: Boolean = false, val offline: Boolean = true,
    val scenario: FakeScenario = FakeScenario.MORE_INFORMATION,
    val baseUrl: String = "http://10.0.2.2:8000/", val phase: Phase = Phase.INPUT,
    val analysis: Analysis? = null, val error: String? = null, val retryable: Boolean = false
)
class AnalysisViewModel : ViewModel() {
    private val mutable = MutableStateFlow(AnalysisUiState())
    val state = mutable.asStateFlow()
    private var job: Job? = null
    private fun edit(change: (AnalysisUiState) -> AnalysisUiState) {
        job?.cancel()
        mutable.update { change(it).copy(phase = Phase.INPUT, analysis = null, error = null, retryable = false) }
    }
    fun device(value: String) = edit { it.copy(deviceId = value) }
    fun question(value: String) = edit { it.copy(question = value) }
    fun offline(value: Boolean) = edit { it.copy(offline = value) }
    fun scenario(value: FakeScenario) = edit { it.copy(scenario = value) }
    fun baseUrl(value: String) = edit { it.copy(baseUrl = value) }
    fun photoLoading() = edit { it.copy(photo = null, photoLoading = true) }
    fun photo(value: Photo) = edit { it.copy(photo = value, photoLoading = false) }
    fun inputError(message: String) = edit { it.copy(photo = null, photoLoading = false) }.also {
        mutable.update { it.copy(error = message) }
    }
    fun selectPhoto(resolver: ContentResolver, uri: Uri) {
        photoLoading()
        job = viewModelScope.launch {
            try {
                val loaded = PhotoLoader.load(resolver, uri)
                mutable.update { it.copy(photo = loaded, photoLoading = false) }
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                mutable.update { it.copy(photo = null, photoLoading = false, error = e.message ?: "사진을 읽을 수 없습니다.") }
            }
        }
    }
    fun analyze() {
        if (state.value.phase == Phase.LOADING || state.value.photoLoading) return
        val input = state.value
        val invalid = InputRules.validate(input.deviceId, input.question, input.photo)
        if (invalid != null) { mutable.update { it.copy(error = invalid, retryable = false) }; return }
        job = viewModelScope.launch {
            mutable.update { it.copy(phase = Phase.LOADING, error = null, analysis = null, retryable = false) }
            try {
                val repository: AnalysisRepository = if (input.offline) FakeAnalysisRepository(input.scenario)
                    else HttpAnalysisRepository(input.baseUrl.trim())
                val result = repository.analyze(input.deviceId, input.question, requireNotNull(input.photo))
                mutable.update { it.copy(phase = Phase.RESULT, analysis = result) }
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                mutable.update { it.copy(phase = Phase.ERROR,
                    error = if (e is ApiFailure) e.message else "설정 또는 응답을 확인하세요: ${e.message.orEmpty()}",
                    retryable = (e as? ApiFailure)?.retryable ?: false) }
            }
        }
    }
    fun cancel() = edit { it }
}
