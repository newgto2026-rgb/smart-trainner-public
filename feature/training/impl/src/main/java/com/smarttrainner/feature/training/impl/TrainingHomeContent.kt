package com.smarttrainner.feature.training.impl

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
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
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.RoutineSource

internal fun androidx.compose.foundation.lazy.LazyListScope.homeContent(
    state: TrainingUiState,
    onWorkoutStarted: (PlannedExercise) -> Unit,
    onCompleteRoutineDay: () -> Unit
) {
    item {
        SectionTitle(
            stringResourceId = R.string.training_today_title,
            testTag = "training_section_title_today"
        )
    }
    val nextRoutineDay = state.nextRoutineDayUi
    if (nextRoutineDay == null) {
        item { EmptyState(text = stringResource(R.string.training_empty_plan)) }
    } else {
        item {
            NextRoutineDayCard(
                routineDay = nextRoutineDay,
                onRecordSelected = onWorkoutStarted,
                onCompleteRoutineDay = onCompleteRoutineDay
            )
        }
    }
    state.formError?.takeIf { it == RecordFormError.COMPLETE_DAY_FAILED }?.let {
        item {
            Text(
                text = it.message(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
internal fun NextRoutineDayCard(
    routineDay: NextRoutineDayUiModel,
    onRecordSelected: (PlannedExercise) -> Unit,
    onCompleteRoutineDay: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("training_next_routine_day_card"),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, SmartTrainnerColors.Line),
        colors = CardDefaults.cardColors(containerColor = SmartTrainnerColors.SurfaceRaised)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            routineDay.routineTemplate?.let { template ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RoutineSourceChip(template.source, template.source.homeRoutineSourceTag())
                    Text(
                        text = template.localizedName(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("training_home_current_routine_name"),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = SmartTrainnerColors.Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val isCustomRoutine = routineDay.routineTemplate?.source == RoutineSource.CUSTOM
                val fallbackDayTitle = planDayDisplayTitle(routineDay.day.title, routineDay.dayNumber)
                val hasCustomDayTitle = routineDay.day.title
                    .hasMeaningfulPlanDayTitle(routineDay.dayNumber)
                val shouldShowCustomDayLabel = isCustomRoutine &&
                    (routineDay.primaryFocus != null || hasCustomDayTitle)
                Text(
                    text = routineDay.primaryFocus?.let { focus ->
                        stringResource(R.string.training_today_focus_title, focus.localizedTodayFocusLabel())
                    } ?: fallbackDayTitle,
                    modifier = Modifier.testTag("training_next_routine_day_${routineDay.dayNumber}"),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = SmartTrainnerColors.Ink
                )
                if (shouldShowCustomDayLabel) {
                    Text(
                        text = stringResource(R.string.training_day_label, routineDay.dayNumber),
                        color = SmartTrainnerColors.Coral,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = stringResource(
                            R.string.training_routine_day_subtitle,
                            routineDay.dayNumber,
                            routineDay.focus.localizedPlanFocus(),
                            routineDay.sessionMinutes
                        ),
                        modifier = Modifier.testTag("training_next_routine_time_estimate"),
                        color = SmartTrainnerColors.Coral,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            RoutineProgressBar(
                progress = if (routineDay.totalExerciseCount == 0) {
                    0f
                } else {
                    routineDay.completedExerciseCount.toFloat() / routineDay.totalExerciseCount
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )
            RoutineDayBadgeRow(routineDay)
            Text(
                text = stringResource(
                    R.string.training_routine_completed_progress,
                    routineDay.completedExerciseCount,
                    routineDay.totalExerciseCount
                ),
                color = SmartTrainnerColors.Muted,
                style = MaterialTheme.typography.bodySmall
            )
            val focusItems = (listOfNotNull(routineDay.primaryFocus) + routineDay.secondaryFocuses).distinct()
            if (focusItems.isNotEmpty()) {
                Column(
                    modifier = Modifier.testTag("training_next_routine_focus_section"),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.training_routine_main_focus),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    WrappedChipRows(
                        items = focusItems,
                        maxItemsPerRow = 4
                    ) { focus ->
                        TrainingBadge(
                            text = focus.localizedShortLabel(),
                            containerColor = SmartTrainnerColors.CoralSoft,
                            contentColor = SmartTrainnerColors.Ink,
                            modifier = Modifier.testTag("training_next_routine_focus_${focus.name}")
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val previewNames = routineDay.previewExercises.map { it.exercise.localizedName() }
                Text(
                    text = stringResource(R.string.training_routine_examples),
                    modifier = Modifier.testTag("training_next_routine_plan_title"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = previewNames.joinToString(" · "),
                    modifier = Modifier.testTag("training_next_routine_plan_exercises"),
                    color = SmartTrainnerColors.Muted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            routineDay.startExercise?.let { startExercise ->
                Button(
                    onClick = { onRecordSelected(startExercise) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("training_home_start_workout")
                ) {
                    Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(R.string.training_start_record))
                }
            }
            OutlinedButton(
                onClick = onCompleteRoutineDay,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("training_complete_routine_day"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.training_complete_routine_day))
            }
            routineDay.nextPrimaryFocus?.let { nextFocus ->
                Text(
                    text = stringResource(R.string.training_next_routine_day, nextFocus.localizedTodayFocusLabel()),
                    color = SmartTrainnerColors.Muted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
internal fun RoutineDayBadgeRow(routineDay: NextRoutineDayUiModel) {
    TrainingBadgeRow(
        badges = listOf(
            TrainingBadgeSpec(
                text = stringResource(R.string.training_routine_badge_duration, routineDay.sessionMinutes),
                icon = Icons.Default.Timer,
                containerColor = SmartTrainnerColors.CoralSoft,
                contentColor = SmartTrainnerColors.Ink,
                testTag = "training_next_routine_badge_duration"
            ),
            TrainingBadgeSpec(
                text = stringResource(R.string.training_routine_badge_exercises, routineDay.totalExerciseCount),
                icon = Icons.Default.FitnessCenter,
                containerColor = SmartTrainnerColors.GreenSoft,
                contentColor = SmartTrainnerColors.Ink,
                testTag = "training_next_routine_badge_exercises"
            ),
            TrainingBadgeSpec(
                text = stringResource(R.string.training_routine_badge_recovery, routineDay.minRecoveryHours),
                icon = Icons.Default.DateRange,
                containerColor = SmartTrainnerColors.AmberSoft,
                contentColor = SmartTrainnerColors.Ink,
                testTag = "training_next_routine_badge_recovery"
            )
        ),
        maxItemsPerRow = 3,
        modifier = Modifier.testTag("training_next_routine_badges")
    )
}
