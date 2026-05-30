package com.smarttrainner.feature.training.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.smarttrainner.core.designsystem.SmartTrainnerColors
import com.smarttrainner.core.designsystem.SmartTrainnerGradients
import com.smarttrainner.core.ui.SmartTrainnerEmptyState
import com.smarttrainner.core.ui.SmartTrainnerSectionTitle
import com.smarttrainner.core.ui.SmartTrainnerStatusChip
import com.smarttrainner.core.ui.SmartTrainnerStatusIcon
import com.smarttrainner.core.ui.SmartTrainnerUiTone

@Composable
internal fun Header(state: TrainingUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .background(SmartTrainnerGradients.brandLight(), RoundedCornerShape(8.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.training_app_title),
                    modifier = Modifier.testTag("training_app_title"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = SmartTrainnerColors.Ink
                )
                Text(
                    text = state.plan?.localizedName().orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SmartTrainnerColors.Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
internal fun StatusChip(completed: Boolean) {
    SmartTrainnerStatusChip(
        label = stringResource(if (completed) R.string.training_completed else R.string.training_incomplete),
        tone = if (completed) SmartTrainnerUiTone.Success else SmartTrainnerUiTone.Neutral
    )
}

@Composable
internal fun StatusIcon(completed: Boolean) {
    SmartTrainnerStatusIcon(
        completed = completed,
        contentDescription = stringResource(
            if (completed) R.string.training_completed else R.string.training_incomplete
        )
    )
}

@Composable
internal fun SectionTitle(stringResourceId: Int, testTag: String) {
    SmartTrainnerSectionTitle(
        text = stringResource(stringResourceId),
        modifier = Modifier.testTag(testTag)
    )
}

@Composable
internal fun EmptyState(text: String) {
    SmartTrainnerEmptyState(text = text)
}

internal data class TrainingBadgeSpec(
    val text: String,
    val icon: ImageVector? = null,
    val containerColor: Color = SmartTrainnerColors.SteelSoft,
    val contentColor: Color = SmartTrainnerColors.Ink,
    val borderColor: Color? = null,
    val testTag: String? = null
)

@Composable
internal fun TrainingBadge(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    containerColor: Color = SmartTrainnerColors.SteelSoft,
    contentColor: Color = SmartTrainnerColors.Ink,
    borderColor: Color? = null
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
        border = borderColor?.let { BorderStroke(1.dp, it) }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = contentColor
                )
            }
            Text(
                text = text,
                color = contentColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun TrainingBadgeRow(
    badges: List<TrainingBadgeSpec>,
    maxItemsPerRow: Int,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val rowLimit = if (maxWidth < 360.dp) {
            maxItemsPerRow.coerceAtMost(2)
        } else {
            maxItemsPerRow
        }.coerceAtLeast(1)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            badges.chunked(rowLimit).forEach { rowBadges ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowBadges.forEach { badge ->
                        TrainingBadge(
                            text = badge.text,
                            icon = badge.icon,
                            containerColor = badge.containerColor,
                            contentColor = badge.contentColor,
                            borderColor = badge.borderColor,
                            modifier = badge.testTag?.let { Modifier.testTag(it) } ?: Modifier
                        )
                    }
                }
            }
        }
    }
}
