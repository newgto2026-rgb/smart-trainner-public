package com.smarttrainner.feature.routine.api

import androidx.compose.runtime.Composable
import com.smarttrainner.core.ui.SmartTrainnerScreenChrome

interface RoutineFeatureEntry {
    @Composable
    fun rememberRouteState(callbacks: RoutineFeatureCallbacks): RoutineRouteState

    @Composable
    fun HomeSummaryRoute(
        routeState: RoutineRouteState,
        chrome: SmartTrainnerScreenChrome
    )

    @Composable
    fun Route(
        routeState: RoutineRouteState,
        chrome: SmartTrainnerScreenChrome
    )

    @Composable
    fun Dialogs(routeState: RoutineRouteState)
}
