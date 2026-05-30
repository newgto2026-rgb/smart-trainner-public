package com.smarttrainner.feature.workout.impl

import androidx.compose.runtime.Composable
import com.smarttrainner.feature.exercise.api.ExerciseMediaFeatureEntry
import com.smarttrainner.feature.workout.api.WorkoutRecordingActions
import com.smarttrainner.feature.workout.api.WorkoutRecordingFeatureEntry
import com.smarttrainner.feature.workout.api.WorkoutRecordingUiState
import javax.inject.Inject

class WorkoutFeatureEntryImpl @Inject constructor() : WorkoutRecordingFeatureEntry {
    @Composable
    override fun Dialog(
        state: WorkoutRecordingUiState,
        actions: WorkoutRecordingActions,
        exerciseMediaFeatureEntry: ExerciseMediaFeatureEntry
    ) {
        WorkoutRecordDialog(
            state = state,
            actions = actions,
            exerciseMediaFeatureEntry = exerciseMediaFeatureEntry
        )
    }
}
