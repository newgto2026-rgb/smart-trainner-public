package com.smarttrainner.app.training

import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.feature.exercise.api.ExerciseDetailFeatureEntry
import com.smarttrainner.feature.routine.api.RoutineRouteState
import com.smarttrainner.feature.workout.api.WorkoutRecordingFeatureEntry

@Composable
internal fun TrainingRoute(
    exerciseDetailFeatureEntry: ExerciseDetailFeatureEntry,
    workoutRecordingFeatureEntry: WorkoutRecordingFeatureEntry,
    routineRouteState: RoutineRouteState,
    viewModel: TrainingViewModel = sharedTrainingViewModel(),
    routineDialogs: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TrainingScreen(
        exerciseDetailFeatureEntry = exerciseDetailFeatureEntry,
        workoutRecordingFeatureEntry = workoutRecordingFeatureEntry,
        state = state,
        routineRouteState = routineRouteState,
        onExerciseMethodSelected = viewModel::showExerciseMethod,
        onRecordSelected = viewModel::selectPlannedExercise,
        onRecordSaved = { planned ->
            val skippedIds = state.skippedPlannedExerciseIds
            val nextPlannedExercise = routineRouteState.nextPlannedExerciseAfterSaved(
                plannedExercise = planned,
                skippedPlannedExerciseIds = skippedIds
            )
            val savedResult = viewModel.handleRecordSaved(
                nextPlannedExercise = nextPlannedExercise
            )
            if (
                savedResult.wasContinuous &&
                nextPlannedExercise == null &&
                !planned.id.value.startsWith("routine-added|")
            ) {
                routineRouteState.requestCompleteRoutineDay(
                    skippedPlannedExerciseIds = skippedIds,
                    justRecordedPlannedExerciseIds = savedResult.recordedPlannedExerciseIds
                )
            }
        },
        onSkipExercise = { planned ->
            val skippedIds = state.skippedPlannedExerciseIds + planned.id
            val nextPlannedExercise = routineRouteState.nextPlannedExerciseAfterSkipped(
                plannedExercise = planned,
                skippedPlannedExerciseIds = skippedIds
            )
            viewModel.skipCurrentExercise(nextPlannedExercise)
            if (state.recordingFlow == RecordingFlow.CONTINUOUS && nextPlannedExercise == null) {
                routineRouteState.requestCompleteRoutineDay(
                    skippedPlannedExerciseIds = skippedIds,
                    justRecordedPlannedExerciseIds = state.recordedPlannedExerciseIds
                )
            }
        },
        onSubstituteExerciseRequested = routineRouteState::requestSubstituteExercise,
        onAddExerciseRequested = { planned -> routineRouteState.requestAdditionalExercise(planned) },
        onExerciseDetailDismiss = viewModel::dismissExerciseDetail,
        onRecordDialogDismiss = viewModel::dismissRecordDialog,
        routineDialogs = routineDialogs,
        content = content
    )
}

@Composable
internal fun sharedTrainingViewModel(): TrainingViewModel {
    val context = LocalContext.current
    val sharedOwner = remember(context) { context.findViewModelStoreOwner() }
    return if (sharedOwner != null) {
        hiltViewModel(sharedOwner)
    } else {
        hiltViewModel()
    }
}

private tailrec fun Context.findViewModelStoreOwner(): ViewModelStoreOwner? = when (this) {
    is ViewModelStoreOwner -> this
    is ContextWrapper -> baseContext.findViewModelStoreOwner()
    else -> null
}

@Composable
private fun TrainingScreen(
    exerciseDetailFeatureEntry: ExerciseDetailFeatureEntry,
    workoutRecordingFeatureEntry: WorkoutRecordingFeatureEntry,
    state: TrainingUiState,
    routineRouteState: RoutineRouteState,
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
            routineRouteState.nextPlannedExerciseAfterSaved(
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
            remember(routineRouteState, selectedExerciseId) {
                routineRouteState.recordablePlannedExerciseFor(selectedExerciseId)
            }
        } else {
            null
        }
        exerciseDetailFeatureEntry.DialogRoute(
            exerciseId = selectedExerciseId,
            showRecordAction = selectedPlannedExercise != null,
            onDismiss = onExerciseDetailDismiss,
            onRecordRequested = {
                selectedPlannedExercise?.let(routineRouteState::requestRecordSelected)
            }
        )
    }

    content()
}
