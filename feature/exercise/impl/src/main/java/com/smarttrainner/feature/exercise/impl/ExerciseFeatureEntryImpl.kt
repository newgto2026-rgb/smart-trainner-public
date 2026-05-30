package com.smarttrainner.feature.exercise.impl

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.feature.exercise.api.ExerciseCatalogActions
import com.smarttrainner.feature.exercise.api.ExerciseCatalogFeatureEntry
import com.smarttrainner.feature.exercise.api.ExerciseCatalogUiState
import com.smarttrainner.feature.exercise.api.ExerciseDetailActions
import com.smarttrainner.feature.exercise.api.ExerciseDetailFeatureEntry
import com.smarttrainner.feature.exercise.api.ExerciseDetailUiState
import com.smarttrainner.feature.exercise.api.ExerciseMediaFeatureEntry
import javax.inject.Inject

class ExerciseFeatureEntryImpl @Inject constructor() :
    ExerciseCatalogFeatureEntry,
    ExerciseDetailFeatureEntry,
    ExerciseMediaFeatureEntry {
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
