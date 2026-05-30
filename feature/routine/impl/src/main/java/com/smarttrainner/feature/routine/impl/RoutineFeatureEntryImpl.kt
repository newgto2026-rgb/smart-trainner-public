package com.smarttrainner.feature.routine.impl

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import com.smarttrainner.feature.exercise.api.ExerciseMediaFeatureEntry
import com.smarttrainner.feature.routine.api.RoutineActions
import com.smarttrainner.feature.routine.api.RoutineFeatureEntry
import com.smarttrainner.feature.routine.api.RoutineUiState
import javax.inject.Inject

class RoutineFeatureEntryImpl @Inject constructor() : RoutineFeatureEntry {
    override fun LazyListScope.HomeSummary(
        state: RoutineUiState,
        actions: RoutineActions
    ) {
        homeSummaryContent(
            state = state,
            actions = actions
        )
    }

    override fun LazyListScope.Content(
        state: RoutineUiState,
        actions: RoutineActions,
        exerciseMediaFeatureEntry: ExerciseMediaFeatureEntry
    ) {
        planContent(
            state = state,
            actions = actions,
            exerciseMediaFeatureEntry = exerciseMediaFeatureEntry
        )
    }

    @Composable
    override fun Dialogs(
        state: RoutineUiState,
        actions: RoutineActions
    ) {
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
