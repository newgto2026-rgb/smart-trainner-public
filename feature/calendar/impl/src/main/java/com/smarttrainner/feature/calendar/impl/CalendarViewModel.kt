package com.smarttrainner.feature.calendar.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttrainner.core.domain.ObserveExercisesUseCase
import com.smarttrainner.core.domain.SaveWorkoutLogUseCase
import com.smarttrainner.core.domain.UpdateWorkoutLogUseCase
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.ExerciseLoadType
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.WorkoutLogId
import com.smarttrainner.core.model.WorkoutLogInput
import com.smarttrainner.core.model.WorkoutSetLog
import com.smarttrainner.feature.calendar.domain.ObserveCalendarMonthExpandedUseCase
import com.smarttrainner.feature.calendar.domain.ObserveWorkoutCalendarMonthUseCase
import com.smarttrainner.feature.calendar.domain.UpdateCalendarMonthExpandedUseCase
import com.smarttrainner.feature.calendar.domain.WorkoutCalendarLog
import com.smarttrainner.feature.calendar.domain.WorkoutDateSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val STATE_MONTH_KEY = "calendar_month"
private const val STATE_SELECTED_DATE_KEY = "calendar_selected_date"
private const val STATE_IS_MONTH_EXPANDED_KEY = "calendar_is_month_expanded"
private const val MAX_CALENDAR_RECORD_SETS = 12
private const val MAX_MEMO_LENGTH = 120

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val observeWorkoutCalendarMonth: ObserveWorkoutCalendarMonthUseCase,
    observeExercises: ObserveExercisesUseCase,
    observeCalendarMonthExpanded: ObserveCalendarMonthExpandedUseCase,
    private val updateCalendarMonthExpanded: UpdateCalendarMonthExpandedUseCase,
    private val saveWorkoutLog: SaveWorkoutLogUseCase,
    private val updateWorkoutLog: UpdateWorkoutLogUseCase,
    private val clock: Clock
) : ViewModel() {
    private val monthState = MutableStateFlow(savedStateHandle.initialMonth(clock))
    private val selectedDateState = MutableStateFlow(savedStateHandle.initialSelectedDate(clock))
    private val isMonthExpandedState = MutableStateFlow(savedStateHandle.initialIsMonthExpanded())
    private val editorDraftState = MutableStateFlow<CalendarWorkoutEditorDraft?>(null)
    private val exercisesState = observeExercises().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    private val calendarMonthState = monthState
        .flatMapLatest { month ->
            observeWorkoutCalendarMonth(
                month = month,
                today = LocalDate.now(clock)
            )
        }

    private val calendarDisplayState = combine(
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

    internal val uiState = combine(
        calendarDisplayState,
        exercisesState,
        editorDraftState
    ) { state, exercises, editorDraft ->
        state.copy(editor = editorDraft?.toUiState(exercises))
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
            CalendarAction.OnAddWorkoutClick -> openAddWorkoutEditor()
            is CalendarAction.OnEditWorkoutClick -> openEditWorkoutEditor(action.workout)
            is CalendarAction.OnEditorExerciseSelected -> updateEditorExercise(action.exerciseId)
            is CalendarAction.OnEditorSetRepsChanged -> updateEditorSetEntry(action.index) {
                it.copy(reps = action.value.onlyNumber())
            }
            is CalendarAction.OnEditorSetWeightChanged -> updateEditorSetEntry(action.index) {
                it.copy(weightKg = action.value.onlyDecimal())
            }
            is CalendarAction.OnEditorSetDurationChanged -> updateEditorSetEntry(action.index) {
                it.copy(durationMinutes = action.value.onlyNumber())
            }
            is CalendarAction.OnEditorSetRestChanged -> updateEditorSetEntry(action.index) {
                it.copy(restSeconds = action.value.onlyNumber())
            }
            CalendarAction.OnEditorAddSet -> addEditorSet()
            is CalendarAction.OnEditorRemoveSet -> removeEditorSet(action.index)
            is CalendarAction.OnEditorMemoChanged -> updateEditorMemo(action.value)
            CalendarAction.OnEditorSaveClick -> saveEditor()
            CalendarAction.OnEditorDismiss -> editorDraftState.value = null
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

    private fun openAddWorkoutEditor() {
        val selectedDate = uiState.value.selectedDate
        val exercise = exercisesState.value.sortedBy { it.name }.firstOrNull()
        editorDraftState.value = CalendarWorkoutEditorDraft(
            mode = CalendarWorkoutEditorMode.ADD,
            selectedDate = selectedDate,
            selectedExerciseId = exercise?.id,
            performedAt = selectedDate.atTime(LocalTime.now(clock)),
            setEntries = exercise?.defaultSetForms() ?: listOf(CalendarWorkoutSetFormUiState())
        )
    }

    private fun openEditWorkoutEditor(workout: CalendarSelectedWorkoutUiModel) {
        editorDraftState.value = CalendarWorkoutEditorDraft(
            mode = CalendarWorkoutEditorMode.EDIT,
            logId = workout.id,
            selectedDate = workout.performedAt.toLocalDate(),
            selectedExerciseId = workout.exerciseId,
            fallbackExerciseName = workout.exerciseName,
            performedAt = workout.performedAt,
            plannedExerciseId = workout.plannedExerciseId,
            routineDayInstanceId = workout.routineDayInstanceId,
            setEntries = workout.displaySetForms(),
            memo = workout.memo
        )
    }

    private fun updateEditorExercise(exerciseId: ExerciseId) {
        val exercise = exercisesState.value.firstOrNull { it.id == exerciseId }
        editorDraftState.update { draft ->
            if (draft == null || draft.mode != CalendarWorkoutEditorMode.ADD) {
                draft
            } else {
                draft.copy(
                    selectedExerciseId = exerciseId,
                    setEntries = exercise?.defaultSetForms() ?: draft.setEntries,
                    error = null
                )
            }
        }
    }

    private fun updateEditorSetEntry(
        index: Int,
        update: (CalendarWorkoutSetFormUiState) -> CalendarWorkoutSetFormUiState
    ) {
        editorDraftState.update { draft ->
            if (draft == null || index !in draft.setEntries.indices) {
                draft
            } else {
                draft.copy(
                    setEntries = draft.setEntries.mapIndexed { entryIndex, entry ->
                        if (entryIndex == index) update(entry) else entry
                    },
                    error = null
                )
            }
        }
    }

    private fun addEditorSet() {
        val exercise = editorDraftState.value?.selectedExerciseId
            ?.let { selectedId -> exercisesState.value.firstOrNull { it.id == selectedId } }
        editorDraftState.update { draft ->
            if (draft == null || draft.setEntries.size >= MAX_CALENDAR_RECORD_SETS) {
                draft
            } else {
                draft.copy(
                    setEntries = draft.setEntries + (draft.setEntries.lastOrNull() ?: exercise?.defaultSetForm()
                        ?: CalendarWorkoutSetFormUiState()),
                    error = null
                )
            }
        }
    }

    private fun removeEditorSet(index: Int) {
        editorDraftState.update { draft ->
            if (draft == null || draft.setEntries.size <= 1 || index !in draft.setEntries.indices) {
                draft
            } else {
                draft.copy(
                    setEntries = draft.setEntries.filterIndexed { entryIndex, _ -> entryIndex != index },
                    error = null
                )
            }
        }
    }

    private fun updateEditorMemo(value: String) {
        editorDraftState.update { draft ->
            draft?.copy(memo = value.take(MAX_MEMO_LENGTH), error = null)
        }
    }

    private fun saveEditor() {
        val draft = editorDraftState.value ?: return
        val exerciseId = draft.selectedExerciseId
        if (exerciseId == null) {
            editorDraftState.value = draft.copy(error = CalendarWorkoutEditorError.EXERCISE)
            return
        }
        val exercise = exercisesState.value.firstOrNull { it.id == exerciseId }
        if (draft.mode == CalendarWorkoutEditorMode.ADD && exercise == null) {
            editorDraftState.value = draft.copy(error = CalendarWorkoutEditorError.EXERCISE)
            return
        }
        val validationError = validateCalendarSetEntries(
            entries = draft.setEntries,
            requireReps = exercise?.defaultRepRange != null,
            requireDuration = exercise?.defaultRepRange == null && exercise != null
        )
        if (validationError != null) {
            editorDraftState.value = draft.copy(error = validationError)
            return
        }
        val setEntries = draft.setEntries.toWorkoutSetLogs(
            includeRepsAndWeight = exercise?.defaultRepRange != null || draft.setEntries.any { it.reps.isNotBlank() },
            includeDuration = exercise?.defaultRepRange == null || draft.setEntries.any { it.durationMinutes.isNotBlank() }
        )
        val firstReps = setEntries.firstOrNull { it.reps != null }?.reps
        val firstWeight = setEntries.firstOrNull { it.weightKg != null }?.weightKg
        val totalDuration = setEntries.sumOf { it.durationMinutes ?: 0 }.takeIf { it > 0 }
        val input = WorkoutLogInput(
            plannedExerciseId = draft.plannedExerciseId ?: PlannedExerciseId(""),
            exerciseId = exerciseId,
            performedAt = draft.performedAt,
            sets = setEntries.size,
            reps = firstReps,
            weightKg = firstWeight,
            durationMinutes = totalDuration,
            memo = draft.memo,
            completed = true,
            setEntries = setEntries,
            routineDayInstanceId = draft.routineDayInstanceId
        )
        editorDraftState.value = draft.copy(isSaving = true, error = null)
        viewModelScope.launch {
            val result = if (draft.mode == CalendarWorkoutEditorMode.EDIT && draft.logId != null) {
                updateWorkoutLog(draft.logId, input)
            } else {
                saveWorkoutLog(input)
            }
            if (result.isSuccess) {
                editorDraftState.value = null
            } else {
                editorDraftState.update { current ->
                    current?.copy(isSaving = false, error = CalendarWorkoutEditorError.SAVE_FAILED)
                }
            }
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
        exerciseId = exerciseId,
        plannedExerciseId = plannedExerciseId,
        exerciseName = exerciseName,
        muscleGroup = muscleGroup,
        performedAt = performedAt,
        sets = sets,
        reps = reps,
        weightKg = weightKg,
        durationMinutes = durationMinutes,
        memo = memo,
        completed = completed,
        volumeKg = volumeKg,
        loadType = loadType,
        effectiveVolumeKg = effectiveVolumeKg,
        effectiveSetLoadsKg = effectiveSetLoadsKg,
        setEntries = setEntries,
        routineDayInstanceId = routineDayInstanceId
    )

internal data class CalendarWorkoutEditorDraft(
    val mode: CalendarWorkoutEditorMode,
    val logId: WorkoutLogId? = null,
    val selectedDate: LocalDate,
    val selectedExerciseId: ExerciseId?,
    val fallbackExerciseName: String = "",
    val performedAt: java.time.LocalDateTime,
    val plannedExerciseId: PlannedExerciseId? = null,
    val routineDayInstanceId: String? = null,
    val setEntries: List<CalendarWorkoutSetFormUiState>,
    val memo: String = "",
    val error: CalendarWorkoutEditorError? = null,
    val isSaving: Boolean = false
)

internal fun CalendarWorkoutEditorDraft.toUiState(
    exercises: List<Exercise>
): CalendarWorkoutEditorUiState {
    val sortedExercises = exercises.sortedBy { it.name }
    val selectedExercise = sortedExercises.firstOrNull { it.id == selectedExerciseId }
    val inferredReps = setEntries.any { it.reps.isNotBlank() }
    val inferredDuration = setEntries.any { it.durationMinutes.isNotBlank() }
    val showReps = selectedExercise?.defaultRepRange != null || (selectedExercise == null && inferredReps)
    val showDuration = selectedExercise?.defaultRepRange == null || inferredDuration
    return CalendarWorkoutEditorUiState(
        mode = mode,
        selectedDate = selectedDate,
        exerciseOptions = sortedExercises.map { CalendarExerciseOptionUiModel(id = it.id, name = it.name) },
        selectedExerciseId = selectedExerciseId,
        selectedExerciseName = selectedExercise?.name ?: fallbackExerciseName,
        selectedExerciseLoadType = selectedExercise?.loadType ?: ExerciseLoadType.EXTERNAL_LOAD,
        showReps = showReps,
        showWeight = showReps,
        showDuration = showDuration,
        setEntries = setEntries,
        memo = memo,
        error = error,
        isSaving = isSaving
    )
}

internal fun Exercise.defaultSetForms(): List<CalendarWorkoutSetFormUiState> =
    List(defaultSets.coerceIn(1, MAX_CALENDAR_RECORD_SETS)) { defaultSetForm() }

internal fun Exercise.defaultSetForm(): CalendarWorkoutSetFormUiState =
    CalendarWorkoutSetFormUiState(
        reps = defaultRepRange?.first?.toString().orEmpty(),
        weightKg = "",
        durationMinutes = defaultDurationMinutes?.toString().orEmpty(),
        restSeconds = restSeconds.toString()
    )

internal fun CalendarSelectedWorkoutUiModel.displaySetForms(): List<CalendarWorkoutSetFormUiState> {
    val entries = setEntries.takeIf { it.isNotEmpty() }
        ?: List(sets.coerceIn(1, MAX_CALENDAR_RECORD_SETS)) {
            WorkoutSetLog(
                order = it + 1,
                reps = reps,
                weightKg = weightKg,
                durationMinutes = durationMinutes,
                restSeconds = null
            )
        }
    return entries.sortedBy { it.order }.map {
        CalendarWorkoutSetFormUiState(
            reps = it.reps?.toString().orEmpty(),
            weightKg = it.weightKg?.toRecordInput().orEmpty(),
            durationMinutes = it.durationMinutes?.toString().orEmpty(),
            restSeconds = it.restSeconds?.toString().orEmpty()
        )
    }.ifEmpty { listOf(CalendarWorkoutSetFormUiState()) }
}

internal fun validateCalendarSetEntries(
    entries: List<CalendarWorkoutSetFormUiState>,
    requireReps: Boolean,
    requireDuration: Boolean
): CalendarWorkoutEditorError? {
    if (entries.isEmpty() || entries.size > MAX_CALENDAR_RECORD_SETS) {
        return CalendarWorkoutEditorError.SETS
    }
    entries.forEach { entry ->
        val reps = entry.reps.toIntOrNull()
        val weight = entry.weightKg.toDoubleOrNull()
        val duration = entry.durationMinutes.toIntOrNull()
        val rest = entry.restSeconds.toIntOrNull()
        when {
            requireReps && reps == null -> return CalendarWorkoutEditorError.REPS
            entry.reps.isNotBlank() && (reps == null || reps !in 1..50) -> {
                return CalendarWorkoutEditorError.REPS
            }
            entry.weightKg.isNotBlank() && (weight == null || weight < 0.0) -> {
                return CalendarWorkoutEditorError.WEIGHT
            }
            requireDuration && duration == null -> return CalendarWorkoutEditorError.DURATION
            entry.durationMinutes.isNotBlank() && (duration == null || duration !in 1..240) -> {
                return CalendarWorkoutEditorError.DURATION
            }
            entry.restSeconds.isNotBlank() && (rest == null || rest !in 0..600) -> {
                return CalendarWorkoutEditorError.REST
            }
            !requireReps && !requireDuration && reps == null && duration == null -> {
                return CalendarWorkoutEditorError.SETS
            }
        }
    }
    return null
}

internal fun List<CalendarWorkoutSetFormUiState>.toWorkoutSetLogs(
    includeRepsAndWeight: Boolean,
    includeDuration: Boolean
): List<WorkoutSetLog> = mapIndexed { index, entry ->
    WorkoutSetLog(
        order = index + 1,
        reps = if (includeRepsAndWeight) entry.reps.toIntOrNull() else null,
        weightKg = if (includeRepsAndWeight) entry.weightKg.toDoubleOrNull() else null,
        durationMinutes = if (includeDuration) entry.durationMinutes.toIntOrNull() else null,
        restSeconds = entry.restSeconds.toIntOrNull()
    )
}

internal fun String.onlyNumber(): String = filter { it.isDigit() }.take(3)

internal fun String.onlyDecimal(): String {
    var dotSeen = false
    return filter { char ->
        when {
            char.isDigit() -> true
            char == '.' && !dotSeen -> {
                dotSeen = true
                true
            }
            else -> false
        }
    }.take(6)
}

private fun Double.toRecordInput(): String =
    if (rem(1.0) == 0.0) toLong().toString() else toString()
