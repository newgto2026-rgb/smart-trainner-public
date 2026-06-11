package com.smarttrainner.feature.calendar.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttrainner.feature.calendar.domain.ObserveCalendarMonthExpandedUseCase
import com.smarttrainner.feature.calendar.domain.ObserveWorkoutCalendarMonthUseCase
import com.smarttrainner.feature.calendar.domain.UpdateCalendarMonthExpandedUseCase
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
import kotlinx.coroutines.launch

private const val STATE_MONTH_KEY = "calendar_month"
private const val STATE_SELECTED_DATE_KEY = "calendar_selected_date"
private const val STATE_IS_MONTH_EXPANDED_KEY = "calendar_is_month_expanded"

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val observeWorkoutCalendarMonth: ObserveWorkoutCalendarMonthUseCase,
    observeCalendarMonthExpanded: ObserveCalendarMonthExpandedUseCase,
    private val updateCalendarMonthExpanded: UpdateCalendarMonthExpandedUseCase,
    private val clock: Clock
) : ViewModel() {
    private val monthState = MutableStateFlow(savedStateHandle.initialMonth(clock))
    private val selectedDateState = MutableStateFlow(savedStateHandle.initialSelectedDate(clock))
    private val isMonthExpandedState = MutableStateFlow(savedStateHandle.initialIsMonthExpanded())

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
        calendarMonthState,
        isMonthExpandedState
    ) { month, selectedDate, calendarMonth, isMonthExpanded ->
        val adjustedSelectedDate = selectedDate.normalizeToMonth(month)
        val isDataCurrent = calendarMonth.month == month
        val days = buildMonthCells(
            yearMonth = month,
            selectedDate = adjustedSelectedDate,
            today = LocalDate.now(clock),
            summariesByDate = if (isDataCurrent) calendarMonth.summariesByDate else emptyMap()
        )
        CalendarUiState(
            currentMonth = month,
            selectedDate = adjustedSelectedDate,
            isMonthExpanded = isMonthExpanded,
            days = days,
            selectedWeekDays = days.selectedWeekDays(adjustedSelectedDate),
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
            started = SharingStarted.Eagerly,
            initialValue = initialCalendarUiState(
                currentMonth = monthState.value,
                selectedDate = selectedDateState.value,
                today = LocalDate.now(clock),
                isMonthExpanded = isMonthExpandedState.value
            )
        )

    init {
        viewModelScope.launch {
            observeCalendarMonthExpanded().collect { isExpanded ->
                savedStateHandle[STATE_IS_MONTH_EXPANDED_KEY] = isExpanded
                isMonthExpandedState.value = isExpanded
            }
        }
    }

    internal fun onAction(action: CalendarAction) {
        when (action) {
            CalendarAction.OnNextMonthClick -> moveMonthBy(1)
            CalendarAction.OnPreviousMonthClick -> moveMonthBy(-1)
            CalendarAction.OnToggleMonthExpansion -> toggleMonthExpansion()
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

    private fun toggleMonthExpansion() {
        val nextValue = !isMonthExpandedState.value
        savedStateHandle[STATE_IS_MONTH_EXPANDED_KEY] = nextValue
        isMonthExpandedState.value = nextValue
        viewModelScope.launch {
            updateCalendarMonthExpanded(nextValue)
        }
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

internal fun SavedStateHandle.initialIsMonthExpanded(): Boolean =
    get<Boolean>(STATE_IS_MONTH_EXPANDED_KEY) ?: true

internal fun initialCalendarUiState(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    today: LocalDate,
    isMonthExpanded: Boolean = true
): CalendarUiState {
    val adjustedSelectedDate = selectedDate.normalizeToMonth(currentMonth)
    val days = buildMonthCells(
        yearMonth = currentMonth,
        selectedDate = adjustedSelectedDate,
        today = today,
        summariesByDate = emptyMap()
    )
    return CalendarUiState(
        currentMonth = currentMonth,
        selectedDate = adjustedSelectedDate,
        isMonthExpanded = isMonthExpanded,
        days = days,
        selectedWeekDays = days.selectedWeekDays(adjustedSelectedDate),
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

internal fun List<CalendarDayUiModel>.selectedWeekDays(selectedDate: LocalDate): List<CalendarDayUiModel> =
    chunked(7)
        .firstOrNull { week -> week.any { it.date == selectedDate } }
        ?: take(7)

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
        sets = sets,
        reps = reps,
        weightKg = weightKg,
        memo = memo,
        completed = completed,
        volumeKg = volumeKg,
        setEntries = setEntries
    )
