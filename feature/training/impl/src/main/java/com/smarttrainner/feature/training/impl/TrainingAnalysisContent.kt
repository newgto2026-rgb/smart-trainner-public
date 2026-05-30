package com.smarttrainner.feature.training.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.smarttrainner.core.designsystem.SmartTrainnerColors
import com.smarttrainner.core.designsystem.SmartTrainnerGradients
import com.smarttrainner.core.model.WeeklySummary
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.ui.SmartTrainnerMetricTile
import com.smarttrainner.feature.analysis.api.AnalysisUiState
import com.smarttrainner.feature.analysis.api.RecentWorkoutLogUiModel

internal fun androidx.compose.foundation.lazy.LazyListScope.analysisContent(
    state: AnalysisUiState
) {
    item {
        SummaryBand(state.summary)
    }
    if (state.recentLogs.isNotEmpty()) {
        item {
            RecentRecordsCard(records = state.recentLogs)
        }
    }
    item {
        Text(
            text = stringResource(R.string.training_muscle_balance),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
    val summary = state.summary
    if (summary == null || summary.muscleBalance.isEmpty()) {
        item { EmptyState(text = stringResource(R.string.training_empty_logs)) }
    } else {
        items(summary.muscleBalance.entries.toList(), key = { it.key.name }) { entry ->
            MuscleBalanceRow(
                label = entry.key.localizedLabel(),
                count = entry.value,
                max = summary.muscleBalance.values.maxOrNull() ?: 1
            )
        }
        item {
            InsightCard(text = summary.insight)
        }
    }
}

@Composable
internal fun RecentRecordsCard(
    records: List<RecentWorkoutLogUiModel>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("training_recent_records_card"),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, SmartTrainnerColors.Line),
        colors = CardDefaults.cardColors(containerColor = SmartTrainnerColors.SurfaceRaised)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.training_recent_records),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                TrainingBadge(
                    text = stringResource(R.string.training_recent_records_count, records.size),
                    icon = Icons.Default.DateRange,
                    containerColor = SmartTrainnerColors.CoralSoft,
                    contentColor = SmartTrainnerColors.Ink,
                    modifier = Modifier.testTag("training_recent_records_count")
                )
            }
            records.forEach { record ->
                RecentRecordItem(record = record)
            }
        }
    }
}

@Composable
private fun RecentRecordItem(
    record: RecentWorkoutLogUiModel
) {
    val log = record.log
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = SmartTrainnerColors.Surface,
        border = BorderStroke(1.dp, SmartTrainnerColors.Line)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.exercise?.localizedName() ?: log.exerciseId.value.toExerciseTitle(),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                TrainingBadge(
                    text = stringResource(
                        R.string.training_recent_record_date,
                        log.performedAt.monthValue,
                        log.performedAt.dayOfMonth
                    ),
                    containerColor = SmartTrainnerColors.SteelSoft,
                    contentColor = SmartTrainnerColors.Ink
                )
            }
            TrainingBadgeRow(
                badges = log.metricBadges(),
                maxItemsPerRow = 3
            )
        }
    }
}

@Composable
private fun WorkoutLog.metricBadges(): List<TrainingBadgeSpec> = buildList {
    add(
        TrainingBadgeSpec(
            text = stringResource(R.string.training_record_metric_sets, sets),
            icon = Icons.Default.FitnessCenter,
            containerColor = SmartTrainnerColors.GreenSoft,
            contentColor = SmartTrainnerColors.Ink
        )
    )
    reps?.let { value ->
        add(
            TrainingBadgeSpec(
                text = stringResource(R.string.training_record_metric_reps, value),
                containerColor = SmartTrainnerColors.CoralSoft,
                contentColor = SmartTrainnerColors.Ink
            )
        )
    }
    weightKg?.let { value ->
        add(
            TrainingBadgeSpec(
                text = stringResource(R.string.training_record_metric_weight, value),
                containerColor = SmartTrainnerColors.SteelSoft,
                contentColor = SmartTrainnerColors.Ink
            )
        )
    }
    durationMinutes?.let { value ->
        add(
            TrainingBadgeSpec(
                text = stringResource(R.string.training_record_metric_duration, value),
                icon = Icons.Default.Timer,
                containerColor = SmartTrainnerColors.AmberSoft,
                contentColor = SmartTrainnerColors.Ink
            )
        )
    }
}

@Composable
internal fun SummaryBand(summary: WeeklySummary?) {
    Card(
        modifier = Modifier.testTag("training_summary_band"),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, SmartTrainnerColors.Line),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .background(SmartTrainnerGradients.brandLight())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.training_week_summary),
                color = SmartTrainnerColors.Ink,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmartTrainnerMetricTile(
                    label = stringResource(R.string.training_completion_rate),
                    value = "${summary?.completionRate ?: 0}%",
                    accent = SmartTrainnerColors.Coral,
                    modifier = Modifier.weight(1f)
                )
                SmartTrainnerMetricTile(
                    label = stringResource(R.string.training_total_sets),
                    value = "${summary?.totalSets ?: 0}",
                    accent = SmartTrainnerColors.Green,
                    modifier = Modifier.weight(1f)
                )
                SmartTrainnerMetricTile(
                    label = stringResource(R.string.training_streak),
                    value = stringResource(R.string.training_days_value, summary?.streakDays ?: 0),
                    accent = SmartTrainnerColors.Amber,
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                text = summary?.localizedInsight() ?: stringResource(R.string.training_empty_logs),
                color = SmartTrainnerColors.Ink,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
internal fun MuscleBalanceRow(
    label: String,
    count: Int,
    max: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row {
            Text(label, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.training_count_value, count), color = SmartTrainnerColors.Muted)
        }
        RoutineProgressBar(
            progress = count.toFloat() / max.coerceAtLeast(1),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
    }
}

@Composable
internal fun InsightCard(text: String) {
    Surface(shape = RoundedCornerShape(8.dp), color = SmartTrainnerColors.AmberSoft) {
        Text(
            text = text,
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = SmartTrainnerColors.Ink
        )
    }
}
