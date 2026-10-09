package kr.howlens.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import kr.howlens.app.data.Analysis
import kr.howlens.app.data.ConditionStatus
import kr.howlens.app.data.Mode
import kr.howlens.app.data.Precondition

/**
 * Presents only the supplied analysis details. The parent owns visibility and
 * navigation; the step CTA is exposed only when the immutable model allows it.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AnalysisSummarySheet(
    analysis: Analysis,
    thumbnail: ImageBitmap? = null,
    onDismissRequest: () -> Unit,
    onOpenGuide: () -> Unit,
    onRequestMoreInformation: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val maxHeight = (LocalConfiguration.current.screenHeightDp * 0.82f).dp

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxHeight)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (thumbnail != null) {
                    Image(
                        bitmap = thumbnail,
                        contentDescription = "분석에 사용한 사진",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp)),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "분석 요약",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.semantics { heading() },
                    )
                    if (analysis.mode == Mode.MOCK) {
                        Text(
                            text = "테스트 예시",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                OutlinedButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text("닫기")
                }
            }

            Spacer(Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                val warnings = analysis.warnings.nonBlankItems()
                if (warnings.isNotEmpty()) {
                    SummarySection(title = "주의") {
                        warnings.forEach { SummaryBullet(text = it, emphasized = true) }
                    }
                }

                val missing = analysis.missingInformation.nonBlankItems()
                if (missing.isNotEmpty()) {
                    SummarySection(title = "이어서 확인할 항목") {
                        missing.forEach { SummaryBullet(text = it) }
                    }
                }

                val preconditions = analysis.preconditions
                    .filter { it.description.isNotBlank() }
                    .sortedWith(compareBy<Precondition>({ !it.required }, { it.status == ConditionStatus.SATISFIED }))
                if (preconditions.isNotEmpty()) {
                    SummarySection(title = "시작 전 확인") {
                        preconditions.forEach { precondition ->
                            SummaryBullet(
                                text = "${preconditionStatus(precondition.status)} · ${precondition.description}",
                                emphasized = precondition.required && precondition.status != ConditionStatus.SATISFIED,
                            )
                        }
                    }
                }

                val observations = analysis.observations.nonBlankItems().take(2)
                if (observations.isNotEmpty()) {
                    SummarySection(title = "관찰 내용") {
                        observations.forEach { SummaryBullet(text = it) }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Button(
                onClick = summarySheetPrimaryAction(
                    canShowSteps = analysis.canShowSteps,
                    onOpenGuide = onOpenGuide,
                    onRequestMoreInformation = onRequestMoreInformation,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            ) {
                Text(if (analysis.canShowSteps) "문서 근거 단계 보기" else "확인한 내용과 자료 보기")
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SummarySection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.semantics { heading() },
        )
        content()
    }
}

@Composable
private fun SummaryBullet(text: String, emphasized: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyLarge,
            color = if (emphasized) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun List<String>.nonBlankItems(): List<String> = map(String::trim).filter(String::isNotEmpty)

internal fun summarySheetPrimaryAction(
    canShowSteps: Boolean,
    onOpenGuide: () -> Unit,
    onRequestMoreInformation: () -> Unit,
): () -> Unit = if (canShowSteps) onOpenGuide else onRequestMoreInformation

private fun preconditionStatus(status: ConditionStatus): String = when (status) {
    ConditionStatus.SATISFIED -> "확인됨"
    ConditionStatus.UNSATISFIED -> "확인 필요"
    ConditionStatus.UNKNOWN -> "확인되지 않음"
}
