package com.smarttrainner.feature.training.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smarttrainner.core.designsystem.SmartTrainnerColors
import com.smarttrainner.core.model.RoutineSource

@Composable
internal fun RoutineProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = SmartTrainnerColors.Green,
    trackColor: Color = SmartTrainnerColors.Line
) {
    val boundedProgress = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .background(trackColor, RoundedCornerShape(8.dp))
    ) {
        if (boundedProgress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(boundedProgress)
                    .fillMaxHeight()
                    .background(color, RoundedCornerShape(8.dp))
            )
        }
    }
}

@Composable
internal fun <T> WrappedChipRows(
    items: List<T>,
    maxItemsPerRow: Int,
    content: @Composable (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(maxItemsPerRow.coerceAtLeast(1)).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { item ->
                    content(item)
                }
            }
        }
    }
}

@Composable
internal fun RoutineSourceChip(source: RoutineSource, testTag: String = source.routineSourceTag()) {
    TrainingBadge(
        modifier = Modifier.testTag(testTag),
        text = stringResource(
            if (source == RoutineSource.CUSTOM) {
                R.string.training_custom_routine_badge
            } else {
                R.string.training_default_routine_badge
            }
        ),
        icon = if (source == RoutineSource.CUSTOM) Icons.Default.Edit else Icons.Default.FitnessCenter,
        containerColor = if (source == RoutineSource.CUSTOM) SmartTrainnerColors.GreenSoft else SmartTrainnerColors.CoralSoft,
        contentColor = SmartTrainnerColors.Ink
    )
}

internal fun RoutineSource.routineSourceTag(): String = if (this == RoutineSource.CUSTOM) {
    "training_routine_source_custom"
} else {
    "training_routine_source_default"
}

internal fun RoutineSource.homeRoutineSourceTag(): String = if (this == RoutineSource.CUSTOM) {
    "training_home_routine_source_custom"
} else {
    "training_home_routine_source_default"
}
