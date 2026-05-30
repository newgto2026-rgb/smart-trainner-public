package com.smarttrainner.feature.exercise.impl

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.smarttrainner.core.designsystem.SmartTrainnerColors
import com.smarttrainner.core.model.Exercise

@Composable
internal fun ExerciseDetailDialog(
    state: ExerciseDetailUiState,
    actions: ExerciseDetailActions
) {
    val exercise = state.exercise ?: return
    Dialog(
        onDismissRequest = actions.onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(horizontal = 18.dp)
                .testTag("training_exercise_detail_dialog"),
            shape = RoundedCornerShape(8.dp),
            color = SmartTrainnerColors.SurfaceRaised
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, top = 8.dp, end = 4.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = exercise.localizedName(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        ExerciseMetaChips(exercise)
                    }
                    IconButton(
                        onClick = actions.onDismiss,
                        modifier = Modifier.testTag("training_close_exercise_detail")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.exercise_close_detail))
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ExerciseDetailContent(
                        exercise = exercise,
                        showHeader = false
                    )
                }
                if (state.showRecordAction) {
                    Button(
                        onClick = { actions.onRecordRequested(exercise.id) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 14.dp, end = 14.dp, bottom = 14.dp)
                            .testTag("training_detail_start_record")
                    ) {
                        Text(stringResource(R.string.exercise_start_record))
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseDetailContent(
    exercise: Exercise,
    showHeader: Boolean = true
) {
    var imageViewerTarget by remember(exercise.id) {
        mutableStateOf<ExerciseImageViewerTarget?>(null)
    }
    imageViewerTarget?.let { target ->
        ExerciseImageViewerDialog(
            exercise = exercise,
            stepIndex = target.stepIndex,
            onDismissRequest = { imageViewerTarget = null }
        )
    }
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        TrainerExerciseImage(
            exercise = exercise,
            modifier = Modifier
                .size(width = 216.dp, height = 240.dp)
                .clickable { imageViewerTarget = ExerciseImageViewerTarget(stepIndex = null) }
                .testTag("training_detail_main_image"),
            cleanThumbnailCrop = true,
            contentDescription = exercise.localizedName()
        )
    }
    if (showHeader) {
        Text(exercise.localizedName(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        ExerciseMetaChips(exercise)
    }
    Text(exercise.localizedSummary(), style = MaterialTheme.typography.bodyMedium)
    StepImageSection(
        title = stringResource(R.string.exercise_instruction),
        exercise = exercise,
        onImageSelected = { stepIndex ->
            imageViewerTarget = ExerciseImageViewerTarget(stepIndex = stepIndex)
        }
    )
    BulletSection(title = stringResource(R.string.exercise_safety), bullets = exercise.localizedSafetyCues())
}

private data class ExerciseImageViewerTarget(
    val stepIndex: Int?
)

@Composable
private fun ExerciseImageViewerDialog(
    exercise: Exercise,
    stepIndex: Int?,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.86f)
                .padding(horizontal = 12.dp)
                .testTag("training_exercise_image_viewer"),
            shape = RoundedCornerShape(8.dp),
            color = SmartTrainnerColors.SurfaceRaised
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, top = 8.dp, end = 4.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = exercise.localizedName(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (stepIndex == null) {
                                stringResource(R.string.exercise_image_viewer_title)
                            } else {
                                "${stringResource(R.string.exercise_image_viewer_title)} ${stepIndex + 1}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = SmartTrainnerColors.Muted
                        )
                    }
                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.testTag("training_close_exercise_image_viewer")
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.exercise_close_image_viewer)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TrainerExerciseImage(
                        exercise = exercise,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("training_exercise_image_viewer_image"),
                        stepIndex = stepIndex,
                        contentDescription = if (stepIndex == null) {
                            exercise.localizedName()
                        } else {
                            "${exercise.localizedName()} ${stepIndex + 1}"
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseMetaChips(exercise: Exercise) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExerciseMetaChip(label = exercise.muscleGroup.localizedLabel())
            ExerciseMetaChip(label = exercise.equipment.localizedLabel())
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExerciseMetaChip(label = exercise.difficulty.localizedLabel())
        }
    }
}

@Composable
private fun ExerciseMetaChip(label: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = SmartTrainnerColors.SteelSoft,
        border = BorderStroke(1.dp, SmartTrainnerColors.Line)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = SmartTrainnerColors.Ink,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun BulletSection(
    title: String,
    bullets: List<String>
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        bullets.forEach { bullet ->
            Text("• $bullet", style = MaterialTheme.typography.bodyMedium, color = SmartTrainnerColors.Muted)
        }
    }
}

internal data class LocalizedExerciseStep(
    val label: String,
    val instruction: String
)

@Composable
private fun StepImageSection(
    title: String,
    exercise: Exercise,
    onImageSelected: (Int) -> Unit = {}
) {
    val stepItems = exercise.localizedStepItems()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        stepItems.forEachIndexed { index, step ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SmartTrainnerColors.Surface,
                border = BorderStroke(1.dp, SmartTrainnerColors.Line)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TrainerExerciseImage(
                        exercise = exercise,
                        modifier = Modifier
                            .size(width = 76.dp, height = 84.dp)
                            .clickable { onImageSelected(index) }
                            .testTag("training_step_image_$index"),
                        stepIndex = index,
                        contentDescription = "${exercise.localizedName()} ${index + 1}. ${step.label}"
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${index + 1}. ${step.label}",
                            style = MaterialTheme.typography.labelLarge,
                            color = SmartTrainnerColors.Coral,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = step.instruction,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SmartTrainnerColors.Ink
                        )
                    }
                }
            }
        }
    }
}
