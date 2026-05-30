package com.smarttrainner.feature.training.impl

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.RoutineFocus
import com.smarttrainner.core.model.RoutineSource
import com.smarttrainner.core.model.WorkoutLog

internal fun androidx.compose.foundation.lazy.LazyListScope.planContent(
    state: TrainingUiState,
    onShowRoutineLibrary: () -> Unit,
    onCreateCustomRoutine: () -> Unit,
    onEditCustomRoutine: (String) -> Unit,
    onRecordSelected: (PlannedExercise) -> Unit
) {
    item {
        Text(
            text = stringResource(R.string.training_current_routine),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
    item {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val selectedTemplate = state.templates.firstOrNull { it.id == state.selectedTemplateId }
            if (selectedTemplate != null) {
                CurrentRoutineSummaryCard(
                    template = selectedTemplate,
                    onEditCustomRoutine = onEditCustomRoutine,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                EmptyState(text = stringResource(R.string.training_empty_plan))
            }
            Button(
                onClick = onShowRoutineLibrary,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("training_find_routine_button"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.training_change_routine))
            }
            OutlinedButton(
                onClick = onCreateCustomRoutine,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("training_create_custom_routine_button"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.training_create_custom_routine))
            }
        }
    }
    item {
        Text(
            text = stringResource(R.string.training_routine_schedule),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
    state.plan?.days.orEmpty().forEach { day ->
        item {
            DayPlanSection(
                title = day.title,
                focus = day.focus,
                primaryFocus = day.primaryFocus,
                dayNumber = day.dayNumber,
                exercises = day.exercises,
                weeklyLogs = state.logs,
                latestLogs = state.latestWorkoutLogs,
                completedIds = state.completedPlannedExerciseIds,
                onRecordSelected = onRecordSelected
            )
        }
    }
}

@Composable
internal fun CurrentRoutineSummaryCard(
    template: PlanTemplate,
    onEditCustomRoutine: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("training_current_routine_card"),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, SmartTrainnerColors.Line),
        colors = CardDefaults.cardColors(containerColor = SmartTrainnerColors.SurfaceRaised)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                RoutineSourceChip(template.source, template.source.currentRoutineSourceTag())
            }
            Text(
                text = template.localizedMeta(),
                color = SmartTrainnerColors.Coral,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            RoutineFocusFlow(template)
            if (template.source == RoutineSource.CUSTOM) {
                OutlinedButton(
                    onClick = { onEditCustomRoutine(template.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("training_edit_current_custom_routine"),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(R.string.training_edit_custom_routine))
                }
            }
        }
    }
}

@Composable
internal fun DayPlanSection(
    title: String,
    focus: String,
    primaryFocus: RoutineFocus?,
    dayNumber: Int,
    exercises: List<PlannedExercise>,
    weeklyLogs: List<com.smarttrainner.core.model.WorkoutLog>,
    latestLogs: List<com.smarttrainner.core.model.WorkoutLog>,
    completedIds: Set<com.smarttrainner.core.model.PlannedExerciseId>,
    onRecordSelected: (PlannedExercise) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = planDayScheduleTitle(title, dayNumber),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        val focusText = when {
            primaryFocus != null && (focus.isBlank() || focus == primaryFocus.name) -> {
                primaryFocus.localizedTodayFocusLabel()
            }
            focus.isNotBlank() -> focus.localizedPlanFocus()
            else -> null
        }
        focusText?.let {
            Text(text = it, style = MaterialTheme.typography.bodySmall, color = SmartTrainnerColors.Muted)
        }
        exercises.forEach { exercise ->
            PlanExerciseRow(
                exercise = exercise,
                displayLog = weeklyLogs.firstOrNull { it.plannedExerciseId == exercise.id }
                    ?: latestLogs.latestForExercise(exercise.exercise.id),
                completed = exercise.id in completedIds,
                onClick = { onRecordSelected(exercise) }
            )
        }
    }
}

@Composable
internal fun PlanExerciseRow(
    exercise: PlannedExercise,
    displayLog: com.smarttrainner.core.model.WorkoutLog?,
    completed: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("training_plan_exercise_${exercise.exercise.id.value}")
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, SmartTrainnerColors.Line),
        colors = CardDefaults.cardColors(containerColor = SmartTrainnerColors.SurfaceRaised)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TrainerExerciseImage(
                exercise = exercise.exercise,
                modifier = Modifier.size(width = 76.dp, height = 84.dp),
                cleanThumbnailCrop = true
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(exercise.exercise.localizedName(), fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(exercise.localizedTrainingDisplayText(displayLog), color = SmartTrainnerColors.Muted, style = MaterialTheme.typography.bodySmall)
            }
            if (completed) {
                StatusIcon(completed = true)
            } else {
                OutlinedButton(
                    onClick = onClick,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, SmartTrainnerColors.Coral),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("training_plan_record_button")
                ) {
                    Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(6.dp))
                    Text(stringResource(R.string.training_record_action), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
