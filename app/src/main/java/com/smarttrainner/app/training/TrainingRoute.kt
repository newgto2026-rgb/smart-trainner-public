package com.smarttrainner.app.training

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttrainner.core.domain.RoutineSessionCoordinator
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.feature.exercise.api.ExerciseDetailFeatureEntry
import com.smarttrainner.feature.workout.api.WorkoutRecordingFeatureEntry

@Composable
internal fun TrainingRoute(
    exerciseDetailFeatureEntry: ExerciseDetailFeatureEntry,
    workoutRecordingFeatureEntry: WorkoutRecordingFeatureEntry,
    routineSessionCoordinator: RoutineSessionCoordinator,
    viewModel: TrainingViewModel,
    routineDialogs: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TrainingScreen(
        exerciseDetailFeatureEntry = exerciseDetailFeatureEntry,
        workoutRecordingFeatureEntry = workoutRecordingFeatureEntry,
        state = state,
        routineSessionCoordinator = routineSessionCoordinator,
        onExerciseMethodSelected = viewModel::showExerciseMethod,
        onRecordSelected = viewModel::selectPlannedExercise,
        onRecordSaved = { planned ->
            val skippedIds = state.skippedPlannedExerciseIds
            val nextPlannedExercise = routineSessionCoordinator.nextPlannedExerciseAfterSaved(
                plannedExercise = planned,
                skippedPlannedExerciseIds = skippedIds
            )
            val savedResult = viewModel.handleRecordSaved(
                nextPlannedExercise = nextPlannedExercise
            )
            if (
                savedResult.wasContinuous &&
                nextPlannedExercise == null &&
                !routineSessionCoordinator.isAdditionalRoutineExercise(planned)
            ) {
                routineSessionCoordinator.requestCompleteRoutineDay(
                    skippedPlannedExerciseIds = skippedIds,
                    justRecordedPlannedExerciseIds = savedResult.recordedPlannedExerciseIds
                )
            }
        },
        onSkipExercise = { planned ->
            val skippedIds = state.skippedPlannedExerciseIds + planned.id
            val nextPlannedExercise = routineSessionCoordinator.nextPlannedExerciseAfterSkipped(
                plannedExercise = planned,
                skippedPlannedExerciseIds = skippedIds
            )
            viewModel.skipCurrentExercise(nextPlannedExercise)
            if (state.recordingFlow == RecordingFlow.CONTINUOUS && nextPlannedExercise == null) {
                routineSessionCoordinator.requestCompleteRoutineDay(
                    skippedPlannedExerciseIds = skippedIds,
                    justRecordedPlannedExerciseIds = state.recordedPlannedExerciseIds
                )
            }
        },
        onSubstituteExerciseRequested = routineSessionCoordinator::requestSubstituteExercise,
        onAddExerciseRequested = { planned -> routineSessionCoordinator.requestAdditionalExercise(planned) },
        onExerciseDetailDismiss = viewModel::dismissExerciseDetail,
        onRecordDialogDismiss = viewModel::dismissRecordDialog,
        routineDialogs = routineDialogs,
        content = content
    )
}

@Composable
internal fun sharedTrainingViewModel(viewModelStoreOwner: ViewModelStoreOwner): TrainingViewModel =
    hiltViewModel(viewModelStoreOwner)

@Composable
private fun TrainingScreen(
    exerciseDetailFeatureEntry: ExerciseDetailFeatureEntry,
    workoutRecordingFeatureEntry: WorkoutRecordingFeatureEntry,
    state: TrainingUiState,
    routineSessionCoordinator: RoutineSessionCoordinator,
    onExerciseMethodSelected: (ExerciseId) -> Unit,
    onRecordSelected: (PlannedExercise) -> Unit,
    onRecordSaved: (PlannedExercise) -> Unit,
    onSkipExercise: (PlannedExercise) -> Unit,
    onSubstituteExerciseRequested: (PlannedExercise) -> Unit,
    onAddExerciseRequested: (PlannedExercise) -> Unit,
    onExerciseDetailDismiss: () -> Unit,
    onRecordDialogDismiss: () -> Unit,
    routineDialogs: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    val selectedExerciseId = state.selectedExerciseId
    val recordingPlannedExercise = state.recordingPlannedExercise
    if (recordingPlannedExercise != null && selectedExerciseId == null) {
        val hasNextPlannedExercise = state.recordingFlow == RecordingFlow.CONTINUOUS &&
            routineSessionCoordinator.nextPlannedExerciseAfterSaved(
                plannedExercise = recordingPlannedExercise,
                skippedPlannedExerciseIds = state.skippedPlannedExerciseIds
            ) != null
        workoutRecordingFeatureEntry.DialogRoute(
            plannedExercise = recordingPlannedExercise,
            showRoutineSessionActions = state.recordingFlow == RecordingFlow.CONTINUOUS,
            hasNextPlannedExercise = hasNextPlannedExercise,
            onRecordSaved = onRecordSaved,
            onSkipExercise = onSkipExercise,
            onSubstituteExerciseRequested = onSubstituteExerciseRequested,
            onAddExerciseRequested = onAddExerciseRequested,
            onExerciseMethodSelected = onExerciseMethodSelected,
            onDismiss = onRecordDialogDismiss
        )
    }
    routineDialogs()
    if (selectedExerciseId != null) {
        val selectedPlannedExercise = if (recordingPlannedExercise == null) {
            remember(routineSessionCoordinator, selectedExerciseId) {
                routineSessionCoordinator.recordablePlannedExerciseFor(selectedExerciseId)
            }
        } else {
            null
        }
        exerciseDetailFeatureEntry.DialogRoute(
            exerciseId = selectedExerciseId,
            showRecordAction = selectedPlannedExercise != null,
            onDismiss = onExerciseDetailDismiss,
            onRecordRequested = {
                selectedPlannedExercise?.let(routineSessionCoordinator::requestRecordSelected)
            }
        )
    }

    content()
}
