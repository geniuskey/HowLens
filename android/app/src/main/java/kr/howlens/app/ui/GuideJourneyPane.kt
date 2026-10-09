package kr.howlens.app.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kr.howlens.app.data.Analysis
import kr.howlens.app.data.Panel

private val JourneySaver = listSaver<GuideJourneyState, Any>(
    save = { listOf(it.analysisId, it.currentStepId.orEmpty(), it.userConfirmed) + it.checkedStepIds.sorted() },
    restore = { saved -> GuideJourneyState(
        analysisId = saved[0] as String,
        currentStepId = (saved[1] as String).ifEmpty { null },
        userConfirmed = saved[2] as Boolean,
        checkedStepIds = saved.drop(3).map { it as String }.toSet(),
    ) },
)

/** Call above the parent's screen branch, so leaving/reopening this pane retains user marks.
 * Saves only IDs and a local mark, never photos, image bytes, analysis text, or a server verdict.
 */
@Composable
fun rememberGuideJourneyState(): MutableState<GuideJourneyState> =
    rememberSaveable(stateSaver = JourneySaver) { mutableStateOf(GuideJourneyState()) }

/** Callback-driven, standalone screen; the parent owns navigation, image requests and local state. */
@Composable
fun GuideJourneyPane(
    analysis: Analysis,
    state: GuideJourneyState,
    onStateChange: (GuideJourneyState) -> Unit,
    onBack: () -> Unit,
    onConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
    panels: List<Panel> = emptyList(),
    images: Map<Int, ByteArray> = emptyMap(),
    visualLoading: Boolean = false,
    visualError: String? = null,
    onRequestImages: (() -> Unit)? = null,
    onAskQuestion: (() -> Unit)? = null,
) {
    val local = state.forAnalysis(analysis)
    val page = guideJourneyPage(analysis, local, panels)
    LaunchedEffect(analysis, state) {
        if (local != state) onStateChange(local)
    }
    val scroll = rememberScrollState()
    LaunchedEffect(analysis.analysisId, local.currentStepId) { scroll.scrollTo(0) }

    Column(
        modifier.fillMaxSize().verticalScroll(scroll).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) { Text("뒤로 가기") }
        Text("문서 근거 단계 안내", style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() })
        if (analysis.warnings.isNotEmpty()) {
            Surface(color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.medium) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("주의사항", style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer)
                    analysis.warnings.forEach { Text(it, color = MaterialTheme.colorScheme.onErrorContainer) }
                }
            }
        }
        if (page.step == null) {
            Text("이 분석에서는 작업 단계를 표시할 수 없어요.", style = MaterialTheme.typography.titleMedium)
            analysis.missingInformation.forEach { Text(it) }
            Text("분석 결과의 경고와 추가 정보 요청을 확인해 주세요.")
        } else {
            val step = requireNotNull(page.step)
            val last = page.currentIndex == page.steps.lastIndex
            Text("현재 ${page.currentIndex + 1} / ${page.total}단계", style = MaterialTheme.typography.titleMedium)
            LinearProgressIndicator(progress = { (page.currentIndex + 1).toFloat() / page.total },
                modifier = Modifier.fillMaxWidth())
            Text("직접 확인 ${local.checkedStepIds.size} / ${page.total}개")
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("${page.currentIndex + 1}단계", style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.semantics { heading() })
                    Text(step.description, style = MaterialTheme.typography.bodyLarge)
                    Text("확인 포인트는 아래 문서 근거와 설명용 이미지를 함께 참고하세요.",
                        style = MaterialTheme.typography.bodySmall)
                    if (visualLoading) Text("설명용 이미지를 준비하고 있어요. 문서 안내는 계속 볼 수 있어요.")
                    if (visualError != null) Text("이미지를 표시할 수 없어요. 문서 안내는 유지됩니다.",
                        color = MaterialTheme.colorScheme.error)
                    if (page.scenes.isEmpty()) Text("이 단계의 설명용 이미지가 없어요. 아래 문서 근거를 확인해 주세요.")
                    page.scenes.forEach { panel ->
                        val bytes = images[panel.index]
                        val bitmap = remember(bytes) { decodeScene(bytes) }
                        Text("설명용 장면 ${panel.index + 1} · 현재 ${page.currentIndex + 1}단계",
                            style = MaterialTheme.typography.bodySmall)
                        if (bitmap == null) {
                            Text("장면을 불러오지 못했어요. 위 단계 설명과 아래 문서 근거를 확인해 주세요.")
                        } else {
                            Image(bitmap = bitmap.asImageBitmap(),
                                contentDescription = "${page.currentIndex + 1}단계 설명용 장면 ${panel.index + 1}. 문서 근거와 함께 확인하세요.",
                                modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp), contentScale = ContentScale.Fit)
                        }
                    }
                    if (onRequestImages != null) {
                        OutlinedButton(onClick = onRequestImages, enabled = !visualLoading,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("설명용 이미지 요청") }
                    }
                    Text("문서 근거", style = MaterialTheme.typography.titleMedium)
                    analysis.evidence.filter { it.evidenceId in step.evidenceIds }.forEach { evidence ->
                        Text("${evidence.documentId} · 버전 ${evidence.documentVersion} · PDF ${evidence.pdfPage}쪽" +
                            (evidence.printedPage?.let { " · 인쇄 $it" } ?: ""))
                        Text(evidence.section, style = MaterialTheme.typography.bodySmall)
                        Text(evidence.quote)
                    }
                }
            }
            if (!last) {
                JourneyCheck("이 단계를 직접 확인했어요", step.stepId in local.checkedStepIds) {
                    onStateChange(local.check(analysis, step.stepId, it))
                }
            } else {
                Text("최종 확인 체크리스트", style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.semantics { heading() })
                page.steps.forEachIndexed { index, item ->
                    JourneyCheck("${index + 1}단계 · ${item.description}", item.stepId in local.checkedStepIds) {
                        onStateChange(local.check(analysis, item.stepId, it))
                    }
                    if (item.stepId !in local.checkedStepIds && item.stepId != step.stepId) {
                        TextButton(onClick = { onStateChange(local.select(analysis, item.stepId)) },
                            modifier = Modifier.heightIn(min = 48.dp)) { Text("${index + 1}단계 다시 보기") }
                    }
                }
                Text("확인은 이 기기에 남는 사용자 표시예요. 사진과 안내만으로 안전, 수리 성공, 정상 동작을 보증하지 않아요.")
                if (local.userConfirmed) Text("직접 확인 표시를 남겼어요.", color = MaterialTheme.colorScheme.primary)
            }
            if (page.currentIndex > 0) {
                OutlinedButton(onClick = { onStateChange(local.move(analysis, -1)) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("이전 단계") }
            }
            if (last) {
                Button(onClick = {
                    val confirmed = local.confirm(analysis)
                    if (confirmed.userConfirmed && !local.userConfirmed) {
                        onStateChange(confirmed)
                        onConfirmed()
                    }
                }, enabled = !local.userConfirmed && page.steps.all { it.stepId in local.checkedStepIds },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), contentPadding = PaddingValues(16.dp)) {
                    Text(if (local.userConfirmed) "직접 확인 표시됨" else "확인했어요")
                }
            } else {
                Button(onClick = { onStateChange(local.move(analysis, 1)) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), contentPadding = PaddingValues(16.dp)) {
                    Text("다음 단계")
                }
            }
            if (onAskQuestion != null) {
                TextButton(onClick = onAskQuestion, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text("이 단계에 대해 질문하기")
                }
            }
        }
    }
}

@Composable
private fun JourneyCheck(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp)
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange)
            .padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = checked, onCheckedChange = null)
            Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        }
    }
}

/** Bound the decoded decoration; its bytes stay out of saved state. Decode failure never hides text. */
private fun decodeScene(bytes: ByteArray?): android.graphics.Bitmap? {
    if (bytes == null || bytes.isEmpty()) return null
    return runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        val options = BitmapFactory.Options().apply { inSampleSize = 1 }
        while (bounds.outWidth / options.inSampleSize > 1200 || bounds.outHeight / options.inSampleSize > 1200) {
            options.inSampleSize *= 2
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
    }.getOrNull()
}
