package com.smarttrainner.feature.exercise.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.ui.SmartTrainnerScreenChrome
import com.smarttrainner.core.ui.SmartTrainnerScreenScaffold
import com.smarttrainner.feature.exercise.api.ExerciseCatalogFeatureEntry
import com.smarttrainner.feature.exercise.api.ExerciseDetailFeatureEntry
import javax.inject.Inject

class ExerciseFeatureEntryImpl @Inject constructor() :
    ExerciseCatalogFeatureEntry,
    ExerciseDetailFeatureEntry {
    @Composable
    override fun Route(
        title: String,
        subtitle: String,
        selectedExerciseId: ExerciseId?,
        onExerciseSelected: (ExerciseId) -> Unit
    ) {
        val viewModel: ExerciseCatalogViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        val routeState = state.copy(selectedExerciseId = selectedExerciseId)
        SmartTrainnerScreenScaffold(
            chrome = SmartTrainnerScreenChrome(
                title = title,
                subtitle = subtitle
            )
        ) {
            exerciseCatalogContent(
                state = routeState,
                onSearchQueryChanged = viewModel::updateSearchQuery,
                onExerciseSelected = onExerciseSelected
            )
        }
    }

    @Composable
    override fun DialogRoute(
        exerciseId: ExerciseId?,
        showRecordAction: Boolean,
        onDismiss: () -> Unit,
        onRecordRequested: (ExerciseId) -> Unit
    ) {
        val viewModel: ExerciseDetailViewModel = hiltViewModel()
        DisposableEffect(exerciseId, showRecordAction) {
            viewModel.updateSelection(
                exerciseId = exerciseId,
                shouldShowRecordAction = showRecordAction
            )
            onDispose {
                viewModel.updateSelection(exerciseId = null, shouldShowRecordAction = false)
            }
        }
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        ExerciseDetailDialog(
            state = state,
            actions = ExerciseDetailActions(
                onDismiss = onDismiss,
                onRecordRequested = onRecordRequested
            )
        )
    }

}
