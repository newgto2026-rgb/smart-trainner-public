package com.smarttrainner.feature.routine.api

import androidx.compose.runtime.Composable

interface RoutineFeatureEntry {
    @Composable
    fun rememberRouteState(callbacks: RoutineFeatureCallbacks): RoutineRouteState
}
