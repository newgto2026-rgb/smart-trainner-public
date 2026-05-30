package com.smarttrainner.app.training

import androidx.compose.runtime.Composable
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
    TrainingRoute(
        exerciseDetailFeatureEntry = exerciseDetailFeatureEntry,
        exerciseMediaRenderer = exerciseMediaRenderer,
        routineFeatureEntry = routineFeatureEntry,
        workoutRecordingFeatureEntry = workoutRecordingFeatureEntry
    ) { state, _, exerciseCatalogActions ->
        with(exerciseCatalogFeatureEntry) {
            Content(
                state = state.exerciseCatalog,
                actions = exerciseCatalogActions
            )
        }
    }
}
