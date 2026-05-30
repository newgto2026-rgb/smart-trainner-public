package com.smarttrainner.feature.exercise.api

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import com.smarttrainner.core.model.ExerciseId

interface ExerciseCatalogFeatureEntry {
    fun LazyListScope.Content(
        state: ExerciseCatalogUiState,
        actions: ExerciseCatalogActions
    )
}

interface ExerciseDetailFeatureEntry {
    @Composable
    fun DialogRoute(
        exerciseId: ExerciseId?,
        showRecordAction: Boolean,
        onDismiss: () -> Unit,
        onRecordRequested: (ExerciseId) -> Unit
    )

    @Composable
    fun Dialog(
        state: ExerciseDetailUiState,
        actions: ExerciseDetailActions
    )
}
