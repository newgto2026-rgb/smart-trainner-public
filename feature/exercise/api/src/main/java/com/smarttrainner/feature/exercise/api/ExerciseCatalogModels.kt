package com.smarttrainner.feature.exercise.api

import com.smarttrainner.core.model.ExerciseId

data class ExerciseCatalogActions(
    val onExerciseSelected: (ExerciseId) -> Unit = {}
)
