package com.smarttrainner.feature.routine.api

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.WeeklyPlan
import com.smarttrainner.core.ui.ExerciseMediaRenderer

data class RoutineCoordinatorState(
    val plan: WeeklyPlan? = null,
    val completedPlannedExerciseIds: Set<PlannedExerciseId> = emptySet(),
    val customRoutineBuilderVisible: Boolean = false
)

data class RoutineFeatureCallbacks(
    val onWorkoutStarted: (PlannedExercise) -> Unit = {},
    val onRoutineDayCompleted: () -> Unit = {},
    val onExerciseMethodSelected: (ExerciseId) -> Unit = {},
    val onRecordSelected: (PlannedExercise) -> Unit = {}
)

interface RoutineRouteState {
    val coordinatorState: RoutineCoordinatorState

    @Composable
    fun currentRoutineName(): String

    fun LazyListScope.HomeSummary()

    fun LazyListScope.Content(exerciseMediaRenderer: ExerciseMediaRenderer)

    @Composable
    fun Dialogs()
}
