package com.smarttrainner.feature.training.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttrainner.core.designsystem.SmartTrainnerGradients
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.RoutineFeeling
import com.smarttrainner.core.model.RoutineFocus
import com.smarttrainner.core.model.TrainingExperience
import com.smarttrainner.feature.exercise.api.ExerciseCatalogActions
import com.smarttrainner.feature.routine.api.RoutineActions
import com.smarttrainner.feature.training.api.TrainingDestination

@Composable
fun TrainingRoute(
    destination: TrainingDestination,
    viewModel: TrainingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TrainingScreen(
        destination = destination,
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
        onSetRepsChanged = viewModel::updateSetReps,
        onSetWeightChanged = viewModel::updateSetWeight,
        onSetDurationChanged = viewModel::updateSetDuration,
        onSetRestChanged = viewModel::updateSetRest,
        onAddSet = viewModel::addSetEntry,
        onRemoveSet = viewModel::removeSetEntry,
        onMemoChanged = viewModel::updateMemo,
        onSaveRecord = viewModel::saveRecord,
        onExerciseDetailDismiss = viewModel::dismissExerciseDetail,
        onRecordDialogDismiss = viewModel::dismissRecordDialog
    )
}

@Composable
private fun TrainingScreen(
    destination: TrainingDestination,
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
    onSetRepsChanged: (Int, String) -> Unit,
    onSetWeightChanged: (Int, String) -> Unit,
    onSetDurationChanged: (Int, String) -> Unit,
    onSetRestChanged: (Int, String) -> Unit,
    onAddSet: () -> Unit,
    onRemoveSet: (Int) -> Unit,
    onMemoChanged: (String) -> Unit,
    onSaveRecord: () -> Unit,
    onExerciseDetailDismiss: () -> Unit,
    onRecordDialogDismiss: () -> Unit
) {
    val selectedExercise = state.selectedExercise
    val recordingPlannedExercise = state.recordingPlannedExercise
    val routineState = state.routine
    val exerciseCatalogState = state.exerciseCatalog
    val analysisState = state.analysis
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
            onExerciseMethodSelected = onExerciseMethodSelected,
            onRecordSelected = onRecordSelected
        )
    }
    val exerciseCatalogActions = remember(onExerciseSelected) {
        ExerciseCatalogActions(
            onExerciseSelected = onExerciseSelected
        )
    }
    if (recordingPlannedExercise != null && selectedExercise == null) {
        RecordDialog(
            state = state,
            planned = recordingPlannedExercise,
            onSetRepsChanged = onSetRepsChanged,
            onSetWeightChanged = onSetWeightChanged,
            onSetDurationChanged = onSetDurationChanged,
            onSetRestChanged = onSetRestChanged,
            onAddSet = onAddSet,
            onRemoveSet = onRemoveSet,
            onMemoChanged = onMemoChanged,
            onSaveRecord = onSaveRecord,
            onExerciseMethodSelected = { onExerciseMethodSelected(recordingPlannedExercise.exercise.id) },
            onDismissRequest = onRecordDialogDismiss
        )
    }
    if (routineState.showRoutineLibraryDialog) {
        RoutineLibraryDialog(
            state = routineState,
            onTemplateSelected = onTemplateSelected,
            onShowRoutineSettings = onShowRoutineSettings,
            onCreateCustomRoutine = onCreateCustomRoutine,
            onCopyTemplateToCustom = onCopyTemplateToCustom,
            onEditCustomRoutine = onEditCustomRoutine,
            onDismissRequest = onRoutineLibraryDismiss
        )
    }
    if (routineState.showRoutineSettingsDialog) {
        RoutineSettingsDialog(
            form = routineState.routineRecommendationInput,
            onDaysPerWeekChanged = onRoutineDaysPerWeekChanged,
            onSessionMinutesChanged = onRoutineSessionMinutesChanged,
            onExperienceChanged = onRoutineExperienceChanged,
            onFeelingChanged = onRoutineFeelingChanged,
            onShowRecommendations = onShowRoutineRecommendations,
            onDismissRequest = onRoutineSettingsDismiss
        )
    }
    if (routineState.showRoutineRecommendationsDialog) {
        RoutineRecommendationsDialog(
            state = routineState,
            onTemplatePreviewSelected = onRoutinePreviewSelected,
            onStartRoutine = onStartPreviewRoutine,
            onDismissRequest = onRoutineRecommendationsDismiss
        )
    }
    if (routineState.customRoutineBuilder.visible) {
        CustomRoutineBuilderSheet(
            builder = routineState.customRoutineBuilder,
            exercises = routineState.exercises,
            onNameChanged = onCustomRoutineNameChanged,
            onDaySelected = onCustomRoutineDaySelected,
            onDayFocusChanged = onCustomRoutineDayFocusChanged,
            onAddDay = onCustomRoutineDayAdded,
            onRemoveDay = onCustomRoutineDayRemoved,
            onExerciseGroupToggled = onCustomRoutineExerciseGroupToggled,
            onExerciseDetailRequested = onExerciseMethodSelected,
            onAddExercise = onCustomRoutineExerciseAdded,
            onRemoveExercise = onCustomRoutineExerciseRemoved,
            onMoveExerciseUp = onCustomRoutineExerciseMovedUp,
            onMoveExerciseDown = onCustomRoutineExerciseMovedDown,
            onSave = { onCustomRoutineSaved(false) },
            onDismissRequest = onCustomRoutineBuilderDismiss
        )
    }
    if (selectedExercise != null) {
        ExerciseDetailDialog(
            exercise = selectedExercise,
            plannedExercise = if (state.recordingPlannedExercise == null && !routineState.customRoutineBuilder.visible) {
                routineState.plan?.days
                    ?.flatMap { it.exercises }
                    ?.firstOrNull { it.exercise.id == selectedExercise.id }
            } else {
                null
            },
            onRecordSelected = onRecordSelected,
            onDismissRequest = onExerciseDetailDismiss,
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SmartTrainnerGradients.screen())
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, top = 14.dp, end = 18.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Header(state) }
            when (destination) {
                TrainingDestination.Home -> homeContent(state, onWorkoutStarted, onCompleteRoutineDay)
                TrainingDestination.Routine -> planContent(
                    state = routineState,
                    actions = routineActions
                )
                TrainingDestination.Exercises -> exerciseContent(
                    state = exerciseCatalogState,
                    actions = exerciseCatalogActions
                )
                TrainingDestination.Analysis -> analysisContent(analysisState)
            }
        }
    }
}
