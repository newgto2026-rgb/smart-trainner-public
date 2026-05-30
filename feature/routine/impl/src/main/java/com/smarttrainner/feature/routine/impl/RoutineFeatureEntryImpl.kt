package com.smarttrainner.feature.routine.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.ui.ExerciseMediaRenderer
import com.smarttrainner.core.ui.SmartTrainnerScreenChrome
import com.smarttrainner.core.ui.SmartTrainnerScreenScaffold
import com.smarttrainner.feature.routine.api.RoutineFeatureCallbacks
import com.smarttrainner.feature.routine.api.RoutineFeatureEntry
import com.smarttrainner.feature.routine.api.RoutineRouteState
import javax.inject.Inject

class RoutineFeatureEntryImpl @Inject constructor(
    private val exerciseMediaRenderer: ExerciseMediaRenderer
) : RoutineFeatureEntry {
    @Composable
    override fun rememberRouteState(callbacks: RoutineFeatureCallbacks): RoutineRouteState {
        val viewModel: RoutineViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        LaunchedEffect(viewModel) {
            viewModel.refreshWeekStartOnWeekBoundary()
        }
        val currentRoutineName = state.plan?.localizedName().orEmpty()
        val actions = remember(callbacks, viewModel) {
            RoutineActions(
                onTemplateSelected = viewModel::selectTemplate,
                onDaysPerWeekChanged = viewModel::updateRoutineDaysPerWeek,
                onSessionMinutesChanged = viewModel::updateRoutineSessionMinutes,
                onExperienceChanged = viewModel::updateRoutineExperience,
                onFeelingChanged = viewModel::updateRoutineFeeling,
                onShowLibrary = viewModel::showRoutineLibrary,
                onLibraryDismiss = viewModel::dismissRoutineLibrary,
                onShowSettings = viewModel::showRoutineSettings,
                onSettingsDismiss = viewModel::dismissRoutineSettings,
                onShowRecommendations = viewModel::showRoutineRecommendations,
                onRecommendationsDismiss = viewModel::dismissRoutineRecommendations,
                onPreviewSelected = viewModel::selectRoutinePreview,
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
                onWorkoutStarted = callbacks.onWorkoutStarted,
                onCompleteRoutineDay = { viewModel.completeCurrentRoutineDay(callbacks.onRoutineDayCompleted) },
                onExerciseMethodSelected = callbacks.onExerciseMethodSelected,
                onRecordSelected = callbacks.onRecordSelected
            )
        }
        return remember(state, actions, currentRoutineName) {
            DefaultRoutineRouteState(
                state = state,
                actions = actions,
                currentRoutineName = currentRoutineName
            )
        }
    }

    @Composable
    override fun HomeSummaryRoute(
        routeState: RoutineRouteState,
        title: String,
        subtitle: String
    ) {
        val routineState = routeState.asDefaultRoutineRouteState()
        SmartTrainnerScreenScaffold(
            chrome = SmartTrainnerScreenChrome(
                title = title,
                subtitle = subtitle
            )
        ) {
            homeSummaryContent(
                state = routineState.state,
                actions = routineState.actions
            )
        }
    }

    @Composable
    override fun Route(
        routeState: RoutineRouteState,
        title: String,
        subtitle: String
    ) {
        val routineState = routeState.asDefaultRoutineRouteState()
        SmartTrainnerScreenScaffold(
            chrome = SmartTrainnerScreenChrome(
                title = title,
                subtitle = subtitle
            )
        ) {
            planContent(
                state = routineState.state,
                actions = routineState.actions,
                exerciseMediaRenderer = exerciseMediaRenderer
            )
        }
    }

    @Composable
    override fun Dialogs(routeState: RoutineRouteState) {
        val routineState = routeState.asDefaultRoutineRouteState()
        val state = routineState.state
        val actions = routineState.actions
        if (state.showRoutineLibraryDialog) {
            RoutineLibraryDialog(
                state = state,
                onTemplateSelected = actions.onTemplateSelected,
                onShowRoutineSettings = actions.onShowSettings,
                onCreateCustomRoutine = actions.onCreateCustomRoutine,
                onCopyTemplateToCustom = actions.onCopyTemplateToCustom,
                onEditCustomRoutine = actions.onEditCustomRoutine,
                onDismissRequest = actions.onLibraryDismiss
            )
        }
        if (state.showRoutineSettingsDialog) {
            RoutineSettingsDialog(
                form = state.routineRecommendationInput,
                onDaysPerWeekChanged = actions.onDaysPerWeekChanged,
                onSessionMinutesChanged = actions.onSessionMinutesChanged,
                onExperienceChanged = actions.onExperienceChanged,
                onFeelingChanged = actions.onFeelingChanged,
                onShowRecommendations = actions.onShowRecommendations,
                onDismissRequest = actions.onSettingsDismiss
            )
        }
        if (state.showRoutineRecommendationsDialog) {
            RoutineRecommendationsDialog(
                state = state,
                onTemplatePreviewSelected = actions.onPreviewSelected,
                onStartRoutine = actions.onStartPreviewRoutine,
                onDismissRequest = actions.onRecommendationsDismiss
            )
        }
        if (state.customRoutineBuilder.visible) {
            CustomRoutineBuilderSheet(
                builder = state.customRoutineBuilder,
                exercises = state.exercises,
                onNameChanged = actions.onCustomRoutineNameChanged,
                onDaySelected = actions.onCustomRoutineDaySelected,
                onDayFocusChanged = actions.onCustomRoutineDayFocusChanged,
                onAddDay = actions.onCustomRoutineDayAdded,
                onRemoveDay = actions.onCustomRoutineDayRemoved,
                onExerciseGroupToggled = actions.onCustomRoutineExerciseGroupToggled,
                onExerciseDetailRequested = actions.onExerciseMethodSelected,
                onAddExercise = actions.onCustomRoutineExerciseAdded,
                onRemoveExercise = actions.onCustomRoutineExerciseRemoved,
                onMoveExerciseUp = actions.onCustomRoutineExerciseMovedUp,
                onMoveExerciseDown = actions.onCustomRoutineExerciseMovedDown,
                onSave = { actions.onCustomRoutineSaved(false) },
                onDismissRequest = actions.onCustomRoutineBuilderDismiss
            )
        }
    }
}

internal class DefaultRoutineRouteState(
    val state: RoutineUiState,
    val actions: RoutineActions,
    override val currentRoutineName: String
) : RoutineRouteState {
    override fun nextPlannedExerciseAfterSaved(plannedExercise: PlannedExercise): PlannedExercise? =
        state.nextPlannedExerciseAfterSaved(plannedExercise)

    override fun recordablePlannedExerciseFor(exerciseId: ExerciseId): PlannedExercise? =
        state.recordablePlannedExerciseFor(exerciseId)
}

internal fun RoutineRouteState.asDefaultRoutineRouteState(): DefaultRoutineRouteState =
    this as? DefaultRoutineRouteState
        ?: error("RoutineRouteState must be created by RoutineFeatureEntry.rememberRouteState.")
