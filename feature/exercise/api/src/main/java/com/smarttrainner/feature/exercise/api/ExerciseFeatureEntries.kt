package com.smarttrainner.feature.exercise.api

import androidx.compose.runtime.Composable
import com.smarttrainner.core.model.ExerciseId

interface ExerciseCatalogFeatureEntry {
    @Composable
    fun Route(
        title: String,
        subtitle: String,
        selectedExerciseId: ExerciseId?,
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
}
