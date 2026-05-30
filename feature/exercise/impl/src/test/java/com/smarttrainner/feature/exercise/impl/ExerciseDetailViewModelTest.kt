package com.smarttrainner.feature.exercise.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.domain.ExerciseRepository
import com.smarttrainner.core.domain.GetExerciseUseCase
import com.smarttrainner.core.domain.ObserveLatestWorkoutLogsUseCase
import com.smarttrainner.core.domain.WorkoutLogRepository
import com.smarttrainner.core.model.CustomRoutineInput
import com.smarttrainner.core.model.DifficultyLevel
import com.smarttrainner.core.model.EquipmentType
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.RoutineProgress
import com.smarttrainner.core.model.UserSessionId
import com.smarttrainner.core.model.WeeklyPlan
import com.smarttrainner.core.model.WeeklySummary
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.WorkoutLogId
import com.smarttrainner.core.model.WorkoutLogInput
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
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
class ExerciseDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeExerciseRepository()

    @Before
    fun setUp() {
        repository.reset()
    }

    @Test
    fun updateSelection_loadsExerciseAndLatestWorkoutLog() = runTest {
        val olderLog = workoutLog(
            id = 1,
            exerciseId = ExerciseId("chest_press"),
            performedAt = LocalDateTime.of(2026, 5, 23, 9, 0)
        )
        val latestLog = workoutLog(
            id = 2,
            exerciseId = ExerciseId("chest_press"),
            performedAt = LocalDateTime.of(2026, 5, 24, 9, 0)
        )
        repository.latestLogs.value = listOf(olderLog, latestLog)
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            viewModel.updateSelection(ExerciseId("chest_press"), shouldShowRecordAction = true)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertThat(state.exercise?.id?.value).isEqualTo("chest_press")
            assertThat(state.latestWorkoutLog?.id).isEqualTo(latestLog.id)
            assertThat(state.showRecordAction).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateSelection_nullClearsDetailState() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            viewModel.updateSelection(ExerciseId("chest_press"), shouldShowRecordAction = true)
            advanceUntilIdle()
            viewModel.updateSelection(null, shouldShowRecordAction = true)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertThat(state.exercise).isNull()
            assertThat(state.latestWorkoutLog).isNull()
            assertThat(state.showRecordAction).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun viewModel() = ExerciseDetailViewModel(
        observeLatestWorkoutLogs = ObserveLatestWorkoutLogsUseCase(repository),
        getExercise = GetExerciseUseCase(repository)
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

private class FakeExerciseRepository :
    ExerciseRepository,
    WorkoutLogRepository {
    val latestLogs = MutableStateFlow<List<WorkoutLog>>(emptyList())
    private val exercises = listOf(
        exercise("chest_press", MuscleGroup.CHEST),
        exercise("back_pull", MuscleGroup.BACK)
    )

    fun reset() {
        latestLogs.value = emptyList()
    }

    override fun observeLatestWorkoutLogs(): Flow<List<WorkoutLog>> = latestLogs

    override fun observeWorkoutLogs(weekStartDate: LocalDate): Flow<List<WorkoutLog>> = unused()

    override suspend fun getExercise(id: ExerciseId): Exercise? =
        exercises.firstOrNull { it.id == id }

    override fun observeExercises(): Flow<List<Exercise>> = unused()

    override suspend fun getLatestWorkoutLog(exerciseId: ExerciseId): WorkoutLog? = unused()

    override suspend fun saveWorkoutLog(input: WorkoutLogInput): Result<Unit> = unused()

    private fun unused(): Nothing = throw UnsupportedOperationException("Not used by exercise detail tests")
}

private fun exercise(id: String, muscleGroup: MuscleGroup) = Exercise(
    id = ExerciseId(id),
    name = id,
    muscleGroup = muscleGroup,
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

private fun workoutLog(
    id: Long,
    exerciseId: ExerciseId,
    performedAt: LocalDateTime
) = WorkoutLog(
    id = WorkoutLogId(id),
    sessionId = UserSessionId("local-default"),
    plannedExerciseId = PlannedExerciseId("planned_${exerciseId.value}"),
    exerciseId = exerciseId,
    performedAt = performedAt,
    sets = 3,
    reps = 8,
    weightKg = 40.0,
    durationMinutes = null,
    memo = "",
    completed = true
)
