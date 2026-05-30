package com.smarttrainner.app.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExercise
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
    private val recordingFlow = MutableStateFlow(RecordingFlow.SINGLE)

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
        recordingFlow.value = RecordingFlow.SINGLE
        recordingPlannedExercise.value = exercise
        selectedExerciseId.value = null
    }

    fun startContinuousRecording(exercise: PlannedExercise) {
        recordingFlow.value = RecordingFlow.CONTINUOUS
        recordingPlannedExercise.value = exercise
        selectedExerciseId.value = null
    }

    fun dismissRecordDialog() {
        recordingPlannedExercise.value = null
        recordingFlow.value = RecordingFlow.SINGLE
    }

    fun handleRecordSaved(nextPlannedExercise: PlannedExercise?) {
        val nextPlanned = if (recordingFlow.value == RecordingFlow.CONTINUOUS) nextPlannedExercise else null
        if (nextPlanned != null) {
            recordingPlannedExercise.value = nextPlanned
        } else {
            recordingPlannedExercise.value = null
            recordingFlow.value = RecordingFlow.SINGLE
        }
    }

    fun clearRecordingFlow() {
        recordingPlannedExercise.value = null
        recordingFlow.value = RecordingFlow.SINGLE
    }
}
