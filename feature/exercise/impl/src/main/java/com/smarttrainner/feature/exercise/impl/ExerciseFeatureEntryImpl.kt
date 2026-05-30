package com.smarttrainner.feature.exercise.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.feature.exercise.api.ExerciseDetailActions
import com.smarttrainner.feature.exercise.api.ExerciseDetailFeatureEntry
import com.smarttrainner.feature.exercise.api.ExerciseDetailUiState
import com.smarttrainner.feature.exercise.api.ExerciseMediaFeatureEntry
import javax.inject.Inject

class ExerciseFeatureEntryImpl @Inject constructor() : ExerciseDetailFeatureEntry, ExerciseMediaFeatureEntry {
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
