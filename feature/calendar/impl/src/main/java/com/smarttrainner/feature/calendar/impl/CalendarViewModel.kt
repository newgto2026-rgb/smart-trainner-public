package com.smarttrainner.feature.calendar.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttrainner.feature.calendar.domain.ObserveWorkoutCalendarMonthUseCase
import com.smarttrainner.feature.calendar.domain.WorkoutCalendarLog
import com.smarttrainner.feature.calendar.domain.WorkoutDateSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject
import kotlin.math.min
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

private const val STATE_MONTH_KEY = "calendar_month"
private const val STATE_SELECTED_DATE_KEY = "calendar_selected_date"

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val observeWorkoutCalendarMonth: ObserveWorkoutCalendarMonthUseCase,
    private val clock: Clock
) : ViewModel() {
    private val monthState = MutableStateFlow(savedStateHandle.initialMonth(clock))
    private val selectedDateState = MutableStateFlow(savedStateHandle.initialSelectedDate(clock))

    private val calendarMonthState = monthState
        .flatMapLatest { month ->
            observeWorkoutCalendarMonth(
                month = month,
                today = LocalDate.now(clock)
            )
        }

    internal val uiState = combine(
        monthState,
        selectedDateState,
        calendarMonthState
    ) { month, selectedDate, calendarMonth ->
        val adjustedSelectedDate = selectedDate.normalizeToMonth(month)
        val isDataCurrent = calendarMonth.month == month
        CalendarUiState(
            currentMonth = month,
            selectedDate = adjustedSelectedDate,
            days = buildMonthCells(
                yearMonth = month,
                selectedDate = adjustedSelectedDate,
                today = LocalDate.now(clock),
                summariesByDate = if (isDataCurrent) calendarMonth.summariesByDate else emptyMap()
            ),
            todayWorkoutCount = if (isDataCurrent) calendarMonth.todayWorkoutCount else 0,
            selectedDateWorkouts = if (isDataCurrent) {
                calendarMonth.logsByDate[adjustedSelectedDate].orEmpty().map { it.toUiModel() }
            } else {
                emptyList()
            }
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = initialCalendarUiState(
                currentMonth = monthState.value,
                selectedDate = selectedDateState.value,
                today = LocalDate.now(clock)
            )
        )

    internal fun onAction(action: CalendarAction) {
        when (action) {
            CalendarAction.OnNextMonthClick -> moveMonthBy(1)
            CalendarAction.OnPreviousMonthClick -> moveMonthBy(-1)
            is CalendarAction.OnDateClick -> updateSelectedDate(action.date)
        }
    }

    private fun moveMonthBy(offsetMonths: Long) {
        val newMonth = monthState.value.plusMonths(offsetMonths)
        updateMonthAndSelectedDate(
            month = newMonth,
            selectedDate = selectedDateState.value.normalizeToMonth(newMonth)
        )
    }

    private fun updateSelectedDate(date: LocalDate) {
        updateMonthAndSelectedDate(
            month = YearMonth.from(date),
            selectedDate = date
        )
    }

    private fun updateMonthAndSelectedDate(
        month: YearMonth,
        selectedDate: LocalDate
    ) {
        savedStateHandle[STATE_MONTH_KEY] = month.toString()
        savedStateHandle[STATE_SELECTED_DATE_KEY] = selectedDate.toString()
        monthState.value = month
        selectedDateState.value = selectedDate
    }
}

internal fun SavedStateHandle.initialMonth(clock: Clock): YearMonth =
    get<String>(STATE_MONTH_KEY)
        ?.let { rawMonth -> runCatching { YearMonth.parse(rawMonth) }.getOrNull() }
        ?: YearMonth.now(clock)

internal fun SavedStateHandle.initialSelectedDate(clock: Clock): LocalDate =
    get<String>(STATE_SELECTED_DATE_KEY)
        ?.let { rawDate -> runCatching { LocalDate.parse(rawDate) }.getOrNull() }
        ?: LocalDate.now(clock)

internal fun initialCalendarUiState(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    today: LocalDate
): CalendarUiState {
    val adjustedSelectedDate = selectedDate.normalizeToMonth(currentMonth)
    return CalendarUiState(
        currentMonth = currentMonth,
        selectedDate = adjustedSelectedDate,
        days = buildMonthCells(
            yearMonth = currentMonth,
            selectedDate = adjustedSelectedDate,
            today = today,
            summariesByDate = emptyMap()
        ),
        todayWorkoutCount = 0,
        selectedDateWorkouts = emptyList()
    )
}

internal fun buildMonthCells(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    today: LocalDate,
    summariesByDate: Map<LocalDate, WorkoutDateSummary>
): List<CalendarDayUiModel> {
    val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
    val firstDate = yearMonth.atDay(1)
    val leadingBlanks = firstDate.dayOfWeek.distanceFrom(firstDayOfWeek)
    val daysInMonth = yearMonth.lengthOfMonth()
    val totalCells = ((leadingBlanks + daysInMonth + 6) / 7) * 7
    val previousMonth = yearMonth.minusMonths(1)
    val nextMonth = yearMonth.plusMonths(1)
    val previousMonthDays = previousMonth.lengthOfMonth()

    return List(totalCells) { index ->
        val dayOfMonth = index - leadingBlanks + 1
        val isCurrentMonth = dayOfMonth in 1..daysInMonth
        val date = when {
            dayOfMonth < 1 -> previousMonth.atDay(previousMonthDays + dayOfMonth)
            dayOfMonth > daysInMonth -> nextMonth.atDay(dayOfMonth - daysInMonth)
            else -> yearMonth.atDay(dayOfMonth)
        }
        val summary = summariesByDate[date]
        CalendarDayUiModel(
            date = date,
            isCurrentMonth = isCurrentMonth,
            isToday = date == today,
            isSelected = isCurrentMonth && date == selectedDate,
            workoutCount = if (isCurrentMonth) summary?.workoutCount ?: 0 else 0,
            completedCount = if (isCurrentMonth) summary?.completedCount ?: 0 else 0
        )
    }
}

internal fun LocalDate.normalizeToMonth(targetMonth: YearMonth): LocalDate {
    val normalizedDay = min(dayOfMonth, targetMonth.lengthOfMonth())
    return targetMonth.atDay(normalizedDay)
}

internal fun DayOfWeek.distanceFrom(other: DayOfWeek): Int =
    (value - other.value + 7) % 7

internal fun WorkoutCalendarLog.toUiModel(): CalendarSelectedWorkoutUiModel =
    CalendarSelectedWorkoutUiModel(
        id = id,
        exerciseName = exerciseName,
        muscleGroup = muscleGroup,
        performedAt = performedAt,
        sets = sets,
        reps = reps,
        weightKg = weightKg,
        durationMinutes = durationMinutes,
        memo = memo,
        completed = completed,
        volumeKg = volumeKg
    )
