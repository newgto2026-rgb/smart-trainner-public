package com.smarttrainner.feature.training.impl

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.RemoveCircleOutline
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.smarttrainner.core.designsystem.SmartTrainnerColors
import com.smarttrainner.core.ui.SmartTrainnerNumberField
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.WorkoutLog

@Composable
internal fun RecordDialog(
    state: TrainingUiState,
    planned: PlannedExercise,
    onSetRepsChanged: (Int, String) -> Unit,
    onSetWeightChanged: (Int, String) -> Unit,
    onSetDurationChanged: (Int, String) -> Unit,
    onSetRestChanged: (Int, String) -> Unit,
    onAddSet: () -> Unit,
    onRemoveSet: (Int) -> Unit,
    onMemoChanged: (String) -> Unit,
    onSaveRecord: () -> Unit,
    onExerciseMethodSelected: () -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
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
                        text = stringResource(R.string.training_record_dialog_title, planned.exercise.localizedName()),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    RecordForm(
                        state = state,
                        planned = planned,
                        onSetRepsChanged = onSetRepsChanged,
                        onSetWeightChanged = onSetWeightChanged,
                        onSetDurationChanged = onSetDurationChanged,
                        onSetRestChanged = onSetRestChanged,
                        onAddSet = onAddSet,
                        onRemoveSet = onRemoveSet,
                        onMemoChanged = onMemoChanged,
                        onSaveRecord = onSaveRecord,
                        onExerciseMethodSelected = onExerciseMethodSelected
                    )
                }
                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.training_close_record))
                }
            }
        }
    }
}

@Composable
internal fun RecordForm(
    state: TrainingUiState,
    planned: PlannedExercise,
    onSetRepsChanged: (Int, String) -> Unit,
    onSetWeightChanged: (Int, String) -> Unit,
    onSetDurationChanged: (Int, String) -> Unit,
    onSetRestChanged: (Int, String) -> Unit,
    onAddSet: () -> Unit,
    onRemoveSet: (Int) -> Unit,
    onMemoChanged: (String) -> Unit,
    onSaveRecord: () -> Unit,
    onExerciseMethodSelected: () -> Unit
) {
    val showReps = planned.repRange != null
    val showDuration = planned.durationMinutes != null || !showReps
    val showWeight = showReps
    val displayLog = state.logs.firstOrNull { it.plannedExerciseId == planned.id }
        ?: state.latestWorkoutLogs.latestForExercise(planned.exercise.id)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.testTag("training_record_selected_exercise"),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrainerExerciseImage(
                exercise = planned.exercise,
                modifier = Modifier.size(width = 78.dp, height = 86.dp),
                cleanThumbnailCrop = true
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(planned.localizedTrainingDisplayText(displayLog), color = SmartTrainnerColors.Muted)
            }
        }
        OutlinedButton(
            onClick = onExerciseMethodSelected,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("training_show_exercise_method")
        ) {
            Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
            Text(stringResource(R.string.training_show_exercise_method))
        }
        state.formError?.let { error ->
            Text(
                text = error.message(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text(
            text = stringResource(R.string.training_set_entries_title),
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
                        text = stringResource(R.string.training_set_number, index + 1),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { onRemoveSet(index) },
                        enabled = state.recordForm.setEntries.size > 1,
                        modifier = Modifier
                            .testTag("training_remove_set_button_$index")
                    ) {
                        Icon(
                            Icons.Default.RemoveCircleOutline,
                            contentDescription = stringResource(R.string.training_remove_set)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (showReps) {
                        SmartTrainnerNumberField(
                            label = stringResource(R.string.training_reps),
                            value = setEntry.reps,
                            onValueChange = { onSetRepsChanged(index, it) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("training_set_reps_input_$index")
                        )
                    }
                    if (showWeight) {
                        SmartTrainnerNumberField(
                            label = stringResource(R.string.training_weight_short),
                            value = setEntry.weightKg,
                            onValueChange = { onSetWeightChanged(index, it) },
                            keyboardType = KeyboardType.Decimal,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("training_set_weight_input_$index")
                        )
                    }
                    if (showDuration) {
                        SmartTrainnerNumberField(
                            label = stringResource(R.string.training_duration),
                            value = setEntry.durationMinutes,
                            onValueChange = { onSetDurationChanged(index, it) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("training_set_duration_input_$index")
                        )
                    }
                    SmartTrainnerNumberField(
                        label = stringResource(R.string.training_rest_seconds),
                        value = setEntry.restSeconds,
                        onValueChange = { onSetRestChanged(index, it) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("training_set_rest_input_$index")
                    )
                }
            }
        }
        OutlinedButton(
            onClick = onAddSet,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("training_add_set_button")
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
            Text(stringResource(R.string.training_add_set))
        }
        Button(
            onClick = onSaveRecord,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("training_save_record"),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(stringResource(R.string.training_save_record))
        }
        if (state.recordSaved) {
            Text(
                text = stringResource(R.string.training_saved),
                color = SmartTrainnerColors.Green,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("training_record_saved_message")
            )
        }
        OutlinedTextField(
            value = state.recordForm.memo,
            onValueChange = onMemoChanged,
            label = { Text(stringResource(R.string.training_memo)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 1,
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
internal fun LogRow(
    exercise: Exercise,
    log: com.smarttrainner.core.model.WorkoutLog
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = SmartTrainnerColors.SurfaceRaised,
        border = BorderStroke(1.dp, SmartTrainnerColors.Line)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusIcon(completed = log.completed)
            Column(modifier = Modifier.weight(1f)) {
                Text(exercise.localizedName(), fontWeight = FontWeight.Bold)
                Text(
                    text = log.localizedRecordDisplayText(),
                    color = SmartTrainnerColors.Muted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
internal fun RecordFormError.message(): String = when (this) {
    RecordFormError.SELECT_EXERCISE -> stringResource(R.string.training_error_select)
    RecordFormError.SETS -> stringResource(R.string.training_error_sets)
    RecordFormError.REPS -> stringResource(R.string.training_error_reps)
    RecordFormError.WEIGHT -> stringResource(R.string.training_error_weight)
    RecordFormError.DURATION -> stringResource(R.string.training_error_duration)
    RecordFormError.REST -> stringResource(R.string.training_error_rest)
    RecordFormError.SAVE_FAILED -> stringResource(R.string.training_error_save)
    RecordFormError.COMPLETE_DAY_FAILED -> stringResource(R.string.training_error_complete_day)
}
