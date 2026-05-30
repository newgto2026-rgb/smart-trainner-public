package com.smarttrainner.feature.exercise.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttrainner.core.domain.GetExerciseUseCase
import com.smarttrainner.core.domain.ObserveLatestWorkoutLogsUseCase
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.WorkoutLog
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ExerciseDetailViewModel @Inject constructor(
    observeLatestWorkoutLogs: ObserveLatestWorkoutLogsUseCase,
    private val getExercise: GetExerciseUseCase
) : ViewModel() {
    private val selectedExerciseId = MutableStateFlow<ExerciseId?>(null)
    private val showRecordAction = MutableStateFlow(false)

    private val latestWorkoutLogs = observeLatestWorkoutLogs().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val selectedExercise = selectedExerciseId.mapLatest { id ->
        id?.let { getExercise(it) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    internal val uiState = combine(
        selectedExerciseId,
        selectedExercise,
        showRecordAction,
        latestWorkoutLogs
    ) { exerciseId, exercise, showRecordAction, latestLogs ->
        val currentExercise = exercise?.takeIf { it.id == exerciseId }
        ExerciseDetailUiState(
            exercise = currentExercise,
            latestWorkoutLog = currentExercise?.let { latestLogs.latestForExercise(it.id) },
            showRecordAction = showRecordAction && currentExercise != null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExerciseDetailUiState()
    )

    fun updateSelection(
        exerciseId: ExerciseId?,
        shouldShowRecordAction: Boolean
    ) {
        if (exerciseId == null) {
            selectedExerciseId.value = null
            showRecordAction.value = false
            return
        }
        showRecordAction.value = shouldShowRecordAction
        if (selectedExerciseId.value == exerciseId) return
        selectedExerciseId.value = exerciseId
    }
}

private fun List<WorkoutLog>.latestForExercise(exerciseId: ExerciseId): WorkoutLog? =
    filter { it.exerciseId == exerciseId }
        .maxByOrNull { it.performedAt }
