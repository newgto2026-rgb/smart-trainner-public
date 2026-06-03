package com.smarttrainner.app.training

import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.model.DifficultyLevel
import com.smarttrainner.core.model.EquipmentType
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.PlannedExerciseId
import org.junit.Test

class TrainingFlowStateTest {
    @Test
    fun selectPlannedExercise_startsSingleRecordingAndClearsSelectedExercise() {
        val planned = plannedExercise("chest_press")

        val state = TrainingFlowState()
            .showExerciseDetail(planned.exercise.id)
            .selectPlannedExercise(planned)

        assertThat(state.recordingFlow).isEqualTo(RecordingFlow.SINGLE)
        assertThat(state.recordingPlannedExercise).isEqualTo(planned)
        assertThat(state.selectedExerciseId).isNull()
    }

    @Test
    fun recordSaved_continuousFlowAdvancesWhenNextExerciseExists() {
        val first = plannedExercise("back_pull")
        val second = plannedExercise("back_row")

        val state = TrainingFlowState()
            .startContinuousRecording(first)
            .recordSaved(second)

        assertThat(state.recordingFlow).isEqualTo(RecordingFlow.CONTINUOUS)
        assertThat(state.recordingPlannedExercise).isEqualTo(second)
        assertThat(state.recordedPlannedExerciseIds).containsExactly(first.id)
    }

    @Test
    fun recordSaved_continuousFlowEndsWhenNextExerciseIsMissing() {
        val first = plannedExercise("back_pull")
        val state = TrainingFlowState()
            .startContinuousRecording(first)
            .recordSaved(nextPlannedExercise = null)

        assertThat(state.recordingFlow).isEqualTo(RecordingFlow.SINGLE)
        assertThat(state.recordingPlannedExercise).isNull()
        assertThat(state.recordedPlannedExerciseIds).isEmpty()
    }

    @Test
    fun skipCurrentExercise_continuousFlowAdvancesAndKeepsSkippedCandidate() {
        val first = plannedExercise("back_pull")
        val second = plannedExercise("back_row")

        val state = TrainingFlowState()
            .startContinuousRecording(first)
            .skipCurrentExercise(second)

        assertThat(state.recordingFlow).isEqualTo(RecordingFlow.CONTINUOUS)
        assertThat(state.recordingPlannedExercise).isEqualTo(second)
        assertThat(state.skippedPlannedExerciseIds).containsExactly(first.id)
    }

    @Test
    fun recordAdditionalExercise_resumesPausedRoutineExerciseAfterSave() {
        val current = plannedExercise("back_pull")
        val added = plannedExercise("extra_curl")

        val state = TrainingFlowState()
            .startContinuousRecording(current)
            .recordAdditionalExercise(added)
            .recordSaved(nextPlannedExercise = null)

        assertThat(state.recordingFlow).isEqualTo(RecordingFlow.CONTINUOUS)
        assertThat(state.recordingPlannedExercise).isEqualTo(current)
    }

    @Test
    fun skipCurrentExercise_onAdditionalExerciseResumesPausedRoutineExercise() {
        val current = plannedExercise("back_pull")
        val added = plannedExercise("extra_curl")

        val state = TrainingFlowState()
            .startContinuousRecording(current)
            .recordAdditionalExercise(added)
            .skipCurrentExercise(nextPlannedExercise = null)

        assertThat(state.recordingFlow).isEqualTo(RecordingFlow.CONTINUOUS)
        assertThat(state.recordingPlannedExercise).isEqualTo(current)
        assertThat(state.skippedPlannedExerciseIds).isEmpty()
    }
}

private fun plannedExercise(id: String) = PlannedExercise(
    id = PlannedExerciseId("planned_$id"),
    exercise = exercise(id),
    sets = 3,
    repRange = 8..12,
    durationMinutes = null,
    restSeconds = 90,
    note = ""
)

private fun exercise(id: String) = Exercise(
    id = ExerciseId(id),
    name = id,
    muscleGroup = MuscleGroup.BACK,
    equipment = EquipmentType.MACHINE,
    difficulty = DifficultyLevel.INTERMEDIATE,
    imageKey = id,
    summary = "",
    instructions = emptyList(),
    safetyCues = emptyList(),
    defaultSets = 3,
    defaultRepRange = 8..12,
    defaultDurationMinutes = null,
    restSeconds = 90
)
