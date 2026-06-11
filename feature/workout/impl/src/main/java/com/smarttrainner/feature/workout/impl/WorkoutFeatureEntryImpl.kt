package com.smarttrainner.feature.workout.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.exercisemedia.ExerciseMediaRenderer
import com.smarttrainner.feature.workout.api.WorkoutRecordingFeatureEntry
import javax.inject.Inject

class WorkoutFeatureEntryImpl @Inject constructor(
    private val exerciseMediaRenderer: ExerciseMediaRenderer
) : WorkoutRecordingFeatureEntry {
    @Composable
    override fun DialogRoute(
        plannedExercise: PlannedExercise?,
        showRoutineSessionActions: Boolean,
        hasNextPlannedExercise: Boolean,
        onRecordSaved: (PlannedExercise) -> Unit,
        onSkipExercise: (PlannedExercise) -> Unit,
        onSubstituteExerciseRequested: (PlannedExercise) -> Unit,
        onAddExerciseRequested: (PlannedExercise) -> Unit,
        onExerciseMethodSelected: (ExerciseId) -> Unit,
        onDismiss: () -> Unit
    ) {
        val viewModel: WorkoutRecordingViewModel = hiltViewModel()
        LaunchedEffect(plannedExercise) {
            viewModel.updatePlannedExercise(plannedExercise)
        }
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        WorkoutRecordDialog(
            state = state.copy(
                showRoutineSessionActions = showRoutineSessionActions,
                hasNextPlannedExercise = hasNextPlannedExercise
            ),
            actions = WorkoutRecordingActions(
                onSetRepsChanged = viewModel::updateSetReps,
                onSetWeightChanged = viewModel::updateSetWeight,
                onSetDurationChanged = viewModel::updateSetDuration,
                onSetRestChanged = viewModel::updateSetRest,
                onAddSet = viewModel::addSetEntry,
                onRemoveSet = viewModel::removeSetEntry,
                onMemoChanged = viewModel::updateMemo,
                onSaveRecord = {
                    viewModel.saveRecord { saved ->
                        viewModel.clearRecording()
                        onRecordSaved(saved)
                    }
                },
                onSkipExercise = {
                    state.recordingPlannedExercise?.let { planned ->
                        viewModel.clearRecording()
                        onSkipExercise(planned)
                    }
                },
                onSubstituteExerciseRequested = {
                    state.recordingPlannedExercise?.let(onSubstituteExerciseRequested)
                },
                onAddExerciseRequested = {
                    state.recordingPlannedExercise?.let(onAddExerciseRequested)
                },
                onExerciseMethodSelected = {
                    state.recordingPlannedExercise
                        ?.exercise
                        ?.id
                        ?.let(onExerciseMethodSelected)
                },
                onDismiss = {
                    viewModel.clearRecording()
                    onDismiss()
                }
            ),
            exerciseMediaRenderer = exerciseMediaRenderer
        )
    }
}
