package com.smarttrainner.feature.workout.api

import androidx.compose.runtime.Composable
import com.smarttrainner.core.ui.ExerciseMediaRenderer

interface WorkoutRecordingFeatureEntry {
    @Composable
    fun Dialog(
        state: WorkoutRecordingUiState,
        actions: WorkoutRecordingActions,
        exerciseMediaRenderer: ExerciseMediaRenderer
    )
}
