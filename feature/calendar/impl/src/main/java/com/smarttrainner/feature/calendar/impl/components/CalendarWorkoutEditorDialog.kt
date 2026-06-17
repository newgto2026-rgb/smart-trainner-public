package com.smarttrainner.feature.calendar.impl.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.smarttrainner.core.designsystem.SmartTrainnerColors
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.ExerciseLoadType
import com.smarttrainner.feature.calendar.impl.CalendarExerciseOptionUiModel
import com.smarttrainner.feature.calendar.impl.CalendarWorkoutEditorError
import com.smarttrainner.feature.calendar.impl.CalendarWorkoutEditorMode
import com.smarttrainner.feature.calendar.impl.CalendarWorkoutEditorUiState
import com.smarttrainner.feature.calendar.impl.CalendarWorkoutSetFormUiState
import com.smarttrainner.feature.calendar.impl.R
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun CalendarWorkoutEditorDialog(
    state: CalendarWorkoutEditorUiState,
    onExerciseSelected: (ExerciseId) -> Unit,
    onSetRepsChanged: (Int, String) -> Unit,
    onSetWeightChanged: (Int, String) -> Unit,
    onSetDurationChanged: (Int, String) -> Unit,
    onSetRestChanged: (Int, String) -> Unit,
    onAddSet: () -> Unit,
    onRemoveSet: (Int) -> Unit,
    onMemoChanged: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val windowHeight = with(density) { LocalWindowInfo.current.containerSize.height.toDp() }
    val maxDialogHeight = minOf(windowHeight * 0.85f, 640.dp)
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxDialogHeight)
                .padding(horizontal = 18.dp)
                .testTag("calendar_workout_editor_dialog"),
            shape = RoundedCornerShape(8.dp),
            color = SmartTrainnerColors.SurfaceRaised
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(start = 14.dp, top = 42.dp, end = 14.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(
                            if (state.mode == CalendarWorkoutEditorMode.ADD) {
                                R.string.calendar_editor_add_title
                            } else {
                                R.string.calendar_editor_edit_title
                            }
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SmartTrainnerColors.Ink
                    )
                    Text(
                        text = state.selectedDate.format(
                            DateTimeFormatter.ofPattern(
                                stringResource(R.string.calendar_selected_date_format),
                                Locale.getDefault()
                            )
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = SmartTrainnerColors.Muted,
                        modifier = Modifier.testTag("calendar_editor_date")
                    )
                    if (state.mode == CalendarWorkoutEditorMode.ADD) {
                        ExerciseSelector(
                            options = state.exerciseOptions,
                            selectedExerciseName = state.selectedExerciseName,
                            onExerciseSelected = onExerciseSelected
                        )
                    } else {
                        Text(
                            text = state.selectedExerciseName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SmartTrainnerColors.Ink,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("calendar_editor_exercise_name")
                        )
                    }
                    state.error?.let { error ->
                        Text(
                            text = error.message(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.testTag("calendar_editor_error")
                        )
                    }
                    Text(
                        text = stringResource(R.string.calendar_editor_sets_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SmartTrainnerColors.Ink
                    )
                    state.setEntries.forEachIndexed { index, setEntry ->
                        CalendarSetEditorRow(
                            index = index,
                            setEntry = setEntry,
                            showReps = state.showReps,
                            showWeight = state.showWeight,
                            showDuration = state.showDuration,
                            loadType = state.selectedExerciseLoadType,
                            removeEnabled = state.setEntries.size > 1,
                            onSetRepsChanged = { value -> onSetRepsChanged(index, value) },
                            onSetWeightChanged = { value -> onSetWeightChanged(index, value) },
                            onSetDurationChanged = { value -> onSetDurationChanged(index, value) },
                            onSetRestChanged = { value -> onSetRestChanged(index, value) },
                            onRemoveSet = { onRemoveSet(index) }
                        )
                    }
                    OutlinedButton(
                        onClick = onAddSet,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("calendar_editor_add_set")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text(stringResource(R.string.calendar_editor_add_set))
                    }
                    OutlinedTextField(
                        value = state.memo,
                        onValueChange = onMemoChanged,
                        label = { Text(stringResource(R.string.calendar_editor_memo)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("calendar_editor_memo"),
                        minLines = 1,
                        shape = RoundedCornerShape(8.dp)
                    )
                    Button(
                        onClick = onSave,
                        enabled = !state.isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("calendar_editor_save"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            stringResource(
                                if (state.mode == CalendarWorkoutEditorMode.ADD) {
                                    R.string.calendar_editor_save_add
                                } else {
                                    R.string.calendar_editor_save_edit
                                }
                            )
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .testTag("calendar_editor_close")
                ) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.calendar_editor_close))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseSelector(
    options: List<CalendarExerciseOptionUiModel>,
    selectedExerciseName: String,
    onExerciseSelected: (ExerciseId) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedExerciseName,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.calendar_editor_exercise)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .testTag("calendar_editor_exercise_selector"),
            shape = RoundedCornerShape(8.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.name) },
                    onClick = {
                        onExerciseSelected(option.id)
                        expanded = false
                    },
                    modifier = Modifier.testTag("calendar_editor_exercise_option_${option.id.value}")
                )
            }
        }
    }
}

@Composable
private fun CalendarSetEditorRow(
    index: Int,
    setEntry: CalendarWorkoutSetFormUiState,
    showReps: Boolean,
    showWeight: Boolean,
    showDuration: Boolean,
    loadType: ExerciseLoadType,
    removeEnabled: Boolean,
    onSetRepsChanged: (String) -> Unit,
    onSetWeightChanged: (String) -> Unit,
    onSetDurationChanged: (String) -> Unit,
    onSetRestChanged: (String) -> Unit,
    onRemoveSet: () -> Unit
) {
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
                text = stringResource(R.string.calendar_editor_set_number, (index + 1).toString()),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = SmartTrainnerColors.Ink
            )
            IconButton(
                onClick = onRemoveSet,
                enabled = removeEnabled,
                modifier = Modifier.testTag("calendar_editor_remove_set_$index")
            ) {
                Icon(
                    Icons.Default.RemoveCircleOutline,
                    contentDescription = stringResource(R.string.calendar_editor_remove_set)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (showReps) {
                CalendarNumberField(
                    label = stringResource(R.string.calendar_editor_reps),
                    value = setEntry.reps,
                    onValueChange = onSetRepsChanged,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("calendar_editor_set_reps_$index")
                )
            }
            if (showWeight) {
                CalendarNumberField(
                    label = stringResource(
                        if (loadType == ExerciseLoadType.ASSISTANCE_LOAD) {
                            R.string.calendar_editor_assistance_weight
                        } else {
                            R.string.calendar_editor_weight
                        }
                    ),
                    value = setEntry.weightKg,
                    onValueChange = onSetWeightChanged,
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("calendar_editor_set_weight_$index")
                )
            }
            if (showDuration) {
                CalendarNumberField(
                    label = stringResource(R.string.calendar_editor_duration),
                    value = setEntry.durationMinutes,
                    onValueChange = onSetDurationChanged,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("calendar_editor_set_duration_$index")
                )
            }
        }
        CalendarNumberField(
            label = stringResource(R.string.calendar_editor_rest_seconds),
            value = setEntry.restSeconds,
            onValueChange = onSetRestChanged,
            keyboardType = KeyboardType.Number,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("calendar_editor_set_rest_$index")
        )
    }
}

@Composable
private fun CalendarNumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier,
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
private fun CalendarWorkoutEditorError.message(): String = when (this) {
    CalendarWorkoutEditorError.EXERCISE -> stringResource(R.string.calendar_editor_error_exercise)
    CalendarWorkoutEditorError.SETS -> stringResource(R.string.calendar_editor_error_sets)
    CalendarWorkoutEditorError.REPS -> stringResource(R.string.calendar_editor_error_reps)
    CalendarWorkoutEditorError.WEIGHT -> stringResource(R.string.calendar_editor_error_weight)
    CalendarWorkoutEditorError.DURATION -> stringResource(R.string.calendar_editor_error_duration)
    CalendarWorkoutEditorError.REST -> stringResource(R.string.calendar_editor_error_rest)
    CalendarWorkoutEditorError.SAVE_FAILED -> stringResource(R.string.calendar_editor_error_save)
}
