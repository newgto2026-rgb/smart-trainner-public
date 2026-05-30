package com.smarttrainner.feature.training.impl

import androidx.compose.runtime.Composable
import com.smarttrainner.feature.exercise.api.ExerciseCatalogFeatureEntry
import com.smarttrainner.feature.exercise.api.ExerciseDetailFeatureEntry
import com.smarttrainner.feature.exercise.api.ExerciseMediaFeatureEntry
import com.smarttrainner.feature.routine.api.RoutineFeatureEntry
import com.smarttrainner.feature.training.api.TrainingFeatureEntry
import com.smarttrainner.feature.workout.api.WorkoutRecordingFeatureEntry
import javax.inject.Inject

class TrainingFeatureEntryImpl @Inject constructor(
    private val exerciseCatalogFeatureEntry: ExerciseCatalogFeatureEntry,
    private val exerciseDetailFeatureEntry: ExerciseDetailFeatureEntry,
    private val exerciseMediaFeatureEntry: ExerciseMediaFeatureEntry,
    private val routineFeatureEntry: RoutineFeatureEntry,
    private val workoutRecordingFeatureEntry: WorkoutRecordingFeatureEntry
) : TrainingFeatureEntry {
    @Composable
    override fun Home() {
        TrainingRoute(
            exerciseDetailFeatureEntry = exerciseDetailFeatureEntry,
            exerciseMediaFeatureEntry = exerciseMediaFeatureEntry,
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
    override fun Routine() {
        TrainingRoute(
            exerciseDetailFeatureEntry = exerciseDetailFeatureEntry,
            exerciseMediaFeatureEntry = exerciseMediaFeatureEntry,
            routineFeatureEntry = routineFeatureEntry,
            workoutRecordingFeatureEntry = workoutRecordingFeatureEntry
        ) { state, routineActions, _ ->
            with(routineFeatureEntry) {
                Content(
                    state = state.routine,
                    actions = routineActions,
                    exerciseMediaFeatureEntry = exerciseMediaFeatureEntry
                )
            }
        }
    }

    @Composable
    override fun Exercises() {
        TrainingRoute(
            exerciseDetailFeatureEntry = exerciseDetailFeatureEntry,
            exerciseMediaFeatureEntry = exerciseMediaFeatureEntry,
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
}
