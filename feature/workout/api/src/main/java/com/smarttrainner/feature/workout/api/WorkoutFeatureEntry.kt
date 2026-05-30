package com.smarttrainner.feature.workout.api

import androidx.compose.runtime.Composable
import com.smarttrainner.feature.exercise.api.ExerciseMediaFeatureEntry

interface WorkoutRecordingFeatureEntry {
    @Composable
    fun Dialog(
        state: WorkoutRecordingUiState,
        actions: WorkoutRecordingActions,
        exerciseMediaFeatureEntry: ExerciseMediaFeatureEntry
    )
}
