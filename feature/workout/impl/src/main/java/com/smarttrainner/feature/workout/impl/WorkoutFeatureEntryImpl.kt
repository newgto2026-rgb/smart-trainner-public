package com.smarttrainner.feature.workout.impl

import androidx.compose.runtime.Composable
import com.smarttrainner.core.ui.ExerciseMediaRenderer
import com.smarttrainner.feature.workout.api.WorkoutRecordingActions
import com.smarttrainner.feature.workout.api.WorkoutRecordingFeatureEntry
import com.smarttrainner.feature.workout.api.WorkoutRecordingUiState
import javax.inject.Inject

class WorkoutFeatureEntryImpl @Inject constructor() : WorkoutRecordingFeatureEntry {
    @Composable
    override fun Dialog(
        state: WorkoutRecordingUiState,
        actions: WorkoutRecordingActions,
        exerciseMediaRenderer: ExerciseMediaRenderer
    ) {
        WorkoutRecordDialog(
            state = state,
            actions = actions,
            exerciseMediaRenderer = exerciseMediaRenderer
        )
    }
}
