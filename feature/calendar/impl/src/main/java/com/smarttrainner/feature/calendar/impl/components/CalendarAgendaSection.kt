package com.smarttrainner.feature.calendar.impl.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smarttrainner.core.designsystem.SmartTrainnerColors
import com.smarttrainner.feature.calendar.impl.CalendarSelectedWorkoutUiModel
import com.smarttrainner.feature.calendar.impl.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val WORKOUT_PREVIEW_LIMIT = 3
private const val WORKOUT_INCREMENT = 5

@Composable
internal fun CalendarAgendaSection(
    selectedDate: LocalDate,
    selectedDateWorkouts: List<CalendarSelectedWorkoutUiModel>,
    modifier: Modifier = Modifier
) {
    val locale = Locale.getDefault()
    val selectedDateLabel = selectedDate.formatSelectedDateLabel(locale)
    val workoutCount = selectedDateWorkouts.size
    var visibleWorkoutLimit by remember(selectedDate, selectedDateWorkouts) {
        mutableIntStateOf(WORKOUT_PREVIEW_LIMIT)
    }
    val visibleWorkouts = selectedDateWorkouts.take(visibleWorkoutLimit)
    val canShowMore = visibleWorkoutLimit < selectedDateWorkouts.size
    val canCollapse = visibleWorkoutLimit > WORKOUT_PREVIEW_LIMIT
    val showMoreCount = (selectedDateWorkouts.size - visibleWorkoutLimit).coerceAtMost(WORKOUT_INCREMENT)

    Column(modifier = modifier.testTag("calendar_day_workout_sheet")) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.calendar_agenda_date_label, selectedDateLabel),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.testTag("calendar_day_workout_list_title")
                    )
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = SmartTrainnerColors.SteelSoft
                    ) {
                        Text(
                            text = pluralStringResource(
                                id = R.plurals.calendar_agenda_workout_count_badge,
                                count = workoutCount,
                                workoutCount
                            ),
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SmartTrainnerColors.Ink
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.calendar_agenda_title),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = SmartTrainnerColors.Muted
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (selectedDateWorkouts.isEmpty()) {
            Text(
                text = stringResource(R.string.calendar_bottom_sheet_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = SmartTrainnerColors.Muted,
                modifier = Modifier
                    .testTag("calendar_day_workout_list_empty")
                    .padding(bottom = 20.dp)
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                visibleWorkouts.forEach { workout ->
                    CalendarAgendaItem(workout = workout)
                }
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
                                visibleWorkoutLimit = (visibleWorkoutLimit + WORKOUT_INCREMENT)
                                    .coerceAtMost(selectedDateWorkouts.size)
                            },
                            modifier = Modifier.testTag("calendar_workout_show_more")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = null
                            )
                            Text(
                                text = pluralStringResource(
                                    R.plurals.calendar_workout_show_more,
                                    showMoreCount,
                                    showMoreCount
                                )
                            )
                        }
                    }
                    if (canCollapse) {
                        TextButton(
                            onClick = { visibleWorkoutLimit = WORKOUT_PREVIEW_LIMIT },
                            modifier = Modifier.testTag("calendar_workout_collapse")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExpandLess,
                                contentDescription = null
                            )
                            Text(text = stringResource(R.string.calendar_workout_collapse))
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

private fun LocalDate.formatSelectedDateLabel(locale: Locale): String {
    val pattern = if (locale.language == Locale.KOREAN.language) {
        "yyyy년 M월 d일 (E)"
    } else {
        "yyyy MMM d (E)"
    }
    return format(DateTimeFormatter.ofPattern(pattern, locale))
}
