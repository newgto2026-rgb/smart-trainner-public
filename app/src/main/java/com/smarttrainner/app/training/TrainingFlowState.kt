package com.smarttrainner.app.training

import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExercise

internal data class TrainingFlowState(
    val recordingPlannedExercise: PlannedExercise? = null,
    val selectedExerciseId: ExerciseId? = null,
    val recordingFlow: RecordingFlow = RecordingFlow.SINGLE
) {
    val uiState: TrainingUiState
        get() = TrainingUiState(
            recordingPlannedExercise = recordingPlannedExercise,
            selectedExerciseId = selectedExerciseId
        )

    fun showExerciseDetail(exerciseId: ExerciseId): TrainingFlowState =
        copy(selectedExerciseId = exerciseId)

    fun dismissExerciseDetail(): TrainingFlowState =
        copy(selectedExerciseId = null)

    fun selectPlannedExercise(exercise: PlannedExercise): TrainingFlowState =
        copy(
            recordingFlow = RecordingFlow.SINGLE,
            recordingPlannedExercise = exercise,
            selectedExerciseId = null
        )

    fun startContinuousRecording(exercise: PlannedExercise): TrainingFlowState =
        copy(
            recordingFlow = RecordingFlow.CONTINUOUS,
            recordingPlannedExercise = exercise,
            selectedExerciseId = null
        )

    fun dismissRecordDialog(): TrainingFlowState =
        copy(
            recordingPlannedExercise = null,
            recordingFlow = RecordingFlow.SINGLE
        )

    fun recordSaved(nextPlannedExercise: PlannedExercise?): TrainingFlowState {
        val nextPlanned = if (recordingFlow == RecordingFlow.CONTINUOUS) nextPlannedExercise else null
        return if (nextPlanned != null) {
            copy(recordingPlannedExercise = nextPlanned)
        } else {
            copy(
                recordingPlannedExercise = null,
                recordingFlow = RecordingFlow.SINGLE
            )
        }
    }

    fun clearRecordingFlow(): TrainingFlowState =
        copy(
            recordingPlannedExercise = null,
            recordingFlow = RecordingFlow.SINGLE
        )
}
