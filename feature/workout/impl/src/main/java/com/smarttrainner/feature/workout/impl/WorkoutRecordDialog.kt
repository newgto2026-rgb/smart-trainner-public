package com.smarttrainner.feature.workout.impl

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.smarttrainner.core.designsystem.SmartTrainnerColors
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.WorkoutSetLog
import com.smarttrainner.core.ui.SmartTrainnerNumberField
import com.smarttrainner.core.exercisemedia.ExerciseMediaRenderer
import com.smarttrainner.core.ui.SmartTrainnerBadgeRow
import com.smarttrainner.core.ui.SmartTrainnerBadgeSpec
import java.util.Locale

@Composable
internal fun WorkoutRecordDialog(
    state: WorkoutRecordingUiState,
    actions: WorkoutRecordingActions,
    exerciseMediaRenderer: ExerciseMediaRenderer
) {
    val planned = state.recordingPlannedExercise ?: return
    Dialog(
        onDismissRequest = actions.onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 640.dp)
                .padding(horizontal = 18.dp)
                .testTag("training_record_dialog"),
            shape = RoundedCornerShape(8.dp),
            color = SmartTrainnerColors.SurfaceRaised
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 14.dp, top = 38.dp, end = 14.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(
                            R.string.workout_record_dialog_title,
                            planned.exercise.localizedWorkoutName()
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    RecordForm(
                        state = state,
                        planned = planned,
                        actions = actions,
                        exerciseMediaRenderer = exerciseMediaRenderer
                    )
                }
                IconButton(
                    onClick = actions.onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.workout_close_record))
                }
            }
        }
    }
}

@Composable
private fun RecordForm(
    state: WorkoutRecordingUiState,
    planned: PlannedExercise,
    actions: WorkoutRecordingActions,
    exerciseMediaRenderer: ExerciseMediaRenderer
) {
    val showReps = planned.repRange != null
    val showDuration = planned.durationMinutes != null || !showReps
    val showWeight = showReps
    val displayLog = state.weeklyLogs.firstOrNull { it.plannedExerciseId == planned.id }
        ?: state.latestWorkoutLogs.latestForExercise(planned.exercise.id)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.testTag("training_record_selected_exercise"),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            exerciseMediaRenderer.Image(
                exercise = planned.exercise,
                modifier = Modifier.size(width = 78.dp, height = 86.dp),
                stepIndex = null,
                cleanThumbnailCrop = true,
                contentDescription = null
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = planned.exercise.localizedWorkoutName(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = SmartTrainnerColors.Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                SmartTrainnerBadgeRow(
                    badges = planned.workoutMetricBadges(displayLog),
                    maxItemsPerRow = 3
                )
            }
        }
        OutlinedButton(
            onClick = actions.onExerciseMethodSelected,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("training_show_exercise_method")
        ) {
            Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
            Text(stringResource(R.string.workout_show_exercise_method))
        }
        state.formError?.let { error ->
            Text(
                text = error.message(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text(
            text = stringResource(R.string.workout_set_entries_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        state.recordForm.setEntries.forEachIndexed { index, setEntry ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.workout_set_number, index + 1),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { actions.onRemoveSet(index) },
                        enabled = state.recordForm.setEntries.size > 1,
                        modifier = Modifier.testTag("training_remove_set_button_$index")
                    ) {
                        Icon(
                            Icons.Default.RemoveCircleOutline,
                            contentDescription = stringResource(R.string.workout_remove_set)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (showReps) {
                        SmartTrainnerNumberField(
                            label = stringResource(R.string.workout_reps),
                            value = setEntry.reps,
                            onValueChange = { actions.onSetRepsChanged(index, it) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("training_set_reps_input_$index")
                        )
                    }
                    if (showWeight) {
                        SmartTrainnerNumberField(
                            label = stringResource(R.string.workout_weight_short),
                            value = setEntry.weightKg,
                            onValueChange = { actions.onSetWeightChanged(index, it) },
                            keyboardType = KeyboardType.Decimal,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("training_set_weight_input_$index")
                        )
                    }
                    if (showDuration) {
                        SmartTrainnerNumberField(
                            label = stringResource(R.string.workout_duration),
                            value = setEntry.durationMinutes,
                            onValueChange = { actions.onSetDurationChanged(index, it) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("training_set_duration_input_$index")
                        )
                    }
                    SmartTrainnerNumberField(
                        label = stringResource(R.string.workout_rest_seconds),
                        value = setEntry.restSeconds,
                        onValueChange = { actions.onSetRestChanged(index, it) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("training_set_rest_input_$index")
                    )
                }
            }
        }
        OutlinedButton(
            onClick = actions.onAddSet,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("training_add_set_button")
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
            Text(stringResource(R.string.workout_add_set))
        }
        Button(
            onClick = actions.onSaveRecord,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("training_save_record"),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(stringResource(R.string.workout_save_record))
        }
        if (state.recordSaved) {
            Text(
                text = stringResource(R.string.workout_saved),
                color = SmartTrainnerColors.Green,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("training_record_saved_message")
            )
        }
        OutlinedTextField(
            value = state.recordForm.memo,
            onValueChange = actions.onMemoChanged,
            label = { Text(stringResource(R.string.workout_memo)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 1,
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
private fun RecordFormError.message(): String = when (this) {
    RecordFormError.SELECT_EXERCISE -> stringResource(R.string.workout_error_select)
    RecordFormError.SETS -> stringResource(R.string.workout_error_sets)
    RecordFormError.REPS -> stringResource(R.string.workout_error_reps)
    RecordFormError.WEIGHT -> stringResource(R.string.workout_error_weight)
    RecordFormError.DURATION -> stringResource(R.string.workout_error_duration)
    RecordFormError.REST -> stringResource(R.string.workout_error_rest)
    RecordFormError.SAVE_FAILED -> stringResource(R.string.workout_error_save)
    RecordFormError.COMPLETE_DAY_FAILED -> stringResource(R.string.workout_error_complete_day)
}

@Composable
private fun Exercise.localizedWorkoutName(): String =
    if (isKoreanLocale()) name else id.value.toExerciseTitle()

@Composable
private fun isKoreanLocale(): Boolean {
    val locales = LocalConfiguration.current.locales
    val language = if (locales.isEmpty) {
        Locale.getDefault().language
    } else {
        locales[0]?.language
    }
    return language.equals("ko", ignoreCase = true)
}

@Composable
private fun PlannedExercise.workoutMetricBadges(latestLog: WorkoutLog?): List<SmartTrainnerBadgeSpec> =
    latestLog?.workoutRecordMetricBadges() ?: buildList {
        add(
            SmartTrainnerBadgeSpec(
                text = stringResource(R.string.workout_metric_recommended),
                containerColor = SmartTrainnerColors.CoralSoft,
                contentColor = SmartTrainnerColors.Ink
            )
        )
        add(
            SmartTrainnerBadgeSpec(
                text = stringResource(R.string.workout_set_number, sets),
                icon = Icons.Default.FitnessCenter,
                containerColor = SmartTrainnerColors.GreenSoft,
                contentColor = SmartTrainnerColors.Ink
            )
        )
        val reps = repRange
        if (reps != null) {
            add(
                SmartTrainnerBadgeSpec(
                    text = stringResource(R.string.workout_actual_reps, "${reps.first}-${reps.last}"),
                    containerColor = SmartTrainnerColors.CoralSoft,
                    contentColor = SmartTrainnerColors.Ink
                )
            )
        } else {
            add(
                SmartTrainnerBadgeSpec(
                    text = stringResource(R.string.workout_actual_duration, (durationMinutes ?: 10).toString()),
                    icon = Icons.Default.Timer,
                    containerColor = SmartTrainnerColors.AmberSoft,
                    contentColor = SmartTrainnerColors.Ink
                )
            )
        }
        add(
            SmartTrainnerBadgeSpec(
                text = stringResource(R.string.workout_actual_rest, restSeconds.toString()),
                icon = Icons.Default.Timer,
                containerColor = SmartTrainnerColors.SteelSoft,
                contentColor = SmartTrainnerColors.Ink
            )
        )
    }

@Composable
private fun WorkoutLog.workoutRecordMetricBadges(): List<SmartTrainnerBadgeSpec> {
    val entries = displaySetEntries()
    val reps = entries.mapNotNull { it.reps }.toCollapsedText()
    val weights = entries.mapNotNull { it.weightKg }.map { it.toRecordInput() }.toCollapsedText()
    val durations = entries.mapNotNull { it.durationMinutes }.toCollapsedText()
    val rests = entries.mapNotNull { it.restSeconds }.toCollapsedText()
    return buildList {
        add(
            SmartTrainnerBadgeSpec(
                text = stringResource(R.string.workout_metric_latest),
                containerColor = SmartTrainnerColors.SteelSoft,
                contentColor = SmartTrainnerColors.Ink
            )
        )
        add(
            SmartTrainnerBadgeSpec(
                text = stringResource(R.string.workout_set_number, entries.size.coerceAtLeast(sets)),
                icon = Icons.Default.FitnessCenter,
                containerColor = SmartTrainnerColors.GreenSoft,
                contentColor = SmartTrainnerColors.Ink
            )
        )
        reps?.let {
            add(
                SmartTrainnerBadgeSpec(
                    text = stringResource(R.string.workout_actual_reps, it),
                    containerColor = SmartTrainnerColors.CoralSoft,
                    contentColor = SmartTrainnerColors.Ink
                )
            )
        }
        weights?.let {
            add(
                SmartTrainnerBadgeSpec(
                    text = stringResource(R.string.workout_actual_weight, it),
                    containerColor = SmartTrainnerColors.SteelSoft,
                    contentColor = SmartTrainnerColors.Ink
                )
            )
        }
        durations?.let {
            add(
                SmartTrainnerBadgeSpec(
                    text = stringResource(R.string.workout_actual_duration, it),
                    icon = Icons.Default.Timer,
                    containerColor = SmartTrainnerColors.AmberSoft,
                    contentColor = SmartTrainnerColors.Ink
                )
            )
        }
        rests?.let {
            add(
                SmartTrainnerBadgeSpec(
                    text = stringResource(R.string.workout_actual_rest, it),
                    icon = Icons.Default.Timer,
                    containerColor = SmartTrainnerColors.SteelSoft,
                    contentColor = SmartTrainnerColors.Ink
                )
            )
        }
    }
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
    filter { it.exerciseId == exerciseId }.maxByOrNull { it.performedAt }

private fun String.toExerciseTitle(): String =
    split('_', '-')
        .filter { it.isNotBlank() }
        .joinToString(" ") { word ->
            when (word.lowercase(Locale.ENGLISH)) {
                "lat" -> "Lat"
                "rpe" -> "RPE"
                "y" -> "Y"
                "pushup" -> "Push-up"
                else -> word.replaceFirstChar { char ->
                    if (char.isLowerCase()) char.titlecase(Locale.ENGLISH) else char.toString()
                }
            }
        }
