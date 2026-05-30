package com.smarttrainner.feature.exercise.impl

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.smarttrainner.core.designsystem.SmartTrainnerColors
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.WorkoutSetLog
import com.smarttrainner.core.ui.SmartTrainnerEmptyState
import com.smarttrainner.feature.exercise.api.ExerciseCatalogActions
import com.smarttrainner.feature.exercise.api.ExerciseCatalogUiState

private val armDetailGroups = listOf(
    MuscleGroup.BICEPS,
    MuscleGroup.TRICEPS,
    MuscleGroup.FOREARMS
)

internal fun LazyListScope.exerciseCatalogContent(
    state: ExerciseCatalogUiState,
    actions: ExerciseCatalogActions
) {
    val selectedExerciseId = state.selectedExerciseId
    item {
        Text(
            text = stringResource(R.string.exercise_all_exercises, state.exercises.size),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
    if (selectedExerciseId == null) {
        item {
            SmartTrainnerEmptyState(text = stringResource(R.string.exercise_select_exercise_hint))
        }
    }
    MuscleGroup.entries
        .filterNot { it in armDetailGroups }
        .forEach { group ->
            exerciseGroupSection(
                group = group,
                state = state,
                selectedExerciseId = selectedExerciseId,
                actions = actions
            )
        }
}

private fun LazyListScope.exerciseGroupSection(
    group: MuscleGroup,
    state: ExerciseCatalogUiState,
    selectedExerciseId: ExerciseId?,
    actions: ExerciseCatalogActions
) {
    val groupExercises = state.exercises.filter {
        if (group == MuscleGroup.ARMS) {
            it.muscleGroup == MuscleGroup.ARMS || it.muscleGroup in armDetailGroups
        } else {
            it.muscleGroup == group
        }
    }
    if (groupExercises.isEmpty()) return

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
                exerciseRows(
                    exercises = armExercises,
                    latestWorkoutLogs = state.latestWorkoutLogs,
                    selectedExerciseId = selectedExerciseId,
                    actions = actions
                )
            }
        }
        val uncategorizedArms = groupExercises.filter { it.muscleGroup == MuscleGroup.ARMS }
        exerciseRows(
            exercises = uncategorizedArms,
            latestWorkoutLogs = state.latestWorkoutLogs,
            selectedExerciseId = selectedExerciseId,
            actions = actions
        )
    } else {
        exerciseRows(
            exercises = groupExercises,
            latestWorkoutLogs = state.latestWorkoutLogs,
            selectedExerciseId = selectedExerciseId,
            actions = actions
        )
    }
}

private fun LazyListScope.exerciseRows(
    exercises: List<Exercise>,
    latestWorkoutLogs: List<WorkoutLog>,
    selectedExerciseId: ExerciseId?,
    actions: ExerciseCatalogActions
) {
    items(exercises, key = { it.id.value }) { exercise ->
        ExerciseRow(
            exercise = exercise,
            latestLog = latestWorkoutLogs.latestForExercise(exercise.id),
            selected = exercise.id == selectedExerciseId,
            onClick = { actions.onExerciseSelected(exercise.id) },
            modifier = Modifier.testTag("training_exercise_row_${exercise.id.value}")
        )
    }
}

@Composable
private fun ExerciseRow(
    exercise: Exercise,
    latestLog: WorkoutLog?,
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
                    text = exercise.localizedCatalogDisplayText(latestLog),
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
private fun Exercise.localizedCatalogTargetText(): String {
    val reps = defaultRepRange
    return if (reps != null) {
        stringResource(
            R.string.exercise_target_reps,
            defaultSets,
            reps.first,
            reps.last
        )
    } else {
        stringResource(R.string.exercise_target_duration, defaultSets, defaultDurationMinutes ?: 10)
    }
}

@Composable
private fun Exercise.localizedCatalogDisplayText(latestLog: WorkoutLog?): String =
    latestLog?.localizedRecordDisplayText()?.let { recordText ->
        stringResource(R.string.exercise_latest_record, recordText)
    } ?: stringResource(R.string.exercise_recommended_record, localizedCatalogTargetText())

@Composable
private fun WorkoutLog.localizedRecordDisplayText(): String {
    val entries = displaySetEntries()
    val reps = entries.mapNotNull { it.reps }.toCollapsedText()
    val weights = entries.mapNotNull { it.weightKg }.map { it.toRecordInput() }.toCollapsedText()
    val durations = entries.mapNotNull { it.durationMinutes }.toCollapsedText()
    val rests = entries.mapNotNull { it.restSeconds }.toCollapsedText()
    val parts = buildList {
        add(stringResource(R.string.exercise_set_number, entries.size.coerceAtLeast(sets)))
        reps?.let { add(stringResource(R.string.exercise_actual_reps, it)) }
        weights?.let { add(stringResource(R.string.exercise_actual_weight, it)) }
        durations?.let { add(stringResource(R.string.exercise_actual_duration, it)) }
        rests?.let { add(stringResource(R.string.exercise_actual_rest, it)) }
    }
    return parts.joinToString(" · ")
}

private fun WorkoutLog.displaySetEntries(): List<WorkoutSetLog> =
    setEntries.takeIf { it.isNotEmpty() }
        ?: List(sets.coerceIn(1, 12)) { index ->
            WorkoutSetLog(
                order = index + 1,
                reps = reps,
                weightKg = weightKg,
                durationMinutes = durationMinutes
            )
        }

private fun <T> List<T>.toCollapsedText(): String? =
    takeIf { it.isNotEmpty() }?.let { values ->
        val distinctValues = values.distinct()
        if (distinctValues.size == 1) distinctValues.single().toString() else values.joinToString("/")
    }

private fun Double.toRecordInput(): String =
    if (rem(1.0) == 0.0) toLong().toString() else toString()

private fun List<WorkoutLog>.latestForExercise(exerciseId: ExerciseId): WorkoutLog? =
    firstOrNull { it.exerciseId == exerciseId }
        ?: filter { it.exerciseId == exerciseId }.maxByOrNull { it.performedAt }
