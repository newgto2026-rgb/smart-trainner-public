package com.smarttrainner.feature.routine.api

import androidx.compose.runtime.Composable
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.ui.ExerciseMediaRenderer
import com.smarttrainner.core.ui.SmartTrainnerScreenChrome

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

    @Composable
    fun HomeSummaryRoute(chrome: SmartTrainnerScreenChrome)

    @Composable
    fun Route(
        chrome: SmartTrainnerScreenChrome,
        exerciseMediaRenderer: ExerciseMediaRenderer
    )

    @Composable
    fun Dialogs()
}
