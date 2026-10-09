@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)

package kr.howlens.app

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.howlens.app.camera.CameraCapturePane
import kr.howlens.app.data.*
import kr.howlens.app.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HowLensTheme { Surface(Modifier.fillMaxSize()) { HowLensScreen() } } }
    }
}

private enum class InputTab { PHOTO, CAMERA }

@Composable
fun HowLensScreen(vm: AnalysisViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var tabName by rememberSaveable { mutableStateOf(InputTab.PHOTO.name) }
    var settings by rememberSaveable { mutableStateOf(false) }
    var deviceSheet by rememberSaveable { mutableStateOf(false) }
    val tab = InputTab.valueOf(tabName)
    val busy = state.phase == Phase.LOADING || state.photoLoading
    val compactViewport = LocalConfiguration.current.screenHeightDp < 500
    val showingResult = state.phase == Phase.RESULT && state.analysis != null
    val journey = rememberGuideJourneyState()
    var guideOpen by rememberSaveable(state.analysis?.analysisId) { mutableStateOf(false) }
    var summaryOpen by rememberSaveable(state.analysis?.analysisId) { mutableStateOf(false) }
    LaunchedEffect(state.analysis?.analysisId) {
        if (state.analysis != null) summaryOpen = true
    }

    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.selectPhoto(context.contentResolver, uri)
    }
    val verificationGallery = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.selectVerificationPhoto(context.contentResolver, uri)
    }

    BackHandler(enabled = settings || showingResult || (tab == InputTab.CAMERA && !settings)) {
        when {
            settings -> settings = false
            showingResult && summaryOpen -> summaryOpen = false
            showingResult && guideOpen -> guideOpen = false
            showingResult -> vm.showInput()
            tab == InputTab.CAMERA -> tabName = InputTab.PHOTO.name
        }
    }

    if (settings) {
        SettingsScreen(state = state, vm = vm, onBack = { settings = false })
        return
    }
    if (showingResult) {
        val analysis = requireNotNull(state.analysis)
        if (guideOpen && analysis.canShowSteps) {
            GuideJourneyPane(
                analysis = analysis, state = journey.value, onStateChange = { journey.value = it },
                onBack = { guideOpen = false }, onConfirmed = { guideOpen = false },
                panels = state.visualPanels, images = state.visualImages,
                visualLoading = state.visualLoading, visualError = state.visualError,
                onRequestImages = if (!state.offline && analysis.canRequestVisual && state.visualAttempts < 2)
                    vm::requestVisual else null,
            )
        } else {
            ResultScreen(
                state = state, vm = vm, onBack = vm::showInput,
                onOpenGuide = { guideOpen = true }, onOpenSummary = { summaryOpen = true },
                userConfirmed = journey.value.forAnalysis(analysis).userConfirmed,
                onSelectVerificationPhoto = { verificationGallery.launch(arrayOf("image/jpeg", "image/png")) },
            )
        }
        if (summaryOpen) {
            AnalysisSummarySheet(analysis = analysis,
                onDismissRequest = { summaryOpen = false },
                onOpenGuide = { summaryOpen = false; guideOpen = true },
                onRequestMoreInformation = { summaryOpen = false; vm.showInput() })
        }
        return
    }

    val noPhotoCamera = tab == InputTab.CAMERA && state.photo == null && !state.photoLoading
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { BrandWordmark() },
                    actions = {
                        if (state.analysis != null) TextButton(onClick = vm::showResult) { Text("최근 결과") }
                        Text(if (state.offline) "데모" else "실제 분석", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        IconButton(onClick = { settings = true }, enabled = !busy, modifier = Modifier.semantics { contentDescription = "설정" }) {
                            SettingsGlyph(Modifier.size(24.dp), MaterialTheme.colorScheme.onSurface)
                        }
                    },
                )
                TabRow(selectedTabIndex = if (tab == InputTab.PHOTO) 0 else 1) {
                    Tab(selected = tab == InputTab.PHOTO, onClick = { tabName = InputTab.PHOTO.name }, enabled = !busy,
                        modifier = Modifier.heightIn(min = 48.dp), text = { Text("사진 분석") })
                    Tab(selected = tab == InputTab.CAMERA, onClick = { tabName = InputTab.CAMERA.name }, enabled = !busy,
                        modifier = Modifier.heightIn(min = 48.dp), text = { Text("카메라") })
                }
            }
        },
        bottomBar = {
            if (!noPhotoCamera && !compactViewport) {
                InputActionBar(
                    state = state,
                    tab = tab,
                    onChoosePhoto = { gallery.launch(arrayOf("image/jpeg", "image/png")) },
                    onAnalyze = vm::analyze,
                    onCancel = vm::cancel,
                )
            }
        },
    ) { inset ->
        if (noPhotoCamera) {
            Column(Modifier.fillMaxSize().padding(inset)) {
                Spacer(Modifier.height(12.dp))
                DeviceRow(state.deviceId, enabled = !busy, onClick = { deviceSheet = true }, modifier = Modifier.padding(horizontal = 16.dp))
                state.error?.let { InlineError(it) }
                Spacer(Modifier.height(8.dp))
                CameraCapturePane(
                    isActive = true,
                    onPhotoCaptured = { uri -> vm.selectPhoto(context.contentResolver, uri, deleteFileAfterRead = true) },
                    onError = vm::inputError,
                    onChooseFromGallery = { gallery.launch(arrayOf("image/jpeg", "image/png")) },
                    onOpenPhotoAnalysis = { tabName = InputTab.PHOTO.name },
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                )
            }
        } else {
            Column(
                Modifier.fillMaxSize().padding(inset).imePadding()
                    .verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Spacer(Modifier.height(2.dp))
                DeviceRow(state.deviceId, enabled = !busy, onClick = { deviceSheet = true })
                when {
                    state.photoLoading -> Box(Modifier.fillMaxWidth().aspectRatio(4f / 3f), Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    state.photo != null -> {
                        PhotoFrame(state.photo!!, "선택한 장비 사진")
                        if (tab == InputTab.CAMERA) {
                            OutlinedButton(onClick = vm::clearPhoto, enabled = !busy,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("다시 촬영") }
                        } else {
                            OutlinedButton(onClick = { gallery.launch(arrayOf("image/jpeg", "image/png")) },
                                enabled = !busy, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("사진 바꾸기") }
                        }
                        QuestionField(state.question, enabled = !busy, error = if (state.error?.contains("질문") == true) state.error else null,
                            onValueChange = vm::question)
                    }
                    else -> EmptyPhotoFrame()
                }
                if (state.phase == Phase.LOADING) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                    Text(if (state.offline) "데모 결과를 준비하고 있어요" else "사진을 확인하고 있어요")
                }
                if (compactViewport) {
                    InputActionInline(state, tab,
                        onChoosePhoto = { gallery.launch(arrayOf("image/jpeg", "image/png")) },
                        onAnalyze = vm::analyze, onCancel = vm::cancel)
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }

    if (deviceSheet) {
        ModalBottomSheet(onDismissRequest = { deviceSheet = false }) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("장비 선택", style = MaterialTheme.typography.titleLarge)
                InputRules.devices.forEach { (id, label) ->
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = !busy) { vm.device(id); deviceSheet = false }
                        .padding(horizontal = 8.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = state.deviceId == id, onClick = null)
                        Spacer(Modifier.width(8.dp))
                        Text(label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun InputActionInline(state: AnalysisUiState, tab: InputTab, onChoosePhoto: () -> Unit, onAnalyze: () -> Unit, onCancel: () -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when {
            state.phase == Phase.LOADING -> Button(onClick = onCancel, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("취소") }
            state.photo == null -> Button(onClick = onChoosePhoto, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("사진 선택") }
            else -> {
                Button(onClick = onAnalyze, enabled = !state.photoLoading, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                    Text(if (state.phase == Phase.ERROR && state.retryable) "다시 시도" else "확인하기")
                }
                if (tab == InputTab.CAMERA) OutlinedButton(onClick = onChoosePhoto,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("사진 선택") }
            }
        }
    }
}

@Composable
private fun InputActionBar(
    state: AnalysisUiState, tab: InputTab, onChoosePhoto: () -> Unit,
    onAnalyze: () -> Unit, onCancel: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
        Column(Modifier.fillMaxWidth().imePadding().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (state.phase == Phase.LOADING) {
                Button(onClick = onCancel, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("취소") }
            } else if (state.photo == null) {
                Button(onClick = onChoosePhoto, enabled = !state.photoLoading,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("사진 선택") }
                if (state.error != null) InlineError(state.error)
            } else {
                if (state.phase == Phase.ERROR && state.error != null) InlineError(state.error)
                Button(onClick = onAnalyze, enabled = !state.photoLoading,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                    Text(if (state.phase == Phase.ERROR && state.retryable) "다시 시도" else "확인하기")
                }
                if (tab == InputTab.CAMERA) {
                    OutlinedButton(onClick = onChoosePhoto, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("사진 선택") }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(state: AnalysisUiState, vm: AnalysisViewModel, onBack: () -> Unit) {
    val compactViewport = LocalConfiguration.current.screenHeightDp < 500
    Scaffold(
        topBar = { TopAppBar(title = { Text("설정") }, navigationIcon = {
            TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) { Text("뒤로") }
        }) },
        bottomBar = { if (!compactViewport) Button(onClick = onBack, modifier = Modifier.fillMaxWidth().padding(16.dp).heightIn(min = 56.dp)) { Text("완료") } },
    ) { inset ->
        Column(Modifier.fillMaxSize().padding(inset).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(if (state.offline) "데모" else "실제 분석", style = MaterialTheme.typography.titleMedium)
                    Text(if (state.offline) "합성 화면" else "설정한 서버로 요청", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = state.offline, onCheckedChange = vm::offline)
            }
            if (state.offline) {
                WarningCard("데모 결과는 합성 예시예요. 실제 작업에 사용하지 마세요.")
                Text("데모 화면", style = MaterialTheme.typography.titleMedium)
                listOf(FakeScenario.MORE_INFORMATION to "추가 정보", FakeScenario.STOP to "중단")
                    .forEach { (scenario, label) ->
                        Row(Modifier.fillMaxWidth().clickable { vm.scenario(scenario) }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(state.scenario == scenario, onClick = { vm.scenario(scenario) })
                            Text(label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
            } else {
                OutlinedTextField(value = state.baseUrl, onValueChange = vm::baseUrl,
                    label = { Text("API 서버 루트 주소") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = state.demoToken.value, onValueChange = vm::demoToken,
                    label = { Text("데모 접속 토큰 (선택)") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Password),
                    supportingText = { Text("서버 주소를 먼저 입력하세요. 토큰은 앱을 닫으면 지워져요.") })
            }
            if (compactViewport) Button(onClick = onBack, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("완료") }
        }
    }
}

@Composable
private fun ResultScreen(state: AnalysisUiState, vm: AnalysisViewModel, onBack: () -> Unit,
    onOpenGuide: () -> Unit, onOpenSummary: () -> Unit, userConfirmed: Boolean,
    onSelectVerificationPhoto: () -> Unit) {
    val analysis = state.analysis ?: return
    var evidenceOpen by rememberSaveable(analysis.analysisId) { mutableStateOf(false) }
    var shareOpen by rememberSaveable(analysis.analysisId) { mutableStateOf(false) }
    var shareError by remember(analysis.analysisId) { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val compactViewport = LocalConfiguration.current.screenHeightDp < 500
    val shareText = remember(analysis) { SharePreview.build(analysis) }
    Scaffold(
        topBar = { TopAppBar(title = { BrandWordmark() }, navigationIcon = {
            TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) { Text("뒤로") }
        }) },
        bottomBar = {
            if (!compactViewport) {
                Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                    Button(onClick = onBack, modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp).heightIn(min = 56.dp)) {
                        Text("입력으로 돌아가기")
                    }
                }
            }
        },
    ) { inset ->
        Column(Modifier.fillMaxSize().padding(inset).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(if (analysis.mode == Mode.MOCK) "데모" else "실제 분석", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(when (analysis.decision) {
                Decision.GUIDE -> "문서 근거 안내"
                Decision.NEEDS_MORE_INFORMATION -> "추가 정보가 필요해요"
                Decision.STOP -> "작업을 멈추고 확인해 주세요"
            }, style = MaterialTheme.typography.headlineLarge)
            if (analysis.mode == Mode.MOCK) WarningCard("합성 예시예요. 실제 작업에 사용하지 마세요.")
            if (state.photo != null) PhotoFrame(state.photo, "분석한 장비 사진", maxHeightFraction = 0.36f)
            OutlinedButton(onClick = onOpenSummary, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("분석 요약") }
            if (userConfirmed) Text("단계 확인을 기록했어요. 안전이나 정상 동작을 보증하지 않아요.")
            analysis.missingInformation.forEach { WarningCard("추가 정보: $it", warning = true) }
            analysis.warnings.forEach { WarningCard(it, warning = true) }
            val unsatisfiedRequired = analysis.preconditions.filter { it.required && it.status != ConditionStatus.SATISFIED }
            unsatisfiedRequired.forEach { WarningCard("필수 조건 미확인: ${it.description}", warning = true) }
            if (analysis.decision == Decision.GUIDE && analysis.canShowSteps) {
                Button(onClick = onOpenGuide, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("단계별 안내 시작") }
                Text("승인된 단계", style = MaterialTheme.typography.titleLarge)
                analysis.visibleSteps.forEachIndexed { index, step ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("${index + 1}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
                            Text(step.description, style = MaterialTheme.typography.bodyLarge)
                            Text("근거 ${step.evidenceIds.joinToString()}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                OutlinedButton(onClick = { evidenceOpen = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text("문서 근거 ${analysis.evidence.size}개 보기")
                }
                Text("시각 안내는 단계와 별개인 9개 설명 패널이에요.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                VisualSection(state, analysis, vm)
                VerificationSection(state, analysis, vm, onSelectVerificationPhoto)
            } else {
                if (analysis.observations.isNotEmpty()) {
                    Text(analysis.observations.joinToString("\n"), style = MaterialTheme.typography.bodyLarge)
                }
            }
            if (analysis.decision != Decision.GUIDE) {
                OutlinedButton(onClick = { evidenceOpen = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text("관찰과 근거 자세히 보기")
                }
            }
            if (shareError != null) InlineError(shareError!!)
            OutlinedButton(onClick = { shareOpen = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text("근거 요약 공유")
            }
            if (compactViewport) Button(onClick = onBack, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("입력으로 돌아가기") }
            Spacer(Modifier.height(12.dp))
        }
    }
    if (evidenceOpen) {
        ModalBottomSheet(onDismissRequest = { evidenceOpen = false }) {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("문서 근거", style = MaterialTheme.typography.titleLarge)
                analysis.evidence.forEach { item ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${item.documentId} · ${item.documentVersion}", style = MaterialTheme.typography.titleMedium)
                        Text("${item.evidenceId} · PDF ${item.pdfPage}쪽 · 인쇄 ${item.printedPage ?: "표기 없음"}")
                        Text(item.section)
                        Text(item.quote)
                        Text(item.sourceUrl, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val uri = item.sourceUrl.toUri()
                        if (uri.scheme in setOf("https", "http") && !uri.host.isNullOrBlank()) {
                            TextButton(onClick = {
                                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                            }, modifier = Modifier.heightIn(min = 48.dp)) { Text("원문 열기") }
                        }
                    }
                    HorizontalDivider()
                }
                if (analysis.observations.isNotEmpty()) {
                    Text("관찰", style = MaterialTheme.typography.titleMedium)
                    analysis.observations.forEach { Text(it) }
                }
                analysis.preconditions.forEach { Text("${it.description} · ${conditionLabel(it.status)}${if (it.required) " · 필수" else ""}") }
            }
        }
    }
    if (shareOpen) {
        ModalBottomSheet(onDismissRequest = { shareOpen = false }) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("공유할 내용", style = MaterialTheme.typography.titleLarge)
                Text(shareText, modifier = Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState()))
                Button(onClick = {
                    try {
                        val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, shareText)
                        context.startActivity(Intent.createChooser(send, "HowLens 요약 공유"))
                        shareOpen = false
                    } catch (_: Exception) { shareError = "공유 앱을 열 수 없습니다. 결과는 유지됩니다."; shareOpen = false }
                }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("공유 앱 선택") }
                TextButton(onClick = { shareOpen = false }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("취소") }
            }
        }
    }
}

@Composable
private fun VisualSection(state: AnalysisUiState, analysis: Analysis, vm: AnalysisViewModel) {
    when {
        state.offline || analysis.mode != Mode.LIVE -> WarningCard("데모 결과에서는 시각 안내를 요청할 수 없어요.")
        state.visualLoading -> {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Text("시각 안내를 확인하고 있어요")
            TextButton(onClick = vm::cancelVisual, modifier = Modifier.heightIn(min = 48.dp)) { Text("취소") }
        }
        state.visualImages.size == 9 -> {
            state.visualPanels.forEach { panel ->
                Card {
                    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("패널 ${panel.index + 1} · ${panel.stepId}", style = MaterialTheme.typography.titleMedium)
                        val bytes = state.visualImages[panel.index]
                        if (bytes != null) DecodedImage(bytes, "설명용 시각 패널 ${panel.index + 1}", Modifier.fillMaxWidth().heightIn(min = 120.dp, max = 220.dp))
                    }
                }
            }
        }
        else -> {
            state.visualError?.let { InlineError(it) }
            Button(onClick = vm::requestVisual, enabled = state.visualAttempts < 2,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                Text(if (state.visualAttempts == 0) "시각 안내 요청" else "한 번 더 요청 (${state.visualAttempts}/2)")
            }
            if (state.visualAttempts >= 2) Text("시각 안내는 두 번까지만 요청할 수 있어요. 승인된 텍스트 단계는 유지됩니다.")
        }
    }
}

@Composable
private fun VerificationSection(state: AnalysisUiState, analysis: Analysis, vm: AnalysisViewModel, onSelectPhoto: () -> Unit) {
    HorizontalDivider()
    Text("전후 사진", style = MaterialTheme.typography.titleLarge)
    Text("사진에서 보이는 변화만 확인해요.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(Modifier.weight(1f)) {
            Text("이전", style = MaterialTheme.typography.titleMedium)
            state.photo?.let { PhotoFrame(it, "이전 사진", compact = true) }
        }
        Column(Modifier.weight(1f)) {
            Text("이후", style = MaterialTheme.typography.titleMedium)
            if (state.verificationPhoto != null) PhotoFrame(state.verificationPhoto, "이후 사진", compact = true)
            else Box(Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.background), Alignment.Center) { Text("사진 선택") }
        }
    }
    OutlinedButton(onClick = onSelectPhoto, enabled = !state.verificationPhotoLoading && !state.verificationBusy,
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(if (state.verificationPhoto == null) "이후 사진 선택" else "이후 사진 바꾸기") }
    OutlinedTextField(value = state.confirmation, onValueChange = vm::confirmation,
        label = { Text("직접 확인한 내용 (선택)") }, enabled = !state.verificationBusy,
        modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 5)
    state.verificationError?.let { InlineError(it) }
    if (state.verificationBusy) {
        LinearProgressIndicator(Modifier.fillMaxWidth())
        Text("사진 변화를 확인하고 있어요")
        TextButton(onClick = vm::cancelVerification, modifier = Modifier.heightIn(min = 48.dp)) { Text("비교 취소") }
    } else {
        Button(onClick = vm::verify, enabled = state.verificationPhoto != null && !state.offline,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("변화 확인") }
    }
    state.verification?.let { result ->
        Text(when (result.result) {
            VerificationResult.OBSERVED_CHANGE -> "보이는 변화가 있어요"
            VerificationResult.ISSUE_REMAINING -> "문제가 남아 보여요"
            VerificationResult.INCONCLUSIVE -> "사진만으로 판단하기 어려워요"
        }, style = MaterialTheme.typography.titleMedium)
        result.observations.forEach { Text(it) }
        result.missingInformation.forEach { WarningCard(it) }
        result.limitations.forEach { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
    WarningCard("사진 비교는 안전·수리 성공·정상 동작을 보증하지 않아요. 별도 기능 시험과 담당자 확인이 필요해요.", warning = true)
}

@Composable
private fun DeviceRow(deviceId: String, enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth().heightIn(min = 64.dp).clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("장비", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(16.dp))
            Text(InputRules.devices[deviceId] ?: "장비 선택", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            Text("⌄", color = MaterialTheme.colorScheme.primary, fontSize = 24.sp)
        }
    }
}

@Composable
private fun QuestionField(value: String, enabled: Boolean, error: String?, onValueChange: (String) -> Unit) {
    val requester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    OutlinedTextField(value = value, onValueChange = onValueChange, enabled = enabled,
        label = { Text("무엇을 확인할까요?") }, modifier = Modifier.fillMaxWidth()
            .bringIntoViewRequester(requester).onFocusChanged { focus ->
                if (focus.isFocused) scope.launch { delay(150); requester.bringIntoView() }
            }, minLines = 3, maxLines = 6,
        isError = error != null, supportingText = error?.let { message -> { Text(message) } })
}

@Composable
private fun EmptyPhotoFrame() {
    Box(Modifier.fillMaxWidth().aspectRatio(4f / 3f).clip(RoundedCornerShape(12.dp))
        .background(MaterialTheme.colorScheme.background).border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .4f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ImageGlyph(Modifier.size(48.dp), MaterialTheme.colorScheme.primary)
            Text("장비 사진", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun PhotoFrame(photo: Photo, description: String, compact: Boolean = false, maxHeightFraction: Float = 0.36f) {
    val bitmap = remember(photo) {
        BitmapFactory.decodeByteArray(photo.bytes, 0, photo.bytes.size,
            BitmapFactory.Options().apply { inSampleSize = if (compact) 8 else 4 })
    }
    val shape = RoundedCornerShape(12.dp)
    val config = LocalConfiguration.current
    val fontScale = LocalDensity.current.fontScale
    val heightFraction = if (fontScale >= 1.3f) minOf(maxHeightFraction, 0.28f) else maxHeightFraction
    val screenContentWidth = (config.screenWidthDp.dp - 32.dp).coerceAtLeast(120.dp)
    val desiredHeight = if (compact) screenContentWidth / 2 else screenContentWidth * 0.75f
    val frameHeight = minOf(desiredHeight, config.screenHeightDp.dp * heightFraction)
    Box(Modifier.fillMaxWidth().height(frameHeight)
        .clip(shape).background(MaterialTheme.colorScheme.background)) {
        bitmap?.let { Image(it.asImageBitmap(), description, Modifier.fillMaxSize(), contentScale = ContentScale.Fit) }
        DisposableEffect(bitmap) { onDispose { bitmap?.recycle() } }
    }
}

@Composable
private fun DecodedImage(bytes: ByteArray, description: String, modifier: Modifier = Modifier) {
    val bitmap = remember(bytes) { BitmapFactory.decodeByteArray(bytes, 0, bytes.size) }
    bitmap?.let { Image(it.asImageBitmap(), description, modifier, contentScale = ContentScale.Fit) }
    DisposableEffect(bitmap) { onDispose { bitmap?.recycle() } }
}

@Composable
private fun WarningCard(text: String, warning: Boolean = false) {
    val background = if (warning) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
    val foreground = if (warning) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
    Surface(color = background, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            if (warning) Text("!", color = foreground, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
            Text(text, color = foreground, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun InlineError(text: String) {
    Text(text, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
}

@Composable
private fun BrandWordmark() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ScannerMark(Modifier.size(32.dp))
        Text(buildAnnotatedString {
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)) { append("How") }
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) { append("Lens") }
        }, fontSize = 22.sp, lineHeight = 28.sp)
    }
}

@Composable
private fun ScannerMark(modifier: Modifier = Modifier) {
    val blue = MaterialTheme.colorScheme.primary
    Canvas(modifier) {
        drawRoundRect(blue, cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.minDimension * .22f))
        val inset = size.minDimension * .25f
        val length = size.minDimension * .22f
        val stroke = size.minDimension * .075f
        val corners = listOf(
            Triple(inset, inset, 1), Triple(size.width - inset, inset, 2),
            Triple(inset, size.height - inset, 3), Triple(size.width - inset, size.height - inset, 4),
        )
        corners.forEach { (x, y, corner) ->
            val p = Path().apply {
                if (corner == 1 || corner == 3) { moveTo(x, y + if (corner == 1) length else -length); lineTo(x, y); lineTo(x + length, y) }
                else { moveTo(x - length, y); lineTo(x, y); lineTo(x, y + if (corner == 2) length else -length) }
            }
            drawPath(p, Color.White, style = Stroke(stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round))
        }
    }
}

@Composable
private fun SettingsGlyph(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier) {
        val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
        drawCircle(color, size.minDimension * .29f, center, style = Stroke(1.8.dp.toPx()))
        drawCircle(color, size.minDimension * .08f, center)
        for (index in 0 until 8) {
            val angle = index * 45f
            val start = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2 - size.minDimension * .31f)
            val end = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2 - size.minDimension * .46f)
            rotate(angle, center) { drawLine(color, start, end, 1.8.dp.toPx()) }
        }
    }
}

@Composable
private fun ImageGlyph(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier) {
        val radius = size.minDimension * .18f
        drawRoundRect(color, cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius))
        val inner = size.minDimension * .22f
        drawCircle(Color.White, size.minDimension * .07f, androidx.compose.ui.geometry.Offset(size.width * .37f, size.height * .36f))
        val path = Path().apply {
            moveTo(inner, size.height * .78f); lineTo(size.width * .43f, size.height * .5f)
            lineTo(size.width * .56f, size.height * .64f); lineTo(size.width * .69f, size.height * .43f)
            lineTo(size.width - inner, size.height * .78f); close()
        }
        drawPath(path, Color.White)
    }
}

private fun conditionLabel(status: ConditionStatus): String = when (status) {
    ConditionStatus.SATISFIED -> "확인됨"
    ConditionStatus.UNSATISFIED -> "미충족"
    ConditionStatus.UNKNOWN -> "확인 필요"
}
