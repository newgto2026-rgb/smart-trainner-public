package com.smarttrainner.feature.routine.impl

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
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
import com.smarttrainner.core.exercisemedia.ExerciseMediaRenderer
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.RoutineFocus
import com.smarttrainner.core.model.RoutineSource
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.toRecommendedDisplayRepRange
import com.smarttrainner.core.ui.SmartTrainnerBadgeSpec
import com.smarttrainner.core.ui.SmartTrainnerEmptyState
import com.smarttrainner.core.ui.SmartTrainnerMetricCluster

internal fun LazyListScope.planContent(
    state: RoutineUiState,
    actions: RoutineActions,
    exerciseMediaRenderer: ExerciseMediaRenderer
) {
    val cycleLogsByPlannedExerciseId = state.logs.firstByPlannedExerciseId()
    val latestLogsByExerciseId = state.latestWorkoutLogs.latestByExerciseId()
    val currentDayExercisesById = state.nextRoutineDayUi
        ?.previewExercises
        ?.associateBy { it.id }
        .orEmpty()

    item {
        Text(
            text = stringResource(R.string.routine_current_routine),
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
                    onEditCustomRoutine = actions.onEditCustomRoutine,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                SmartTrainnerEmptyState(text = stringResource(R.string.routine_empty_plan))
            }
            Button(
                onClick = actions.onShowLibrary,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("training_find_routine_button"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.routine_change_routine))
            }
            OutlinedButton(
                onClick = actions.onCreateCustomRoutine,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("training_create_custom_routine_button"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.routine_create_custom_routine))
            }
        }
    }
    item {
        Text(
            text = stringResource(R.string.routine_routine_schedule),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
    state.plan?.days.orEmpty().forEachIndexed { dayIndex, day ->
        item(
            key = "routine-day-${day.date}-header",
            contentType = "routine-day-header"
        ) {
            DayPlanHeader(
                title = day.title,
                focus = day.focus,
                primaryFocus = day.primaryFocus,
                dayNumber = day.dayNumber
            )
        }
        items(
            items = day.exercises,
            key = { it.id.value },
            contentType = { "routine-plan-exercise" }
        ) { exercise ->
            val recordableExercise = currentDayExercisesById[exercise.id]
            val completed = state.isPlanExerciseCompleted(dayIndex, exercise)
            PlanExerciseRow(
                exercise = recordableExercise ?: exercise,
                displayLog = cycleLogsByPlannedExerciseId[exercise.id]
                    ?: latestLogsByExerciseId[exercise.exercise.id],
                completed = completed,
                recordable = recordableExercise != null && !completed,
                exerciseMediaRenderer = exerciseMediaRenderer,
                onClick = { recordableExercise?.let(actions.onRecordSelected) }
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
            RoutineTemplateBadgeRow(template)
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
                    Text(stringResource(R.string.routine_edit_custom_routine))
                }
            }
        }
    }
}

@Composable
internal fun DayPlanHeader(
    title: String,
    focus: String,
    primaryFocus: RoutineFocus?,
    dayNumber: Int
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
    }
}

@Composable
internal fun PlanExerciseRow(
    exercise: PlannedExercise,
    displayLog: com.smarttrainner.core.model.WorkoutLog?,
    completed: Boolean,
    recordable: Boolean,
    exerciseMediaRenderer: ExerciseMediaRenderer,
    onClick: () -> Unit
) {
    val cardModifier = Modifier
        .fillMaxWidth()
        .testTag("training_plan_exercise_${exercise.exercise.id.value}")
        .let { modifier ->
            if (recordable) {
                modifier.clickable(onClick = onClick)
            } else {
                modifier
            }
        }
    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, SmartTrainnerColors.Line),
        colors = CardDefaults.cardColors(containerColor = SmartTrainnerColors.SurfaceRaised)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            exerciseMediaRenderer.Image(
                exercise = exercise.exercise,
                modifier = Modifier.size(width = 76.dp, height = 84.dp),
                stepIndex = null,
                cleanThumbnailCrop = true,
                contentDescription = null
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text(
                    text = exercise.exercise.localizedName(),
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                SmartTrainnerMetricCluster(
                    label = stringResource(
                        if (displayLog == null) {
                            R.string.routine_metric_recommended
                        } else {
                            R.string.routine_metric_latest
                        }
                    ),
                    metrics = exercise.trainingMetricBadges(displayLog),
                    maxItemsPerRow = 2,
                    labelContainerColor = if (displayLog == null) {
                        SmartTrainnerColors.CoralSoft
                    } else {
                        SmartTrainnerColors.SteelSoft
                    }
                )
            }
            if (completed) {
                StatusIcon(completed = true)
            } else if (recordable) {
                OutlinedButton(
                    onClick = onClick,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, SmartTrainnerColors.Coral),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("training_plan_record_button")
                ) {
                    Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(6.dp))
                    Text(stringResource(R.string.routine_record_action), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun PlannedExercise.trainingMetricBadges(
    displayLog: WorkoutLog?
): List<SmartTrainnerBadgeSpec> = displayLog?.recordMetricBadges() ?: buildList {
    add(
        SmartTrainnerBadgeSpec(
            text = stringResource(R.string.routine_set_number, sets),
            icon = Icons.Default.FitnessCenter,
            containerColor = SmartTrainnerColors.GreenSoft,
            contentColor = SmartTrainnerColors.Ink
        )
    )
    val reps = repRange
    if (reps != null) {
        val displayReps = reps.toRecommendedDisplayRepRange()
        add(
            SmartTrainnerBadgeSpec(
                text = stringResource(
                    R.string.routine_actual_reps,
                    "${displayReps.first}-${displayReps.last}"
                ),
                containerColor = SmartTrainnerColors.CoralSoft,
                contentColor = SmartTrainnerColors.Ink
            )
        )
    } else {
        add(
            SmartTrainnerBadgeSpec(
                text = stringResource(R.string.routine_actual_duration, (durationMinutes ?: 10).toString()),
                icon = Icons.Default.Timer,
                containerColor = SmartTrainnerColors.AmberSoft,
                contentColor = SmartTrainnerColors.Ink
            )
        )
    }
    add(
        SmartTrainnerBadgeSpec(
            text = stringResource(R.string.routine_actual_rest, restSeconds.toString()),
            icon = Icons.Default.Timer,
            containerColor = SmartTrainnerColors.SteelSoft,
            contentColor = SmartTrainnerColors.Ink
        )
    )
    if (estimatedMinutes > 0) {
        add(
            SmartTrainnerBadgeSpec(
                text = stringResource(R.string.routine_estimated_duration, estimatedMinutes),
                icon = Icons.Default.Timer,
                containerColor = SmartTrainnerColors.AmberSoft,
                contentColor = SmartTrainnerColors.Ink
            )
        )
    }
}

@Composable
private fun WorkoutLog.recordMetricBadges(): List<SmartTrainnerBadgeSpec> {
    val entries = displaySetEntries()
    val reps = entries.mapNotNull { it.reps }.toCollapsedText()
    val weights = entries.mapNotNull { it.weightKg }.map { it.toRecordInput() }.toCollapsedText()
    val durations = entries.mapNotNull { it.durationMinutes }.toCollapsedText()
    val rests = entries.mapNotNull { it.restSeconds }.toCollapsedText()
    return buildList {
        add(
            SmartTrainnerBadgeSpec(
                text = stringResource(R.string.routine_set_number, entries.size.coerceAtLeast(sets)),
                icon = Icons.Default.FitnessCenter,
                containerColor = SmartTrainnerColors.GreenSoft,
                contentColor = SmartTrainnerColors.Ink
            )
        )
        reps?.let {
            add(
                SmartTrainnerBadgeSpec(
                    text = stringResource(R.string.routine_actual_reps, it),
                    containerColor = SmartTrainnerColors.CoralSoft,
                    contentColor = SmartTrainnerColors.Ink
                )
            )
        }
        weights?.let {
            add(
                SmartTrainnerBadgeSpec(
                    text = stringResource(R.string.routine_actual_weight, it),
                    containerColor = SmartTrainnerColors.SteelSoft,
                    contentColor = SmartTrainnerColors.Ink
                )
            )
        }
        durations?.let {
            add(
                SmartTrainnerBadgeSpec(
                    text = stringResource(R.string.routine_actual_duration, it),
                    icon = Icons.Default.Timer,
                    containerColor = SmartTrainnerColors.AmberSoft,
                    contentColor = SmartTrainnerColors.Ink
                )
            )
        }
        rests?.let {
            add(
                SmartTrainnerBadgeSpec(
                    text = stringResource(R.string.routine_actual_rest, it),
                    icon = Icons.Default.Timer,
                    containerColor = SmartTrainnerColors.SteelSoft,
                    contentColor = SmartTrainnerColors.Ink
                )
            )
        }
    }
}

private fun List<WorkoutLog>.firstByPlannedExerciseId(): Map<PlannedExerciseId, WorkoutLog> =
    buildMap {
        for (log in this@firstByPlannedExerciseId) {
            if (log.plannedExerciseId !in this) {
                put(log.plannedExerciseId, log)
            }
        }
    }

private fun List<WorkoutLog>.latestByExerciseId(): Map<ExerciseId, WorkoutLog> =
    buildMap {
        for (log in this@latestByExerciseId) {
            val current = get(log.exerciseId)
            if (current == null || log.performedAt > current.performedAt) {
                put(log.exerciseId, log)
            }
        }
    }
