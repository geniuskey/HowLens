package kr.howlens.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlinx.serialization.json.Json
import kr.howlens.app.data.*
import kr.howlens.app.ui.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FoundationTest {
    private val photo = Photo(byteArrayOf(1, 2, 3), "image/png", 100, 100)
    private fun guide() = Analysis("test-analysis", "server", Decision.GUIDE, listOf("Test fixture only"),
        listOf(Evidence("e1", "synthetic-doc", "test-only", 1, null, "fixture", "Synthetic quote", "https://example.org/test")),
        listOf(Precondition("p1", "fixture", ConditionStatus.SATISFIED, true, listOf("e1"))),
        listOf(Step("s1", "Synthetic step; never execute", listOf("e1"), "fixture")), emptyList(), emptyList(), Mode.LIVE)

    @Test fun nonGuideAndMockNeverExposeStepsOrImagesEvenWithInjectedSteps() {
        val original = guide()
        assertTrue(original.canShowSteps)
        listOf(Decision.STOP, Decision.NEEDS_MORE_INFORMATION).forEach {
            val blocked = original.copy(decision = it)
            assertTrue(blocked.visibleSteps.isEmpty())
            assertFalse(blocked.canRequestVisual)
        }
        assertFalse(original.copy(mode = Mode.MOCK).canShowSteps)
        assertFalse(original.copy(mode = Mode.MOCK).canRequestVisual)
        assertFalse(original.copy(preconditions = listOf(original.preconditions.single().copy(status = ConditionStatus.UNKNOWN))).canShowSteps)
        assertFalse(original.copy(steps = listOf(original.steps.single().copy(evidenceIds = listOf("missing")))).canShowSteps)
        assertFalse(original.copy(steps = List(10) { original.steps.single() }).canShowSteps)
    }
    @Test fun inputBoundaries() {
        assertNull(InputRules.validate("server", " q ", photo))
        assertNotNull(InputRules.validate("invalid", "q", photo))
        assertNotNull(InputRules.validate("server", " \n ", photo))
        assertNull(InputRules.validate("server", "q".repeat(2000), photo))
        assertNotNull(InputRules.validate("server", "q".repeat(2001), photo))
        assertNull(InputRules.validate("server", "😀".repeat(2000), photo))
        assertNotNull(InputRules.validate("server", "😀".repeat(2001), photo))
        assertNotNull(InputRules.validate("server", "q", null))
        assertNotNull(InputRules.validate("server", "q", photo.copy(mime = "image/gif")))
        assertNotNull(InputRules.validate("server", "q", photo.copy(width = -1)))
        assertNull(InputRules.validate("server", "q", photo.copy(width = 5000, height = 4000)))
        assertNotNull(InputRules.validate("server", "q", photo.copy(width = 5001, height = 4000)))
        assertNull(InputRules.validate("server", "q", photo.copy(bytes = ByteArray(InputRules.MAX_BYTES))))
        assertNotNull(InputRules.validate("server", "q", photo.copy(bytes = ByteArray(InputRules.MAX_BYTES + 1))))
    }
    @Test fun bothErrorShapesAndFallback() {
        val structured = ApiErrors.parse(503, """{"detail":{"code":"upstream","message":"Unavailable","retryable":true}}""")
        assertEquals("upstream", structured.code); assertTrue(structured.retryable)
        val validation = ApiErrors.parse(422, """{"detail":[{"loc":["body","question"],"msg":"Too short","type":"value_error"}]}""")
        assertEquals("Too short", validation.message); assertFalse(validation.retryable)
        assertTrue(ApiErrors.parse(504, "not json").retryable)
        assertFalse(ApiErrors.parse(415, "not json").retryable)
    }
    @Test fun multipartAndSnakeCaseRoundTrip() = runTest {
        val server = MockWebServer()
        server.start()
        try {
            server.enqueue(MockResponse().setBody(Json.encodeToString(Analysis.serializer(), guide())))
            val repository = HttpAnalysisRepository(server.url("/").toString())
            val response = repository.analyze("server", "  inspect  ", photo)
            assertEquals("test-analysis", response.analysisId)
            assertNull(response.evidence.single().printedPage)
            val request = server.takeRequest()
            assertEquals("POST", request.method); assertEquals("/analyses", request.path)
            assertTrue(request.getHeader("Content-Type")!!.startsWith("multipart/form-data; boundary="))
            val body = request.body.readUtf8()
            assertTrue(body.contains("name=\"device_id\"")); assertTrue(body.contains("server"))
            assertTrue(body.contains("name=\"question\"")); assertTrue(body.contains("\r\ninspect\r\n"))
            assertTrue(body.contains("name=\"photo\"; filename=\"photo.png\""))
            assertTrue(body.contains("Content-Type: image/png"))
            assertFails { repository.createVisual(guide().copy(decision = Decision.STOP)) }
            assertFails { repository.createVisual(guide().copy(mode = Mode.MOCK)) }
            assertEquals(1, server.requestCount)
            assertFails { repository.assetUrl("https://evil.example/image.png") }
            assertFails { repository.assetUrl("/visual-assets/../secret") }
            assertTrue(repository.assetUrl("/visual-assets/test.png").startsWith(server.url("/").toString()))
        } finally { server.shutdown() }
    }
    @Test fun fakeScreensLoadingResultAndInputReset() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val vm = AnalysisViewModel()
            vm.analyze(); assertNotNull(vm.state.value.error)
            vm.photo(photo); vm.question("Question")
            vm.analyze(); runCurrent()
            assertEquals(Phase.LOADING, vm.state.value.phase)
            advanceUntilIdle()
            val result = vm.state.value.analysis!!
            assertEquals(Mode.MOCK, result.mode)
            assertEquals(Decision.NEEDS_MORE_INFORMATION, result.decision)
            assertTrue(result.visibleSteps.isEmpty())
            vm.scenario(FakeScenario.STOP)
            assertNull(vm.state.value.analysis)
            vm.analyze(); advanceUntilIdle()
            assertEquals(Decision.STOP, vm.state.value.analysis!!.decision)
            assertFalse(vm.state.value.analysis!!.canRequestVisual)
            vm.question("Changed")
            assertEquals(Phase.INPUT, vm.state.value.phase); assertNull(vm.state.value.analysis)
            vm.analyze(); runCurrent(); vm.cancel(); advanceUntilIdle()
            assertEquals(Phase.INPUT, vm.state.value.phase); assertNull(vm.state.value.analysis)
        } finally { Dispatchers.resetMain() }
    }
    private suspend fun assertFails(block: suspend () -> Unit) {
        try { block(); fail("Expected gate rejection") } catch (_: IllegalArgumentException) { }
    }
}
