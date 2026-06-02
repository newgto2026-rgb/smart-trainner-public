package com.smarttrainner.feature.workout.api

import androidx.compose.runtime.Composable
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExercise

interface WorkoutRecordingFeatureEntry {
    @Composable
    fun DialogRoute(
        plannedExercise: PlannedExercise?,
        showRoutineSessionActions: Boolean,
        hasNextPlannedExercise: Boolean,
        onRecordSaved: (PlannedExercise) -> Unit,
        onSkipExercise: (PlannedExercise) -> Unit,
        onSubstituteExerciseRequested: (PlannedExercise) -> Unit,
        onAddExerciseRequested: (PlannedExercise) -> Unit,
        onExerciseMethodSelected: (ExerciseId) -> Unit,
        onDismiss: () -> Unit
    )
}
