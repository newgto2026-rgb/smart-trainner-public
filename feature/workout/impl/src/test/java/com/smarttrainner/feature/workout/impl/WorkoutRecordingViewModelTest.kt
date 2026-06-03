package com.smarttrainner.feature.workout.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.domain.ObserveLatestWorkoutLogsUseCase
import com.smarttrainner.core.domain.ObserveWorkoutLogsUseCase
import com.smarttrainner.core.domain.WorkoutLogRepository
import com.smarttrainner.core.model.CustomRoutineInput
import com.smarttrainner.core.model.DifficultyLevel
import com.smarttrainner.core.model.EquipmentType
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.RoutineProgress
import com.smarttrainner.core.model.UserSessionId
import com.smarttrainner.core.model.WeeklyPlan
import com.smarttrainner.core.model.WeeklySummary
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.WorkoutLogId
import com.smarttrainner.core.model.WorkoutLogInput
import com.smarttrainner.core.model.WorkoutSetLog
import com.smarttrainner.feature.workout.domain.SaveWorkoutLogUseCase
import com.smarttrainner.feature.workout.domain.WorkoutRecordingRepository
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutRecordingViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fixedClock = Clock.fixed(Instant.parse("2026-05-24T12:00:00Z"), ZoneOffset.UTC)
    private val repository = FakeTrainingRepository()

    @Before
    fun setUp() {
        repository.reset()
    }

    @Test
    fun updatePlannedExercise_prefillsSetCountRepsAndWeightFromLatestPlannedExerciseLog() = runTest {
        repository.setLogs(
            listOf(
                repository.completedLog(
                    plannedExercise = repository.plannedExercise,
                    performedAt = LocalDateTime.of(2026, 5, 19, 7, 0),
                    setEntries = listOf(
                        WorkoutSetLog(order = 1, reps = 7, weightKg = 42.5, durationMinutes = null),
                        WorkoutSetLog(order = 2, reps = 8, weightKg = 45.0, durationMinutes = null, restSeconds = 120),
                        WorkoutSetLog(order = 3, reps = 6, weightKg = 47.5, durationMinutes = null, restSeconds = 150),
                        WorkoutSetLog(order = 4, reps = 5, weightKg = 50.0, durationMinutes = null, restSeconds = 180)
                    )
                )
            )
        )
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            advanceUntilIdle()
            viewModel.updatePlannedExercise(repository.plannedExercise)
            advanceUntilIdle()

            val setEntries = viewModel.uiState.value.recordForm.setEntries
            assertThat(setEntries.map { it.reps }).containsExactly("7", "8", "6", "5").inOrder()
            assertThat(setEntries.map { it.weightKg }).containsExactly("42.5", "45", "47.5", "50").inOrder()
            assertThat(setEntries.map { it.restSeconds }).containsExactly("90", "120", "150", "180").inOrder()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updatePlannedExercise_resetsWeightWhenSameExerciseHasNoPlannedExerciseLog() = runTest {
        repository.setLogs(
            listOf(
                repository.completedLog(
                    plannedExercise = repository.plannedExercise,
                    performedAt = LocalDateTime.of(2026, 5, 19, 7, 0),
                    setEntries = listOf(
                        WorkoutSetLog(order = 1, reps = 7, weightKg = 42.5, durationMinutes = null),
                        WorkoutSetLog(order = 2, reps = 8, weightKg = 45.0, durationMinutes = null)
                    )
                )
            )
        )
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            advanceUntilIdle()
            viewModel.updatePlannedExercise(repository.nextPlannedExerciseWithSameExercise)
            advanceUntilIdle()

            val setEntries = viewModel.uiState.value.recordForm.setEntries
            assertThat(setEntries.map { it.reps }).containsExactly("8", "8", "8").inOrder()
            assertThat(setEntries.map { it.weightKg }).containsExactly("", "", "").inOrder()
            assertThat(setEntries.map { it.restSeconds }).containsExactly("90", "90", "90").inOrder()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun saveRecord_persistsWorkoutLogAndReportsSavedPlannedExercise() = runTest {
        val viewModel = viewModel()
        var savedPlanned: PlannedExercise? = null

        viewModel.uiState.test {
            skipItems(1)
            viewModel.updatePlannedExercise(repository.plannedExercise)
            advanceUntilIdle()

            viewModel.saveRecord { savedPlanned = it }
            advanceUntilIdle()

            val input = repository.savedInputs.single()
            assertThat(savedPlanned).isEqualTo(repository.plannedExercise)
            assertThat(input.plannedExerciseId).isEqualTo(repository.plannedExercise.id)
            assertThat(input.exerciseId).isEqualTo(repository.plannedExercise.exercise.id)
            assertThat(input.performedAt).isEqualTo(LocalDateTime.of(2026, 5, 24, 12, 0))
            assertThat(input.sets).isEqualTo(3)
            assertThat(input.reps).isEqualTo(8)
            assertThat(input.setEntries).hasSize(3)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun clearRecording_resetsDialogState() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            viewModel.updatePlannedExercise(repository.plannedExercise)
            advanceUntilIdle()
            viewModel.updateSetReps(index = 0, value = "12")
            viewModel.clearRecording()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertThat(state.recordingPlannedExercise).isNull()
            assertThat(state.recordForm.setEntries).isEmpty()
            assertThat(state.formError).isNull()
            assertThat(state.recordSaved).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun viewModel() = WorkoutRecordingViewModel(
        observeWorkoutLogs = ObserveWorkoutLogsUseCase(repository),
        observeLatestWorkoutLogs = ObserveLatestWorkoutLogsUseCase(repository),
        saveWorkoutLog = SaveWorkoutLogUseCase(repository),
        clock = fixedClock
    )
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

private class FakeTrainingRepository : WorkoutLogRepository, WorkoutRecordingRepository {
    val plannedExercise = PlannedExercise(
        id = PlannedExerciseId("planned_chest_press"),
        exercise = Exercise(
            id = ExerciseId("chest_press"),
            name = "Chest press",
            muscleGroup = MuscleGroup.CHEST,
            equipment = EquipmentType.MACHINE,
            difficulty = DifficultyLevel.INTERMEDIATE,
            imageKey = "chest_press",
            summary = "",
            instructions = emptyList(),
            safetyCues = emptyList(),
            defaultSets = 3,
            defaultRepRange = 8..12,
            defaultDurationMinutes = null,
            restSeconds = 90
        ),
        sets = 3,
        repRange = 8..12,
        durationMinutes = null,
        restSeconds = 90,
        note = ""
    )
    val nextPlannedExerciseWithSameExercise = plannedExercise.copy(
        id = PlannedExerciseId("next_planned_chest_press")
    )
    val savedInputs = mutableListOf<WorkoutLogInput>()
    private val logs = MutableStateFlow<List<WorkoutLog>>(emptyList())

    fun reset() {
        savedInputs.clear()
        logs.value = emptyList()
    }

    fun setLogs(value: List<WorkoutLog>) {
        logs.value = value
    }

    fun completedLog(
        plannedExercise: PlannedExercise = this.plannedExercise,
        performedAt: LocalDateTime,
        setEntries: List<WorkoutSetLog>
    ) = WorkoutLog(
        id = WorkoutLogId(1),
        sessionId = UserSessionId("local-default"),
        plannedExerciseId = plannedExercise.id,
        exerciseId = plannedExercise.exercise.id,
        performedAt = performedAt,
        sets = setEntries.size,
        reps = setEntries.firstOrNull { it.reps != null }?.reps,
        weightKg = setEntries.firstOrNull { it.weightKg != null }?.weightKg,
        durationMinutes = null,
        memo = "",
        completed = true,
        setEntries = setEntries
    )

    override fun observeWorkoutLogs(weekStartDate: LocalDate): Flow<List<WorkoutLog>> = logs

    override fun observeLatestWorkoutLogs(): Flow<List<WorkoutLog>> = logs

    override suspend fun getLatestWorkoutLog(exerciseId: ExerciseId): WorkoutLog? =
        logs.value
            .filter { it.exerciseId == exerciseId }
            .maxByOrNull { it.performedAt }

    override suspend fun saveWorkoutLog(input: WorkoutLogInput): Result<Unit> {
        savedInputs += input
        return Result.success(Unit)
    }
}
