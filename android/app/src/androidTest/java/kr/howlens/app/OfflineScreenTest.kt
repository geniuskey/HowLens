package kr.howlens.app

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import kotlinx.coroutines.runBlocking
import kr.howlens.app.data.*
import kr.howlens.app.ui.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OfflineScreenTest {
    @get:Rule val compose = createComposeRule()

    private fun photo(): Photo {
        val bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        bitmap.recycle()
        return Photo(output.toByteArray(), "image/png", 32, 32)
    }

    private fun liveGuide(): Analysis = Analysis("fixture-analysis-id", "server", Decision.GUIDE,
        listOf("합성 안내 화면 테스트"),
        listOf(Evidence("public-e1", "manual-public-id", "v1", 4, "7", "합성 절", "합성 발췌", "https://manual.example/device.pdf")),
        emptyList(), listOf(Step("approved-s1", "Synthetic approved step fixture", listOf("public-e1"), "fixture")),
        emptyList(), emptyList(), Mode.LIVE)

    @Test fun photoFirstInputShowsTabsAndSettingsPreserveDraft() {
        val vm = AnalysisViewModel()
        compose.runOnUiThread { vm.photo(photo()); vm.question("합성 초안") }
        compose.setContent { HowLensTheme { HowLensScreen(vm) } }
        compose.onNodeWithText("사진 분석").assertExists()
        compose.onNodeWithText("카메라").assertExists()
        compose.onNodeWithText("합성 초안").assertExists()
        compose.onNodeWithContentDescription("설정").performClick()
        compose.onNodeWithText("설정").assertExists()
        compose.onNodeWithText("완료").performClick()
        compose.onNodeWithText("합성 초안").assertExists()
        compose.onNodeWithText("확인하기").assertExists()
    }

    @Test fun offlineNonGuideResultsShowMissingInformationAndNoActions() {
        val vm = AnalysisViewModel()
        compose.runOnUiThread { vm.photo(photo()); vm.question("합성 UI 테스트") }
        compose.setContent { HowLensTheme { HowLensScreen(vm) } }
        compose.onNodeWithText("확인하기").performClick()
        compose.waitUntil(5000) { vm.state.value.phase == Phase.RESULT }
        compose.onNodeWithText("추가 정보가 필요해요").assertExists()
        compose.onNodeWithText("데모").assertExists()
        compose.onNodeWithText("시각 안내 요청").assertDoesNotExist()
        compose.onNodeWithText("승인된 단계").assertDoesNotExist()
        val screenshot = compose.onRoot().captureToImage().asAndroidBitmap()
        File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, "w3-non-guide.png").outputStream().use {
            screenshot.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        screenshot.recycle()
    }

    @Test fun emptyInputShowsValidation() {
        val vm = AnalysisViewModel()
        compose.setContent { HowLensTheme { HowLensScreen(vm) } }
        compose.runOnUiThread { vm.analyze() }
        compose.onNodeWithText("질문은 공백 제거 후 1–2,000자여야 합니다.").assertExists()
    }

    @Test fun blockedResultKeepsSourceVersionPagesAndQuoteInDetails() {
        val fixture = Analysis("synthetic-source-ui", "server", Decision.NEEDS_MORE_INFORMATION,
            listOf("합성 근거 표시 테스트; 실제 문서 아님"),
            listOf(Evidence("synthetic-e1", "synthetic-document", "fixture-v1", 3, "ii", "합성 절", "합성 발췌", "https://example.org/fixture")),
            emptyList(), emptyList(), emptyList(), listOf("추가 확인 필요"), Mode.MOCK)
        val vm = AnalysisViewModel(AnalysisUiState(phase = Phase.RESULT, analysis = fixture, offline = true))
        compose.setContent { HowLensTheme { HowLensScreen(vm) } }
        compose.onNodeWithText("추가 정보가 필요해요").assertExists()
        compose.onNodeWithText("관찰과 근거 자세히 보기").performClick()
        compose.onNodeWithText("synthetic-document · fixture-v1").assertExists()
        compose.onNodeWithText("PDF 3쪽", substring = true).assertExists()
        compose.onNodeWithText("인쇄 ii", substring = true).assertExists()
        compose.onNodeWithText("합성 발췌").assertExists()
        compose.onNodeWithText("https://example.org/fixture").assertExists()
    }

    @Test fun livePanelsRenderAllNineWithStepReferences() {
        val fixture = liveGuide()
        val state = AnalysisUiState(phase = Phase.RESULT, analysis = fixture, offline = false,
            visualPanels = (0..8).map { Panel(it, "approved-s1", "/visual-assets/test-$it.png") },
            visualImages = (0..8).associateWith { photo().bytes })
        val vm = AnalysisViewModel(state)
        compose.setContent { HowLensTheme { HowLensScreen(vm) } }
        compose.onNodeWithText("승인된 단계").assertExists()
        compose.onNodeWithText("시각 안내는 단계와 별개인 9개 설명 패널이에요.").assertExists()
        (1..9).forEach { index -> compose.onNodeWithText("패널 $index · approved-s1").assertExists() }
        compose.onNodeWithText("전후 사진").assertExists()
        compose.onNodeWithText("안전·수리 성공·정상 동작을 보증하지 않아요.", substring = true).assertExists()
    }

    @Test fun sharePreviewUsesAllowlistAndRequiresChooserAction() {
        val fixture = liveGuide().copy(evidence = liveGuide().evidence + liveGuide().evidence.single().copy(
            evidenceId = "bad-url", sourceUrl = "https://manual.example/doc?token=PRIVATE"))
        val vm = AnalysisViewModel(AnalysisUiState(phase = Phase.RESULT, analysis = fixture, offline = false))
        compose.setContent { HowLensTheme { HowLensScreen(vm) } }
        compose.onNodeWithText("근거 요약 공유").performScrollTo().performClick()
        compose.onNodeWithText("공유할 내용").assertExists()
        compose.onNodeWithText(SharePreview.build(fixture), substring = true).assertExists()
        compose.onNodeWithText("공유 앱 선택").assertExists()
        compose.onNodeWithText("취소").performClick()
    }

    @Test fun decoderAcceptsPngAndRejectsCorruptContent() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.cacheDir, "camera").apply { mkdirs() }
        val file = File(directory, "loader-test.png")
        try {
            file.writeBytes(photo().bytes)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.photos", file)
            val result = PhotoLoader.load(context.contentResolver, uri)
            assertEquals("image/png", result.mime); assertEquals(32, result.width)
            file.writeBytes(byteArrayOf(1, 2, 3))
            try { PhotoLoader.load(context.contentResolver, uri); fail("Corrupt content accepted") }
            catch (_: IllegalArgumentException) { }
        } finally { file.delete() }
    }
}
