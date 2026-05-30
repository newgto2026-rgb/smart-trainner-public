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
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.RoutineFeeling
import com.smarttrainner.core.model.RoutineFocus
import com.smarttrainner.core.model.TrainingExperience
import com.smarttrainner.feature.exercise.api.ExerciseCatalogActions
import com.smarttrainner.feature.exercise.api.ExerciseDetailFeatureEntry
import com.smarttrainner.core.ui.ExerciseMediaRenderer
import com.smarttrainner.feature.routine.api.RoutineActions
import com.smarttrainner.feature.routine.api.RoutineFeatureEntry
import com.smarttrainner.feature.workout.api.WorkoutRecordingFeatureEntry

internal typealias TrainingRouteContent = LazyListScope.(
    TrainingUiState,
    RoutineActions,
    ExerciseCatalogActions
) -> Unit

@Composable
internal fun TrainingRoute(
    exerciseDetailFeatureEntry: ExerciseDetailFeatureEntry,
    exerciseMediaRenderer: ExerciseMediaRenderer,
    routineFeatureEntry: RoutineFeatureEntry,
    workoutRecordingFeatureEntry: WorkoutRecordingFeatureEntry,
    viewModel: TrainingViewModel = sharedTrainingViewModel(),
    content: TrainingRouteContent
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TrainingScreen(
        exerciseDetailFeatureEntry = exerciseDetailFeatureEntry,
        exerciseMediaRenderer = exerciseMediaRenderer,
        routineFeatureEntry = routineFeatureEntry,
        workoutRecordingFeatureEntry = workoutRecordingFeatureEntry,
        state = state,
        onTemplateSelected = viewModel::selectTemplate,
        onRoutineDaysPerWeekChanged = viewModel::updateRoutineDaysPerWeek,
        onRoutineSessionMinutesChanged = viewModel::updateRoutineSessionMinutes,
        onRoutineExperienceChanged = viewModel::updateRoutineExperience,
        onRoutineFeelingChanged = viewModel::updateRoutineFeeling,
        onShowRoutineLibrary = viewModel::showRoutineLibrary,
        onRoutineLibraryDismiss = viewModel::dismissRoutineLibrary,
        onShowRoutineSettings = viewModel::showRoutineSettings,
        onRoutineSettingsDismiss = viewModel::dismissRoutineSettings,
        onShowRoutineRecommendations = viewModel::showRoutineRecommendations,
        onRoutineRecommendationsDismiss = viewModel::dismissRoutineRecommendations,
        onRoutinePreviewSelected = viewModel::selectRoutinePreview,
        onStartPreviewRoutine = viewModel::startPreviewRoutine,
        onCreateCustomRoutine = viewModel::showCreateCustomRoutine,
        onCopyTemplateToCustom = viewModel::copyTemplateToCustom,
        onEditCustomRoutine = viewModel::editCustomRoutine,
        onCustomRoutineNameChanged = viewModel::updateCustomRoutineName,
        onCustomRoutineDaySelected = viewModel::selectCustomRoutineDay,
        onCustomRoutineDayFocusChanged = viewModel::updateCustomRoutineDayFocus,
        onCustomRoutineDayAdded = viewModel::addCustomRoutineDay,
        onCustomRoutineDayRemoved = viewModel::removeCustomRoutineDay,
        onCustomRoutineExerciseGroupToggled = viewModel::toggleCustomRoutineExerciseGroup,
        onCustomRoutineExerciseAdded = viewModel::addExerciseToCustomRoutine,
        onCustomRoutineExerciseRemoved = viewModel::removeExerciseFromCustomRoutine,
        onCustomRoutineExerciseMovedUp = viewModel::moveCustomRoutineExerciseUp,
        onCustomRoutineExerciseMovedDown = viewModel::moveCustomRoutineExerciseDown,
        onCustomRoutineSaved = viewModel::saveCustomRoutine,
        onCustomRoutineBuilderDismiss = viewModel::dismissCustomRoutineBuilder,
        onExerciseSelected = viewModel::selectExercise,
        onExerciseMethodSelected = viewModel::showExerciseMethod,
        onWorkoutStarted = viewModel::startWorkout,
        onRecordSelected = viewModel::selectPlannedExercise,
        onCompleteRoutineDay = viewModel::completeCurrentRoutineDay,
        onRecordSaved = viewModel::handleRecordSaved,
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
    routineFeatureEntry: RoutineFeatureEntry,
    workoutRecordingFeatureEntry: WorkoutRecordingFeatureEntry,
    state: TrainingUiState,
    onTemplateSelected: (String) -> Unit,
    onRoutineDaysPerWeekChanged: (Int) -> Unit,
    onRoutineSessionMinutesChanged: (Int) -> Unit,
    onRoutineExperienceChanged: (TrainingExperience) -> Unit,
    onRoutineFeelingChanged: (RoutineFeeling) -> Unit,
    onShowRoutineLibrary: () -> Unit,
    onRoutineLibraryDismiss: () -> Unit,
    onShowRoutineSettings: () -> Unit,
    onRoutineSettingsDismiss: () -> Unit,
    onShowRoutineRecommendations: () -> Unit,
    onRoutineRecommendationsDismiss: () -> Unit,
    onRoutinePreviewSelected: (String) -> Unit,
    onStartPreviewRoutine: () -> Unit,
    onCreateCustomRoutine: () -> Unit,
    onCopyTemplateToCustom: (String) -> Unit,
    onEditCustomRoutine: (String) -> Unit,
    onCustomRoutineNameChanged: (String) -> Unit,
    onCustomRoutineDaySelected: (Int) -> Unit,
    onCustomRoutineDayFocusChanged: (RoutineFocus?) -> Unit,
    onCustomRoutineDayAdded: () -> Unit,
    onCustomRoutineDayRemoved: (Int) -> Unit,
    onCustomRoutineExerciseGroupToggled: (MuscleGroup) -> Unit,
    onCustomRoutineExerciseAdded: (ExerciseId) -> Unit,
    onCustomRoutineExerciseRemoved: (Int) -> Unit,
    onCustomRoutineExerciseMovedUp: (Int) -> Unit,
    onCustomRoutineExerciseMovedDown: (Int) -> Unit,
    onCustomRoutineSaved: (Boolean) -> Unit,
    onCustomRoutineBuilderDismiss: () -> Unit,
    onExerciseSelected: (ExerciseId) -> Unit,
    onExerciseMethodSelected: (ExerciseId) -> Unit,
    onWorkoutStarted: (PlannedExercise) -> Unit,
    onRecordSelected: (PlannedExercise) -> Unit,
    onCompleteRoutineDay: () -> Unit,
    onRecordSaved: (PlannedExercise) -> Unit,
    onExerciseDetailDismiss: () -> Unit,
    onRecordDialogDismiss: () -> Unit,
    content: TrainingRouteContent
) {
    val selectedExerciseId = state.selectedExerciseId
    val recordingPlannedExercise = state.recordingPlannedExercise
    val routineState = state.routine
    val routineActions = remember(
        onTemplateSelected,
        onRoutineDaysPerWeekChanged,
        onRoutineSessionMinutesChanged,
        onRoutineExperienceChanged,
        onRoutineFeelingChanged,
        onShowRoutineLibrary,
        onRoutineLibraryDismiss,
        onShowRoutineSettings,
        onRoutineSettingsDismiss,
        onShowRoutineRecommendations,
        onRoutineRecommendationsDismiss,
        onRoutinePreviewSelected,
        onStartPreviewRoutine,
        onCreateCustomRoutine,
        onCopyTemplateToCustom,
        onEditCustomRoutine,
        onCustomRoutineNameChanged,
        onCustomRoutineDaySelected,
        onCustomRoutineDayFocusChanged,
        onCustomRoutineDayAdded,
        onCustomRoutineDayRemoved,
        onCustomRoutineExerciseGroupToggled,
        onCustomRoutineExerciseAdded,
        onCustomRoutineExerciseRemoved,
        onCustomRoutineExerciseMovedUp,
        onCustomRoutineExerciseMovedDown,
        onCustomRoutineSaved,
        onCustomRoutineBuilderDismiss,
        onWorkoutStarted,
        onCompleteRoutineDay,
        onExerciseMethodSelected,
        onRecordSelected
    ) {
        RoutineActions(
            onTemplateSelected = onTemplateSelected,
            onDaysPerWeekChanged = onRoutineDaysPerWeekChanged,
            onSessionMinutesChanged = onRoutineSessionMinutesChanged,
            onExperienceChanged = onRoutineExperienceChanged,
            onFeelingChanged = onRoutineFeelingChanged,
            onShowLibrary = onShowRoutineLibrary,
            onLibraryDismiss = onRoutineLibraryDismiss,
            onShowSettings = onShowRoutineSettings,
            onSettingsDismiss = onRoutineSettingsDismiss,
            onShowRecommendations = onShowRoutineRecommendations,
            onRecommendationsDismiss = onRoutineRecommendationsDismiss,
            onPreviewSelected = onRoutinePreviewSelected,
            onStartPreviewRoutine = onStartPreviewRoutine,
            onCreateCustomRoutine = onCreateCustomRoutine,
            onCopyTemplateToCustom = onCopyTemplateToCustom,
            onEditCustomRoutine = onEditCustomRoutine,
            onCustomRoutineNameChanged = onCustomRoutineNameChanged,
            onCustomRoutineDaySelected = onCustomRoutineDaySelected,
            onCustomRoutineDayFocusChanged = onCustomRoutineDayFocusChanged,
            onCustomRoutineDayAdded = onCustomRoutineDayAdded,
            onCustomRoutineDayRemoved = onCustomRoutineDayRemoved,
            onCustomRoutineExerciseGroupToggled = onCustomRoutineExerciseGroupToggled,
            onCustomRoutineExerciseAdded = onCustomRoutineExerciseAdded,
            onCustomRoutineExerciseRemoved = onCustomRoutineExerciseRemoved,
            onCustomRoutineExerciseMovedUp = onCustomRoutineExerciseMovedUp,
            onCustomRoutineExerciseMovedDown = onCustomRoutineExerciseMovedDown,
            onCustomRoutineSaved = onCustomRoutineSaved,
            onCustomRoutineBuilderDismiss = onCustomRoutineBuilderDismiss,
            onWorkoutStarted = onWorkoutStarted,
            onCompleteRoutineDay = onCompleteRoutineDay,
            onExerciseMethodSelected = onExerciseMethodSelected,
            onRecordSelected = onRecordSelected
        )
    }
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
    routineFeatureEntry.Dialogs(
        state = routineState,
        actions = routineActions
    )
    if (selectedExerciseId != null) {
        val selectedPlannedExercise = if (state.recordingPlannedExercise == null && !routineState.customRoutineBuilder.visible) {
            routineState.plan?.days
                ?.flatMap { it.exercises }
                ?.firstOrNull { it.exercise.id == selectedExerciseId }
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
            item { Header(state) }
            content(state, routineActions, exerciseCatalogActions)
        }
    }
}
