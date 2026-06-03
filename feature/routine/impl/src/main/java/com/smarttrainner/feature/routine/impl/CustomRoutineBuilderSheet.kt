package com.smarttrainner.feature.routine.impl

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.smarttrainner.core.designsystem.SmartTrainnerColors
import com.smarttrainner.core.domain.ExercisePrescription
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.RoutineFocus
import com.smarttrainner.core.model.targetsAnyMuscleGroup
import com.smarttrainner.core.model.targetsMuscleGroup
import com.smarttrainner.core.ui.SmartTrainnerExercisePickerCard

@Composable
internal fun CustomRoutineBuilderSheet(
    builder: CustomRoutineBuilderState,
    exercises: List<Exercise>,
    exercisePrescriptions: Map<ExerciseId, ExercisePrescription>,
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
    val focusManager = LocalFocusManager.current

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
                            text = stringResource(R.string.routine_custom_routine_builder_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.routine_custom_routine_builder_body),
                            color = SmartTrainnerColors.Muted,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.testTag("training_close_custom_routine_builder")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.routine_close_detail))
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
                        label = { Text(stringResource(R.string.routine_custom_routine_name)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("training_custom_routine_name"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
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
                            text = stringResource(R.string.routine_custom_routine_saved),
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
                        exercisePrescriptions = exercisePrescriptions,
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
                        Text(stringResource(R.string.routine_remove_custom_day))
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
                        Text(stringResource(R.string.routine_save_custom_routine))
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
            text = stringResource(R.string.routine_custom_routine_days),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        builder.days.mapIndexed { index, day -> index to day }.chunked(3).forEach { rowDays ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowDays.forEach { (dayIndex, _) ->
                    val selected = dayIndex == builder.selectedDayIndex
                    FilterChip(
                        selected = selected,
                        onClick = { onDaySelected(dayIndex) },
                        label = { Text(stringResource(R.string.routine_day_label, dayIndex + 1)) },
                        modifier = Modifier.testTag("training_custom_day_tab_$dayIndex"),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = SmartTrainnerColors.SurfaceRaised,
                            labelColor = SmartTrainnerColors.Muted,
                            selectedContainerColor = SmartTrainnerColors.CoralSoft,
                            selectedLabelColor = SmartTrainnerColors.Ink
                        )
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
                Text(stringResource(R.string.routine_add_custom_day))
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
            text = stringResource(R.string.routine_custom_day_editing, dayIndex + 1),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        CustomDayFocusSelector(
            selectedFocus = day.focus,
            onFocusChanged = onFocusChanged
        )
        if (day.exercises.isEmpty()) {
            Text(
                text = stringResource(R.string.routine_custom_day_empty),
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
                        Text(item.exercise.localizedName(), fontWeight = FontWeight.Bold)
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
                        Icon(Icons.Default.ArrowUpward, contentDescription = stringResource(R.string.routine_move_up))
                    }
                    IconButton(
                        onClick = { onMoveExerciseDown(index) },
                        enabled = index < day.exercises.lastIndex,
                        modifier = Modifier.testTag("training_custom_move_down_$index")
                    ) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = stringResource(R.string.routine_move_down))
                    }
                    IconButton(
                        onClick = { onRemoveExercise(index) },
                        modifier = Modifier.testTag("training_remove_custom_exercise_$index")
                    ) {
                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = stringResource(R.string.routine_remove_set))
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
        ?: stringResource(R.string.routine_muscle_full_body)
    val selectedGroupLabel = selectedFocus.focusGroupLabel()
    val selectedTag = focusSelectedTestTag(selectedFocus)
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val groups = customRoutineFocusGroups()
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
                        text = stringResource(R.string.routine_routine_main_focus),
                        style = MaterialTheme.typography.labelMedium,
                        color = SmartTrainnerColors.Muted
                    )
                    Text(
                        text = selectedGroupLabel,
                        color = SmartTrainnerColors.Green,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
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
        if (expanded) {
            DropdownMenu(
                expanded = true,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .heightIn(max = 430.dp)
                    .width(maxWidth)
                    .testTag("training_custom_focus_menu")
            ) {
                groups.forEach { group ->
                    Text(
                        text = stringResource(group.titleRes),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SmartTrainnerColors.Muted,
                        modifier = Modifier.padding(start = 14.dp, top = 10.dp, end = 14.dp, bottom = 4.dp)
                    )
                    group.options.forEach { focus ->
                        FocusOptionRow(
                            focus = focus,
                            caption = stringResource(group.captionRes),
                            selected = focus == selectedFocus,
                            onClick = {
                                onFocusChanged(focus)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FocusOptionRow(
    focus: RoutineFocus?,
    caption: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 3.dp)
            .testTag(focusOptionTestTag(focus)),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) SmartTrainnerColors.GreenSoft else SmartTrainnerColors.Surface,
        border = BorderStroke(1.dp, if (selected) SmartTrainnerColors.Green else SmartTrainnerColors.Line)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = focus?.localizedOptionLabel() ?: stringResource(R.string.routine_muscle_full_body),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = SmartTrainnerColors.Ink
                )
                Text(
                    text = caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = SmartTrainnerColors.Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.routine_selected),
                    tint = SmartTrainnerColors.Green,
                    modifier = Modifier.size(18.dp)
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
    exercisePrescriptions: Map<ExerciseId, ExercisePrescription>,
    selectedFocus: RoutineFocus?,
    selectedExerciseIds: Set<ExerciseId>,
    expandedGroups: Set<MuscleGroup>,
    onGroupToggled: (MuscleGroup) -> Unit,
    onExerciseDetailRequested: (ExerciseId) -> Unit,
    onAddExercise: (ExerciseId) -> Unit
) {
    val allowedGroups = allowedCustomRoutineMuscleGroups(selectedFocus)
    val availableExercises = exercises
        .filter { it.targetsAnyMuscleGroup(allowedGroups) }
        .filterNot { it.id in selectedExerciseIds }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.routine_add_exercise_to_day),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        if (availableExercises.isEmpty()) {
            Text(
                text = stringResource(
                    if (selectedFocus == null) {
                        R.string.routine_custom_all_exercises_added
                    } else {
                        R.string.routine_custom_focus_exercises_added
                    }
                ),
                color = SmartTrainnerColors.Muted,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag("training_custom_all_exercises_added")
            )
        }
        MuscleGroup.entries.filter { it in allowedGroups }.forEach { group ->
            val groupExercises = availableExercises.filter { it.targetsMuscleGroup(group) }
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
                        SmartTrainnerExercisePickerCard(
                            title = exercise.localizedName(),
                            subtitle = exercisePrescriptions[exercise.id]?.localizedTargetText()
                                ?: exercise.localizedTargetText(),
                            leadingIcon = Icons.Default.Add,
                            secondaryActionLabel = stringResource(R.string.routine_instruction),
                            secondaryActionIcon = Icons.Default.Info,
                            onClick = { onAddExercise(exercise.id) },
                            onSecondaryActionClick = { onExerciseDetailRequested(exercise.id) },
                            clickModifier = Modifier.testTag("training_custom_add_exercise_${exercise.id.value}"),
                            secondaryActionModifier = Modifier.testTag(
                                "training_custom_view_exercise_${exercise.id.value}"
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomRoutineFormError.localizedMessage(): String = stringResource(
    when (this) {
        CustomRoutineFormError.NAME -> R.string.routine_custom_error_name
        CustomRoutineFormError.DAYS -> R.string.routine_custom_error_days
        CustomRoutineFormError.EMPTY_DAY -> R.string.routine_custom_error_empty_day
        CustomRoutineFormError.EXERCISE -> R.string.routine_custom_error_exercise
        CustomRoutineFormError.SAVE_FAILED -> R.string.routine_custom_error_save
    }
)

@Composable
private fun CustomRoutineExerciseFormState.targetText(): String =
    if (repRangeStart != null && repRangeEnd != null) {
        if (repRangeStart == repRangeEnd) {
            stringResource(R.string.routine_target_reps_single, sets, repRangeStart)
        } else {
            stringResource(R.string.routine_target_reps, sets, repRangeStart, repRangeEnd)
        }
    } else {
        stringResource(R.string.routine_target_duration, sets, durationMinutes ?: 10)
    }

private data class CustomRoutineFocusGroup(
    val titleRes: Int,
    val captionRes: Int,
    val options: List<RoutineFocus?>
)

private val compoundCustomRoutineFocuses = setOf(
    RoutineFocus.UPPER_BODY,
    RoutineFocus.PUSH,
    RoutineFocus.PULL,
    RoutineFocus.ARMS
)

private fun customRoutineFocusGroups(): List<CustomRoutineFocusGroup> = listOf(
    CustomRoutineFocusGroup(
        titleRes = R.string.routine_muscle_full_body,
        captionRes = R.string.routine_custom_focus_caption_full_body,
        options = listOf(null)
    ),
    CustomRoutineFocusGroup(
        titleRes = R.string.routine_custom_focus_group_single,
        captionRes = R.string.routine_custom_focus_caption_single,
        options = listOf(
            RoutineFocus.CHEST,
            RoutineFocus.BACK,
            RoutineFocus.LOWER_BODY,
            RoutineFocus.SHOULDERS,
            RoutineFocus.BICEPS,
            RoutineFocus.TRICEPS,
            RoutineFocus.FOREARMS,
            RoutineFocus.CORE,
            RoutineFocus.CARDIO_CONDITIONING
        )
    ),
    CustomRoutineFocusGroup(
        titleRes = R.string.routine_custom_focus_group_compound,
        captionRes = R.string.routine_custom_focus_caption_compound,
        options = compoundCustomRoutineFocuses.toList()
    )
)

@Composable
private fun RoutineFocus?.focusGroupLabel(): String = stringResource(
    when {
        this == null -> R.string.routine_muscle_full_body
        this in compoundCustomRoutineFocuses -> R.string.routine_custom_focus_group_compound
        else -> R.string.routine_custom_focus_group_single
    }
)

@Composable
private fun RoutineFocus.localizedOptionLabel(): String = stringResource(
    when (this) {
        RoutineFocus.FULL_BODY -> R.string.routine_muscle_full_body
        RoutineFocus.UPPER_BODY -> R.string.routine_muscle_upper_body
        RoutineFocus.PUSH -> R.string.routine_muscle_push
        RoutineFocus.PULL -> R.string.routine_muscle_pull
        RoutineFocus.CHEST -> R.string.routine_muscle_chest
        RoutineFocus.BACK -> R.string.routine_muscle_back
        RoutineFocus.LOWER_BODY -> R.string.routine_muscle_lower_body
        RoutineFocus.SHOULDERS -> R.string.routine_muscle_shoulders
        RoutineFocus.ARMS -> R.string.routine_muscle_arms
        RoutineFocus.BICEPS -> R.string.routine_muscle_biceps
        RoutineFocus.TRICEPS -> R.string.routine_muscle_triceps
        RoutineFocus.FOREARMS -> R.string.routine_muscle_forearms
        RoutineFocus.CARDIO_CONDITIONING -> R.string.routine_muscle_cardio
        RoutineFocus.CORE -> R.string.routine_muscle_core
    }
)
