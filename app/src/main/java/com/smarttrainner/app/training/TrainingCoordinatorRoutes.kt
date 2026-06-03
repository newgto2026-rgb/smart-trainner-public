package com.smarttrainner.app.training

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttrainner.R
import com.smarttrainner.feature.exercise.api.ExerciseCatalogFeatureEntry
import com.smarttrainner.feature.exercise.api.ExerciseDetailFeatureEntry
import com.smarttrainner.feature.routine.api.RoutineFeatureCallbacks
import com.smarttrainner.feature.routine.api.RoutineFeatureEntry
import com.smarttrainner.feature.routine.api.RoutineRouteState
import com.smarttrainner.feature.workout.api.WorkoutRecordingFeatureEntry

@Composable
fun TrainingHomeRoute(
    exerciseDetailFeatureEntry: ExerciseDetailFeatureEntry,
    routineFeatureEntry: RoutineFeatureEntry,
    workoutRecordingFeatureEntry: WorkoutRecordingFeatureEntry
) {
    val viewModel = sharedTrainingViewModel()
    val routineRouteState = rememberRoutineRouteState(
        routineFeatureEntry = routineFeatureEntry,
        viewModel = viewModel
    )
    val routeChrome = trainingRouteChrome(routineRouteState)
    TrainingRoute(
        exerciseDetailFeatureEntry = exerciseDetailFeatureEntry,
        workoutRecordingFeatureEntry = workoutRecordingFeatureEntry,
        routineRouteState = routineRouteState,
        viewModel = viewModel,
        routineDialogs = { routineFeatureEntry.Dialogs(routineRouteState) }
    ) {
        routineFeatureEntry.HomeSummaryRoute(
            routeState = routineRouteState,
            title = routeChrome.title,
            subtitle = routeChrome.subtitle
        )
    }
}

@Composable
fun TrainingRoutineRoute(
    exerciseDetailFeatureEntry: ExerciseDetailFeatureEntry,
    routineFeatureEntry: RoutineFeatureEntry,
    workoutRecordingFeatureEntry: WorkoutRecordingFeatureEntry,
    routineLibraryOpenRequest: Int = 0,
    onRoutineLibraryOpenRequestConsumed: (Int) -> Unit = {}
) {
    val viewModel = sharedTrainingViewModel()
    val routineRouteState = rememberRoutineRouteState(
        routineFeatureEntry = routineFeatureEntry,
        viewModel = viewModel,
        routineLibraryOpenRequest = routineLibraryOpenRequest,
        onRoutineLibraryOpenRequestConsumed = onRoutineLibraryOpenRequestConsumed
    )
    val routeChrome = trainingRouteChrome(routineRouteState)
    TrainingRoute(
        exerciseDetailFeatureEntry = exerciseDetailFeatureEntry,
        workoutRecordingFeatureEntry = workoutRecordingFeatureEntry,
        routineRouteState = routineRouteState,
        viewModel = viewModel,
        routineDialogs = { routineFeatureEntry.Dialogs(routineRouteState) }
    ) {
        routineFeatureEntry.Route(
            routeState = routineRouteState,
            title = routeChrome.title,
            subtitle = routeChrome.subtitle
        )
    }
}

@Composable
fun TrainingExercisesRoute(
    exerciseCatalogFeatureEntry: ExerciseCatalogFeatureEntry,
    exerciseDetailFeatureEntry: ExerciseDetailFeatureEntry,
    routineFeatureEntry: RoutineFeatureEntry,
    workoutRecordingFeatureEntry: WorkoutRecordingFeatureEntry
) {
    val viewModel = sharedTrainingViewModel()
    val trainingState by viewModel.uiState.collectAsStateWithLifecycle()
    val routineRouteState = rememberRoutineRouteState(
        routineFeatureEntry = routineFeatureEntry,
        viewModel = viewModel
    )
    val routeChrome = trainingRouteChrome(routineRouteState)
    TrainingRoute(
        exerciseDetailFeatureEntry = exerciseDetailFeatureEntry,
        workoutRecordingFeatureEntry = workoutRecordingFeatureEntry,
        routineRouteState = routineRouteState,
        viewModel = viewModel,
        routineDialogs = { routineFeatureEntry.Dialogs(routineRouteState) }
    ) {
        exerciseCatalogFeatureEntry.Route(
            title = routeChrome.title,
            subtitle = routeChrome.subtitle,
            selectedExerciseId = trainingState.selectedExerciseId,
            onExerciseSelected = viewModel::selectExercise
        )
    }
}

@Composable
private fun trainingRouteChrome(routineRouteState: RoutineRouteState): TrainingRouteChrome =
    TrainingRouteChrome(
        title = stringResource(R.string.app_name),
        subtitle = routineRouteState.currentRoutineName
    )

private data class TrainingRouteChrome(
    val title: String,
    val subtitle: String
)

@Composable
private fun rememberRoutineRouteState(
    routineFeatureEntry: RoutineFeatureEntry,
    viewModel: TrainingViewModel,
    routineLibraryOpenRequest: Int = 0,
    onRoutineLibraryOpenRequestConsumed: (Int) -> Unit = {}
): RoutineRouteState = routineFeatureEntry.rememberRouteState(
    callbacks = RoutineFeatureCallbacks(
        onWorkoutStarted = viewModel::startContinuousRecording,
        onRoutineDayCompleted = viewModel::clearRecordingFlow,
        onExerciseMethodSelected = viewModel::showExerciseMethod,
        onRecordSelected = viewModel::selectPlannedExercise,
        onSubstituteExerciseSelected = viewModel::replaceRecordingExercise,
        onAdditionalExerciseSelected = viewModel::recordAdditionalExercise,
        routineLibraryOpenRequest = routineLibraryOpenRequest,
        onRoutineLibraryOpenRequestConsumed = onRoutineLibraryOpenRequestConsumed
    )
)
