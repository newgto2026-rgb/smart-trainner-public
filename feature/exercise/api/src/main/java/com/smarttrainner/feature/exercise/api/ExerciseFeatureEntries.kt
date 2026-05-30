package com.smarttrainner.feature.exercise.api

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable

interface ExerciseCatalogFeatureEntry {
    fun LazyListScope.Content(
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
