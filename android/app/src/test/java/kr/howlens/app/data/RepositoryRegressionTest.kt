package kr.howlens.app.data

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

/** Uses a per-repository decoder double; Android's actual decoder is covered by androidTest. */
class RepositoryRegressionTest {
    @Test
    fun assetPathUsesInjectedDecoderWithoutGlobalState() = runBlocking {
        val server = MockWebServer()
        server.start()
        val png = java.util.Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAAC0lEQVR4nGP4DwQACfsD/fteaysAAAAASUVORK5CYII="
        )
        val decodeCalls = AtomicInteger()
        val decodedBytes = AtomicReference<ByteArray>()
        try {
            server.enqueue(MockResponse().setHeader("Content-Type", "image/png").setBody(okio.Buffer().write(png)))
            val decoder = PngAssetDecoder { bytes ->
                decodedBytes.set(bytes)
                decodeCalls.incrementAndGet()
            }
            val repository = HttpAnalysisRepository(server.url("/").toString(), decoder)

            val response = repository.visualAsset(liveGuide(), "/visual-assets/synthetic.png")

            assertArrayEquals(png, response)
            assertArrayEquals(png, decodedBytes.get())
            assertEquals(1, decodeCalls.get())
            assertEquals("/visual-assets/synthetic.png", server.takeRequest().path)
        } finally {
            server.shutdown()
        }
    }

    private fun liveGuide() = Analysis(
        analysisId = "analysis-test",
        deviceId = "server",
        decision = Decision.GUIDE,
        observations = emptyList(),
        evidence = listOf(Evidence("e1", "manual-test", "synthetic", 1, null, "test", "test", "https://example.org/manual")),
        preconditions = listOf(Precondition("p1", "test", ConditionStatus.SATISFIED, true, listOf("e1"))),
        steps = listOf(Step("s1", "synthetic", listOf("e1"), "")),
        warnings = emptyList(),
        missingInformation = emptyList(),
        mode = Mode.LIVE
    )
}
