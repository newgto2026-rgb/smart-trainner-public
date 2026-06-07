package com.smarttrainner.feature.calendar.impl

import java.time.LocalDate

internal sealed interface CalendarAction {
    data object OnPreviousMonthClick : CalendarAction
    data object OnNextMonthClick : CalendarAction
    data object OnToggleMonthExpansion : CalendarAction
    data class OnDateClick(val date: LocalDate) : CalendarAction
}
