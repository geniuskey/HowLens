package kr.howlens.app.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class AnalysisSummarySheetTest {
    @Test
    fun guideCallbackIsSelectedOnlyWhenStepsAreAllowed() {
        var guideCalls = 0
        var updateCalls = 0

        summarySheetPrimaryAction(
            canShowSteps = true,
            onOpenGuide = { guideCalls++ },
            onRequestMoreInformation = { updateCalls++ },
        ).invoke()

        assertEquals(1, guideCalls)
        assertEquals(0, updateCalls)
    }

    @Test
    fun updateCallbackIsSelectedWhenStepsAreNotAllowed() {
        var guideCalls = 0
        var updateCalls = 0

        summarySheetPrimaryAction(
            canShowSteps = false,
            onOpenGuide = { guideCalls++ },
            onRequestMoreInformation = { updateCalls++ },
        ).invoke()

        assertEquals(0, guideCalls)
        assertEquals(1, updateCalls)
    }
}
