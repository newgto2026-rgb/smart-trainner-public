package com.smarttrainner.feature.analysis.impl

import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.WeeklySummary
import com.smarttrainner.core.model.WorkoutLog

internal data class AnalysisUiState(
    val recentLogs: List<RecentWorkoutLogUiModel> = emptyList(),
    val summary: WeeklySummary? = null,
    val cycleNumber: Int = 1
)

internal data class RecentWorkoutLogUiModel(
    val log: WorkoutLog,
    val exercise: Exercise?
)
