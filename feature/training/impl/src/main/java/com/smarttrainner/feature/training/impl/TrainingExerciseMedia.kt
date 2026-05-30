package com.smarttrainner.feature.training.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.feature.exercise.api.ExerciseMediaFeatureEntry

@Composable
internal fun TrainingExerciseMedia(
    exerciseMediaFeatureEntry: ExerciseMediaFeatureEntry,
    exercise: Exercise,
    modifier: Modifier = Modifier,
    stepIndex: Int? = null,
    cleanThumbnailCrop: Boolean = false,
    contentDescription: String? = null
) {
    exerciseMediaFeatureEntry.Image(
        exercise = exercise,
        modifier = modifier,
        stepIndex = stepIndex,
        cleanThumbnailCrop = cleanThumbnailCrop,
        contentDescription = contentDescription
    )
}
