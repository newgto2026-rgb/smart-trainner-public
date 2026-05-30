package com.smarttrainner.feature.exercise.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttrainner.core.domain.GetExerciseUseCase
import com.smarttrainner.core.domain.ObserveLatestWorkoutLogsUseCase
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.feature.exercise.api.ExerciseDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ExerciseDetailViewModel @Inject constructor(
    observeLatestWorkoutLogs: ObserveLatestWorkoutLogsUseCase,
    private val getExercise: GetExerciseUseCase
) : ViewModel() {
    private val selectedExerciseId = MutableStateFlow<ExerciseId?>(null)
    private val selectedExercise = MutableStateFlow<Exercise?>(null)
    private val showRecordAction = MutableStateFlow(false)
    private var exerciseLookupToken = 0L

    private val latestWorkoutLogs = observeLatestWorkoutLogs().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val uiState = combine(
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
        showRecordAction.value = shouldShowRecordAction
        if (exerciseId == null) {
            clearSelection()
            return
        }
        if (selectedExerciseId.value == exerciseId) return
        selectedExerciseId.value = exerciseId
        selectedExercise.value = null
        val token = exerciseLookupToken + 1
        exerciseLookupToken = token
        viewModelScope.launch {
            val exercise = getExercise(exerciseId)
            if (exerciseLookupToken == token && selectedExerciseId.value == exerciseId) {
                selectedExercise.value = exercise
            }
        }
    }

    private fun clearSelection() {
        exerciseLookupToken += 1
        selectedExerciseId.value = null
        selectedExercise.value = null
        showRecordAction.value = false
    }
}

private fun List<WorkoutLog>.latestForExercise(exerciseId: ExerciseId): WorkoutLog? =
    filter { it.exerciseId == exerciseId }
        .maxByOrNull { it.performedAt }
