package kr.howlens.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.test.*
import kotlinx.serialization.json.Json
import kr.howlens.app.data.*
import kr.howlens.app.ui.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import java.util.concurrent.TimeUnit
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
    @Test fun sharePreviewUsesOnlyAllowlistedEvidenceAndFixedLimitations() {
        val source = guide().copy(
            observations = listOf("SERIAL=SN-123 IP=10.0.0.1 token=secret do not share"),
            steps = listOf(Step("s1", "raw work instruction", listOf("e1"), "visual")),
            evidence = listOf(guide().evidence.single().copy(
                quote = "private free-form quote", sourceUrl = "https://manual.example/public?doc=1"),
                guide().evidence.single().copy(evidenceId = "e2", sourceUrl = "http://insecure.example/manual")))
        val preview = SharePreview.build(source)
        assertTrue(preview.contains("Dell PowerEdge R750"))
        assertTrue(preview.contains("test-only")); assertTrue(preview.contains("PDF 1쪽"))
        assertTrue(preview.contains("https://manual.example/public?doc=1"))
        assertFalse(preview.contains("SERIAL")); assertFalse(preview.contains("10.0.0.1")); assertFalse(preview.contains("secret"))
        assertFalse(preview.contains("raw work instruction")); assertFalse(preview.contains("private free-form quote"))
        assertFalse(preview.contains("http://insecure.example")); assertFalse(preview.contains("test-analysis"))
        assertTrue(preview.contains("안전, 작업 성공"))
        assertTrue(SharePreview.build(source.copy(mode = Mode.MOCK)).contains("MOCK 합성 예시"))
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
    @Test fun liveGuideVisualRequestLoadsNineOrderedPanelsAndSameServerAssets() = runBlocking {
        Dispatchers.setMain(Dispatchers.Unconfined)
        val server = MockWebServer().apply { start() }
        try {
            server.enqueue(MockResponse().setBody(Json.encodeToString(Analysis.serializer(), guide())))
            server.enqueue(MockResponse().setBody(Json.encodeToString(VisualJob.serializer(), VisualJob(
                "visual-1", "test-analysis", VisualStatus.COMPLETED, null,
                (0..8).map { Panel(it, "s1", "/visual-assets/p$it.png") }, null, Mode.LIVE))))
            val png = java.util.Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+ip1sAAAAASUVORK5CYII=")
            repeat(9) { server.enqueue(MockResponse().setHeader("Content-Type", "image/png").setBody(okio.Buffer().write(png))) }
            val vm = AnalysisViewModel(repositoryFactory = { base, token ->
                HttpAnalysisRepository(base, PngAssetDecoder { bytes ->
                    assertArrayEquals(png, bytes) // Test-only decoder double; Android decoder remains the production default.
                }, token)
            })
            vm.offline(false); vm.baseUrl(server.url("/").toString()); vm.photo(photo); vm.question("test")
            vm.analyze(); awaitState(vm) { it.analysis != null }
            vm.requestVisual(); awaitState(vm) { !it.visualLoading && it.visualImages.size == 9 }
            assertEquals((0..8).toList(), vm.state.value.visualPanels.map { it.index })
            assertTrue(vm.state.value.analysis!!.canShowSteps)
            assertEquals(11, server.requestCount)
            assertEquals("/analyses", server.takeRequest().path)
            assertEquals("/analyses/test-analysis/visual", server.takeRequest().path)
            assertEquals((0..8).map { "/visual-assets/p$it.png" }.toSet(),
                (0..8).map { server.takeRequest().path }.toSet())
        } finally { server.shutdown(); Dispatchers.resetMain() }
    }
    @Test fun verificationUploadsAfterPhotoAndLimitsEvidenceToOriginalGuide() = runBlocking {
        val server = MockWebServer()
        server.start()
        try {
            server.enqueue(MockResponse().setBody(Json.encodeToString(Verification.serializer(), Verification(
                "test-analysis", VerificationResult.INCONCLUSIVE, listOf("Synthetic only"), listOf("e1"),
                listOf("A clearer after photo is needed"), listOf("Does not establish safe or normal operation"), Mode.LIVE))))
            val result = HttpAnalysisRepository(server.url("/").toString()).verify(guide(), photo, "operator note")
            assertEquals(VerificationResult.INCONCLUSIVE, result.result)
            val request = server.takeRequest()
            assertEquals("POST", request.method); assertEquals("/analyses/test-analysis/verification", request.path)
            val body = request.body.readUtf8()
            assertTrue(body.contains("name=\"photo\"; filename=\"photo.png\""))
            assertTrue(body.contains("name=\"user_confirmation\"")); assertTrue(body.contains("operator note"))
            assertFails { HttpAnalysisRepository(server.url("/").toString()).verify(guide(), photo,
                "x".repeat(2001)) }
            server.enqueue(MockResponse().setBody(Json.encodeToString(Verification.serializer(), Verification(
                "test-analysis", VerificationResult.INCONCLUSIVE, emptyList(), listOf("unregistered"), emptyList(), emptyList(), Mode.LIVE))))
            try {
                HttpAnalysisRepository(server.url("/").toString()).verify(guide(), photo)
                fail("unregistered evidence must be rejected")
            } catch (_: IllegalArgumentException) { }
        } finally { server.shutdown() }
    }
    @Test fun visualFailureKeepsTextAndAllowsOnlyOneUserRetry() = runBlocking {
        Dispatchers.setMain(Dispatchers.Unconfined)
        val server = MockWebServer().apply { start() }
        try {
            server.enqueue(MockResponse().setBody(Json.encodeToString(Analysis.serializer(), guide())))
            repeat(2) { server.enqueue(MockResponse().setBody(Json.encodeToString(VisualJob.serializer(), VisualJob(
                "visual-$it", "test-analysis", VisualStatus.FAILED, null, emptyList(), "Synthetic failure", Mode.LIVE)))) }
            val vm = AnalysisViewModel()
            vm.offline(false); vm.baseUrl(server.url("/").toString()); vm.photo(photo); vm.question("test")
            vm.analyze(); awaitState(vm) { it.analysis != null }
            vm.requestVisual(); awaitState(vm) { !it.visualLoading && it.visualError != null }
            assertEquals("test-analysis", vm.state.value.analysis?.analysisId)
            assertTrue(vm.state.value.analysis!!.visibleSteps.isNotEmpty())
            vm.requestVisual(); awaitState(vm) { !it.visualLoading && it.visualAttempts == 2 }
            vm.requestVisual()
            assertEquals(2, vm.state.value.visualAttempts)
            assertEquals(3, server.requestCount)
        } finally { server.shutdown(); Dispatchers.resetMain() }
    }
    @Test fun malformedPanelsAreRejectedAndVisualCancellationPreservesGuide() = runBlocking {
        Dispatchers.setMain(Dispatchers.Unconfined)
        val server = MockWebServer().apply { start() }
        try {
            server.enqueue(MockResponse().setBody(Json.encodeToString(Analysis.serializer(), guide())))
            server.enqueue(MockResponse().setBody(Json.encodeToString(VisualJob.serializer(), VisualJob(
                "visual-bad", "test-analysis", VisualStatus.COMPLETED, null,
                (0..7).map { Panel(it, "s1", "/visual-assets/p$it.png") }, null, Mode.LIVE))))
            val vm = AnalysisViewModel()
            vm.offline(false); vm.baseUrl(server.url("/").toString()); vm.photo(photo); vm.question("test")
            vm.analyze(); awaitState(vm) { it.analysis != null }
            vm.requestVisual(); awaitState(vm) { !it.visualLoading && it.visualError != null }
            assertTrue(vm.state.value.visualError!!.contains("패널 9개"))
            assertEquals("test-analysis", vm.state.value.analysis?.analysisId)
            assertTrue(vm.state.value.visualImages.isEmpty())
        } finally { server.shutdown(); Dispatchers.resetMain() }
    }
    @Test fun cancellingHttpVisualCallCancelsOkHttpAndRetainsOriginalText() = runBlocking {
        Dispatchers.setMain(Dispatchers.Unconfined)
        val server = MockWebServer().apply { start() }
        try {
            server.enqueue(MockResponse().setBody(Json.encodeToString(Analysis.serializer(), guide())))
            server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
            val vm = AnalysisViewModel()
            vm.offline(false); vm.baseUrl(server.url("/").toString()); vm.photo(photo); vm.question("test")
            vm.analyze(); awaitState(vm) { it.analysis != null }
            vm.requestVisual()
            server.takeRequest(3, TimeUnit.SECONDS)
            vm.cancelVisual()
            awaitState(vm) { !it.visualLoading }
            assertEquals("test-analysis", vm.state.value.analysis?.analysisId)
            assertTrue(vm.state.value.analysis!!.canShowSteps)
            assertTrue(vm.state.value.visualError!!.contains("취소"))
        } finally { server.shutdown(); Dispatchers.resetMain() }
    }
    @Test fun delayedPriorVisualCannotOverwriteNewAnalysis() = runBlocking {
        Dispatchers.setMain(Dispatchers.Unconfined)
        val server = MockWebServer().apply { start() }
        val moreInfo = guide().copy(analysisId = "analysis-b", decision = Decision.NEEDS_MORE_INFORMATION,
            steps = emptyList(), missingInformation = listOf("Synthetic new result"))
        try {
            server.enqueue(MockResponse().setBody(Json.encodeToString(VisualJob.serializer(), VisualJob(
                "visual-old", "test-analysis", VisualStatus.QUEUED, null, emptyList(), null, Mode.LIVE))).setResponseCode(202))
            server.enqueue(MockResponse().setHeadersDelay(2, TimeUnit.SECONDS)
                .setBody(Json.encodeToString(VisualJob.serializer(), VisualJob(
                    "visual-old", "test-analysis", VisualStatus.COMPLETED, null,
                    (0..8).map { Panel(it, "s1", "/visual-assets/old-$it.png") }, null, Mode.LIVE))))
            server.enqueue(MockResponse().setBody(Json.encodeToString(Analysis.serializer(), moreInfo)))
            val vm = AnalysisViewModel(AnalysisUiState(
                deviceId = "server", question = "same photo", photo = photo, offline = false,
                baseUrl = server.url("/").toString(), phase = Phase.RESULT, analysis = guide()))
            vm.requestVisual()
            withTimeout(5000) { while (server.requestCount < 2) delay(10) }
            vm.showInput()
            vm.analyze()
            awaitState(vm) { it.analysis?.analysisId == "analysis-b" }
            delay(2200)
            assertEquals("analysis-b", vm.state.value.analysis?.analysisId)
            assertFalse(vm.state.value.visualLoading)
            assertTrue(vm.state.value.visualPanels.isEmpty())
            assertTrue(vm.state.value.visualImages.isEmpty())
            assertNull(vm.state.value.visualError)
        } finally { server.shutdown(); Dispatchers.resetMain() }
    }
    private suspend fun awaitState(vm: AnalysisViewModel, predicate: (AnalysisUiState) -> Boolean) {
        withTimeout(5000) { while (!predicate(vm.state.value)) delay(10) }
    }
    private suspend fun assertFails(block: suspend () -> Unit) {
        try { block(); fail("Expected gate rejection") } catch (_: IllegalArgumentException) { }
    }
}
