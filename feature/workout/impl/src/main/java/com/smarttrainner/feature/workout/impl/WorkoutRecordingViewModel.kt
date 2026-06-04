package com.smarttrainner.feature.workout.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttrainner.core.domain.ObserveLatestWorkoutLogsUseCase
import com.smarttrainner.core.domain.ObserveWorkoutLogsUseCase
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.WorkoutLogInput
import com.smarttrainner.feature.workout.domain.GetLatestWorkoutLogUseCase
import com.smarttrainner.feature.workout.domain.SaveWorkoutLogUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class WorkoutRecordingViewModel @Inject constructor(
    observeWorkoutLogs: ObserveWorkoutLogsUseCase,
    observeLatestWorkoutLogs: ObserveLatestWorkoutLogsUseCase,
    private val getLatestWorkoutLog: GetLatestWorkoutLogUseCase,
    private val saveWorkoutLog: SaveWorkoutLogUseCase,
    private val clock: Clock
) : ViewModel() {
    private val weekStart = LocalDate.now(clock).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    private val recordingPlannedExercise = MutableStateFlow<PlannedExercise?>(null)
    private val recordForm = MutableStateFlow(RecordFormState())
    private val formError = MutableStateFlow<RecordFormError?>(null)
    private val recordSaved = MutableStateFlow(false)
    private var recordPrefillToken = 0L

    private val weeklyLogs = observeWorkoutLogs(weekStart).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    private val latestWorkoutLogs = observeLatestWorkoutLogs().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    private val logState = combine(
        weeklyLogs,
        latestWorkoutLogs
    ) { weeklyLogs, latestLogs ->
        WorkoutRecordingLogState(
            weeklyLogs = weeklyLogs,
            latestWorkoutLogs = latestLogs
        )
    }

    private val formState = combine(
        recordForm,
        formError,
        recordSaved
    ) { form, error, saved ->
        WorkoutRecordingFormState(
            recordForm = form,
            formError = error,
            recordSaved = saved
        )
    }

    internal val uiState = combine(
        recordingPlannedExercise,
        logState,
        formState
    ) { planned, logs, form ->
        WorkoutRecordingUiState(
            recordingPlannedExercise = planned,
            weeklyLogs = logs.weeklyLogs,
            latestWorkoutLogs = logs.latestWorkoutLogs,
            recordForm = form.recordForm,
            formError = form.formError,
            recordSaved = form.recordSaved
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WorkoutRecordingUiState()
    )

    fun updatePlannedExercise(plannedExercise: PlannedExercise?) {
        val current = recordingPlannedExercise.value
        if (plannedExercise == null) {
            if (current != null) {
                clearRecording()
            }
            return
        }
        if (current?.id == plannedExercise.id && current.exercise.id == plannedExercise.exercise.id) return
        recordingPlannedExercise.value = plannedExercise
        formError.value = null
        recordSaved.value = false
        prefillRecordForm(plannedExercise)
    }

    fun updateSetReps(index: Int, value: String) {
        updateSetEntry(index) { it.copy(reps = value.onlyNumber()) }
    }

    fun updateSetWeight(index: Int, value: String) {
        val nextWeight = value.onlyDecimal()
        recordForm.update { form ->
            if (index !in form.setEntries.indices) {
                form
            } else {
                form.copy(
                    setEntries = form.setEntries.mapIndexed { entryIndex, entry ->
                        when {
                            entryIndex == index -> entry.copy(weightKg = nextWeight)
                            index == 0 && nextWeight.isNotBlank() && entry.weightKg.isBlank() -> {
                                entry.copy(weightKg = nextWeight)
                            }
                            else -> entry
                        }
                    }
                )
            }
        }
    }

    fun updateSetDuration(index: Int, value: String) {
        updateSetEntry(index) { it.copy(durationMinutes = value.onlyNumber()) }
    }

    fun updateSetRest(index: Int, value: String) {
        updateSetEntry(index) { it.copy(restSeconds = value.onlyNumber()) }
    }

    fun addSetEntry() {
        val planned = recordingPlannedExercise.value ?: return
        recordForm.update { form ->
            if (form.setEntries.size >= MAX_RECORD_SETS) return@update form
            val last = form.setEntries.lastOrNull()
            form.copy(
                setEntries = form.setEntries + (last ?: planned.defaultSetForm())
            )
        }
        formError.value = null
    }

    fun removeSetEntry(index: Int) {
        recordForm.update { form ->
            if (form.setEntries.size <= 1 || index !in form.setEntries.indices) {
                form
            } else {
                form.copy(setEntries = form.setEntries.filterIndexed { entryIndex, _ -> entryIndex != index })
            }
        }
        formError.value = null
    }

    fun updateMemo(value: String) = recordForm.update { it.copy(memo = value.take(120)) }

    fun saveRecord(onSaved: (PlannedExercise) -> Unit) {
        val planned = recordingPlannedExercise.value
        if (planned == null) {
            formError.value = RecordFormError.SELECT_EXERCISE
            return
        }
        val form = recordForm.value
        val validationError = validateSetEntries(planned, form.setEntries)
        if (validationError != null) {
            formError.value = validationError
            return
        }
        val setEntries = form.setEntries.toWorkoutSetLogs(planned)
        val firstReps = setEntries.firstOrNull { it.reps != null }?.reps
        val firstWeight = setEntries.firstOrNull { it.weightKg != null }?.weightKg
        val totalDuration = setEntries.sumOf { it.durationMinutes ?: 0 }.takeIf { it > 0 }

        viewModelScope.launch {
            val result = saveWorkoutLog(
                WorkoutLogInput(
                    plannedExerciseId = planned.id,
                    exerciseId = planned.exercise.id,
                    performedAt = LocalDateTime.now(clock),
                    sets = setEntries.size,
                    reps = firstReps,
                    weightKg = firstWeight,
                    durationMinutes = totalDuration,
                    memo = form.memo,
                    completed = true,
                    setEntries = setEntries,
                    routineDayInstanceId = planned.routineDayInstanceId
                )
            )
            if (result.isSuccess) {
                formError.value = null
                recordSaved.value = false
                onSaved(planned)
            } else {
                formError.value = RecordFormError.SAVE_FAILED
                recordSaved.value = false
            }
        }
    }

    private fun prefillRecordForm(planned: PlannedExercise) {
        val previousLog = latestWorkoutLogs.value
            .latestRecordForPlannedExercise(planned.id)
        val initialForm = RecordFormState(setEntries = planned.defaultSetForms(previousLog))
        val token = recordPrefillToken + 1
        recordPrefillToken = token
        recordForm.value = initialForm

        viewModelScope.launch {
            val latestLog = getLatestWorkoutLog(planned.id) ?: return@launch
            val latestForm = RecordFormState(setEntries = planned.defaultSetForms(latestLog))
            if (
                recordPrefillToken == token &&
                recordingPlannedExercise.value?.id == planned.id &&
                recordForm.value == initialForm
            ) {
                recordForm.value = latestForm
            }
        }
    }

    private fun updateSetEntry(
        index: Int,
        update: (RecordSetFormState) -> RecordSetFormState
    ) {
        recordForm.update { form ->
            if (index !in form.setEntries.indices) {
                form
            } else {
                form.copy(
                    setEntries = form.setEntries.mapIndexed { entryIndex, entry ->
                        if (entryIndex == index) update(entry) else entry
                    }
                )
            }
        }
    }

    fun clearRecording() {
        recordPrefillToken += 1
        recordingPlannedExercise.value = null
        recordForm.value = RecordFormState()
        formError.value = null
        recordSaved.value = false
    }
}

private data class WorkoutRecordingLogState(
    val weeklyLogs: List<WorkoutLog>,
    val latestWorkoutLogs: List<WorkoutLog>
)

private data class WorkoutRecordingFormState(
    val recordForm: RecordFormState,
    val formError: RecordFormError?,
    val recordSaved: Boolean
)
