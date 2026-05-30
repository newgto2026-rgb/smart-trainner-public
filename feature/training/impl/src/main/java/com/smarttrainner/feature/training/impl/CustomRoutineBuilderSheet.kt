package com.smarttrainner.feature.training.impl

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.smarttrainner.core.designsystem.SmartTrainnerColors
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.RoutineFocus

@Composable
internal fun CustomRoutineBuilderSheet(
    builder: CustomRoutineBuilderState,
    exercises: List<Exercise>,
    onNameChanged: (String) -> Unit,
    onDaySelected: (Int) -> Unit,
    onDayFocusChanged: (RoutineFocus?) -> Unit,
    onAddDay: () -> Unit,
    onRemoveDay: (Int) -> Unit,
    onExerciseGroupToggled: (MuscleGroup) -> Unit,
    onExerciseDetailRequested: (ExerciseId) -> Unit,
    onAddExercise: (ExerciseId) -> Unit,
    onRemoveExercise: (Int) -> Unit,
    onMoveExerciseUp: (Int) -> Unit,
    onMoveExerciseDown: (Int) -> Unit,
    onSave: () -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 14.dp)
                .testTag("training_custom_routine_builder"),
            shape = RoundedCornerShape(8.dp),
            color = SmartTrainnerColors.SurfaceRaised
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.training_custom_routine_builder_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.training_custom_routine_builder_body),
                            color = SmartTrainnerColors.Muted,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.testTag("training_close_custom_routine_builder")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.training_close_detail))
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = builder.name,
                        onValueChange = onNameChanged,
                        label = { Text(stringResource(R.string.training_custom_routine_name)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("training_custom_routine_name"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        shape = RoundedCornerShape(8.dp)
                    )
                    CustomDayTabs(
                        builder = builder,
                        onDaySelected = onDaySelected,
                        onAddDay = onAddDay
                    )
                    builder.error?.let { error ->
                        Text(
                            text = error.localizedMessage(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.testTag("training_custom_routine_error")
                        )
                    }
                    builder.savedTemplateId?.let {
                        Text(
                            text = stringResource(R.string.training_custom_routine_saved),
                            color = SmartTrainnerColors.Green,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("training_custom_routine_saved")
                        )
                    }
                    val selectedDay = builder.days.getOrNull(builder.selectedDayIndex)
                    if (selectedDay != null) {
                        CustomDayEditor(
                            day = selectedDay,
                            dayIndex = builder.selectedDayIndex,
                            onFocusChanged = onDayFocusChanged,
                            onRemoveExercise = onRemoveExercise,
                            onMoveExerciseUp = onMoveExerciseUp,
                            onMoveExerciseDown = onMoveExerciseDown
                        )
                    }
                    ExercisePicker(
                        exercises = exercises,
                        selectedFocus = selectedDay?.focus,
                        selectedExerciseIds = selectedDay
                            ?.exercises
                            ?.map { it.exercise.id }
                            ?.toSet()
                            .orEmpty(),
                        expandedGroups = builder.expandedExerciseGroups,
                        onGroupToggled = onExerciseGroupToggled,
                        onExerciseDetailRequested = onExerciseDetailRequested,
                        onAddExercise = onAddExercise
                    )
                }
                val canRemoveCurrentDay = builder.days.size > 1 &&
                    builder.selectedDayIndex in builder.days.indices
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onRemoveDay(builder.selectedDayIndex) },
                        enabled = canRemoveCurrentDay,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("training_remove_custom_day"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text(stringResource(R.string.training_remove_custom_day))
                    }
                    Button(
                        onClick = onSave,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("training_save_custom_routine"),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text(stringResource(R.string.training_save_custom_routine))
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomDayTabs(
    builder: CustomRoutineBuilderState,
    onDaySelected: (Int) -> Unit,
    onAddDay: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.training_custom_routine_days),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        builder.days.mapIndexed { index, day -> index to day }.chunked(3).forEach { rowDays ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowDays.forEach { (dayIndex, _) ->
                    AssistChip(
                        onClick = { onDaySelected(dayIndex) },
                        label = { Text(stringResource(R.string.training_day_label, dayIndex + 1)) },
                        modifier = Modifier.testTag("training_custom_day_tab_$dayIndex")
                    )
                }
            }
        }
        if (builder.days.size < 7) {
            OutlinedButton(
                onClick = onAddDay,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("training_add_custom_day"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.training_add_custom_day))
            }
        }
    }
}

@Composable
private fun CustomDayEditor(
    day: CustomRoutineDayFormState,
    dayIndex: Int,
    onFocusChanged: (RoutineFocus?) -> Unit,
    onRemoveExercise: (Int) -> Unit,
    onMoveExerciseUp: (Int) -> Unit,
    onMoveExerciseDown: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.training_custom_day_editing, dayIndex + 1),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        CustomDayFocusSelector(
            selectedFocus = day.focus,
            onFocusChanged = onFocusChanged
        )
        if (day.exercises.isEmpty()) {
            Text(
                text = stringResource(R.string.training_custom_day_empty),
                color = SmartTrainnerColors.Muted,
                modifier = Modifier.testTag("training_custom_day_empty")
            )
        }
        day.exercises.forEachIndexed { index, item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("training_custom_exercise_${item.exercise.id.value}_$index"),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, SmartTrainnerColors.Line),
                colors = CardDefaults.cardColors(containerColor = SmartTrainnerColors.Surface)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.exercise.name, fontWeight = FontWeight.Bold)
                        Text(
                            text = item.targetText(),
                            color = SmartTrainnerColors.Muted,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = { onMoveExerciseUp(index) },
                        enabled = index > 0,
                        modifier = Modifier.testTag("training_custom_move_up_$index")
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = stringResource(R.string.training_move_up))
                    }
                    IconButton(
                        onClick = { onMoveExerciseDown(index) },
                        enabled = index < day.exercises.lastIndex,
                        modifier = Modifier.testTag("training_custom_move_down_$index")
                    ) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = stringResource(R.string.training_move_down))
                    }
                    IconButton(
                        onClick = { onRemoveExercise(index) },
                        modifier = Modifier.testTag("training_remove_custom_exercise_$index")
                    ) {
                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = stringResource(R.string.training_remove_set))
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomDayFocusSelector(
    selectedFocus: RoutineFocus?,
    onFocusChanged: (RoutineFocus?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = selectedFocus?.localizedOptionLabel()
        ?: stringResource(R.string.training_custom_focus_none)
    val selectedTag = focusSelectedTestTag(selectedFocus)
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val options = listOf<RoutineFocus?>(null) + customRoutineFocusOptions()
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .testTag("training_custom_focus_selector"),
            shape = RoundedCornerShape(8.dp),
            color = SmartTrainnerColors.Surface,
            border = BorderStroke(
                width = 1.dp,
                color = if (selectedFocus == null) SmartTrainnerColors.Line else SmartTrainnerColors.Green
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.training_routine_main_focus),
                        style = MaterialTheme.typography.labelMedium,
                        color = SmartTrainnerColors.Muted
                    )
                    Text(
                        text = selectedLabel,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag(selectedTag)
                    )
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = SmartTrainnerColors.Muted
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(maxWidth)
                .testTag("training_custom_focus_menu")
        ) {
            options.forEach { focus ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = focus?.localizedOptionLabel()
                                ?: stringResource(R.string.training_custom_focus_none)
                        )
                    },
                    leadingIcon = if (focus == selectedFocus) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(R.string.training_selected),
                                tint = SmartTrainnerColors.Green
                            )
                        }
                    } else {
                        null
                    },
                    onClick = {
                        onFocusChanged(focus)
                        expanded = false
                    },
                    modifier = Modifier.testTag(focusOptionTestTag(focus))
                )
            }
        }
    }
}

private fun focusOptionTestTag(focus: RoutineFocus?): String =
    if (focus == null) {
        "training_custom_focus_none"
    } else {
        "training_custom_focus_${focus.name}"
    }

private fun focusSelectedTestTag(focus: RoutineFocus?): String =
    if (focus == null) {
        "training_custom_focus_selected_none"
    } else {
        "training_custom_focus_selected_${focus.name}"
    }

@Composable
private fun ExercisePicker(
    exercises: List<Exercise>,
    selectedFocus: RoutineFocus?,
    selectedExerciseIds: Set<ExerciseId>,
    expandedGroups: Set<MuscleGroup>,
    onGroupToggled: (MuscleGroup) -> Unit,
    onExerciseDetailRequested: (ExerciseId) -> Unit,
    onAddExercise: (ExerciseId) -> Unit
) {
    val allowedGroups = allowedCustomRoutineMuscleGroups(selectedFocus)
    val availableExercises = exercises
        .filter { it.muscleGroup in allowedGroups }
        .filterNot { it.id in selectedExerciseIds }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.training_add_exercise_to_day),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        if (availableExercises.isEmpty()) {
            Text(
                text = stringResource(
                    if (selectedFocus == null) {
                        R.string.training_custom_all_exercises_added
                    } else {
                        R.string.training_custom_focus_exercises_added
                    }
                ),
                color = SmartTrainnerColors.Muted,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag("training_custom_all_exercises_added")
            )
        }
        MuscleGroup.entries.filter { it in allowedGroups }.forEach { group ->
            val groupExercises = availableExercises.filter { it.muscleGroup == group }
            if (groupExercises.isNotEmpty()) {
                val expanded = group in expandedGroups
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onGroupToggled(group) }
                        .testTag("training_custom_exercise_group_${group.name}"),
                    shape = RoundedCornerShape(8.dp),
                    color = SmartTrainnerColors.Surface,
                    border = BorderStroke(1.dp, SmartTrainnerColors.Line)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "${group.localizedLabel()} (${groupExercises.size})",
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (expanded) {
                    groupExercises.forEach { exercise ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAddExercise(exercise.id) }
                                .testTag("training_custom_add_exercise_${exercise.id.value}"),
                            shape = RoundedCornerShape(8.dp),
                            color = SmartTrainnerColors.Surface,
                            border = BorderStroke(1.dp, SmartTrainnerColors.Line)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(exercise.name, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = exercise.targetText,
                                        color = SmartTrainnerColors.Muted,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                OutlinedButton(
                                    onClick = { onExerciseDetailRequested(exercise.id) },
                                    modifier = Modifier.testTag("training_custom_view_exercise_${exercise.id.value}"),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.size(4.dp))
                                    Text(
                                        text = stringResource(R.string.training_instruction),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomRoutineFormError.localizedMessage(): String = stringResource(
    when (this) {
        CustomRoutineFormError.NAME -> R.string.training_custom_error_name
        CustomRoutineFormError.DAYS -> R.string.training_custom_error_days
        CustomRoutineFormError.EMPTY_DAY -> R.string.training_custom_error_empty_day
        CustomRoutineFormError.EXERCISE -> R.string.training_custom_error_exercise
        CustomRoutineFormError.SAVE_FAILED -> R.string.training_custom_error_save
    }
)

private fun CustomRoutineExerciseFormState.targetText(): String =
    if (repRangeStart != null && repRangeEnd != null) {
        "${sets}세트 x ${repRangeStart}-${repRangeEnd}회"
    } else {
        "${sets}세트 x ${durationMinutes ?: 10}분"
    }

private fun customRoutineFocusOptions(): List<RoutineFocus> = listOf(
    RoutineFocus.UPPER_BODY,
    RoutineFocus.PUSH,
    RoutineFocus.PULL,
    RoutineFocus.CHEST,
    RoutineFocus.BACK,
    RoutineFocus.LOWER_BODY,
    RoutineFocus.SHOULDERS,
    RoutineFocus.ARMS,
    RoutineFocus.BICEPS,
    RoutineFocus.TRICEPS,
    RoutineFocus.FOREARMS,
    RoutineFocus.CORE,
    RoutineFocus.CARDIO_CONDITIONING
)

@Composable
private fun RoutineFocus.localizedOptionLabel(): String = stringResource(
    when (this) {
        RoutineFocus.FULL_BODY -> R.string.training_muscle_full_body
        RoutineFocus.UPPER_BODY -> R.string.training_muscle_upper_body
        RoutineFocus.PUSH -> R.string.training_muscle_push
        RoutineFocus.PULL -> R.string.training_muscle_pull
        RoutineFocus.CHEST -> R.string.training_muscle_chest
        RoutineFocus.BACK -> R.string.training_muscle_back
        RoutineFocus.LOWER_BODY -> R.string.training_muscle_lower_body
        RoutineFocus.SHOULDERS -> R.string.training_muscle_shoulders
        RoutineFocus.ARMS -> R.string.training_muscle_arms
        RoutineFocus.BICEPS -> R.string.training_muscle_biceps
        RoutineFocus.TRICEPS -> R.string.training_muscle_triceps
        RoutineFocus.FOREARMS -> R.string.training_muscle_forearms
        RoutineFocus.CARDIO_CONDITIONING -> R.string.training_muscle_cardio
        RoutineFocus.CORE -> R.string.training_muscle_core
    }
)
