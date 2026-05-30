package com.smarttrainner.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.smarttrainner.core.designsystem.SmartTrainnerColors

enum class SmartTrainnerUiTone {
    Neutral,
    Success
}

@Composable
fun SmartTrainnerSectionTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun SmartTrainnerEmptyState(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(SmartTrainnerColors.SurfaceRaised, RoundedCornerShape(8.dp))
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = SmartTrainnerColors.Muted)
    }
}

@Composable
fun SmartTrainnerStatusChip(
    label: String,
    tone: SmartTrainnerUiTone,
    modifier: Modifier = Modifier
) {
    val containerColor = when (tone) {
        SmartTrainnerUiTone.Success -> SmartTrainnerColors.GreenSoft
        SmartTrainnerUiTone.Neutral -> SmartTrainnerColors.SteelSoft
    }
    val contentColor = when (tone) {
        SmartTrainnerUiTone.Success -> SmartTrainnerColors.Green
        SmartTrainnerUiTone.Neutral -> SmartTrainnerColors.Muted
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = containerColor
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = contentColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SmartTrainnerStatusIcon(
    completed: Boolean,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = if (completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
        contentDescription = contentDescription,
        tint = if (completed) SmartTrainnerColors.Green else SmartTrainnerColors.Muted,
        modifier = modifier
    )
}

@Composable
fun SmartTrainnerMetricTile(
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
            Text(
                text = value,
                color = accent,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(text = label, color = SmartTrainnerColors.Muted, style = MaterialTheme.typography.labelMedium)
        }
    }
}

data class SmartTrainnerBadgeSpec(
    val text: String,
    val icon: ImageVector? = null,
    val containerColor: Color = SmartTrainnerColors.SteelSoft,
    val contentColor: Color = SmartTrainnerColors.Ink,
    val borderColor: Color? = null,
    val testTag: String? = null
)

@Composable
fun SmartTrainnerBadge(
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SmartTrainnerBadgeRow(
    badges: List<SmartTrainnerBadgeSpec>,
    maxItemsPerRow: Int,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = maxItemsPerRow.coerceAtLeast(1)
    ) {
        badges.forEach { badge ->
            SmartTrainnerBadge(
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

@Composable
fun SmartTrainnerProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = SmartTrainnerColors.Green,
    trackColor: Color = SmartTrainnerColors.Line
) {
    val boundedProgress = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier.background(trackColor, RoundedCornerShape(8.dp))
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
fun <T> SmartTrainnerWrappedRows(
    items: List<T>,
    maxItemsPerRow: Int,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
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
fun SmartTrainnerNumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Number
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        maxLines = 1,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    )
}
