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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smarttrainner.core.designsystem.SmartTrainnerColors
import com.smarttrainner.core.designsystem.SmartTrainnerGradients
import com.smarttrainner.core.model.WeeklySummary

internal fun androidx.compose.foundation.lazy.LazyListScope.analysisContent(
    state: TrainingUiState
) {
    item {
        SummaryBand(state.summary)
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
                MetricTile(
                    label = stringResource(R.string.training_completion_rate),
                    value = "${summary?.completionRate ?: 0}%",
                    accent = SmartTrainnerColors.Coral,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = stringResource(R.string.training_total_sets),
                    value = "${summary?.totalSets ?: 0}",
                    accent = SmartTrainnerColors.Green,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
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
internal fun MetricTile(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = SmartTrainnerColors.SurfaceRaised.copy(alpha = 0.84f),
        border = BorderStroke(1.dp, SmartTrainnerColors.Line)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = value, color = accent, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text = label, color = SmartTrainnerColors.Muted, style = MaterialTheme.typography.labelMedium)
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
