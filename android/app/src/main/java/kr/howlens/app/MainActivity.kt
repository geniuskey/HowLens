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
        state.analysis?.let { AnalysisResult(it) }
        Text("사진만으로 설비의 안전·정상 동작을 보증하지 않습니다.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
internal fun AnalysisResult(analysis: Analysis) {
    val context = LocalContext.current
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
        Text("설명 이미지 통합은 다음 단계에서 제공됩니다. 텍스트 안내는 유지됩니다.")
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
