package com.smarttrainner.app.training

import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttrainner.core.designsystem.SmartTrainnerGradients
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.feature.exercise.api.ExerciseCatalogActions
import com.smarttrainner.feature.exercise.api.ExerciseDetailFeatureEntry
import com.smarttrainner.core.ui.ExerciseMediaRenderer
import com.smarttrainner.feature.routine.api.RoutineRouteState
import com.smarttrainner.feature.workout.api.WorkoutRecordingFeatureEntry

internal typealias TrainingRouteContent = LazyListScope.(
    TrainingUiState,
    ExerciseCatalogActions
) -> Unit

@Composable
internal fun TrainingRoute(
    exerciseDetailFeatureEntry: ExerciseDetailFeatureEntry,
    exerciseMediaRenderer: ExerciseMediaRenderer,
    workoutRecordingFeatureEntry: WorkoutRecordingFeatureEntry,
    routineRouteState: RoutineRouteState,
    viewModel: TrainingViewModel = sharedTrainingViewModel(),
    content: TrainingRouteContent
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TrainingScreen(
        exerciseDetailFeatureEntry = exerciseDetailFeatureEntry,
        exerciseMediaRenderer = exerciseMediaRenderer,
        workoutRecordingFeatureEntry = workoutRecordingFeatureEntry,
        state = state,
        routineRouteState = routineRouteState,
        onExerciseSelected = viewModel::selectExercise,
        onExerciseMethodSelected = viewModel::showExerciseMethod,
        onRecordSelected = viewModel::selectPlannedExercise,
        onRecordSaved = { planned ->
            viewModel.handleRecordSaved(
                nextPlannedExercise = routineRouteState.nextPlannedExerciseAfterSaved(planned)
            )
        },
        onExerciseDetailDismiss = viewModel::dismissExerciseDetail,
        onRecordDialogDismiss = viewModel::dismissRecordDialog,
        content = content
    )
}

@Composable
private fun sharedTrainingViewModel(): TrainingViewModel {
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
    exerciseMediaRenderer: ExerciseMediaRenderer,
    workoutRecordingFeatureEntry: WorkoutRecordingFeatureEntry,
    state: TrainingUiState,
    routineRouteState: RoutineRouteState,
    onExerciseSelected: (ExerciseId) -> Unit,
    onExerciseMethodSelected: (ExerciseId) -> Unit,
    onRecordSelected: (PlannedExercise) -> Unit,
    onRecordSaved: (PlannedExercise) -> Unit,
    onExerciseDetailDismiss: () -> Unit,
    onRecordDialogDismiss: () -> Unit,
    content: TrainingRouteContent
) {
    val selectedExerciseId = state.selectedExerciseId
    val recordingPlannedExercise = state.recordingPlannedExercise
    val exerciseCatalogActions = remember(onExerciseSelected) {
        ExerciseCatalogActions(
            onExerciseSelected = onExerciseSelected
        )
    }
    if (recordingPlannedExercise != null && selectedExerciseId == null) {
        workoutRecordingFeatureEntry.DialogRoute(
            plannedExercise = recordingPlannedExercise,
            onRecordSaved = onRecordSaved,
            onExerciseMethodSelected = onExerciseMethodSelected,
            onDismiss = onRecordDialogDismiss,
            exerciseMediaRenderer = exerciseMediaRenderer
        )
    }
    routineRouteState.Dialogs()
    if (selectedExerciseId != null) {
        val selectedPlannedExercise = if (state.recordingPlannedExercise == null) {
            routineRouteState.recordablePlannedExerciseFor(selectedExerciseId)
        } else {
            null
        }
        exerciseDetailFeatureEntry.DialogRoute(
            exerciseId = selectedExerciseId,
            showRecordAction = selectedPlannedExercise != null,
            onDismiss = onExerciseDetailDismiss,
            onRecordRequested = {
                selectedPlannedExercise?.let(onRecordSelected)
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SmartTrainnerGradients.screen())
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentPadding = PaddingValues(start = 18.dp, top = 14.dp, end = 18.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Header(currentRoutineName = routineRouteState.currentRoutineName) }
            content(state, exerciseCatalogActions)
        }
    }
}
