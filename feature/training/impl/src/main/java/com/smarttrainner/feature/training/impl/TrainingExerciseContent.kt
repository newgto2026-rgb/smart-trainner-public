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
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.feature.exercise.api.ExerciseCatalogActions
import com.smarttrainner.feature.exercise.api.ExerciseCatalogUiState
import com.smarttrainner.feature.exercise.api.ExerciseMediaFeatureEntry

internal fun androidx.compose.foundation.lazy.LazyListScope.exerciseContent(
    state: ExerciseCatalogUiState,
    actions: ExerciseCatalogActions,
    exerciseMediaFeatureEntry: ExerciseMediaFeatureEntry
) {
    val selectedExerciseId = state.selectedExerciseId
    item {
        Text(
            text = stringResource(R.string.training_all_exercises, state.exercises.size),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
    if (selectedExerciseId == null) {
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
                                selected = exercise.id == selectedExerciseId,
                                exerciseMediaFeatureEntry = exerciseMediaFeatureEntry,
                                onClick = { actions.onExerciseSelected(exercise.id) },
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
                        selected = exercise.id == selectedExerciseId,
                        exerciseMediaFeatureEntry = exerciseMediaFeatureEntry,
                        onClick = { actions.onExerciseSelected(exercise.id) },
                        modifier = Modifier.testTag("training_exercise_row_${exercise.id.value}")
                    )
                }
            } else {
                items(groupExercises, key = { it.id.value }) { exercise ->
                    ExerciseRow(
                        exercise = exercise,
                        latestLog = state.latestWorkoutLogs.latestForExercise(exercise.id),
                        selected = exercise.id == selectedExerciseId,
                        exerciseMediaFeatureEntry = exerciseMediaFeatureEntry,
                        onClick = { actions.onExerciseSelected(exercise.id) },
                        modifier = Modifier.testTag("training_exercise_row_${exercise.id.value}")
                    )
                }
            }
        }
    }
}

@Composable
internal fun ExerciseRow(
    exercise: Exercise,
    latestLog: com.smarttrainner.core.model.WorkoutLog?,
    selected: Boolean,
    exerciseMediaFeatureEntry: ExerciseMediaFeatureEntry,
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
            TrainingExerciseMedia(
                exerciseMediaFeatureEntry = exerciseMediaFeatureEntry,
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
