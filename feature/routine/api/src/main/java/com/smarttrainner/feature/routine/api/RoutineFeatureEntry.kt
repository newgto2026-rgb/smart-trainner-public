package com.smarttrainner.feature.routine.api

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import com.smarttrainner.core.ui.ExerciseMediaRenderer

interface RoutineFeatureEntry {
    @Composable
    fun rememberRouteState(callbacks: RoutineFeatureCallbacks): RoutineRouteState

    fun LazyListScope.HomeSummary(
        state: RoutineUiState,
        actions: RoutineActions
    )

    fun LazyListScope.Content(
        state: RoutineUiState,
        actions: RoutineActions,
        exerciseMediaRenderer: ExerciseMediaRenderer
    )

    @Composable
    fun Dialogs(
        state: RoutineUiState,
        actions: RoutineActions
    )
}
