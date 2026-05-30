package com.smarttrainner.feature.routine.api

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import com.smarttrainner.feature.exercise.api.ExerciseMediaFeatureEntry

interface RoutineFeatureEntry {
    fun LazyListScope.HomeSummary(
        state: RoutineUiState,
        actions: RoutineActions
    )

    fun LazyListScope.Content(
        state: RoutineUiState,
        actions: RoutineActions,
        exerciseMediaFeatureEntry: ExerciseMediaFeatureEntry
    )

    @Composable
    fun Dialogs(
        state: RoutineUiState,
        actions: RoutineActions
    )
}
