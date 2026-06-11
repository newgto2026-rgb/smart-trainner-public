package com.smarttrainner.feature.calendar.impl.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.smarttrainner.core.designsystem.SmartTrainnerColors
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.WorkoutSetLog
import com.smarttrainner.core.ui.SmartTrainnerBadgeRow
import com.smarttrainner.core.ui.SmartTrainnerBadgeSpec
import com.smarttrainner.feature.calendar.impl.CalendarSelectedWorkoutUiModel
import com.smarttrainner.feature.calendar.impl.R

@Composable
internal fun CalendarAgendaItem(
    workout: CalendarSelectedWorkoutUiModel
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("calendar_day_workout_item_${workout.id.value}"),
        shape = RoundedCornerShape(8.dp),
        color = if (workout.completed) SmartTrainnerColors.SurfaceRaised else SmartTrainnerColors.Surface,
        border = BorderStroke(1.dp, SmartTrainnerColors.Line)
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
                Icon(
                    imageVector = if (workout.completed) Icons.Default.Check else Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = if (workout.completed) SmartTrainnerColors.Green else SmartTrainnerColors.Coral
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = workout.exerciseName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = SmartTrainnerColors.Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                workout.muscleGroup?.let { muscleGroup ->
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = SmartTrainnerColors.CoralSoft
                    ) {
                        Text(
                            text = muscleGroup.displayLabel(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = SmartTrainnerColors.Ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            SmartTrainnerBadgeRow(
                badges = workout.metricBadges(),
                maxItemsPerRow = 3
            )
            if (workout.memo.isNotBlank()) {
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = workout.memo,
                    style = MaterialTheme.typography.bodySmall,
                    color = SmartTrainnerColors.Muted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CalendarSelectedWorkoutUiModel.metricBadges(): List<SmartTrainnerBadgeSpec> = buildList {
    add(
        SmartTrainnerBadgeSpec(
            text = pluralStringResource(
                R.plurals.calendar_record_metric_sets,
                sets,
                sets
            ),
            icon = Icons.Default.FitnessCenter,
            containerColor = SmartTrainnerColors.GreenSoft,
            contentColor = SmartTrainnerColors.Ink
        )
    )
    reps?.let { value ->
        add(
            SmartTrainnerBadgeSpec(
                text = pluralStringResource(
                    R.plurals.calendar_record_metric_reps,
                    value,
                    value
                ),
                containerColor = SmartTrainnerColors.CoralSoft,
                contentColor = SmartTrainnerColors.Ink
            )
        )
    }
    displayWeightText()?.let { value ->
        add(
            SmartTrainnerBadgeSpec(
                text = stringResource(R.string.calendar_record_metric_weight, value),
                containerColor = SmartTrainnerColors.SteelSoft,
                contentColor = SmartTrainnerColors.Ink
            )
        )
    }
    if (volumeKg > 0.0) {
        add(
            SmartTrainnerBadgeSpec(
                text = stringResource(R.string.calendar_record_metric_volume, volumeKg),
                containerColor = SmartTrainnerColors.SteelSoft,
                contentColor = SmartTrainnerColors.Ink
            )
        )
    }
}

private fun CalendarSelectedWorkoutUiModel.displayWeightText(): String? =
    displaySetWeights()
        .takeIf { it.isNotEmpty() }
        ?.joinToString("/") { it.toRecordInput() }

private fun CalendarSelectedWorkoutUiModel.displaySetWeights(): List<Double> =
    setEntries.weightEntries().ifEmpty {
        val weight = weightKg ?: return@ifEmpty emptyList()
        List(sets.coerceAtLeast(1)) { weight }
    }

private fun List<WorkoutSetLog>.weightEntries(): List<Double> =
    sortedBy { it.order }.mapNotNull { it.weightKg }

private fun Double.toRecordInput(): String =
    if (rem(1.0) == 0.0) toLong().toString() else toString()

private fun MuscleGroup.displayLabel(): String = displayName
