package com.smarttrainner.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.smarttrainner.core.designsystem.SmartTrainnerColors
import com.smarttrainner.core.designsystem.SmartTrainnerGradients

enum class SmartTrainnerUiTone {
    Neutral,
    Success
}

data class SmartTrainnerScreenChrome(
    val title: String,
    val subtitle: String
)

val LocalSmartTrainnerHeaderActions = staticCompositionLocalOf<(@Composable RowScope.() -> Unit)?> {
    null
}

@Composable
fun SmartTrainnerScreenScaffold(
    chrome: SmartTrainnerScreenChrome,
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SmartTrainnerGradients.screen())
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentPadding = PaddingValues(start = 18.dp, top = 14.dp, end = 18.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { SmartTrainnerScreenHeader(chrome = chrome) }
            content()
        }
    }
}

@Composable
private fun SmartTrainnerScreenHeader(chrome: SmartTrainnerScreenChrome) {
    val headerActions = LocalSmartTrainnerHeaderActions.current
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = chrome.title,
                        modifier = Modifier.testTag("training_app_title"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = SmartTrainnerColors.Ink
                    )
                    Text(
                        text = chrome.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SmartTrainnerColors.Muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                headerActions?.let { actions ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        content = actions
                    )
                }
            }
        }
    }
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

@Composable
fun SmartTrainnerBadgeRow(
    badges: List<SmartTrainnerBadgeSpec>,
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
        }
    }
}

@Composable
fun SmartTrainnerMetricCluster(
    label: String,
    metrics: List<SmartTrainnerBadgeSpec>,
    maxItemsPerRow: Int,
    modifier: Modifier = Modifier,
    labelContainerColor: Color = SmartTrainnerColors.CoralSoft,
    labelContentColor: Color = SmartTrainnerColors.Ink,
    metricAlpha: Float = 0.56f
) {
    BoxWithConstraints(modifier = modifier) {
        val rowLimit = if (maxWidth < 220.dp) {
            maxItemsPerRow.coerceAtMost(2)
        } else {
            maxItemsPerRow
        }.coerceAtLeast(1)
        val tokens = listOf(
            SmartTrainnerBadgeSpec(
                text = label,
                containerColor = labelContainerColor,
                contentColor = labelContentColor
            ) to true
        ) + metrics.map { it to false }
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            tokens.chunked(rowLimit).forEach { rowMetrics ->
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    rowMetrics.forEach { (metric, isFirstToken) ->
                        val alpha = if (isFirstToken) 1f else metricAlpha
                        val weight = if (isFirstToken) FontWeight.Bold else FontWeight.SemiBold
                        val horizontalPadding = if (isFirstToken) 7.dp else 6.dp
                        SmartTrainnerMetricToken(
                            text = metric.text,
                            icon = metric.icon,
                            containerColor = metric.containerColor.copy(alpha = alpha),
                            contentColor = metric.contentColor,
                            borderColor = metric.borderColor,
                            fontWeight = weight,
                            horizontalPadding = horizontalPadding,
                            modifier = metric.testTag?.let { Modifier.testTag(it) } ?: Modifier
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SmartTrainnerMetricToken(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    containerColor: Color = SmartTrainnerColors.SteelSoft,
    contentColor: Color = SmartTrainnerColors.Ink,
    borderColor: Color? = null,
    fontWeight: FontWeight = FontWeight.SemiBold,
    horizontalPadding: Dp = 6.dp
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = containerColor,
        border = borderColor?.let { BorderStroke(1.dp, it) }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = contentColor
                )
            }
            Text(
                text = text,
                color = contentColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = fontWeight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
