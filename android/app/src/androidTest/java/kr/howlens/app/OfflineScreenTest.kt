package kr.howlens.app

import android.graphics.Bitmap
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
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
    @Test fun offlineMoreInformationAndStopBlockStepsOnScreen() {
        val vm = AnalysisViewModel()
        compose.runOnUiThread { vm.photo(photo()); vm.question("합성 UI 테스트") }
        compose.setContent { MaterialTheme { HowLensScreen(vm) } }
        compose.onNodeWithText("오프라인 MOCK · 합성 UI 테스트").assertExists()
        compose.onNodeWithText("MOCK 화면 확인").performScrollTo().performClick()
        compose.waitUntil(5000) { vm.state.value.phase == Phase.RESULT }
        compose.onNodeWithText("추가 정보 필요").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("MOCK · 합성 예시 · 실제 작업에 사용 금지").assertExists()
        compose.onNodeWithText("실행 단계와 이미지가 차단되었습니다.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("중단 예시").performScrollTo().performClick()
        compose.onNodeWithText("MOCK 화면 확인").performScrollTo().performClick()
        compose.waitUntil(5000) { vm.state.value.phase == Phase.RESULT }
        compose.onNodeWithText("작업 중단").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("실행 단계와 이미지가 차단되었습니다.").assertExists()
        assertEquals(Mode.MOCK, vm.state.value.analysis!!.mode)
        assertFalse(vm.state.value.analysis!!.canRequestVisual)
    }
    @Test fun emptyInputShowsValidation() {
        compose.setContent { MaterialTheme { HowLensScreen(AnalysisViewModel()) } }
        compose.onNodeWithText("MOCK 화면 확인").performScrollTo().performClick()
        compose.onNodeWithText("질문은 공백 제거 후 1–2,000자여야 합니다.").assertExists()
    }
    @Test fun sourceVersionPagesAndQuoteRemainVisibleInBlockedResult() {
        val fixture = Analysis("synthetic-source-ui", "server", Decision.NEEDS_MORE_INFORMATION,
            listOf("합성 근거 표시 테스트; 실제 문서 아님"),
            listOf(Evidence("synthetic-e1", "synthetic-document", "fixture-v1", 3, "ii", "합성 절", "합성 발췌", "https://example.org/fixture")),
            emptyList(), emptyList(), emptyList(), listOf("추가 확인 필요"), Mode.MOCK)
        compose.setContent { MaterialTheme {
            Column(Modifier.verticalScroll(rememberScrollState())) { AnalysisResult(fixture) }
        } }
        compose.onNodeWithText("synthetic-e1 · synthetic-document · 버전 fixture-v1").assertExists()
        compose.onNodeWithText("PDF 3쪽 · 인쇄 ii · 합성 절").assertExists()
        compose.onNodeWithText("합성 발췌").assertExists()
        compose.onNodeWithText("https://example.org/fixture").assertExists()
        compose.onNodeWithText("실행 단계와 이미지가 차단되었습니다.").assertExists()
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
