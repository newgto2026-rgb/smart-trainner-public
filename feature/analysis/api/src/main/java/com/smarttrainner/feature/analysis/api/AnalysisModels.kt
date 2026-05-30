package com.smarttrainner.feature.analysis.api

import androidx.compose.runtime.Composable
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.WeeklySummary
import com.smarttrainner.core.model.WorkoutLog

interface AnalysisFeatureEntry {
    @Composable
    fun Route()

    @Composable
    fun Content(state: AnalysisUiState)
}

data class AnalysisUiState(
    val recentLogs: List<RecentWorkoutLogUiModel> = emptyList(),
    val summary: WeeklySummary? = null
)

data class RecentWorkoutLogUiModel(
    val log: WorkoutLog,
    val exercise: Exercise?
)
