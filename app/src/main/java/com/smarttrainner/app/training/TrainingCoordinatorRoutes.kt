package com.smarttrainner.app.training

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttrainner.core.ui.ExerciseMediaRenderer
import com.smarttrainner.feature.exercise.api.ExerciseCatalogFeatureEntry
import com.smarttrainner.feature.exercise.api.ExerciseDetailFeatureEntry
import com.smarttrainner.feature.routine.api.RoutineFeatureEntry
import com.smarttrainner.feature.workout.api.WorkoutRecordingFeatureEntry

@Composable
fun TrainingHomeRoute(
    exerciseDetailFeatureEntry: ExerciseDetailFeatureEntry,
    exerciseMediaRenderer: ExerciseMediaRenderer,
    routineFeatureEntry: RoutineFeatureEntry,
    workoutRecordingFeatureEntry: WorkoutRecordingFeatureEntry
) {
    TrainingRoute(
        exerciseDetailFeatureEntry = exerciseDetailFeatureEntry,
        exerciseMediaRenderer = exerciseMediaRenderer,
        routineFeatureEntry = routineFeatureEntry,
        workoutRecordingFeatureEntry = workoutRecordingFeatureEntry
    ) { state, routineActions, _ ->
        with(routineFeatureEntry) {
            HomeSummary(
                state = state.routine,
                actions = routineActions
            )
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
    TrainingRoute(
        exerciseDetailFeatureEntry = exerciseDetailFeatureEntry,
        exerciseMediaRenderer = exerciseMediaRenderer,
        routineFeatureEntry = routineFeatureEntry,
        workoutRecordingFeatureEntry = workoutRecordingFeatureEntry
    ) { state, routineActions, _ ->
        with(routineFeatureEntry) {
            Content(
                state = state.routine,
                actions = routineActions,
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
    val exerciseCatalogState = exerciseCatalogFeatureEntry.rememberUiState(
        selectedExerciseId = trainingState.selectedExerciseId
    )
    TrainingRoute(
        exerciseDetailFeatureEntry = exerciseDetailFeatureEntry,
        exerciseMediaRenderer = exerciseMediaRenderer,
        routineFeatureEntry = routineFeatureEntry,
        workoutRecordingFeatureEntry = workoutRecordingFeatureEntry,
        viewModel = viewModel
    ) { _, _, exerciseCatalogActions ->
        with(exerciseCatalogFeatureEntry) {
            Content(
                state = exerciseCatalogState,
                actions = exerciseCatalogActions
            )
        }
    }
}
