package com.smarttrainner.feature.analysis.api

import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.WeeklySummary
import com.smarttrainner.core.model.WorkoutLog

data class AnalysisUiState(
    val recentLogs: List<RecentWorkoutLogUiModel> = emptyList(),
    val summary: WeeklySummary? = null
)

data class RecentWorkoutLogUiModel(
    val log: WorkoutLog,
    val exercise: Exercise?
)
