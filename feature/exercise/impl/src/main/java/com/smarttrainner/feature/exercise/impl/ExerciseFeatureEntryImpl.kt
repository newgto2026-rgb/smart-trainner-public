package com.smarttrainner.feature.exercise.impl

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.feature.exercise.api.ExerciseCatalogActions
import com.smarttrainner.feature.exercise.api.ExerciseCatalogFeatureEntry
import com.smarttrainner.feature.exercise.api.ExerciseCatalogUiState
import com.smarttrainner.feature.exercise.api.ExerciseDetailActions
import com.smarttrainner.feature.exercise.api.ExerciseDetailFeatureEntry
import com.smarttrainner.feature.exercise.api.ExerciseDetailUiState
import com.smarttrainner.core.ui.ExerciseMediaRenderer
import javax.inject.Inject

class ExerciseFeatureEntryImpl @Inject constructor() :
    ExerciseCatalogFeatureEntry,
    ExerciseDetailFeatureEntry,
    ExerciseMediaRenderer {
    override fun LazyListScope.Content(
        state: ExerciseCatalogUiState,
        actions: ExerciseCatalogActions
    ) {
        exerciseCatalogContent(
            state = state,
            actions = actions
        )
    }

    @Composable
    override fun DialogRoute(
        exerciseId: ExerciseId?,
        showRecordAction: Boolean,
        onDismiss: () -> Unit,
        onRecordRequested: (ExerciseId) -> Unit
    ) {
        val viewModel: ExerciseDetailViewModel = hiltViewModel()
        LaunchedEffect(exerciseId, showRecordAction) {
            viewModel.updateSelection(
                exerciseId = exerciseId,
                shouldShowRecordAction = showRecordAction
            )
        }
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        Dialog(
            state = state,
            actions = ExerciseDetailActions(
                onDismiss = onDismiss,
                onRecordRequested = onRecordRequested
            )
        )
    }

    @Composable
    override fun Dialog(
        state: ExerciseDetailUiState,
        actions: ExerciseDetailActions
    ) {
        ExerciseDetailDialog(
            state = state,
            actions = actions
        )
    }

    @Composable
    override fun Image(
        exercise: Exercise,
        modifier: Modifier,
        stepIndex: Int?,
        cleanThumbnailCrop: Boolean,
        contentDescription: String?
    ) {
        TrainerExerciseImage(
            exercise = exercise,
            modifier = modifier,
            stepIndex = stepIndex,
            cleanThumbnailCrop = cleanThumbnailCrop,
            contentDescription = contentDescription
        )
    }
}
