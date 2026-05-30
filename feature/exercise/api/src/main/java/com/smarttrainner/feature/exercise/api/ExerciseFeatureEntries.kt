package com.smarttrainner.feature.exercise.api

import androidx.compose.runtime.Composable

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
