package com.smarttrainner.core.ui

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
