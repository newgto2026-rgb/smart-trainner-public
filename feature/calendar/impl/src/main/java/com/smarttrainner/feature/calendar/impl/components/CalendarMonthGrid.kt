package com.smarttrainner.feature.calendar.impl.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smarttrainner.core.designsystem.SmartTrainnerColors
import com.smarttrainner.feature.calendar.impl.CalendarDayUiModel
import com.smarttrainner.feature.calendar.impl.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

@Composable
internal fun CalendarMonthGrid(
    days: List<CalendarDayUiModel>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = SmartTrainnerColors.SurfaceRaised,
        border = androidx.compose.foundation.BorderStroke(1.dp, SmartTrainnerColors.Line),
        modifier = modifier
            .fillMaxWidth()
            .testTag("calendar_month_grid")
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)) {
            WeekdayHeaderRow()
            Spacer(modifier = Modifier.height(4.dp))

            days.chunked(7).forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    week.forEach { day ->
                        CalendarDayCell(
                            day = day,
                            onClick = onDateClick
                        )
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
            }
        }
    }
}

@Composable
private fun WeekdayHeaderRow() {
    val locale = Locale.getDefault()
    val firstDayOfWeek = WeekFields.of(locale).firstDayOfWeek
    val dayOrder = List(7) { firstDayOfWeek.plus(it.toLong()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        dayOrder.forEach { dayOfWeek ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dayOfWeek.getDisplayName(TextStyle.SHORT, locale).uppercase(locale),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = SmartTrainnerColors.Muted
                )
            }
        }
    }
}

@Composable
private fun RowScope.CalendarDayCell(
    day: CalendarDayUiModel,
    onClick: (LocalDate) -> Unit
) {
    val isInactiveMonth = !day.isCurrentMonth
    val hasItems = !isInactiveMonth && day.workoutCount > 0
    val textColor = when {
        day.isSelected -> SmartTrainnerColors.SurfaceRaised
        isInactiveMonth -> SmartTrainnerColors.Muted.copy(alpha = 0.34f)
        day.isToday -> SmartTrainnerColors.Coral
        else -> SmartTrainnerColors.Ink
    }
    val dateFontWeight = if (day.isSelected || day.isToday) FontWeight.SemiBold else FontWeight.Medium
    val indicatorColor = if (day.isSelected) SmartTrainnerColors.SurfaceRaised else SmartTrainnerColors.Coral
    val locale = Locale.getDefault()

    val a11yParts = buildList {
        add(day.date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)))
        add(
            pluralStringResource(
                id = R.plurals.calendar_a11y_workout_count,
                count = day.workoutCount,
                day.workoutCount
            )
        )
        if (day.isSelected) add(stringResource(R.string.calendar_a11y_selected))
        if (day.isToday) add(stringResource(R.string.calendar_a11y_today))
    }

    Column(
        modifier = Modifier
            .weight(1f)
            .height(40.dp)
            .testTag("calendar_day_${day.date}")
            .clickable(enabled = day.isCurrentMonth) { onClick(day.date) }
            .semantics { contentDescription = a11yParts.joinToString(separator = ", ") }
            .padding(vertical = 3.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .then(
                    if (day.isToday && !isInactiveMonth && !day.isSelected) {
                        Modifier.border(
                            width = 1.dp,
                            color = SmartTrainnerColors.Coral,
                            shape = CircleShape
                        )
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (day.isSelected) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            color = SmartTrainnerColors.Coral,
                            shape = CircleShape
                        )
                )
            }
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = dateFontWeight,
                color = textColor
            )
        }
        Spacer(modifier = Modifier.height(1.dp))
        Box(
            modifier = Modifier.height(4.dp),
            contentAlignment = Alignment.Center
        ) {
            if (hasItems) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(
                            color = indicatorColor,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}
