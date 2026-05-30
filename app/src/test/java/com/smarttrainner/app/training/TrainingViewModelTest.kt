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
import com.smarttrainner.core.model.RoutineFocus
import com.smarttrainner.core.model.WeeklyPlan
import com.smarttrainner.core.model.WorkoutDayPlan
import java.time.LocalDate
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
    fun startWorkout_saveRecordAdvancesToNextExerciseInSameDay() = runTest {
        val first = plannedExercise("back_pull")
        val second = plannedExercise("back_row")
        val plan = weeklyPlan(first, second)
        val viewModel = TrainingViewModel()

        viewModel.uiState.test {
            skipItems(1)
            viewModel.startWorkout(first)
            viewModel.handleRecordSaved(
                planned = first,
                plan = plan,
                completedIds = emptySet()
            )
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.recordingPlannedExercise?.id).isEqualTo(second.id)
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
            viewModel.handleRecordSaved(
                planned = planned,
                plan = weeklyPlan(planned),
                completedIds = emptySet()
            )
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
            viewModel.startWorkout(plannedExercise("back_pull"))
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

private fun weeklyPlan(vararg exercises: PlannedExercise) = WeeklyPlan(
    id = com.smarttrainner.core.model.PlanId("plan"),
    templateId = "template",
    name = "Template",
    weekStartDate = LocalDate.of(2026, 5, 18),
    days = listOf(
        WorkoutDayPlan(
            date = LocalDate.of(2026, 5, 18),
            title = "Day 1",
            focus = "Back",
            exercises = exercises.toList(),
            dayNumber = 1,
            primaryFocus = MuscleGroup.BACK.toRoutineFocus(),
            secondaryFocuses = emptyList(),
            minRecoveryHours = 24
        )
    )
)

private fun MuscleGroup.toRoutineFocus() = when (this) {
    MuscleGroup.BACK -> RoutineFocus.BACK
    else -> RoutineFocus.FULL_BODY
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
