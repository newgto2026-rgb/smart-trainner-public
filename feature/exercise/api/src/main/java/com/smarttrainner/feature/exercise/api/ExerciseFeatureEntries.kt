package com.smarttrainner.feature.exercise.api

import androidx.compose.runtime.Composable
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.ui.SmartTrainnerScreenChrome

interface ExerciseCatalogFeatureEntry {
    @Composable
    fun Route(
        chrome: SmartTrainnerScreenChrome,
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
