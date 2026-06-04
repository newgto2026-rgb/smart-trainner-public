package com.smarttrainner.app.training

import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.PlannedExerciseId

internal data class TrainingFlowState(
    val recordingPlannedExercise: PlannedExercise? = null,
    val selectedExerciseId: ExerciseId? = null,
    val recordingFlow: RecordingFlow = RecordingFlow.SINGLE,
    val skippedPlannedExerciseIds: Set<PlannedExerciseId> = emptySet(),
    val recordedPlannedExerciseIds: Set<PlannedExerciseId> = emptySet(),
    val pausedPlannedExercise: PlannedExercise? = null
) {
    val uiState: TrainingUiState
        get() = TrainingUiState(
            recordingPlannedExercise = recordingPlannedExercise,
            selectedExerciseId = selectedExerciseId,
            recordingFlow = recordingFlow,
            skippedPlannedExerciseIds = skippedPlannedExerciseIds,
            recordedPlannedExerciseIds = recordedPlannedExerciseIds
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
            selectedExerciseId = null,
            skippedPlannedExerciseIds = emptySet(),
            recordedPlannedExerciseIds = emptySet(),
            pausedPlannedExercise = null
        )

    fun dismissRecordDialog(): TrainingFlowState =
        copy(
            recordingPlannedExercise = null,
            recordingFlow = RecordingFlow.SINGLE,
            skippedPlannedExerciseIds = emptySet(),
            recordedPlannedExerciseIds = emptySet(),
            pausedPlannedExercise = null
        )

    fun recordSaved(nextPlannedExercise: PlannedExercise?): TrainingFlowState {
        val nextRecordedIds = recordedPlannedExerciseIdsAfterCurrentSaved()
        val paused = pausedPlannedExercise
        if (paused != null) {
            return copy(
                recordingPlannedExercise = paused,
                recordedPlannedExerciseIds = nextRecordedIds,
                pausedPlannedExercise = null
            )
        }
        val nextPlanned = if (recordingFlow == RecordingFlow.CONTINUOUS) nextPlannedExercise else null
        return if (nextPlanned != null) {
            copy(
                recordingPlannedExercise = nextPlanned,
                recordedPlannedExerciseIds = nextRecordedIds
            )
        } else {
            copy(
                recordingPlannedExercise = null,
                recordingFlow = RecordingFlow.SINGLE,
                recordedPlannedExerciseIds = emptySet(),
                pausedPlannedExercise = null
            )
        }
    }

    fun recordedPlannedExerciseIdsAfterCurrentSaved(): Set<PlannedExerciseId> {
        val current = recordingPlannedExercise
        return if (recordingFlow == RecordingFlow.CONTINUOUS && current != null) {
            recordedPlannedExerciseIds + current.id
        } else {
            recordedPlannedExerciseIds
        }
    }

    fun skipCurrentExercise(nextPlannedExercise: PlannedExercise?): TrainingFlowState {
        val current = recordingPlannedExercise ?: return this
        val paused = pausedPlannedExercise
        if (paused != null) {
            return copy(
                recordingPlannedExercise = paused,
                pausedPlannedExercise = null,
                selectedExerciseId = null
            )
        }
        return if (nextPlannedExercise != null) {
            copy(
                recordingPlannedExercise = nextPlannedExercise,
                skippedPlannedExerciseIds = skippedPlannedExerciseIds + current.id,
                selectedExerciseId = null
            )
        } else {
            copy(
                recordingPlannedExercise = null,
                recordingFlow = RecordingFlow.SINGLE,
                skippedPlannedExerciseIds = skippedPlannedExerciseIds + current.id,
                recordedPlannedExerciseIds = emptySet(),
                pausedPlannedExercise = null,
                selectedExerciseId = null
            )
        }
    }

    fun replaceRecordingExercise(exercise: PlannedExercise): TrainingFlowState =
        copy(
            recordingPlannedExercise = exercise,
            selectedExerciseId = null
        )

    fun recordAdditionalExercise(exercise: PlannedExercise): TrainingFlowState =
        copy(
            recordingPlannedExercise = exercise,
            pausedPlannedExercise = recordingPlannedExercise,
            selectedExerciseId = null
        )

    fun clearRecordingFlow(): TrainingFlowState =
        copy(
            recordingPlannedExercise = null,
            recordingFlow = RecordingFlow.SINGLE,
            skippedPlannedExerciseIds = emptySet(),
            recordedPlannedExerciseIds = emptySet(),
            pausedPlannedExercise = null
        )
}
