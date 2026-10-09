package kr.howlens.app.data

import kotlinx.coroutines.runBlocking
import kr.howlens.app.ui.AnalysisUiState
import kr.howlens.app.ui.AnalysisViewModel
import kr.howlens.app.ui.DemoToken
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.TimeUnit

class DemoAuthTest {
    @Test fun tokenIsAttachedToApiAndAssetsButNeverRedirected() = runBlocking {
        val server = MockWebServer().apply { start() }
        val other = MockWebServer().apply { start() }
        try {
            val repo = HttpAnalysisRepository(server.url("/").toString(),
                client = OkHttpClient.Builder().followRedirects(true).build(), demoToken = "synthetic-demo-only")
            server.enqueue(MockResponse().setBody("""{"status":"ok","mode":"live"}"""))
            repo.health()
            assertEquals("Bearer synthetic-demo-only", server.takeRequest().getHeader("Authorization"))
            server.enqueue(MockResponse().setResponseCode(302).setHeader("Location", other.url("/capture")))
            other.enqueue(MockResponse().setBody("""{"status":"ok","mode":"live"}"""))
            try { repo.health(); fail("redirect must fail") } catch (failure: ApiFailure) {
                assertEquals("http_302", failure.code)
            }
            assertNull(other.takeRequest(200, TimeUnit.MILLISECONDS))
            server.takeRequest()
            val guide = Analysis("fixture", "server", Decision.GUIDE, emptyList(),
                listOf(Evidence("e", "doc", "1", 1, null, "test", "test", "https://example.org")),
                listOf(Precondition("p", "test", ConditionStatus.SATISFIED, true, listOf("e"))),
                listOf(Step("s", "synthetic", listOf("e"), "")), emptyList(), emptyList(), Mode.LIVE)
            server.enqueue(MockResponse().setResponseCode(401))
            try { repo.visualAsset(guide, "/visual-assets/a.png"); fail("401 must fail") } catch (_: ApiFailure) { }
            val asset = server.takeRequest()
            assertEquals("/visual-assets/a.png", asset.path)
            assertEquals("Bearer synthetic-demo-only", asset.getHeader("Authorization"))
            assertEquals(0, other.requestCount)
        } finally { server.shutdown(); other.shutdown() }
    }

    @Test fun emptyTokenPreservesLocalRequestsAndTokenIsRedacted() = runBlocking {
        val server = MockWebServer().apply { start() }
        try {
            server.enqueue(MockResponse().setBody("""{"status":"ok","mode":"mock"}"""))
            HttpAnalysisRepository(server.url("/").toString()).health()
            assertNull(server.takeRequest().getHeader("Authorization"))
            val vm = AnalysisViewModel(AnalysisUiState(demoToken = DemoToken("synthetic-demo-only")))
            assertFalse(vm.state.value.toString().contains("synthetic-demo-only"))
            vm.baseUrl("https://example.org/")
            assertEquals("", vm.state.value.demoToken.value)
        } finally { server.shutdown() }
    }
}
