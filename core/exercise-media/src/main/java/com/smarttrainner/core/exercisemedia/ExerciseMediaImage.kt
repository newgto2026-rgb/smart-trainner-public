package com.smarttrainner.core.exercisemedia

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.smarttrainner.core.model.Exercise
import javax.inject.Inject

private const val TRAINER_IMAGE_ASPECT_RATIO = 0.9f
private const val TRAINER_THUMBNAIL_SCALE = 1.12f
internal val TrainerExerciseImageBackground = Color(0xFFFAF6ED)

class DefaultExerciseMediaRenderer @Inject constructor() : ExerciseMediaRenderer {
    @Composable
    override fun Image(
        exercise: Exercise,
        modifier: Modifier,
        stepIndex: Int?,
        cleanThumbnailCrop: Boolean,
        contentDescription: String?
    ) {
        TrainerExerciseImage(
            exercise = exercise,
            modifier = modifier,
            stepIndex = stepIndex,
            cleanThumbnailCrop = cleanThumbnailCrop,
            contentDescription = contentDescription
        )
    }
}

@Composable
fun TrainerExerciseImage(
    exercise: Exercise,
    modifier: Modifier = Modifier,
    stepIndex: Int? = null,
    cleanThumbnailCrop: Boolean = false,
    contentDescription: String? = null
) {
    val visuals = exerciseStepVisuals(exercise.id.value)
    val representativeIndex = if (visuals.size > 1) 1 else 0
    val visual = if (stepIndex == null) {
        visuals.getOrNull(representativeIndex)
    } else {
        visuals.getOrNull(stepIndex)
    } ?: visuals.firstOrNull()
    val thumbnailDrawableResId = if (cleanThumbnailCrop) {
        exerciseThumbnailDrawableResId(exercise.id.value)
    } else {
        null
    }
    val needsQaReplacement = exerciseArtNeedsQaReplacement(exercise.id.value)

    if (visual == null || needsQaReplacement) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .background(TrainerExerciseImageBackground)
                .aspectRatio(TRAINER_IMAGE_ASPECT_RATIO),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.exercise_image_pending),
                modifier = Modifier
                    .padding(10.dp),
                color = Color(0xFF44777F),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    val drawableResId = if (thumbnailDrawableResId != null) {
        thumbnailDrawableResId
    } else {
        visual.drawableResId
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(TrainerExerciseImageBackground)
            .aspectRatio(TRAINER_IMAGE_ASPECT_RATIO)
    ) {
        AsyncImage(
            model = drawableResId,
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (cleanThumbnailCrop && thumbnailDrawableResId == null) {
                        Modifier.graphicsLayer {
                            scaleX = TRAINER_THUMBNAIL_SCALE
                            scaleY = TRAINER_THUMBNAIL_SCALE
                        }
                    } else {
                        Modifier
                    }
                ),
            contentScale = ContentScale.Fit
        )
    }
}
