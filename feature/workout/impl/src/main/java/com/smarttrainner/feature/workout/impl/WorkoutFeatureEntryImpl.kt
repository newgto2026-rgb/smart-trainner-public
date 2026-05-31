package com.smarttrainner.feature.workout.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
        onRecordSaved: (PlannedExercise) -> Unit,
        onExerciseMethodSelected: (ExerciseId) -> Unit,
        onDismiss: () -> Unit
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
        WorkoutRecordDialog(
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
}
