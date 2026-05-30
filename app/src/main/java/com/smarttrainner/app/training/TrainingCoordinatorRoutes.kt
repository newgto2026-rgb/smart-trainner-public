package com.smarttrainner.app.training

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttrainner.core.ui.ExerciseMediaRenderer
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
    val viewModel: TrainingViewModel = hiltViewModel()
    val routineRouteState = rememberRoutineRouteState(
        routineFeatureEntry = routineFeatureEntry,
        viewModel = viewModel
    )
    TrainingRoute(
        exerciseDetailFeatureEntry = exerciseDetailFeatureEntry,
        exerciseMediaRenderer = exerciseMediaRenderer,
        workoutRecordingFeatureEntry = workoutRecordingFeatureEntry,
        routineRouteState = routineRouteState,
        viewModel = viewModel
    ) { _, _ ->
        with(routineRouteState) {
            HomeSummary()
        }
    }
}

@Composable
fun TrainingRoutineRoute(
    exerciseDetailFeatureEntry: ExerciseDetailFeatureEntry,
    exerciseMediaRenderer: ExerciseMediaRenderer,
    routineFeatureEntry: RoutineFeatureEntry,
    workoutRecordingFeatureEntry: WorkoutRecordingFeatureEntry
) {
    val viewModel: TrainingViewModel = hiltViewModel()
    val routineRouteState = rememberRoutineRouteState(
        routineFeatureEntry = routineFeatureEntry,
        viewModel = viewModel
    )
    TrainingRoute(
        exerciseDetailFeatureEntry = exerciseDetailFeatureEntry,
        exerciseMediaRenderer = exerciseMediaRenderer,
        workoutRecordingFeatureEntry = workoutRecordingFeatureEntry,
        routineRouteState = routineRouteState,
        viewModel = viewModel
    ) { _, _ ->
        with(routineRouteState) {
            Content(
                exerciseMediaRenderer = exerciseMediaRenderer
            )
        }
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
    val viewModel: TrainingViewModel = hiltViewModel()
    val trainingState by viewModel.uiState.collectAsStateWithLifecycle()
    val routineRouteState = rememberRoutineRouteState(
        routineFeatureEntry = routineFeatureEntry,
        viewModel = viewModel
    )
    val exerciseCatalogState = exerciseCatalogFeatureEntry.rememberUiState(
        selectedExerciseId = trainingState.selectedExerciseId
    )
    TrainingRoute(
        exerciseDetailFeatureEntry = exerciseDetailFeatureEntry,
        exerciseMediaRenderer = exerciseMediaRenderer,
        workoutRecordingFeatureEntry = workoutRecordingFeatureEntry,
        routineRouteState = routineRouteState,
        viewModel = viewModel
    ) { _, exerciseCatalogActions ->
        with(exerciseCatalogFeatureEntry) {
            Content(
                state = exerciseCatalogState,
                actions = exerciseCatalogActions
            )
        }
    }
}

@Composable
private fun rememberRoutineRouteState(
    routineFeatureEntry: RoutineFeatureEntry,
    viewModel: TrainingViewModel
): RoutineRouteState = routineFeatureEntry.rememberRouteState(
    callbacks = RoutineFeatureCallbacks(
        onWorkoutStarted = viewModel::startWorkout,
        onRoutineDayCompleted = viewModel::clearRecordingFlow,
        onExerciseMethodSelected = viewModel::showExerciseMethod,
        onRecordSelected = viewModel::selectPlannedExercise
    )
)
