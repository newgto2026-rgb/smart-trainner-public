package com.smarttrainner.core.exercisemedia

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.smarttrainner.core.model.Exercise

interface ExerciseMediaRenderer {
    @Composable
    fun Image(
        exercise: Exercise,
        modifier: Modifier,
        stepIndex: Int?,
        cleanThumbnailCrop: Boolean,
        contentDescription: String?
    )
}

@Composable
fun ExerciseMediaRenderer.Image(
    exercise: Exercise,
    modifier: Modifier = Modifier,
    stepIndex: Int? = null,
    cleanThumbnailCrop: Boolean = false,
    contentDescription: String? = null
) {
    Image(
        exercise = exercise,
        modifier = modifier,
        stepIndex = stepIndex,
        cleanThumbnailCrop = cleanThumbnailCrop,
        contentDescription = contentDescription
    )
}
