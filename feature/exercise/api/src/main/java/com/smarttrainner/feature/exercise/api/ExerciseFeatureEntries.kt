package com.smarttrainner.feature.exercise.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.smarttrainner.core.model.Exercise

interface ExerciseCatalogFeatureEntry {
    @Composable
    fun Content(
        state: ExerciseCatalogUiState,
        actions: ExerciseCatalogActions
    )
}

interface ExerciseDetailFeatureEntry {
    @Composable
    fun Dialog(
        state: ExerciseDetailUiState,
        actions: ExerciseDetailActions
    )
}

interface ExerciseMediaFeatureEntry {
    @Composable
    fun Image(
        exercise: Exercise,
        modifier: Modifier,
        stepIndex: Int?,
        cleanThumbnailCrop: Boolean,
        contentDescription: String?
    )
}
