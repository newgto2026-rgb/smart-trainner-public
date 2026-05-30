package com.smarttrainner.feature.workout.api

import androidx.compose.runtime.Composable
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.ui.ExerciseMediaRenderer

interface WorkoutRecordingFeatureEntry {
    @Composable
    fun DialogRoute(
        plannedExercise: PlannedExercise?,
        onRecordSaved: (PlannedExercise) -> Unit,
        onExerciseMethodSelected: (ExerciseId) -> Unit,
        onDismiss: () -> Unit,
        exerciseMediaRenderer: ExerciseMediaRenderer
    )
}
