package com.smarttrainner.feature.analysis.impl

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.smarttrainner.core.designsystem.SmartTrainnerColors
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.WeeklySummary
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.ui.SmartTrainnerBadge
import com.smarttrainner.core.ui.SmartTrainnerBadgeRow
import com.smarttrainner.core.ui.SmartTrainnerBadgeSpec
import com.smarttrainner.core.ui.SmartTrainnerMetricTile
import com.smarttrainner.core.ui.SmartTrainnerProgressBar
import java.util.Locale

@Composable
internal fun AnalysisContent(state: AnalysisUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SummaryBand(summary = state.summary, cycleNumber = state.cycleNumber)
        if (state.recentLogs.isNotEmpty()) {
            RecentRecordsCard(records = state.recentLogs)
        }
        MuscleBalanceCard(summary = state.summary)
    }
}

@Composable
private fun RecentRecordsCard(
    records: List<RecentWorkoutLogUiModel>
) {
    var visibleRecordLimit by remember(records) { mutableStateOf(RECENT_RECORD_PREVIEW_LIMIT) }
    val visibleRecords = records.take(visibleRecordLimit)
    val canShowMore = visibleRecordLimit < records.size
    val canCollapse = visibleRecordLimit > RECENT_RECORD_PREVIEW_LIMIT
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
                    text = stringResource(R.string.analysis_recent_records),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                SmartTrainnerBadge(
                    text = stringResource(R.string.analysis_recent_records_count, records.size),
                    icon = Icons.Default.DateRange,
                    containerColor = SmartTrainnerColors.CoralSoft,
                    contentColor = SmartTrainnerColors.Ink,
                    modifier = Modifier.testTag("training_recent_records_count")
                )
            }
            visibleRecords.forEach { record ->
                RecentRecordItem(record = record)
            }
            if (canShowMore || canCollapse) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (canShowMore) {
                        TextButton(
                            onClick = {
                                visibleRecordLimit = (visibleRecordLimit + RECENT_RECORD_INCREMENT)
                                    .coerceAtMost(records.size)
                            },
                            modifier = Modifier.testTag("training_recent_records_show_more")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(text = stringResource(R.string.analysis_recent_records_show_more))
                        }
                    }
                    if (canCollapse) {
                        TextButton(
                            onClick = { visibleRecordLimit = RECENT_RECORD_PREVIEW_LIMIT },
                            modifier = Modifier.testTag("training_recent_records_collapse")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExpandLess,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(text = stringResource(R.string.analysis_recent_records_collapse))
                        }
                    }
                }
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
                    text = record.exercise.displayName(log),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                SmartTrainnerBadge(
                    text = stringResource(
                        R.string.analysis_recent_record_date,
                        log.performedAt.monthValue,
                        log.performedAt.dayOfMonth
                    ),
                    containerColor = SmartTrainnerColors.SteelSoft,
                    contentColor = SmartTrainnerColors.Ink
                )
            }
            SmartTrainnerBadgeRow(
                badges = log.metricBadges(),
                maxItemsPerRow = 3
            )
        }
    }
}

@Composable
private fun WorkoutLog.metricBadges(): List<SmartTrainnerBadgeSpec> = buildList {
    add(
        SmartTrainnerBadgeSpec(
            text = stringResource(R.string.analysis_record_metric_sets, sets),
            icon = Icons.Default.FitnessCenter,
            containerColor = SmartTrainnerColors.GreenSoft,
            contentColor = SmartTrainnerColors.Ink
        )
    )
    reps?.let { value ->
        add(
            SmartTrainnerBadgeSpec(
                text = stringResource(R.string.analysis_record_metric_reps, value),
                containerColor = SmartTrainnerColors.CoralSoft,
                contentColor = SmartTrainnerColors.Ink
            )
        )
    }
    weightKg?.let { value ->
        add(
            SmartTrainnerBadgeSpec(
                text = stringResource(R.string.analysis_record_metric_weight, value),
                containerColor = SmartTrainnerColors.SteelSoft,
                contentColor = SmartTrainnerColors.Ink
            )
        )
    }
    durationMinutes?.let { value ->
        add(
            SmartTrainnerBadgeSpec(
                text = stringResource(R.string.analysis_record_metric_duration, value),
                icon = Icons.Default.Timer,
                containerColor = SmartTrainnerColors.AmberSoft,
                contentColor = SmartTrainnerColors.Ink
            )
        )
    }
}

@Composable
private fun SummaryBand(
    summary: WeeklySummary?,
    cycleNumber: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("training_summary_band"),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, SmartTrainnerColors.Line),
        colors = CardDefaults.cardColors(containerColor = SmartTrainnerColors.SurfaceRaised)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnalysisSectionHeader(title = stringResource(R.string.analysis_cycle_summary, cycleNumber))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmartTrainnerMetricTile(
                    label = stringResource(R.string.analysis_completion_rate),
                    value = "${summary?.completionRate ?: 0}%",
                    accent = SmartTrainnerColors.Coral,
                    modifier = Modifier.weight(1f)
                )
                SmartTrainnerMetricTile(
                    label = stringResource(R.string.analysis_total_sets),
                    value = "${summary?.totalSets ?: 0}",
                    accent = SmartTrainnerColors.Green,
                    modifier = Modifier.weight(1f)
                )
                SmartTrainnerMetricTile(
                    label = stringResource(R.string.analysis_streak),
                    value = stringResource(R.string.analysis_days_value, summary?.streakDays ?: 0),
                    accent = SmartTrainnerColors.Amber,
                    modifier = Modifier.weight(1f)
                )
            }
            val insightText = summary?.insightText() ?: stringResource(R.string.analysis_insight_no_logs)
            Text(
                text = insightText,
                color = SmartTrainnerColors.Ink,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private const val RECENT_RECORD_PREVIEW_LIMIT = 3
private const val RECENT_RECORD_INCREMENT = 10

@Composable
private fun MuscleBalanceCard(summary: WeeklySummary?) {
    AnalysisSectionCard {
        AnalysisSectionHeader(title = stringResource(R.string.analysis_muscle_balance))
        if (summary == null || summary.muscleBalance.isEmpty()) {
            Text(
                text = stringResource(R.string.analysis_empty_logs),
                color = SmartTrainnerColors.Muted,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            val maxMuscleBalance = summary.muscleBalance.values.maxOrNull() ?: 1
            summary.muscleBalance.entries.forEach { entry ->
                MuscleBalanceRow(
                    label = entry.key.localizedLabel(),
                    count = entry.value,
                    max = maxMuscleBalance
                )
            }
            InsightCard(text = summary.insightText())
        }
    }
}

@Composable
private fun AnalysisSectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, SmartTrainnerColors.Line),
        colors = CardDefaults.cardColors(containerColor = SmartTrainnerColors.SurfaceRaised)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
private fun AnalysisSectionHeader(title: String) {
    Text(
        text = title,
        color = SmartTrainnerColors.Ink,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun MuscleBalanceRow(
    label: String,
    count: Int,
    max: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row {
            Text(label, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.analysis_count_value, count), color = SmartTrainnerColors.Muted)
        }
        SmartTrainnerProgressBar(
            progress = count.toFloat() / max.coerceAtLeast(1),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
    }
}

@Composable
private fun InsightCard(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = SmartTrainnerColors.AmberSoft
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = SmartTrainnerColors.Ink
        )
    }
}

@Composable
private fun WeeklySummary.insightText(): String {
    if (isKoreanLocale()) return insight
    val weakestMuscle = remember(muscleBalance) {
        MuscleGroup.entries
            .filterNot {
                it == MuscleGroup.CARDIO ||
                    it == MuscleGroup.ARMS ||
                    it == MuscleGroup.FULL_BODY
            }
            .minByOrNull { muscleBalance[it] ?: 0 }
    }
    return when {
        plannedExerciseCount == 0 -> stringResource(R.string.analysis_insight_empty_plan)
        completedExerciseCount == 0 -> stringResource(R.string.analysis_insight_no_logs)
        completionRate >= 80 -> stringResource(R.string.analysis_insight_good_rate)
        totalVolumeKg > 0 && weakestMuscle != null -> stringResource(
            R.string.analysis_insight_balance,
            weakestMuscle.localizedLabel()
        )
        else -> stringResource(R.string.analysis_insight_steady)
    }
}

@Composable
private fun MuscleGroup.localizedLabel(): String = stringResource(
    when (this) {
        MuscleGroup.LOWER_BODY -> R.string.analysis_muscle_lower_body
        MuscleGroup.BACK -> R.string.analysis_muscle_back
        MuscleGroup.CHEST -> R.string.analysis_muscle_chest
        MuscleGroup.SHOULDERS -> R.string.analysis_muscle_shoulders
        MuscleGroup.ARMS -> R.string.analysis_muscle_arms
        MuscleGroup.BICEPS -> R.string.analysis_muscle_biceps
        MuscleGroup.TRICEPS -> R.string.analysis_muscle_triceps
        MuscleGroup.FOREARMS -> R.string.analysis_muscle_forearms
        MuscleGroup.CORE -> R.string.analysis_muscle_core
        MuscleGroup.CARDIO -> R.string.analysis_muscle_cardio
        MuscleGroup.FULL_BODY -> R.string.analysis_muscle_full_body
    }
)

@Composable
private fun isKoreanLocale(): Boolean =
    LocalConfiguration.current.locales[0]?.language == Locale.KOREAN.language

private fun Exercise?.displayName(log: WorkoutLog): String =
    this?.name ?: log.exerciseId.value.toExerciseTitle()

private fun String.toExerciseTitle(): String =
    split("_", "-")
        .filter { it.isNotBlank() }
        .joinToString(" ") { part ->
            part.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase(Locale.ENGLISH) else char.toString()
            }
        }
