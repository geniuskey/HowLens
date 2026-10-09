package kr.howlens.app.ui

import kr.howlens.app.data.Analysis
import kr.howlens.app.data.Panel
import kr.howlens.app.data.Step

/** Local user marks only. This is never a server verification or a safety verdict. */
data class GuideJourneyState(
    val analysisId: String = "",
    val currentStepId: String? = null,
    val checkedStepIds: Set<String> = emptySet(),
    val userConfirmed: Boolean = false,
) {
    fun forAnalysis(analysis: Analysis): GuideJourneyState {
        val steps = if (analysis.canShowSteps) analysis.visibleSteps else emptyList()
        if (analysisId != analysis.analysisId) {
            return GuideJourneyState(analysis.analysisId, steps.firstOrNull()?.stepId)
        }
        val ids = steps.map { it.stepId }.toSet()
        val checked = checkedStepIds.intersect(ids)
        return copy(
            currentStepId = currentStepId?.takeIf { it in ids } ?: steps.firstOrNull()?.stepId,
            checkedStepIds = checked,
            userConfirmed = userConfirmed && ids.isNotEmpty() && checked.containsAll(ids),
        )
    }

    fun move(analysis: Analysis, offset: Int): GuideJourneyState {
        val state = forAnalysis(analysis)
        val steps = analysis.visibleSteps
        if (steps.isEmpty()) return state
        val index = steps.indexOfFirst { it.stepId == state.currentStepId }
        return state.copy(currentStepId = steps[(index + offset).coerceIn(0, steps.lastIndex)].stepId)
    }

    fun select(analysis: Analysis, stepId: String): GuideJourneyState {
        val state = forAnalysis(analysis)
        return if (analysis.visibleSteps.any { it.stepId == stepId }) state.copy(currentStepId = stepId) else state
    }

    fun check(analysis: Analysis, stepId: String, checked: Boolean): GuideJourneyState {
        val state = forAnalysis(analysis)
        if (analysis.visibleSteps.none { it.stepId == stepId }) return state
        return state.copy(
            checkedStepIds = if (checked) state.checkedStepIds + stepId else state.checkedStepIds - stepId,
            userConfirmed = if (checked) state.userConfirmed else false,
        )
    }

    fun confirm(analysis: Analysis): GuideJourneyState {
        val state = forAnalysis(analysis)
        val steps = analysis.visibleSteps
        return if (steps.isNotEmpty() && state.currentStepId == steps.last().stepId &&
            steps.all { it.stepId in state.checkedStepIds }) state.copy(userConfirmed = true) else state
    }
}

/** Images are optional decoration: approved text and step count come only from visibleSteps. */
data class GuideJourneyPage(
    val steps: List<Step>,
    val currentIndex: Int,
    val scenes: List<Panel>,
) {
    val step: Step? get() = steps.getOrNull(currentIndex)
    val total: Int get() = steps.size
}

fun guideJourneyPage(
    analysis: Analysis,
    state: GuideJourneyState,
    panels: List<Panel> = emptyList(),
): GuideJourneyPage {
    val steps = if (analysis.canShowSteps) analysis.visibleSteps else emptyList()
    val current = state.forAnalysis(analysis).currentStepId
    val index = steps.indexOfFirst { it.stepId == current }
    val scenes = if (index < 0) emptyList() else panels
        .filter { it.stepId == current && it.index in 0..8 }
        .distinctBy { it.index }.sortedBy { it.index }
    return GuideJourneyPage(steps, index, scenes)
}
