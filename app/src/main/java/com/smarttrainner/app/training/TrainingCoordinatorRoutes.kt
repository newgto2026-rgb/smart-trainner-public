package com.smarttrainner.app.training

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttrainner.R
import com.smarttrainner.core.ui.ExerciseMediaRenderer
import com.smarttrainner.core.ui.SmartTrainnerScreenChrome
import com.smarttrainner.feature.exercise.api.ExerciseCatalogActions
import com.smarttrainner.feature.exercise.api.ExerciseCatalogFeatureEntry
import com.smarttrainner.feature.exercise.api.ExerciseDetailFeatureEntry
import com.smarttrainner.feature.routine.api.RoutineFeatureCallbacks
import com.smarttrainner.feature.routine.api.RoutineFeatureEntry
import com.smarttrainner.feature.routine.api.RoutineRouteState
import com.smarttrainner.feature.workout.api.WorkoutRecordingFeatureEntry

@Composable
fun TrainingHomeRoute(
    exerciseDetailFeatureEntry: ExerciseDetailFeatureEntry,
    exerciseMediaRenderer: ExerciseMediaRenderer,
    routineFeatureEntry: RoutineFeatureEntry,
    workoutRecordingFeatureEntry: WorkoutRecordingFeatureEntry
) {
    val viewModel = sharedTrainingViewModel()
    val routineRouteState = rememberRoutineRouteState(
        routineFeatureEntry = routineFeatureEntry,
        viewModel = viewModel
    )
    val chrome = trainingScreenChrome(routineRouteState)
    TrainingRoute(
        exerciseDetailFeatureEntry = exerciseDetailFeatureEntry,
        exerciseMediaRenderer = exerciseMediaRenderer,
        workoutRecordingFeatureEntry = workoutRecordingFeatureEntry,
        routineRouteState = routineRouteState,
        viewModel = viewModel,
        routineDialogs = { routineFeatureEntry.Dialogs(routineRouteState) }
    ) {
        routineFeatureEntry.HomeSummaryRoute(
            routeState = routineRouteState,
            chrome = chrome
        )
    }
}

@Composable
fun TrainingRoutineRoute(
    exerciseDetailFeatureEntry: ExerciseDetailFeatureEntry,
    exerciseMediaRenderer: ExerciseMediaRenderer,
    routineFeatureEntry: RoutineFeatureEntry,
    workoutRecordingFeatureEntry: WorkoutRecordingFeatureEntry
) {
    val viewModel = sharedTrainingViewModel()
    val routineRouteState = rememberRoutineRouteState(
        routineFeatureEntry = routineFeatureEntry,
        viewModel = viewModel
    )
    val chrome = trainingScreenChrome(routineRouteState)
    TrainingRoute(
        exerciseDetailFeatureEntry = exerciseDetailFeatureEntry,
        exerciseMediaRenderer = exerciseMediaRenderer,
        workoutRecordingFeatureEntry = workoutRecordingFeatureEntry,
        routineRouteState = routineRouteState,
        viewModel = viewModel,
        routineDialogs = { routineFeatureEntry.Dialogs(routineRouteState) }
    ) {
        routineFeatureEntry.Route(
            routeState = routineRouteState,
            chrome = chrome,
            exerciseMediaRenderer = exerciseMediaRenderer
        )
    }
}

@Composable
fun TrainingExercisesRoute(
    exerciseCatalogFeatureEntry: ExerciseCatalogFeatureEntry,
    exerciseDetailFeatureEntry: ExerciseDetailFeatureEntry,
    exerciseMediaRenderer: ExerciseMediaRenderer,
    routineFeatureEntry: RoutineFeatureEntry,
    workoutRecordingFeatureEntry: WorkoutRecordingFeatureEntry
) {
    val viewModel = sharedTrainingViewModel()
    val trainingState by viewModel.uiState.collectAsStateWithLifecycle()
    val routineRouteState = rememberRoutineRouteState(
        routineFeatureEntry = routineFeatureEntry,
        viewModel = viewModel
    )
    val chrome = trainingScreenChrome(routineRouteState)
    val exerciseCatalogActions = remember(viewModel) {
        ExerciseCatalogActions(
            onExerciseSelected = viewModel::selectExercise
        )
    }
    TrainingRoute(
        exerciseDetailFeatureEntry = exerciseDetailFeatureEntry,
        exerciseMediaRenderer = exerciseMediaRenderer,
        workoutRecordingFeatureEntry = workoutRecordingFeatureEntry,
        routineRouteState = routineRouteState,
        viewModel = viewModel,
        routineDialogs = { routineFeatureEntry.Dialogs(routineRouteState) }
    ) {
        exerciseCatalogFeatureEntry.Route(
            chrome = chrome,
            selectedExerciseId = trainingState.selectedExerciseId,
            actions = exerciseCatalogActions
        )
    }
}

@Composable
private fun trainingScreenChrome(routineRouteState: RoutineRouteState): SmartTrainnerScreenChrome =
    SmartTrainnerScreenChrome(
        title = stringResource(R.string.app_name),
        subtitle = routineRouteState.currentRoutineName
    )

@Composable
private fun rememberRoutineRouteState(
    routineFeatureEntry: RoutineFeatureEntry,
    viewModel: TrainingViewModel
): RoutineRouteState = routineFeatureEntry.rememberRouteState(
    callbacks = RoutineFeatureCallbacks(
        onWorkoutStarted = viewModel::startContinuousRecording,
        onRoutineDayCompleted = viewModel::clearRecordingFlow,
        onExerciseMethodSelected = viewModel::showExerciseMethod,
        onRecordSelected = viewModel::selectPlannedExercise
    )
)
