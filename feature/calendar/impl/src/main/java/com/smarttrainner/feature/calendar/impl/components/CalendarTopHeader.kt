package com.smarttrainner.feature.calendar.impl.components

import android.text.format.DateFormat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.smarttrainner.core.designsystem.SmartTrainnerColors
import com.smarttrainner.feature.calendar.impl.R
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun CalendarTopHeader(
    currentMonth: YearMonth,
    todayCount: Int,
    isMonthExpanded: Boolean,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    onToggleMonthExpansion: () -> Unit
) {
    val locale = Locale.getDefault()
    val monthLabel = remember(currentMonth, locale) {
        currentMonth.formatLocalizedMonthLabel(locale)
    }
    val toggleDescription = stringResource(
        if (isMonthExpanded) {
            R.string.calendar_collapse_to_selected_week
        } else {
            R.string.calendar_expand_to_month
        }
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = monthLabel,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = SmartTrainnerColors.Ink,
                modifier = Modifier
                    .testTag("calendar_month_label")
                    .semantics { contentDescription = monthLabel }
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = pluralStringResource(
                    id = R.plurals.calendar_header_today_workout_count,
                    count = todayCount,
                    todayCount
                ),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = SmartTrainnerColors.Muted
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = if (isMonthExpanded) {
                    SmartTrainnerColors.Coral.copy(alpha = 0.10f)
                } else {
                    SmartTrainnerColors.SurfaceRaised
                },
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isMonthExpanded) {
                        SmartTrainnerColors.Coral.copy(alpha = 0.24f)
                    } else {
                        SmartTrainnerColors.Line
                    }
                ),
                modifier = Modifier
                    .height(38.dp)
                    .testTag("calendar_toggle_month_expansion")
                    .semantics { contentDescription = toggleDescription }
                    .clickable(onClick = onToggleMonthExpansion)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isMonthExpanded) {
                            Icons.Default.KeyboardArrowUp
                        } else {
                            Icons.Default.KeyboardArrowDown
                        },
                        contentDescription = null,
                        tint = SmartTrainnerColors.Muted,
                        modifier = Modifier.size(19.dp)
                    )
                    Text(
                        text = stringResource(
                            if (isMonthExpanded) {
                                R.string.calendar_month_toggle_week
                            } else {
                                R.string.calendar_month_toggle_month
                            }
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SmartTrainnerColors.Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Surface(
                shape = CircleShape,
                color = SmartTrainnerColors.SurfaceRaised
            ) {
                IconButton(
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("calendar_prev_month"),
                    onClick = onPreviousMonthClick
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = stringResource(R.string.calendar_month_navigation_previous),
                        tint = SmartTrainnerColors.Muted
                    )
                }
            }
            Surface(
                shape = CircleShape,
                color = SmartTrainnerColors.SurfaceRaised
            ) {
                IconButton(
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("calendar_next_month"),
                    onClick = onNextMonthClick
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.calendar_month_navigation_next),
                        tint = SmartTrainnerColors.Muted
                    )
                }
            }
        }
    }
}

private fun YearMonth.formatLocalizedMonthLabel(locale: Locale): String {
    val pattern = DateFormat.getBestDateTimePattern(locale, "yMMMM")
    return atDay(1).format(DateTimeFormatter.ofPattern(pattern, locale))
}
