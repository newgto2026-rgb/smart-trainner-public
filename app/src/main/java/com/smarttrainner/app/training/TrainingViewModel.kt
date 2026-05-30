package com.smarttrainner.app.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.WeeklyPlan
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class TrainingViewModel @Inject constructor() : ViewModel() {
    private val selectedExerciseId = MutableStateFlow<ExerciseId?>(null)
    private val recordingPlannedExercise = MutableStateFlow<PlannedExercise?>(null)
    private val recordingMode = MutableStateFlow(RecordingMode.SINGLE)

    val uiState = combine(
        selectedExerciseId,
        recordingPlannedExercise
    ) { exerciseId, plannedExercise ->
        TrainingUiState(
            recordingPlannedExercise = plannedExercise,
            selectedExerciseId = exerciseId
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TrainingUiState()
    )

    fun selectExercise(exerciseId: ExerciseId) {
        selectedExerciseId.value = exerciseId
    }

    fun showExerciseMethod(exerciseId: ExerciseId) {
        selectedExerciseId.value = exerciseId
    }

    fun dismissExerciseDetail() {
        selectedExerciseId.value = null
    }

    fun selectPlannedExercise(exercise: PlannedExercise) {
        recordingMode.value = RecordingMode.SINGLE
        recordingPlannedExercise.value = exercise
        selectedExerciseId.value = null
    }

    fun startWorkout(exercise: PlannedExercise) {
        recordingMode.value = RecordingMode.ROUTINE
        recordingPlannedExercise.value = exercise
        selectedExerciseId.value = null
    }

    fun dismissRecordDialog() {
        recordingPlannedExercise.value = null
        recordingMode.value = RecordingMode.SINGLE
    }

    fun handleRecordSaved(
        planned: PlannedExercise,
        plan: WeeklyPlan?,
        completedIds: Set<PlannedExerciseId>
    ) {
        val continueRoutine = recordingMode.value == RecordingMode.ROUTINE
        val nextPlanned = if (continueRoutine) {
            plan?.nextIncompleteInSameDay(
                currentId = planned.id,
                completedIds = completedIds + planned.id
            )
        } else {
            null
        }
        if (nextPlanned != null) {
            recordingPlannedExercise.value = nextPlanned
        } else {
            recordingPlannedExercise.value = null
            recordingMode.value = RecordingMode.SINGLE
        }
    }

    fun clearRecordingFlow() {
        recordingPlannedExercise.value = null
        recordingMode.value = RecordingMode.SINGLE
    }
}

private fun WeeklyPlan.nextIncompleteInSameDay(
    currentId: PlannedExerciseId,
    completedIds: Set<PlannedExerciseId>
): PlannedExercise? {
    val day = days.firstOrNull { workoutDay -> workoutDay.exercises.any { it.id == currentId } } ?: return null
    val currentIndex = day.exercises.indexOfFirst { it.id == currentId }
    return day.exercises
        .drop(currentIndex + 1)
        .firstOrNull { it.id !in completedIds }
}
