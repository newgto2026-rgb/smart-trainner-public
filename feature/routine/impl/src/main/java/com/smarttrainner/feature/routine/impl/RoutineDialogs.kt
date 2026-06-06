package com.smarttrainner.feature.routine.impl

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import com.smarttrainner.core.domain.ExercisePrescription
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.RoutineFeeling
import com.smarttrainner.core.model.RoutineSource
import com.smarttrainner.core.model.TrainingExperience
import com.smarttrainner.core.model.estimatedSessionMinutes
import com.smarttrainner.core.model.targetsMuscleGroup
import com.smarttrainner.core.ui.SmartTrainnerBadge
import com.smarttrainner.core.ui.SmartTrainnerBadgeRow
import com.smarttrainner.core.ui.SmartTrainnerBadgeSpec
import com.smarttrainner.core.ui.SmartTrainnerEmptyState
import com.smarttrainner.core.ui.SmartTrainnerExercisePickerCard
import com.smarttrainner.core.ui.SmartTrainnerProgressBar
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoutineDayDatePickerDialog(
    picker: RoutineDayDatePickerState,
    onDateSelected: (LocalDate) -> Unit,
    onDismissRequest: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = picker.initialDate.toUtcMillis(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val date = utcTimeMillis.toUtcLocalDate()
                return (picker.minDateExclusive == null || date.isAfter(picker.minDateExclusive)) &&
                    !date.isAfter(picker.maxDateInclusive)
            }
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier.testTag("training_routine_day_date_dialog"),
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis
                        ?.toUtcLocalDate()
                        ?.let(onDateSelected)
                },
                enabled = datePickerState.selectedDateMillis != null,
                modifier = Modifier.testTag("training_confirm_routine_day_date")
            ) {
                Text(stringResource(R.string.routine_save_custom_routine))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
                modifier = Modifier.testTag("training_cancel_routine_day_date")
            ) {
                Text(stringResource(R.string.routine_cancel))
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            modifier = Modifier.testTag("training_routine_day_date_picker"),
            title = {
                Text(
                    text = stringResource(picker.reason.titleResId()),
                    modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp)
                )
            }
        )
    }
}

private fun RoutineDayDatePickerReason.titleResId(): Int = when (this) {
    RoutineDayDatePickerReason.START_WORKOUT -> R.string.routine_day_date_start_title
    RoutineDayDatePickerReason.COMPLETE_DAY -> R.string.routine_day_date_complete_title
    RoutineDayDatePickerReason.EDIT -> R.string.routine_day_date_edit_title
}

internal fun LocalDate.toUtcMillis(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

internal fun Long.toUtcLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

@Composable
internal fun RoutineRecommendationControls(
    form: RoutineRecommendationFormState,
    availability: RoutineRecommendationFilterAvailability,
    onCycleLengthChanged: (Int) -> Unit,
    onSessionMinutesChanged: (Int) -> Unit,
    onExperienceChanged: (TrainingExperience) -> Unit,
    onFeelingChanged: (RoutineFeeling) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        RoutineOptionRow(
            label = stringResource(R.string.routine_routine_days_question),
            options = listOf(2, 3, 4, 5).map {
                RoutineFilterOption(
                    value = it,
                    label = stringResource(R.string.routine_cycle_length_option, it),
                    enabled = it in availability.cycleLength
                )
            },
            selected = form.cycleLength,
            onSelected = onCycleLengthChanged
        )
        RoutineOptionRow(
            label = stringResource(R.string.routine_routine_minutes_question),
            options = listOf(30, 45, 60).map {
                RoutineFilterOption(
                    value = it,
                    label = stringResource(R.string.routine_minutes_option, it),
                    enabled = it in availability.sessionMinutes
                )
            },
            selected = form.sessionMinutes,
            onSelected = onSessionMinutesChanged
        )
        RoutineOptionRow(
            label = stringResource(R.string.routine_routine_experience_question),
            options = TrainingExperience.entries.map {
                RoutineFilterOption(
                    value = it,
                    label = it.localizedLabel(),
                    enabled = it in availability.experiences
                )
            },
            selected = form.experience,
            onSelected = onExperienceChanged
        )
        RoutineOptionRow(
            label = stringResource(R.string.routine_routine_feeling_question),
            options = listOf(
                RoutineFeeling.APP_RECOMMENDED,
                RoutineFeeling.BALANCED_FULL_BODY,
                RoutineFeeling.FOCUSED_BODY_PART
            ).map {
                RoutineFilterOption(
                    value = it,
                    label = it.localizedLabel(),
                    enabled = it in availability.feelings
                )
            },
            selected = form.feeling,
            onSelected = onFeelingChanged
        )
    }
}

@Composable
internal fun <T> RoutineOptionRow(
    label: String,
    options: List<RoutineFilterOption<T>>,
    selected: T,
    onSelected: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.chunked(2).forEach { rowOptions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowOptions.forEach { option ->
                        RoutineFilterChip(
                            selected = option.value == selected,
                            label = option.label,
                            enabled = option.enabled,
                            onClick = { onSelected(option.value) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowOptions.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
internal fun RoutineFilterChip(
    selected: Boolean,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = SmartTrainnerColors.SurfaceRaised,
            labelColor = SmartTrainnerColors.Muted,
            selectedContainerColor = SmartTrainnerColors.CoralSoft,
            selectedLabelColor = SmartTrainnerColors.Coral
        )
    )
}

@Composable
internal fun RoutineLibraryDialog(
    state: RoutineUiState,
    onTemplateSelected: (String) -> Unit,
    onShowRoutineSettings: () -> Unit,
    onCreateCustomRoutine: () -> Unit,
    onCopyTemplateToCustom: (String) -> Unit,
    onEditCustomRoutine: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .heightIn(max = 640.dp)
                .testTag("training_routine_library_dialog"),
            shape = RoundedCornerShape(8.dp),
            color = SmartTrainnerColors.Surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DialogHeader(
                    title = stringResource(R.string.routine_routine_library_title),
                    onDismissRequest = onDismissRequest
                )
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = stringResource(R.string.routine_my_routines),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (state.customTemplates.isEmpty()) {
                        SmartTrainnerEmptyState(text = stringResource(R.string.routine_custom_routine_empty))
                    } else {
                        state.customTemplates.forEach { template ->
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                PlanTemplateCard(
                                    template = template,
                                    selected = template.id == state.selectedTemplateId,
                                    onClick = { onTemplateSelected(template.id) },
                                    modifier = Modifier.fillMaxWidth(),
                                    cardTestTag = "training_custom_template_card"
                                )
                                OutlinedButton(
                                    onClick = { onEditCustomRoutine(template.id) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("training_edit_custom_template_card"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.size(8.dp))
                                    Text(stringResource(R.string.routine_edit_custom_routine))
                                }
                            }
                        }
                    }
                    OutlinedButton(
                        onClick = onCreateCustomRoutine,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("training_create_custom_routine_from_library_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text(stringResource(R.string.routine_create_custom_routine))
                    }
                    Text(
                        text = stringResource(R.string.routine_default_routines),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    state.systemTemplates.forEach { template ->
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            PlanTemplateCard(
                                template = template,
                                selected = template.id == state.selectedTemplateId,
                                onClick = { onTemplateSelected(template.id) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedButton(
                                onClick = { onCopyTemplateToCustom(template.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("training_copy_template_${template.id}"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.size(8.dp))
                                Text(stringResource(R.string.routine_copy_to_custom_routine))
                            }
                        }
                    }
                }
                Button(
                    onClick = onShowRoutineSettings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("training_find_recommended_routine_button"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(R.string.routine_find_recommended_routine))
                }
            }
        }
    }
}

@Composable
internal fun RoutineSwitchConfirmDialog(
    template: PlanTemplate,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .testTag("training_routine_switch_confirm_dialog"),
            shape = RoundedCornerShape(8.dp),
            color = SmartTrainnerColors.Surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DialogHeader(
                    title = stringResource(R.string.routine_switch_confirm_title),
                    onDismissRequest = onDismissRequest
                )
                Text(
                    text = stringResource(R.string.routine_switch_confirm_body, template.localizedName()),
                    color = SmartTrainnerColors.Muted,
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("training_cancel_routine_switch"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(stringResource(R.string.routine_switch_confirm_dismiss))
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("training_confirm_routine_switch"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(stringResource(R.string.routine_change_routine))
                    }
                }
            }
        }
    }
}

@Composable
internal fun RoutineSettingsDialog(
    form: RoutineRecommendationFormState,
    availability: RoutineRecommendationFilterAvailability,
    onCycleLengthChanged: (Int) -> Unit,
    onSessionMinutesChanged: (Int) -> Unit,
    onExperienceChanged: (TrainingExperience) -> Unit,
    onFeelingChanged: (RoutineFeeling) -> Unit,
    onShowRecommendations: () -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .heightIn(max = 640.dp)
                .testTag("training_routine_settings_dialog"),
            shape = RoundedCornerShape(8.dp),
            color = SmartTrainnerColors.Surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DialogHeader(
                    title = stringResource(R.string.routine_routine_settings_title),
                    onDismissRequest = onDismissRequest
                )
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = stringResource(R.string.routine_routine_settings_body),
                        color = SmartTrainnerColors.Muted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    RoutineRecommendationControls(
                        form = form,
                        availability = availability,
                        onCycleLengthChanged = onCycleLengthChanged,
                        onSessionMinutesChanged = onSessionMinutesChanged,
                        onExperienceChanged = onExperienceChanged,
                        onFeelingChanged = onFeelingChanged
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(stringResource(R.string.routine_cancel))
                    }
                    Button(
                        onClick = onShowRecommendations,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("training_show_recommendations"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(stringResource(R.string.routine_show_recommendations))
                    }
                }
            }
        }
    }
}

@Composable
internal fun RoutineCompleteDayConfirmationDialog(
    confirmation: RoutineCompletionConfirmState,
    routineDay: NextRoutineDayUiModel?,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .heightIn(max = 520.dp)
                .testTag("training_complete_day_confirmation_dialog"),
            shape = RoundedCornerShape(8.dp),
            color = SmartTrainnerColors.Surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DialogHeader(
                    title = stringResource(R.string.routine_complete_day_confirm_title),
                    onDismissRequest = onDismissRequest
                )
                Text(
                    text = stringResource(
                        R.string.routine_complete_day_confirm_body,
                        confirmation.unrecordedExercises.size,
                        routineDay?.cycleNumber ?: 1,
                        routineDay?.dayNumber ?: 1
                    ),
                    color = SmartTrainnerColors.Muted,
                    style = MaterialTheme.typography.bodyMedium
                )
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    confirmation.unrecordedExercises.forEach { exercise ->
                        Text(
                            text = exercise.exercise.localizedName(),
                            color = SmartTrainnerColors.Ink,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.testTag("training_unrecorded_exercise_${exercise.exercise.id.value}")
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(stringResource(R.string.routine_complete_day_confirm_return))
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("training_confirm_complete_routine_day"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text(stringResource(R.string.routine_complete_day_confirm_action))
                    }
                }
            }
        }
    }
}

@Composable
internal fun RoutineCancelLatestDayDialog(
    completion: LatestRoutineDayCompletionUiModel,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .testTag("training_cancel_latest_day_dialog"),
            shape = RoundedCornerShape(8.dp),
            color = SmartTrainnerColors.Surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DialogHeader(
                    title = stringResource(R.string.routine_cancel_latest_day_title),
                    onDismissRequest = onDismissRequest
                )
                Text(
                    text = stringResource(
                        R.string.routine_cancel_latest_day_body,
                        completion.cycleNumber,
                        completion.dayNumber
                    ),
                    color = SmartTrainnerColors.Muted,
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("training_keep_latest_day_completion"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(stringResource(R.string.routine_keep_day_completion))
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("training_confirm_cancel_latest_day"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SmartTrainnerColors.Danger,
                            contentColor = SmartTrainnerColors.SurfaceRaised
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text(stringResource(R.string.routine_delete_and_cancel_day))
                    }
                }
            }
        }
    }
}

@Composable
internal fun RoutineExercisePickerDialog(
    picker: RoutineExercisePickerState,
    exercises: List<Exercise>,
    exercisePrescriptions: Map<ExerciseId, ExercisePrescription>,
    onExerciseSelected: (ExerciseId) -> Unit,
    onExerciseDetailRequested: (ExerciseId) -> Unit,
    onDismissRequest: () -> Unit
) {
    var expandedGroups by remember(picker.mode) { mutableStateOf(emptySet<MuscleGroup>()) }
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .heightIn(max = 640.dp)
                .testTag("training_routine_exercise_picker_dialog"),
            shape = RoundedCornerShape(8.dp),
            color = SmartTrainnerColors.Surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DialogHeader(
                    title = stringResource(
                        if (picker.mode == RoutineExercisePickerMode.SUBSTITUTE) {
                            R.string.routine_substitute_exercise_title
                        } else {
                            R.string.routine_add_exercise_title
                        }
                    ),
                    onDismissRequest = onDismissRequest
                )
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MuscleGroup.entries.forEach { group ->
                        val groupExercises = exercises.filter { it.targetsMuscleGroup(group) }
                        if (groupExercises.isEmpty()) return@forEach
                        val expanded = group in expandedGroups
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expandedGroups = if (expanded) {
                                        expandedGroups - group
                                    } else {
                                        expandedGroups + group
                                    }
                                }
                                .testTag("training_pick_routine_exercise_group_${group.name}"),
                            shape = RoundedCornerShape(8.dp),
                            color = SmartTrainnerColors.SurfaceRaised,
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
                                    modifier = Modifier.size(18.dp),
                                    tint = SmartTrainnerColors.Coral
                                )
                                Text(
                                    text = "${group.localizedLabel()} (${groupExercises.size})",
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        if (!expanded) return@forEach
                        groupExercises.forEach { exercise ->
                            SmartTrainnerExercisePickerCard(
                                title = exercise.localizedName(),
                                subtitle = exercisePrescriptions[exercise.id]?.localizedTargetText()
                                    ?: exercise.localizedTargetText(),
                                leadingIcon = if (picker.mode == RoutineExercisePickerMode.SUBSTITUTE) {
                                    Icons.Default.SwapHoriz
                                } else {
                                    Icons.Default.Add
                                },
                                secondaryActionLabel = stringResource(R.string.routine_instruction),
                                secondaryActionIcon = Icons.Default.Info,
                                onClick = { onExerciseSelected(exercise.id) },
                                onSecondaryActionClick = { onExerciseDetailRequested(exercise.id) },
                                modifier = Modifier.testTag(
                                    "training_pick_routine_exercise_card_${exercise.id.value}"
                                ),
                                clickModifier = Modifier.testTag("training_pick_routine_exercise_${exercise.id.value}"),
                                secondaryActionModifier = Modifier.testTag(
                                    "training_pick_routine_exercise_method_${exercise.id.value}"
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun RoutineRecommendationsDialog(
    state: RoutineUiState,
    onTemplatePreviewSelected: (String) -> Unit,
    onStartRoutine: () -> Unit,
    onDismissRequest: () -> Unit
) {
    val primary = state.templates.firstOrNull { it.id == state.recommendedTemplateId }
    val alternatives = state.alternativeTemplateIds.mapNotNull { templateId ->
        state.templates.firstOrNull { it.id == templateId }
    }
    val options = listOfNotNull(primary) + alternatives
    val previewTemplate = state.templates.firstOrNull { it.id == state.routinePreviewTemplateId }
        ?: primary
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .heightIn(max = 640.dp)
                .testTag("training_routine_recommendations_dialog"),
            shape = RoundedCornerShape(8.dp),
            color = SmartTrainnerColors.Surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DialogHeader(
                    title = stringResource(R.string.routine_routine_recommendations_title),
                    onDismissRequest = onDismissRequest
                )
                Text(
                    text = stringResource(
                        R.string.routine_routine_recommendations_body,
                        state.routineRecommendationInput.cycleLength,
                        state.routineRecommendationInput.sessionMinutes
                    ),
                    color = SmartTrainnerColors.Muted,
                    style = MaterialTheme.typography.bodyMedium
                )
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    options.forEachIndexed { index, template ->
                        if (index == 0) {
                            Text(
                                text = stringResource(R.string.routine_recommended_routine),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        } else if (index == 1) {
                            Text(
                                text = stringResource(R.string.routine_alternative_routines),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        PlanTemplateCard(
                            template = template,
                            selected = template.id == previewTemplate?.id,
                            onClick = { onTemplatePreviewSelected(template.id) },
                            modifier = Modifier.fillMaxWidth(),
                            highlightLabel = if (index == 0) {
                                stringResource(R.string.routine_recommendation_best_fit)
                            } else {
                                null
                            },
                            highlightTestTag = if (index == 0) {
                                "training_recommendation_best_fit"
                            } else {
                                null
                            }
                        )
                    }
                    previewTemplate?.let { template ->
                        RoutinePreviewSchedule(
                            template = template,
                            exercises = state.exercises
                        )
                    }
                }
                Button(
                    onClick = onStartRoutine,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("training_start_preview_routine"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(R.string.routine_start_routine))
                }
            }
        }
    }
}

@Composable
internal fun DialogHeader(
    title: String,
    onDismissRequest: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onDismissRequest) {
            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.routine_close_detail))
        }
    }
}

@Composable
internal fun RoutinePreviewSchedule(
    template: PlanTemplate,
    exercises: List<Exercise>
) {
    val exercisesById = exercises.associateBy { it.id }
    Column(
        modifier = Modifier.testTag("training_routine_preview_${template.id}"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.routine_routine_preview_schedule),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        template.days.forEach { day ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, SmartTrainnerColors.Line),
                colors = CardDefaults.cardColors(containerColor = SmartTrainnerColors.SurfaceRaised)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = day.previewTitle(template.source),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = day.exercises
                            .mapNotNull { exercisesById[it.exerciseId]?.localizedName() }
                            .joinToString(" · "),
                        color = SmartTrainnerColors.Muted,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
internal fun TodayProgressLine(state: RoutineUiState) {
    val todaysExercises = state.plan?.days
        ?.firstOrNull { it.date == state.today }
        ?.exercises
        .orEmpty()
    val completed = todaysExercises.count { it.id in state.completedPlannedExerciseIds }
    val total = todaysExercises.size
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(R.string.routine_today_progress, completed, total),
            color = SmartTrainnerColors.Muted,
            style = MaterialTheme.typography.bodyMedium
        )
        SmartTrainnerProgressBar(
            progress = if (total == 0) 0f else completed.toFloat() / total,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
    }
}

@Composable
internal fun PlanTemplateCard(
    template: PlanTemplate,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cardTestTag: String = "training_template_card_${template.id}",
    sourceTestTag: String = template.source.routineSourceTag(),
    highlightLabel: String? = null,
    highlightTestTag: String? = null
) {
    Card(
        modifier = modifier
            .testTag(cardTestTag)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) SmartTrainnerColors.Coral else SmartTrainnerColors.Line
        ),
        colors = CardDefaults.cardColors(
            containerColor = SmartTrainnerColors.SurfaceRaised
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = template.localizedName(),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (selected) {
                    SmartTrainnerBadge(
                        text = stringResource(R.string.routine_selected),
                        icon = Icons.Default.CheckCircle,
                        containerColor = SmartTrainnerColors.GreenSoft,
                        contentColor = SmartTrainnerColors.Green
                    )
                }
                if (highlightLabel != null) {
                    SmartTrainnerBadge(
                        text = highlightLabel,
                        icon = Icons.Default.CheckCircle,
                        containerColor = SmartTrainnerColors.AmberSoft,
                        contentColor = SmartTrainnerColors.Ink,
                        modifier = highlightTestTag?.let { Modifier.testTag(it) } ?: Modifier
                    )
                }
            }
            RoutineSourceChip(template.source, sourceTestTag)
            RoutineTemplateBadgeRow(template)
            if (template.source == RoutineSource.SYSTEM) {
                Text(
                    text = template.structure.localizedLabel(),
                    color = SmartTrainnerColors.Ink,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            RoutineFocusFlow(template)
            Text(
                text = template.localizedDescription(),
                color = SmartTrainnerColors.Muted,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun RoutineTemplateBadgeRow(template: PlanTemplate) {
    val durationBadge = SmartTrainnerBadgeSpec(
        text = stringResource(R.string.routine_minutes_option, template.estimatedSessionMinutes),
        icon = Icons.Default.Timer,
        containerColor = SmartTrainnerColors.AmberSoft,
        contentColor = SmartTrainnerColors.Ink
    )
    val badges = if (template.source == RoutineSource.CUSTOM) {
        listOf(
            SmartTrainnerBadgeSpec(
                text = stringResource(R.string.routine_custom_template_meta, template.days.size),
                icon = Icons.Default.DateRange,
                containerColor = SmartTrainnerColors.GreenSoft,
                contentColor = SmartTrainnerColors.Ink
            ),
            durationBadge
        )
    } else {
        listOf(
            SmartTrainnerBadgeSpec(
                text = template.level.localizedLabel(),
                containerColor = SmartTrainnerColors.SteelSoft,
                contentColor = SmartTrainnerColors.Ink
            ),
            SmartTrainnerBadgeSpec(
                text = stringResource(R.string.routine_cycle_length_option, template.cycleLength),
                icon = Icons.Default.DateRange,
                containerColor = SmartTrainnerColors.GreenSoft,
                contentColor = SmartTrainnerColors.Ink
            ),
            durationBadge
        )
    }
    SmartTrainnerBadgeRow(
        badges = badges,
        maxItemsPerRow = 3
    )
}

@Composable
internal fun RoutineSourceChip(source: RoutineSource, testTag: String = source.routineSourceTag()) {
    SmartTrainnerBadge(
        modifier = Modifier.testTag(testTag),
        text = stringResource(
            if (source == RoutineSource.CUSTOM) {
                R.string.routine_custom_routine_badge
            } else {
                R.string.routine_default_routine_badge
            }
        ),
        icon = if (source == RoutineSource.CUSTOM) Icons.Default.Edit else Icons.Default.FitnessCenter,
        containerColor = if (source == RoutineSource.CUSTOM) SmartTrainnerColors.GreenSoft else SmartTrainnerColors.SteelSoft,
        contentColor = SmartTrainnerColors.Ink
    )
}

internal fun RoutineSource.routineSourceTag(): String = if (this == RoutineSource.CUSTOM) {
    "training_routine_source_custom"
} else {
    "training_routine_source_default"
}

internal fun RoutineSource.currentRoutineSourceTag(): String = if (this == RoutineSource.CUSTOM) {
    "training_current_routine_source_custom"
} else {
    "training_current_routine_source_default"
}

internal fun RoutineSource.homeRoutineSourceTag(): String = if (this == RoutineSource.CUSTOM) {
    "training_home_routine_source_custom"
} else {
    "training_home_routine_source_default"
}
