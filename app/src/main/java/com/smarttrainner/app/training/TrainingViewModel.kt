package com.smarttrainner.app.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.PlannedExerciseId
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@HiltViewModel
class TrainingViewModel @Inject constructor() : ViewModel() {
    private val flowState = MutableStateFlow(TrainingFlowState())

    internal val uiState = flowState.map { it.uiState }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TrainingUiState()
    )

    fun selectExercise(exerciseId: ExerciseId) {
        flowState.update { it.showExerciseDetail(exerciseId) }
    }

    fun showExerciseMethod(exerciseId: ExerciseId) {
        flowState.update { it.showExerciseDetail(exerciseId) }
    }

    fun dismissExerciseDetail() {
        flowState.update { it.dismissExerciseDetail() }
    }

    fun selectPlannedExercise(exercise: PlannedExercise) {
        flowState.update { it.selectPlannedExercise(exercise) }
    }

    fun startContinuousRecording(exercise: PlannedExercise) {
        flowState.update { it.startContinuousRecording(exercise) }
    }

    fun dismissRecordDialog() {
        flowState.update { it.dismissRecordDialog() }
    }

    internal fun handleRecordSaved(nextPlannedExercise: PlannedExercise?): TrainingRecordSavedResult {
        var result = TrainingRecordSavedResult()
        flowState.update { current ->
            result = TrainingRecordSavedResult(
                wasContinuous = current.recordingFlow == RecordingFlow.CONTINUOUS,
                recordedPlannedExerciseIds = current.recordedPlannedExerciseIdsAfterCurrentSaved()
            )
            current.recordSaved(nextPlannedExercise)
        }
        return result
    }

    fun skipCurrentExercise(nextPlannedExercise: PlannedExercise?) {
        flowState.update { it.skipCurrentExercise(nextPlannedExercise) }
    }

    fun replaceRecordingExercise(exercise: PlannedExercise) {
        flowState.update { it.replaceRecordingExercise(exercise) }
    }

    fun recordAdditionalExercise(exercise: PlannedExercise) {
        flowState.update { it.recordAdditionalExercise(exercise) }
    }

    fun clearRecordingFlow() {
        flowState.update { it.clearRecordingFlow() }
    }
}

internal data class TrainingRecordSavedResult(
    val wasContinuous: Boolean = false,
    val recordedPlannedExerciseIds: Set<PlannedExerciseId> = emptySet()
)
