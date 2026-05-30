package com.smarttrainner.feature.routine.api

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.ui.ExerciseMediaRenderer

data class RoutineFeatureCallbacks(
    val onWorkoutStarted: (PlannedExercise) -> Unit = {},
    val onRoutineDayCompleted: () -> Unit = {},
    val onExerciseMethodSelected: (ExerciseId) -> Unit = {},
    val onRecordSelected: (PlannedExercise) -> Unit = {}
)

interface RoutineRouteState {
    val currentRoutineName: String

    fun nextPlannedExerciseAfterSaved(plannedExercise: PlannedExercise): PlannedExercise?

    fun recordablePlannedExerciseFor(exerciseId: ExerciseId): PlannedExercise?

    fun LazyListScope.HomeSummary()

    fun LazyListScope.Content(exerciseMediaRenderer: ExerciseMediaRenderer)

    @Composable
    fun Dialogs()
}
