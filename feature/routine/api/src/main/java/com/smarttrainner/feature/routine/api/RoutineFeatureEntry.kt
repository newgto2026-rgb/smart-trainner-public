package com.smarttrainner.feature.routine.api

import androidx.compose.runtime.Composable

interface RoutineFeatureEntry {
    @Composable
    fun rememberRouteState(callbacks: RoutineFeatureCallbacks): RoutineRouteState

    @Composable
    fun HomeSummaryRoute(
        routeState: RoutineRouteState,
        title: String,
        subtitle: String
    )

    @Composable
    fun Route(
        routeState: RoutineRouteState,
        title: String,
        subtitle: String
    )

    @Composable
    fun Dialogs(routeState: RoutineRouteState)
}
