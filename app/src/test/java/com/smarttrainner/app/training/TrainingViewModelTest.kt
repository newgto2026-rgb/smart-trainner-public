package com.smarttrainner.app.training

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.model.DifficultyLevel
import com.smarttrainner.core.model.EquipmentType
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.PlannedExerciseId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class TrainingViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun showExerciseMethod_keepsRecordingFlowSelected() = runTest {
        val viewModel = TrainingViewModel()
        val planned = plannedExercise("chest_press")

        viewModel.uiState.test {
            skipItems(1)
            viewModel.selectPlannedExercise(planned)
            viewModel.showExerciseMethod(planned.exercise.id)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertThat(state.selectedExerciseId?.value).isEqualTo("chest_press")
            assertThat(state.recordingPlannedExercise?.id).isEqualTo(planned.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun startContinuousRecording_saveRecordAdvancesToProvidedNextExercise() = runTest {
        val first = plannedExercise("back_pull")
        val second = plannedExercise("back_row")
        val viewModel = TrainingViewModel()

        viewModel.uiState.test {
            skipItems(1)
            viewModel.startContinuousRecording(first)
            viewModel.handleRecordSaved(nextPlannedExercise = second)
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.recordingPlannedExercise?.id).isEqualTo(second.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun startContinuousRecording_saveRecordTracksSessionRecordedExercises() = runTest {
        val first = plannedExercise("back_pull")
        val second = plannedExercise("back_row")
        val third = plannedExercise("lat_pulldown")
        val viewModel = TrainingViewModel()

        viewModel.uiState.test {
            skipItems(1)
            viewModel.startContinuousRecording(first)
            viewModel.handleRecordSaved(nextPlannedExercise = second)
            viewModel.handleRecordSaved(nextPlannedExercise = third)
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.recordedPlannedExerciseIds)
                .containsExactly(first.id, second.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun startContinuousRecording_saveRecordReturnsRecordedExercisesSynchronously() = runTest {
        val first = plannedExercise("back_pull")
        val second = plannedExercise("back_row")
        val third = plannedExercise("lat_pulldown")
        val viewModel = TrainingViewModel()

        viewModel.uiState.test {
            skipItems(1)
            viewModel.startContinuousRecording(first)

            val firstSave = viewModel.handleRecordSaved(nextPlannedExercise = second)
            val secondSave = viewModel.handleRecordSaved(nextPlannedExercise = third)
            val finalSave = viewModel.handleRecordSaved(nextPlannedExercise = null)

            assertThat(firstSave.wasContinuous).isTrue()
            assertThat(firstSave.recordedPlannedExerciseIds).containsExactly(first.id)
            assertThat(secondSave.recordedPlannedExerciseIds).containsExactly(first.id, second.id)
            assertThat(finalSave.recordedPlannedExerciseIds).containsExactly(first.id, second.id, third.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun selectPlannedExercise_saveRecordClosesSingleRecordDialog() = runTest {
        val planned = plannedExercise("chest_press")
        val viewModel = TrainingViewModel()

        viewModel.uiState.test {
            skipItems(1)
            viewModel.selectPlannedExercise(planned)
            viewModel.handleRecordSaved(nextPlannedExercise = plannedExercise("ignored_next"))
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.recordingPlannedExercise).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun clearRecordingFlow_closesRecordDialog() = runTest {
        val viewModel = TrainingViewModel()

        viewModel.uiState.test {
            skipItems(1)
            viewModel.startContinuousRecording(plannedExercise("back_pull"))
            viewModel.clearRecordingFlow()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.recordingPlannedExercise).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
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
