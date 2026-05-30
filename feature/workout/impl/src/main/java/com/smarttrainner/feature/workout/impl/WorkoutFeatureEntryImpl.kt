package com.smarttrainner.feature.workout.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.ui.ExerciseMediaRenderer
import com.smarttrainner.feature.workout.api.WorkoutRecordingActions
import com.smarttrainner.feature.workout.api.WorkoutRecordingFeatureEntry
import com.smarttrainner.feature.workout.api.WorkoutRecordingUiState
import javax.inject.Inject

class WorkoutFeatureEntryImpl @Inject constructor() : WorkoutRecordingFeatureEntry {
    @Composable
    override fun DialogRoute(
        plannedExercise: PlannedExercise?,
        onRecordSaved: (PlannedExercise) -> Unit,
        onExerciseMethodSelected: (ExerciseId) -> Unit,
        onDismiss: () -> Unit,
        exerciseMediaRenderer: ExerciseMediaRenderer
    ) {
        val viewModel: WorkoutRecordingViewModel = hiltViewModel()
        LaunchedEffect(plannedExercise) {
            viewModel.updatePlannedExercise(plannedExercise)
        }
        DisposableEffect(viewModel) {
            onDispose {
                viewModel.clearRecording()
            }
        }
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        Dialog(
            state = state,
            actions = WorkoutRecordingActions(
                onSetRepsChanged = viewModel::updateSetReps,
                onSetWeightChanged = viewModel::updateSetWeight,
                onSetDurationChanged = viewModel::updateSetDuration,
                onSetRestChanged = viewModel::updateSetRest,
                onAddSet = viewModel::addSetEntry,
                onRemoveSet = viewModel::removeSetEntry,
                onMemoChanged = viewModel::updateMemo,
                onSaveRecord = { viewModel.saveRecord(onRecordSaved) },
                onExerciseMethodSelected = {
                    state.recordingPlannedExercise
                        ?.exercise
                        ?.id
                        ?.let(onExerciseMethodSelected)
                },
                onDismiss = onDismiss
            ),
            exerciseMediaRenderer = exerciseMediaRenderer
        )
    }

    @Composable
    override fun Dialog(
        state: WorkoutRecordingUiState,
        actions: WorkoutRecordingActions,
        exerciseMediaRenderer: ExerciseMediaRenderer
    ) {
        WorkoutRecordDialog(
            state = state,
            actions = actions,
            exerciseMediaRenderer = exerciseMediaRenderer
        )
    }
}
