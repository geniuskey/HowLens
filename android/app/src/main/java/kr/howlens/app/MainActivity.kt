package kr.howlens.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import android.graphics.BitmapFactory
import java.io.File
import kr.howlens.app.data.*
import kr.howlens.app.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { Surface { HowLensScreen() } } }
    }
}

@Composable
fun HowLensScreen(vm: AnalysisViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var pendingCamera by rememberSaveable { mutableStateOf<String?>(null) }
    val busy = state.phase == Phase.LOADING || state.photoLoading
    fun load(uri: Uri) {
        vm.selectPhoto(context.contentResolver, uri)
    }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) load(uri)
    }
    val verificationGallery = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.selectVerificationPhoto(context.contentResolver, uri)
    }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) pendingCamera?.let { load(Uri.parse(it)) }
        pendingCamera = null
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("HowLens", style = MaterialTheme.typography.headlineLarge)
        Text("사진과 문서 근거로 확인하는 장비 작업 도우미")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Switch(checked = state.offline, onCheckedChange = vm::offline, enabled = !busy)
            Text(if (state.offline) "오프라인 MOCK · 합성 UI 테스트" else "서버 API 연결")
        }
        if (state.offline) {
            Text("실제 사진 분석이 아닙니다. 작업 허가로 사용할 수 없습니다.", color = MaterialTheme.colorScheme.error)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = state.scenario == FakeScenario.MORE_INFORMATION,
                    onClick = { vm.scenario(FakeScenario.MORE_INFORMATION) }, enabled = !busy, label = { Text("추가 정보 예시") })
                FilterChip(selected = state.scenario == FakeScenario.STOP,
                    onClick = { vm.scenario(FakeScenario.STOP) }, enabled = !busy, label = { Text("중단 예시") })
            }
        } else {
            OutlinedTextField(value = state.baseUrl, onValueChange = vm::baseUrl, enabled = !busy,
                label = { Text("API 서버 루트 주소") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Text("에뮬레이터: http://10.0.2.2:8000/ · 실기기: 서버 HTTPS 주소")
        }
        Text("장비", style = MaterialTheme.typography.titleMedium)
        InputRules.devices.forEach { (id, label) ->
            FilterChip(selected = state.deviceId == id, onClick = { vm.device(id) },
                enabled = !busy, label = { Text(label) })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { gallery.launch(arrayOf("image/jpeg", "image/png")) }, enabled = !busy) { Text("갤러리") }
            OutlinedButton(onClick = {
                try {
                    val directory = File(context.cacheDir, "camera").apply { mkdirs() }
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.photos", File(directory, "capture.jpg"))
                    pendingCamera = uri.toString()
                    camera.launch(uri)
                } catch (e: Exception) { pendingCamera = null; vm.inputError("카메라 앱을 사용할 수 없습니다. 갤러리를 이용하세요.") }
            }, enabled = !busy) { Text("카메라") }
        }
        Text("JPEG/PNG · 최대 10 MiB · 최대 20MP")
        if (state.photoLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
        state.photo?.let { photo ->
            Text("선택됨: ${photo.width} × ${photo.height} · ${photo.bytes.size / 1024} KiB")
            val bitmap = remember(photo) { BitmapFactory.decodeByteArray(photo.bytes, 0, photo.bytes.size,
                BitmapFactory.Options().apply { inSampleSize = 8 }) }
            bitmap?.let { Image(it.asImageBitmap(), "선택한 장비 사진", Modifier.fillMaxWidth().height(160.dp)) }
            DisposableEffect(bitmap) { onDispose { bitmap?.recycle() } }
        }
        OutlinedTextField(value = state.question, onValueChange = vm::question, enabled = !busy,
            label = { Text("확인하고 싶은 작업 또는 질문") }, minLines = 3, modifier = Modifier.fillMaxWidth(),
            supportingText = { Text("${InputRules.questionLength(state.question)} / 2,000자") })
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (state.phase == Phase.LOADING) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Text(if (state.offline) "합성 예시를 불러오는 중…" else "서버에서 분석 중…")
            TextButton(onClick = vm::cancel) { Text("취소") }
        } else {
            Button(onClick = vm::analyze, enabled = !state.photoLoading, modifier = Modifier.fillMaxWidth()) {
                Text(if (state.offline) "MOCK 화면 확인" else "분석 요청")
            }
            if (state.phase == Phase.ERROR && state.retryable) {
                OutlinedButton(onClick = vm::analyze) { Text("다시 시도") }
            }
        }
        state.analysis?.let { AnalysisResult(it, state, vm) { verificationGallery.launch(arrayOf("image/jpeg", "image/png")) } }
        Text("사진만으로 설비의 안전·정상 동작을 보증하지 않습니다.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
internal fun AnalysisResult(analysis: Analysis, state: AnalysisUiState, vm: AnalysisViewModel,
    onSelectVerificationPhoto: () -> Unit) {
    val context = LocalContext.current
    var sharePreviewVisible by rememberSaveable(analysis.analysisId) { mutableStateOf(false) }
    var shareError by remember(analysis.analysisId) { mutableStateOf<String?>(null) }
    val shareText = remember(analysis) { SharePreview.build(analysis) }
    HorizontalDivider()
    Text(when (analysis.decision) {
        Decision.GUIDE -> "문서 근거 안내"
        Decision.NEEDS_MORE_INFORMATION -> "추가 정보 필요"
        Decision.STOP -> "작업 중단"
    }, style = MaterialTheme.typography.headlineSmall)
    Text(if (analysis.mode == Mode.MOCK) "MOCK · 합성 예시 · 실제 작업에 사용 금지" else "LIVE · 서버 응답")
    Text("분석 ID: ${analysis.analysisId}")
    ResultLines("관찰", analysis.observations)
    ResultLines("경고", analysis.warnings)
    ResultLines("추가 정보", analysis.missingInformation)
    if (analysis.preconditions.isNotEmpty()) {
        Text("사전 조건", style = MaterialTheme.typography.titleMedium)
        analysis.preconditions.forEach { Text("${it.description} · ${it.status} · ${if (it.required) "필수" else "선택"}") }
    }
    if (analysis.evidence.isNotEmpty()) {
        Text("문서 근거", style = MaterialTheme.typography.titleMedium)
        analysis.evidence.forEach { source ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("${source.evidenceId} · ${source.documentId} · 버전 ${source.documentVersion}")
                    Text("PDF ${source.pdfPage}쪽 · 인쇄 ${source.printedPage ?: "표기 없음"} · ${source.section}")
                    Text(source.quote)
                    Text(source.sourceUrl)
                    val uri = Uri.parse(source.sourceUrl)
                    if (uri.scheme in setOf("https", "http") && !uri.host.isNullOrBlank()) {
                        TextButton(onClick = { runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) } }) { Text("출처 문서 열기") }
                    }
                }
            }
        }
    }
    if (analysis.canShowSteps) {
        Text("단계 · 문서 근거 확인", style = MaterialTheme.typography.titleMedium)
        analysis.visibleSteps.forEachIndexed { index, step ->
            Text("${index + 1}. ${step.description}\n근거: ${step.evidenceIds.joinToString()}")
        }
        Text("설명 이미지는 시각 참고용입니다. 승인된 텍스트 단계와 근거를 먼저 확인하세요.")
        if (state.offline || analysis.mode != Mode.LIVE) {
            Text("MOCK/오프라인 결과에는 시각 이미지를 요청할 수 없습니다.", color = MaterialTheme.colorScheme.error)
        } else if (state.visualLoading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Text("서버에서 단계별 시각 안내를 요청/확인 중…")
            TextButton(onClick = vm::cancelVisual) { Text("시각 요청 취소") }
        } else if (state.visualImages.isNotEmpty()) {
            Text("시각 패널 · 순서대로 확인")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.visualPanels.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        row.forEach { panel ->
                            Card(Modifier.weight(1f)) {
                                Column(Modifier.padding(6.dp)) {
                                    Text("${panel.index + 1} · 단계 ${analysis.steps.indexOfFirst { it.stepId == panel.stepId } + 1} (${panel.stepId})")
                                    val bytes = state.visualImages[panel.index]
                                    val bitmap = remember(bytes) { bytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) } }
                                    bitmap?.let { Image(it.asImageBitmap(), "설명용 단계 ${panel.index + 1} 이미지", Modifier.fillMaxWidth().height(96.dp)) }
                                    DisposableEffect(bitmap) { onDispose { bitmap?.recycle() } }
                                }
                            }
                        }
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        } else {
            state.visualError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = vm::requestVisual, enabled = state.visualAttempts < 2, modifier = Modifier.fillMaxWidth()) {
                Text(if (state.visualAttempts == 0) "단계 이미지 요청" else "사용자 재시도 (${state.visualAttempts}/2)")
            }
            if (state.visualAttempts >= 2) Text("시각 생성은 최대 두 번 요청했습니다. 문서 근거 텍스트를 계속 확인할 수 있습니다.")
        }
        HorizontalDivider()
        Text("전후 사진 비교 · 관찰 참고만 제공", style = MaterialTheme.typography.titleMedium)
        Text("처음 선택한 사진은 원본으로 보관됩니다. 비교할 작업 후 사진을 추가하세요.")
        OutlinedButton(onClick = onSelectVerificationPhoto,
            enabled = !state.verificationPhotoLoading && !state.verificationBusy) { Text("작업 후 사진 선택") }
        if (state.verificationPhotoLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
        state.verificationPhoto?.let { photo ->
            Text("작업 후 사진: ${photo.width} × ${photo.height}")
            val bitmap = remember(photo) { BitmapFactory.decodeByteArray(photo.bytes, 0, photo.bytes.size,
                BitmapFactory.Options().apply { inSampleSize = 8 }) }
            bitmap?.let { Image(it.asImageBitmap(), "작업 후 사진", Modifier.fillMaxWidth().height(160.dp)) }
            DisposableEffect(bitmap) { onDispose { bitmap?.recycle() } }
        }
        OutlinedTextField(value = state.confirmation, onValueChange = vm::confirmation,
            label = { Text("비교에 참고할 사용자 관찰 (선택)") }, enabled = !state.verificationBusy,
            supportingText = { Text("${state.confirmation.codePointCount(0, state.confirmation.length)} / 2,000자") },
            modifier = Modifier.fillMaxWidth())
        state.verificationError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (state.verificationBusy) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Text("사진의 시각적 변화만 확인 중…")
            TextButton(onClick = vm::cancelVerification) { Text("비교 취소") }
        } else {
            Button(onClick = vm::verify, enabled = state.verificationPhoto != null && !state.offline,
                modifier = Modifier.fillMaxWidth()) { Text("전후 사진 비교") }
        }
        state.verification?.let { result ->
            Text("비교 결과 · ${if (result.mode == Mode.MOCK) "MOCK" else "LIVE"}", style = MaterialTheme.typography.titleMedium)
            Text(when (result.result) {
                VerificationResult.OBSERVED_CHANGE -> "관찰된 변화"
                VerificationResult.ISSUE_REMAINING -> "문제가 남아 있는 것으로 관찰됨"
                VerificationResult.INCONCLUSIVE -> "사진만으로 결론을 내릴 수 없음"
            })
            ResultLines("관찰", result.observations)
            ResultLines("추가 확인 필요", result.missingInformation)
            ResultLines("제한", result.limitations)
        }
        Text("전후 사진은 시각적 변화만 관찰합니다. 안전, 성공, 정상 동작을 보증하지 않으며 별도 기능 시험과 담당자 확인이 필요합니다.",
            color = MaterialTheme.colorScheme.error)
    shareError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    OutlinedButton(onClick = { shareError = null; sharePreviewVisible = true }, modifier = Modifier.fillMaxWidth()) {
        Text("근거 요약 공유 미리보기")
    }
    if (sharePreviewVisible) AlertDialog(
        onDismissRequest = { sharePreviewVisible = false },
        title = { Text("공유할 텍스트 확인") },
        text = { Text(shareText, Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())) },
        confirmButton = {
            TextButton(onClick = {
                try {
                    val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, shareText)
                    context.startActivity(Intent.createChooser(send, "HowLens 요약 공유"))
                    sharePreviewVisible = false
                } catch (_: Exception) {
                    shareError = "공유 앱을 열 수 없습니다. 현재 결과는 유지됩니다."
                    sharePreviewVisible = false
                }
            }) { Text("공유 앱 선택") }
        },
        dismissButton = { TextButton(onClick = { sharePreviewVisible = false }) { Text("취소") } }
    )
    } else {
        Text("실행 단계와 이미지가 차단되었습니다.", color = MaterialTheme.colorScheme.error)
    }
}

@Composable private fun ResultLines(title: String, lines: List<String>) {
    if (lines.isNotEmpty()) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        lines.forEach { Text("• $it") }
    }
}
