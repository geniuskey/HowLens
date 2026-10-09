package kr.howlens.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.*
import kotlinx.serialization.json.Json
import kr.howlens.app.ui.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductDiscoveryTest {
    private val photo = Photo(byteArrayOf(1, 2, 3), "image/png", 1, 1)
    private fun candidate() = ProductDiscovery("synthetic-discovery", DiscoveryStatus.CANDIDATE,
        listOf(ProductCandidate("Example", "Fixture model", "Synthetic product info", listOf("Label resembles fixture"),
            listOf(ProductSource("Manufacturer", "https://example.org/product", "2026-10-09T00:00:00Z")))),
        emptyList(), Mode.MOCK)

    @Test fun multipartUsesSeparateEndpointAndNoCatalogId() = runBlocking {
        val server = MockWebServer().apply { start() }
        try {
            server.enqueue(MockResponse().setBody(Json.encodeToString(ProductDiscovery.serializer(), candidate())))
            val result = HttpAnalysisRepository(server.url("/").toString(), demoToken = "synthetic")
                .discover(photo, "  optional question  ", "  label  ")
            assertEquals(DiscoveryStatus.CANDIDATE, result.status)
            val request = server.takeRequest()
            assertEquals("/product-discoveries", request.path)
            assertEquals("Bearer synthetic", request.getHeader("Authorization"))
            val body = request.body.readUtf8()
            assertTrue(body.contains("name=\"photo\""))
            assertTrue(body.contains("\r\noptional question\r\n"))
            assertTrue(body.contains("\r\nlabel\r\n"))
            assertFalse(body.contains("device_id"))
        } finally { server.shutdown() }
    }

    @Test fun invalidOrPrivateCitationIsRejectedBeforeDisplay() = runBlocking {
        val server = MockWebServer().apply { start() }
        try {
            val original = candidate()
            listOf("file:///private/photo", "https://user:secret@example.org/", "http://127.0.0.1/",
                "https://device.local/", "https://example.org/?token=secret").forEach { url ->
                val invalid = original.copy(candidates = original.candidates.map {
                    it.copy(sources = listOf(it.sources.single().copy(url = url)))
                })
                server.enqueue(MockResponse().setBody(Json.encodeToString(ProductDiscovery.serializer(), invalid)))
                try { HttpAnalysisRepository(server.url("/").toString()).discover(photo); fail("unsafe source") }
                catch (failure: ApiFailure) { assertEquals("invalid_response", failure.code) }
            }
        } finally { server.shutdown() }
    }

    @Test fun hintBoundaryFailsBeforeNetworkAndOptionalQuestionIsAllowed() = runBlocking {
        val server = MockWebServer().apply { start() }
        try {
            val repo = HttpAnalysisRepository(server.url("/").toString())
            try { repo.discover(photo, modelHint = "😀".repeat(201)); fail("hint too long") }
            catch (_: IllegalArgumentException) { }
            assertEquals(0, server.requestCount)
            server.enqueue(MockResponse().setBody(Json.encodeToString(ProductDiscovery.serializer(),
                ProductDiscovery("empty", DiscoveryStatus.NOT_FOUND, emptyList(), listOf("Clear label needed"), Mode.MOCK))))
            assertEquals(DiscoveryStatus.NOT_FOUND, repo.discover(photo, modelHint = "😀".repeat(200)).status)
        } finally { server.shutdown() }
    }

    @Test fun offlineDiscoveryNeverPromotesCandidateToAnalysisAndEditsClearResult() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val vm = AnalysisViewModel(repositoryFactory = { _, _ -> error("offline must not create HTTP repo") })
            vm.discoveryMode(true); vm.photo(photo)
            vm.discoverProducts(); advanceUntilIdle()
            assertEquals(Phase.RESULT, vm.state.value.phase)
            assertEquals(Mode.MOCK, vm.state.value.discovery?.mode)
            assertNull(vm.state.value.analysis)
            assertEquals("server", vm.state.value.deviceId)
            vm.showInput(); vm.showResult()
            assertEquals(Phase.RESULT, vm.state.value.phase)
            vm.modelHint("changed")
            assertNull(vm.state.value.discovery)
            assertEquals(Phase.INPUT, vm.state.value.phase)
        } finally { Dispatchers.resetMain() }
    }
}
