package com.smarttrainner.feature.exercise.impl

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.smarttrainner.core.designsystem.SmartTrainnerColors
import com.smarttrainner.core.exercisemedia.TrainerExerciseImage
import com.smarttrainner.core.model.DifficultyLevel
import com.smarttrainner.core.model.EquipmentType
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.ExerciseSource
import com.smarttrainner.core.model.MuscleGroup

internal data class CustomExerciseFormActions(
    val onDismiss: () -> Unit,
    val onNameChanged: (String) -> Unit,
    val onMuscleGroupChanged: (MuscleGroup) -> Unit,
    val onEquipmentChanged: (EquipmentType) -> Unit,
    val onDifficultyChanged: (DifficultyLevel) -> Unit,
    val onImageUriChanged: (String) -> Unit,
    val onSummaryChanged: (String) -> Unit,
    val onSetsChanged: (String) -> Unit,
    val onRepStartChanged: (String) -> Unit,
    val onRepEndChanged: (String) -> Unit,
    val onDurationChanged: (String) -> Unit,
    val onRestChanged: (String) -> Unit,
    val onInstructionChanged: (Int, String) -> Unit,
    val onAddInstruction: () -> Unit,
    val onSafetyCueChanged: (Int, String) -> Unit,
    val onAddSafetyCue: () -> Unit,
    val onSave: () -> Unit
)

@Composable
internal fun CustomExerciseFormDialog(
    state: CustomExerciseFormUiState,
    actions: CustomExerciseFormActions
) {
    if (!state.visible) return
    Dialog(
        onDismissRequest = actions.onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 14.dp)
                .testTag("training_custom_exercise_form"),
            shape = RoundedCornerShape(8.dp),
            color = SmartTrainnerColors.SurfaceRaised
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, top = 8.dp, end = 4.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.exercise_custom_form_title),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = actions.onDismiss,
                        modifier = Modifier.testTag("training_custom_exercise_cancel")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.exercise_custom_cancel))
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    BasicInfoSection(state, actions)
                    ImageSection(state, actions)
                    TargetSection(state, actions)
                    DynamicTextSection(
                        title = stringResource(R.string.exercise_custom_instructions),
                        values = state.instructions,
                        inputTagPrefix = "training_custom_exercise_instruction",
                        addTag = "training_custom_exercise_add_instruction",
                        onValueChanged = actions.onInstructionChanged,
                        onAdd = actions.onAddInstruction
                    )
                    DynamicTextSection(
                        title = stringResource(R.string.exercise_custom_safety),
                        values = state.safetyCues,
                        inputTagPrefix = "training_custom_exercise_safety",
                        addTag = "training_custom_exercise_add_safety",
                        onValueChanged = actions.onSafetyCueChanged,
                        onAdd = actions.onAddSafetyCue
                    )
                    state.error?.let {
                        Text(
                            text = it.localizedMessage(),
                            modifier = Modifier.testTag("training_custom_exercise_error"),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Button(
                    onClick = actions.onSave,
                    enabled = !state.saving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                        .testTag("training_custom_exercise_save"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stringResource(if (state.saving) R.string.exercise_custom_saving else R.string.exercise_custom_save))
                }
            }
        }
    }
}

@Composable
private fun BasicInfoSection(
    state: CustomExerciseFormUiState,
    actions: CustomExerciseFormActions
) {
    FormSection(title = stringResource(R.string.exercise_custom_section_basic)) {
        OutlinedTextField(
            value = state.name,
            onValueChange = actions.onNameChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("training_custom_exercise_name_input"),
            label = { Text(stringResource(R.string.exercise_custom_name)) },
            singleLine = true,
            shape = RoundedCornerShape(8.dp)
        )
        CustomDropdown(
            label = stringResource(R.string.exercise_custom_category),
            selectedLabel = state.muscleGroup.localizedLabel(),
            options = MuscleGroup.entries.filterNot { it == MuscleGroup.ARMS },
            selectorTag = "training_custom_exercise_category_selector",
            optionTag = { "training_custom_exercise_category_option_${it.name}" },
            onOptionSelected = actions.onMuscleGroupChanged
        ) { it.localizedLabel() }
        CustomDropdown(
            label = stringResource(R.string.exercise_custom_equipment),
            selectedLabel = state.equipment.localizedLabel(),
            options = EquipmentType.entries,
            selectorTag = "training_custom_exercise_equipment_selector",
            optionTag = { "training_custom_exercise_equipment_option_${it.name}" },
            onOptionSelected = actions.onEquipmentChanged
        ) { it.localizedLabel() }
        CustomDropdown(
            label = stringResource(R.string.exercise_custom_difficulty),
            selectedLabel = state.difficulty.localizedLabel(),
            options = DifficultyLevel.entries,
            selectorTag = "training_custom_exercise_difficulty_selector",
            optionTag = { "training_custom_exercise_difficulty_option_${it.name}" },
            onOptionSelected = actions.onDifficultyChanged
        ) { it.localizedLabel() }
        OutlinedTextField(
            value = state.summary,
            onValueChange = actions.onSummaryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("training_custom_exercise_summary_input"),
            label = { Text(stringResource(R.string.exercise_custom_summary)) },
            minLines = 2,
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
private fun ImageSection(
    state: CustomExerciseFormUiState,
    actions: CustomExerciseFormActions
) {
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            actions.onImageUriChanged(uri.toString())
        }
    }
    FormSection(title = stringResource(R.string.exercise_custom_section_image)) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            TrainerExerciseImage(
                exercise = previewExercise(
                    state = state,
                    previewName = stringResource(R.string.exercise_custom_preview_name)
                ),
                modifier = Modifier
                    .size(width = 162.dp, height = 180.dp)
                    .testTag(
                        if (state.imageUri.isBlank()) {
                            "training_custom_exercise_default_image_preview"
                        } else {
                            "training_custom_exercise_image_preview"
                        }
                    ),
                cleanThumbnailCrop = true,
                contentDescription = state.name.ifBlank { stringResource(R.string.exercise_custom_preview_name) }
            )
        }
        OutlinedTextField(
            value = state.imageUri,
            onValueChange = actions.onImageUriChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("training_custom_exercise_image_url_input"),
            label = { Text(stringResource(R.string.exercise_custom_image_url)) },
            singleLine = true,
            shape = RoundedCornerShape(8.dp)
        )
        OutlinedButton(
            onClick = { picker.launch(arrayOf("image/*")) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("training_custom_exercise_image_picker"),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Image, contentDescription = null)
            Text(
                text = stringResource(R.string.exercise_custom_pick_image),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun TargetSection(
    state: CustomExerciseFormUiState,
    actions: CustomExerciseFormActions
) {
    FormSection(title = stringResource(R.string.exercise_custom_section_target)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumberField(
                value = state.defaultSets,
                onValueChange = actions.onSetsChanged,
                label = stringResource(R.string.exercise_custom_sets),
                tag = "training_custom_exercise_sets_input",
                modifier = Modifier.weight(1f)
            )
            NumberField(
                value = state.restSeconds,
                onValueChange = actions.onRestChanged,
                label = stringResource(R.string.exercise_custom_rest),
                tag = "training_custom_exercise_rest_input",
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumberField(
                value = state.repRangeStart,
                onValueChange = actions.onRepStartChanged,
                label = stringResource(R.string.exercise_custom_rep_start),
                tag = "training_custom_exercise_rep_start_input",
                modifier = Modifier.weight(1f)
            )
            NumberField(
                value = state.repRangeEnd,
                onValueChange = actions.onRepEndChanged,
                label = stringResource(R.string.exercise_custom_rep_end),
                tag = "training_custom_exercise_rep_end_input",
                modifier = Modifier.weight(1f)
            )
        }
        NumberField(
            value = state.defaultDurationMinutes,
            onValueChange = actions.onDurationChanged,
            label = stringResource(R.string.exercise_custom_duration),
            tag = "training_custom_exercise_duration_input",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    tag: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { raw -> onValueChange(raw.filter { it.isDigit() }) },
        modifier = modifier.testTag(tag),
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
private fun DynamicTextSection(
    title: String,
    values: List<String>,
    inputTagPrefix: String,
    addTag: String,
    onValueChanged: (Int, String) -> Unit,
    onAdd: () -> Unit
) {
    FormSection(title = title) {
        values.forEachIndexed { index, value ->
            OutlinedTextField(
                value = value,
                onValueChange = { onValueChanged(index, it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("${inputTagPrefix}_${index}_input"),
                label = { Text(stringResource(R.string.exercise_custom_step_number, index + 1)) },
                minLines = 2,
                shape = RoundedCornerShape(8.dp)
            )
        }
        OutlinedButton(
            onClick = onAdd,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(addTag),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text(
                text = stringResource(R.string.exercise_custom_add_line),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun FormSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        content()
    }
}

@Composable
private fun <T> CustomDropdown(
    label: String,
    selectedLabel: String,
    options: List<T>,
    selectorTag: String,
    optionTag: (T) -> String,
    onOptionSelected: (T) -> Unit,
    optionLabel: @Composable (T) -> String
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Surface(
            onClick = { expanded = true },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(selectorTag),
            shape = RoundedCornerShape(8.dp),
            color = SmartTrainnerColors.Surface,
            border = BorderStroke(1.dp, SmartTrainnerColors.Line)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(label, style = MaterialTheme.typography.labelSmall, color = SmartTrainnerColors.Muted)
                    Text(selectedLabel, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    modifier = Modifier.testTag(optionTag(option))
                )
            }
        }
    }
}

@Composable
private fun CustomExerciseFormError.localizedMessage(): String = stringResource(
    when (this) {
        CustomExerciseFormError.NAME -> R.string.exercise_custom_error_name
        CustomExerciseFormError.INSTRUCTIONS -> R.string.exercise_custom_error_instructions
        CustomExerciseFormError.SAFETY -> R.string.exercise_custom_error_safety
        CustomExerciseFormError.SETS -> R.string.exercise_custom_error_sets
        CustomExerciseFormError.TARGET -> R.string.exercise_custom_error_target
        CustomExerciseFormError.REPS -> R.string.exercise_custom_error_reps
        CustomExerciseFormError.DURATION -> R.string.exercise_custom_error_duration
        CustomExerciseFormError.REST -> R.string.exercise_custom_error_rest
        CustomExerciseFormError.SAVE -> R.string.exercise_custom_error_save
    }
)

private fun previewExercise(
    state: CustomExerciseFormUiState,
    previewName: String
): Exercise =
    Exercise(
        id = ExerciseId("custom_exercise_preview"),
        name = state.name.ifBlank { previewName },
        muscleGroup = state.muscleGroup,
        equipment = state.equipment,
        difficulty = state.difficulty,
        imageKey = "custom_exercise_preview",
        summary = state.summary,
        instructions = state.instructions,
        safetyCues = state.safetyCues,
        defaultSets = state.defaultSets.toIntOrNull() ?: 3,
        defaultRepRange = previewRepRange(state),
        defaultDurationMinutes = state.defaultDurationMinutes.toIntOrNull(),
        restSeconds = state.restSeconds.toIntOrNull() ?: 90,
        source = ExerciseSource.USER_CREATED,
        imageUri = state.imageUri.takeIf { it.isNotBlank() }
    )

private fun previewRepRange(state: CustomExerciseFormUiState): IntRange? {
    val start = state.repRangeStart.toIntOrNull()
    val end = state.repRangeEnd.toIntOrNull()
    return if (start != null && end != null) start..end else 8..12
}
