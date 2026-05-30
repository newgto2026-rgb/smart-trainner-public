package com.smarttrainner.feature.training.impl

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.WorkoutLog

internal fun androidx.compose.foundation.lazy.LazyListScope.exerciseContent(
    state: TrainingUiState,
    onExerciseSelected: (com.smarttrainner.core.model.ExerciseId) -> Unit
) {
    val selected = state.selectedExercise
    item {
        Text(
            text = stringResource(R.string.training_all_exercises, state.exercises.size),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
    if (selected == null) {
        item {
            EmptyState(text = stringResource(R.string.training_select_exercise_hint))
        }
    }
    MuscleGroup.entries
        .filterNot { it in armDetailGroups }
        .forEach { group ->
        val groupExercises = state.exercises.filter {
            if (group == MuscleGroup.ARMS) {
                it.muscleGroup == MuscleGroup.ARMS || it.muscleGroup in armDetailGroups
            } else {
                it.muscleGroup == group
            }
        }
        if (groupExercises.isNotEmpty()) {
            item {
                Text(
                    text = group.localizedLabel(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            if (group == MuscleGroup.ARMS) {
                armDetailGroups.forEach { armGroup ->
                    val armExercises = groupExercises.filter { it.muscleGroup == armGroup }
                    if (armExercises.isNotEmpty()) {
                        item {
                            Text(
                                text = armGroup.localizedLabel(),
                                color = SmartTrainnerColors.Muted,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(armExercises, key = { it.id.value }) { exercise ->
                            ExerciseRow(
                                exercise = exercise,
                                latestLog = state.latestWorkoutLogs.latestForExercise(exercise.id),
                                selected = exercise.id == state.selectedExerciseId,
                                onClick = { onExerciseSelected(exercise.id) },
                                modifier = Modifier.testTag("training_exercise_row_${exercise.id.value}")
                            )
                        }
                    }
                }
                val uncategorizedArms = groupExercises.filter { it.muscleGroup == MuscleGroup.ARMS }
                items(uncategorizedArms, key = { it.id.value }) { exercise ->
                    ExerciseRow(
                        exercise = exercise,
                        latestLog = state.latestWorkoutLogs.latestForExercise(exercise.id),
                        selected = exercise.id == state.selectedExerciseId,
                        onClick = { onExerciseSelected(exercise.id) },
                        modifier = Modifier.testTag("training_exercise_row_${exercise.id.value}")
                    )
                }
            } else {
                items(groupExercises, key = { it.id.value }) { exercise ->
                    ExerciseRow(
                        exercise = exercise,
                        latestLog = state.latestWorkoutLogs.latestForExercise(exercise.id),
                        selected = exercise.id == state.selectedExerciseId,
                        onClick = { onExerciseSelected(exercise.id) },
                        modifier = Modifier.testTag("training_exercise_row_${exercise.id.value}")
                    )
                }
            }
        }
    }
}

@Composable
internal fun FeaturedExerciseCard(
    exercise: PlannedExercise,
    displayLog: com.smarttrainner.core.model.WorkoutLog?,
    completed: Boolean,
    onRecordSelected: (PlannedExercise) -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, SmartTrainnerColors.Line),
        colors = CardDefaults.cardColors(containerColor = TrainerExerciseImageBackground)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TrainerExerciseImage(
                    exercise = exercise.exercise,
                    modifier = Modifier
                        .width(126.dp)
                        .height(140.dp),
                    cleanThumbnailCrop = true
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(exercise.exercise.localizedName(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(exercise.localizedTrainingDisplayText(displayLog), color = SmartTrainnerColors.Muted, style = MaterialTheme.typography.bodyMedium)
                        }
                        StatusChip(completed = completed)
                    }
                    Text(exercise.exercise.localizedSummary(), style = MaterialTheme.typography.bodyMedium)
                    Button(
                        onClick = { onRecordSelected(exercise) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("training_home_start_workout")
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text(stringResource(R.string.training_start_record))
                    }
                }
            }
        }
    }
}

@Composable
internal fun ExerciseDetailDialog(
    exercise: Exercise,
    plannedExercise: PlannedExercise?,
    onRecordSelected: (PlannedExercise) -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
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
                        onClick = onDismissRequest,
                        modifier = Modifier.testTag("training_close_exercise_detail")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.training_close_detail))
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
                        plannedExercise = plannedExercise,
                        onRecordSelected = onRecordSelected,
                        showHeader = false,
                        showRecordAction = false
                    )
                }
                if (plannedExercise != null) {
                    Button(
                        onClick = { onRecordSelected(plannedExercise) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 14.dp, end = 14.dp, bottom = 14.dp)
                            .testTag("training_detail_start_record")
                    ) {
                        Text(stringResource(R.string.training_start_record))
                    }
                }
            }
        }
    }
}

@Composable
internal fun ExerciseDetailCard(
    exercise: Exercise,
    plannedExercise: PlannedExercise?,
    onRecordSelected: (PlannedExercise) -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, SmartTrainnerColors.Line),
        colors = CardDefaults.cardColors(containerColor = SmartTrainnerColors.SurfaceRaised)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ExerciseDetailContent(
                exercise = exercise,
                plannedExercise = plannedExercise,
                onRecordSelected = onRecordSelected
            )
        }
    }
}

@Composable
internal fun ExerciseDetailContent(
    exercise: Exercise,
    plannedExercise: PlannedExercise?,
    onRecordSelected: (PlannedExercise) -> Unit,
    showHeader: Boolean = true,
    showRecordAction: Boolean = true
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
                .width(216.dp)
                .height(240.dp)
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
        title = stringResource(R.string.training_instruction),
        exercise = exercise,
        onImageSelected = { stepIndex ->
            imageViewerTarget = ExerciseImageViewerTarget(stepIndex = stepIndex)
        }
    )
    BulletSection(title = stringResource(R.string.training_safety), bullets = exercise.localizedSafetyCues())
    if (plannedExercise != null && showRecordAction) {
        Button(
            onClick = { onRecordSelected(plannedExercise) },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("training_detail_start_record")
        ) {
            Text(stringResource(R.string.training_start_record))
        }
    }
}

internal data class ExerciseImageViewerTarget(
    val stepIndex: Int?
)

@Composable
internal fun ExerciseImageViewerDialog(
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
                                stringResource(R.string.training_image_viewer_title)
                            } else {
                                "${stringResource(R.string.training_image_viewer_title)} ${stepIndex + 1}"
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
                            contentDescription = stringResource(R.string.training_close_image_viewer)
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
                            .fillMaxSize()
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
internal fun ExerciseMetaChips(exercise: Exercise) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = {}, label = { Text(exercise.muscleGroup.localizedLabel()) })
            AssistChip(onClick = {}, label = { Text(exercise.equipment.localizedLabel()) })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = {}, label = { Text(exercise.difficulty.localizedLabel()) })
        }
    }
}

@Composable
internal fun ExerciseRow(
    exercise: Exercise,
    latestLog: com.smarttrainner.core.model.WorkoutLog?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) SmartTrainnerColors.CoralSoft else SmartTrainnerColors.SurfaceRaised,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) SmartTrainnerColors.Coral else SmartTrainnerColors.Line
        )
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrainerExerciseImage(
                exercise = exercise,
                modifier = Modifier.size(width = 72.dp, height = 80.dp),
                cleanThumbnailCrop = true
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(exercise.localizedName(), fontWeight = FontWeight.Bold)
                Text(
                    text = exercise.localizedTrainingDisplayText(latestLog),
                    style = MaterialTheme.typography.bodySmall,
                    color = SmartTrainnerColors.Muted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = exercise.equipment.localizedLabel(),
                    style = MaterialTheme.typography.labelSmall,
                    color = SmartTrainnerColors.Muted
                )
            }
        }
    }
}

@Composable
internal fun BulletSection(
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
internal fun StepImageSection(
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
