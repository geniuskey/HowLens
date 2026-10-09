package kr.howlens.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Callback-only home content. The host owns navigation, permissions, selected photos and results.
 * Pass discovery only when a real destination exists, and resume only for a retained result.
 * Place inside the host's Scaffold content insets; existing photo/camera tabs remain host-owned.
 * [heroImage] is supplied by the host: this pane performs no decoding or network requests.
 */
@Composable
fun HomeEntryPane(
    onOpenCamera: () -> Unit,
    onChoosePhoto: () -> Unit,
    modifier: Modifier = Modifier,
    onDiscoverProducts: (() -> Unit)? = null,
    onResumeLastResult: (() -> Unit)? = null,
    lastResultTitle: String? = null,
    heroImage: Painter? = null,
    heroImageDescription: String? = null,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    Surface(modifier = modifier, color = colors.background) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier.widthIn(max = 560.dp).fillMaxWidth()
                    .verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HomeGlyph(HomeSymbol.CAMERA, colors.primary, Modifier.size(28.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("How", style = typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Lens", color = colors.primary, style = typography.titleLarge,
                        fontWeight = FontWeight.Bold)
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("보면, 할 수 있어요.", style = typography.headlineSmall,
                        fontWeight = FontWeight.Bold, modifier = Modifier.semantics { heading() })
                    Text("문제가 있는 장비를 사진으로 보여 주세요.",
                        style = typography.bodySmall, color = colors.onSurfaceVariant)
                }

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = colors.primary.copy(alpha = 0.07f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        if (heroImage != null) {
                            Surface(shape = RoundedCornerShape(16.dp), color = colors.surface) {
                                Image(
                                    painter = heroImage,
                                    contentDescription = heroImageDescription?.takeIf { it.isNotBlank() }
                                        ?: "촬영할 장비 예시",
                                    modifier = Modifier.fillMaxWidth().height(128.dp),
                                    contentScale = ContentScale.Fit,
                                )
                            }
                        } else {
                            HomeCameraIllustration(Modifier.fillMaxWidth().height(112.dp))
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("사진 한 장으로 시작", style = typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                            Text("부품과 모델명이 잘 보이게 촬영해 주세요.",
                                style = typography.bodySmall, color = colors.onSurfaceVariant)
                        }
                        Button(
                            onClick = onOpenCamera,
                            enabled = enabled,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                                .semantics { contentDescription = "카메라를 열어 장비 촬영하기" },
                        ) {
                            HomeGlyph(HomeSymbol.CAMERA, if (enabled) colors.onPrimary
                                else colors.onSurface.copy(alpha = 0.38f), Modifier.size(22.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("지금 촬영하기", style = typography.labelLarge)
                        }
                    }
                }

                OutlinedButton(
                    onClick = onChoosePhoto,
                    enabled = enabled,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                        .semantics { contentDescription = "갤러리에서 기존 장비 사진 선택하기" },
                ) {
                    HomeGlyph(HomeSymbol.PHOTO, if (enabled) colors.primary
                        else colors.onSurface.copy(alpha = 0.38f), Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("사진 선택", style = typography.labelLarge)
                }

                if (onResumeLastResult != null && !lastResultTitle.isNullOrBlank()) {
                    HomeDestination(
                        eyebrow = "마지막 결과",
                        title = lastResultTitle,
                        action = "이어서 보기",
                        symbol = HomeSymbol.RESULT,
                        enabled = enabled,
                        onClick = onResumeLastResult,
                    )
                }
                if (onDiscoverProducts != null) {
                    HomeDestination(
                        eyebrow = "지원 장비",
                        title = "어떤 장비를 확인할까요?",
                        action = "장비 둘러보기",
                        symbol = HomeSymbol.PRODUCT,
                        enabled = enabled,
                        onClick = onDiscoverProducts,
                    )
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun HomeDestination(
    eyebrow: String,
    title: String,
    action: String,
    symbol: HomeSymbol,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(shape = RoundedCornerShape(18.dp), color = colors.surface) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HomeGlyph(symbol, colors.primary, Modifier.size(28.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(eyebrow, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                }
            }
            OutlinedButton(
                onClick = onClick, enabled = enabled,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primary),
            ) { Text(action, style = MaterialTheme.typography.labelLarge) }
        }
    }
}

private enum class HomeSymbol { CAMERA, PHOTO, PRODUCT, RESULT }

/** Small native strokes keep camera and gallery shapes distinct without adding an icon package. */
@Composable
private fun HomeGlyph(symbol: HomeSymbol, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val unit = size.minDimension
        val stroke = Stroke(unit * 0.065f, cap = StrokeCap.Round)
        fun point(x: Float, y: Float) = Offset(unit * x, unit * y)
        when (symbol) {
            HomeSymbol.CAMERA -> {
                val body = Path().apply {
                    moveTo(unit * .13f, unit * .32f)
                    lineTo(unit * .32f, unit * .32f)
                    lineTo(unit * .39f, unit * .20f)
                    lineTo(unit * .61f, unit * .20f)
                    lineTo(unit * .68f, unit * .32f)
                    lineTo(unit * .87f, unit * .32f)
                    lineTo(unit * .87f, unit * .79f)
                    lineTo(unit * .13f, unit * .79f)
                    close()
                }
                drawPath(body, color, style = stroke)
                drawCircle(color, unit * .15f, point(.5f, .55f), style = stroke)
            }
            HomeSymbol.PHOTO -> {
                drawRoundRect(color, point(.13f, .13f), Size(unit * .74f, unit * .74f),
                    CornerRadius(unit * .08f), style = stroke)
                drawCircle(color, unit * .055f, point(.35f, .34f))
                drawPath(Path().apply {
                    moveTo(unit * .2f, unit * .73f)
                    lineTo(unit * .43f, unit * .50f)
                    lineTo(unit * .56f, unit * .63f)
                    lineTo(unit * .69f, unit * .46f)
                    lineTo(unit * .81f, unit * .63f)
                }, color, style = stroke)
            }
            HomeSymbol.PRODUCT -> {
                drawRoundRect(color, point(.12f, .18f), Size(unit * .76f, unit * .53f),
                    CornerRadius(unit * .05f), style = stroke)
                drawLine(color, point(.5f, .71f), point(.5f, .86f), stroke.width)
                drawLine(color, point(.32f, .86f), point(.68f, .86f), stroke.width, StrokeCap.Round)
            }
            HomeSymbol.RESULT -> {
                drawRoundRect(color, point(.22f, .12f), Size(unit * .56f, unit * .76f),
                    CornerRadius(unit * .06f), style = stroke)
                listOf(.35f, .5f, .65f).forEach { y ->
                    drawLine(color, point(.35f, y), point(.65f, y), stroke.width, StrokeCap.Round)
                }
            }
        }
    }
}

@Composable
private fun HomeCameraIllustration(modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    Box(modifier, contentAlignment = Alignment.Center) {
        Surface(shape = RoundedCornerShape(22.dp), color = colors.surface, shadowElevation = 1.dp) {
            Box(Modifier.size(88.dp), contentAlignment = Alignment.Center) {
                HomeGlyph(HomeSymbol.CAMERA, colors.primary, Modifier.size(52.dp))
            }
        }
        Canvas(Modifier.size(112.dp)) {
            val length = 16.dp.toPx()
            val stroke = 2.dp.toPx()
            val inset = 2.dp.toPx()
            listOf(Offset(inset, inset), Offset(size.width - inset, inset),
                Offset(inset, size.height - inset), Offset(size.width - inset, size.height - inset))
                .forEach { corner ->
                    val horizontal = if (corner.x < size.width / 2) length else -length
                    val vertical = if (corner.y < size.height / 2) length else -length
                    drawLine(colors.primary, corner, corner + Offset(horizontal, 0f), stroke, StrokeCap.Round)
                    drawLine(colors.primary, corner, corner + Offset(0f, vertical), stroke, StrokeCap.Round)
                }
        }
    }
}

@Preview(name = "Home 320dp", widthDp = 320, heightDp = 720, showBackground = true)
@Preview(name = "Home 412dp", widthDp = 412, heightDp = 820, showBackground = true)
@Composable
private fun HomeEntryPreview() {
    HowLensTheme { HomeEntryPane(onOpenCamera = {}, onChoosePhoto = {}) }
}

@Preview(name = "Home 320dp large text", widthDp = 320, heightDp = 720, fontScale = 1.5f, showBackground = true)
@Composable
private fun HomeEntryLargeTextPreview() {
    HowLensTheme { HomeEntryPane(onOpenCamera = {}, onChoosePhoto = {}) }
}
