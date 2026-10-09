package kr.howlens.app.ui

import kr.howlens.app.data.Analysis
import kr.howlens.app.data.ConditionStatus
import kr.howlens.app.data.Decision
import kr.howlens.app.data.Evidence
import kr.howlens.app.data.Mode
import kr.howlens.app.data.Panel
import kr.howlens.app.data.Precondition
import kr.howlens.app.data.Step
import org.junit.Assert.*
import org.junit.Test

/** Synthetic local fixtures only; these do not represent a live approved hardware procedure. */
class GuideJourneyStateTest {
    private val analysis = Analysis(
        analysisId = "synthetic-analysis-a", deviceId = "server", decision = Decision.GUIDE,
        observations = emptyList(),
        evidence = listOf(Evidence("e", "test-document", "test-version", 1, null,
            "synthetic", "synthetic quote", "https://example.org/test.pdf")),
        preconditions = emptyList(),
        steps = listOf(Step("first", "Synthetic text one", listOf("e"), ""),
            Step("second", "Synthetic text two", listOf("e"), "")),
        warnings = listOf("Synthetic warning"), missingInformation = emptyList(), mode = Mode.LIVE,
    )

    @Test fun twoStepsNavigationNeverChecksOrConfirmsAutomatically() {
        val first = GuideJourneyState().forAnalysis(analysis)
        assertEquals("first", first.currentStepId)
        val last = first.move(analysis, 1).move(analysis, 1)
        assertEquals("second", last.currentStepId)
        assertEquals(2, guideJourneyPage(analysis, last).total)
        assertTrue(last.checkedStepIds.isEmpty())
        assertFalse(last.confirm(analysis).userConfirmed)
        assertEquals("first", last.move(analysis, -10).currentStepId)
    }

    @Test fun checkBackAndRevisitRetainStepIdMarks() {
        val checked = GuideJourneyState().check(analysis, "first", true)
        val second = checked.move(analysis, 1).check(analysis, "second", true)
        val revisited = second.move(analysis, -1).forAnalysis(analysis)
        assertEquals(setOf("first", "second"), revisited.checkedStepIds)
        assertEquals("first", revisited.currentStepId)
        assertFalse(revisited.userConfirmed)
        assertFalse(revisited.confirm(analysis).userConfirmed)
        assertTrue(revisited.move(analysis, 1).confirm(analysis).userConfirmed)
    }

    @Test fun newAnalysisResetsEvenWithIdenticalStepIds() {
        val finished = GuideJourneyState().check(analysis, "first", true)
            .move(analysis, 1).check(analysis, "second", true).confirm(analysis)
        val reset = finished.forAnalysis(analysis.copy(analysisId = "synthetic-analysis-b"))
        assertEquals("synthetic-analysis-b", reset.analysisId)
        assertEquals("first", reset.currentStepId)
        assertTrue(reset.checkedStepIds.isEmpty())
        assertFalse(reset.userConfirmed)
    }

    @Test fun nonGuideAndMalformedGuideNeverExposeTextScenesOrMarks() {
        val blocked = listOf(
            analysis.copy(decision = Decision.STOP),
            analysis.copy(decision = Decision.NEEDS_MORE_INFORMATION),
            analysis.copy(mode = Mode.MOCK),
            analysis.copy(evidence = emptyList()),
            analysis.copy(preconditions = listOf(Precondition("p", "synthetic", ConditionStatus.UNKNOWN, true, listOf("e")))),
        )
        val state = GuideJourneyState().check(analysis, "first", true)
        blocked.forEach { response ->
            val page = guideJourneyPage(response, state, listOf(Panel(0, "first", "/visual-assets/test.png")))
            assertTrue(page.steps.isEmpty())
            assertTrue(page.scenes.isEmpty())
            assertNull(page.step)
            assertTrue(state.check(response, "first", true).checkedStepIds.isEmpty())
            assertFalse(state.confirm(response).userConfirmed)
        }
    }

    @Test fun nineScenesAreMappedByStepIdAndNeverBecomeNineSteps() {
        val panels = (0..8).map { Panel(it, if (it < 5) "first" else "second", "/visual-assets/$it.png") }
        val first = guideJourneyPage(analysis, GuideJourneyState(), panels)
        assertEquals(2, first.total)
        assertEquals(listOf(0, 1, 2, 3, 4), first.scenes.map { it.index })
        val second = guideJourneyPage(analysis, GuideJourneyState().move(analysis, 1), panels)
        assertEquals(2, second.total)
        assertEquals(listOf(5, 6, 7, 8), second.scenes.map { it.index })
        assertEquals("Synthetic text two", second.step?.description)
    }

    @Test fun missingOrFailedImagesCannotRemoveApprovedText() {
        val state = GuideJourneyState().move(analysis, 1)
        val missing = guideJourneyPage(analysis, state)
        // A panel whose image cannot be loaded still carries no procedural text or step count.
        val failed = guideJourneyPage(analysis, state, listOf(Panel(7, "second", "/visual-assets/missing.png")))
        assertEquals("Synthetic text two", missing.step?.description)
        assertEquals(missing.step, failed.step)
        assertEquals(missing.total, failed.total)
        assertEquals(listOf("e"), failed.step?.evidenceIds)
        assertEquals(1, failed.scenes.size)
    }

    @Test fun uncheckingRevokesLocalCompletionAndUnknownIdsAreIgnored() {
        val finished = GuideJourneyState().check(analysis, "first", true)
            .move(analysis, 1).check(analysis, "second", true).confirm(analysis)
        assertTrue(finished.userConfirmed)
        val unchecked = finished.check(analysis, "first", false)
        assertFalse(unchecked.userConfirmed)
        assertFalse(unchecked.confirm(analysis).userConfirmed)
        assertEquals(unchecked, unchecked.check(analysis, "unapproved", true))
        assertEquals(unchecked, unchecked.select(analysis, "unapproved"))
    }

    @Test fun stepReorderingPreservesIdentityAndRemovedStepsPruneMarks() {
        val state = GuideJourneyState().check(analysis, "first", true).move(analysis, 1)
        val reordered = analysis.copy(steps = analysis.steps.reversed())
        assertEquals(0, guideJourneyPage(reordered, state).currentIndex)
        assertEquals(setOf("first"), state.forAnalysis(reordered).checkedStepIds)
        val reduced = analysis.copy(steps = listOf(analysis.steps[1]))
        assertTrue(state.forAnalysis(reduced).checkedStepIds.isEmpty())
        assertEquals("second", state.forAnalysis(reduced).currentStepId)
    }
}
