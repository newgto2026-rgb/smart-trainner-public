package com.smarttrainner.feature.calendar.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttrainner.core.ui.SmartTrainnerScreenChrome
import com.smarttrainner.core.ui.SmartTrainnerScreenScaffold
import com.smarttrainner.feature.calendar.api.CalendarFeatureEntry
import com.smarttrainner.feature.calendar.impl.components.CalendarAgendaSection
import com.smarttrainner.feature.calendar.impl.components.CalendarMonthGrid
import com.smarttrainner.feature.calendar.impl.components.CalendarTopHeader
import javax.inject.Inject

class CalendarFeatureEntryImpl @Inject constructor() : CalendarFeatureEntry {
    @Composable
    override fun Route() {
        val viewModel: CalendarViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        SmartTrainnerScreenScaffold(
            chrome = SmartTrainnerScreenChrome(
                title = stringResource(R.string.calendar_route_title),
                subtitle = stringResource(R.string.calendar_route_subtitle)
            )
        ) {
            item {
                CalendarTopHeader(
                    currentMonth = state.currentMonth,
                    todayCount = state.todayWorkoutCount,
                    isMonthExpanded = state.isMonthExpanded,
                    onPreviousMonthClick = { viewModel.onAction(CalendarAction.OnPreviousMonthClick) },
                    onNextMonthClick = { viewModel.onAction(CalendarAction.OnNextMonthClick) },
                    onToggleMonthExpansion = { viewModel.onAction(CalendarAction.OnToggleMonthExpansion) }
                )
            }
            item {
                CalendarMonthGrid(
                    days = if (state.isMonthExpanded) state.days else state.selectedWeekDays,
                    onDateClick = { date -> viewModel.onAction(CalendarAction.OnDateClick(date)) }
                )
            }
            item {
                CalendarAgendaSection(
                    selectedDate = state.selectedDate,
                    selectedDateWorkouts = state.selectedDateWorkouts
                )
            }
        }
    }
}
