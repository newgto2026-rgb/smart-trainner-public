package com.smarttrainner.feature.exercise.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.domain.ObserveExercisesUseCase
import com.smarttrainner.core.domain.ObserveLatestWorkoutLogsUseCase
import com.smarttrainner.core.domain.TrainingRepository
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExerciseCatalogViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeExerciseCatalogRepository()

    @Before
    fun setUp() {
        repository.reset()
    }

    @Test
    fun uiState_loadsExercisesLatestLogsAndSelectedExercise() = runTest {
        val exercises = listOf(
            exercise("chest_press", MuscleGroup.CHEST),
            exercise("back_pull", MuscleGroup.BACK)
        )
        val latestLog = workoutLog(
            id = 1,
            exerciseId = ExerciseId("back_pull"),
            performedAt = LocalDateTime.of(2026, 5, 24, 9, 0)
        )
        repository.exercises.value = exercises
        repository.latestLogs.value = listOf(latestLog)
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            viewModel.updateSelection(ExerciseId("back_pull"))
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertThat(state.exercises).containsExactlyElementsIn(exercises).inOrder()
            assertThat(state.latestWorkoutLogs).containsExactly(latestLog)
            assertThat(state.selectedExerciseId?.value).isEqualTo("back_pull")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateSelection_nullClearsSelectedExercise() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            viewModel.updateSelection(ExerciseId("chest_press"))
            advanceUntilIdle()
            viewModel.updateSelection(null)
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.selectedExerciseId).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun viewModel() = ExerciseCatalogViewModel(
        observeExercises = ObserveExercisesUseCase(repository),
        observeLatestWorkoutLogs = ObserveLatestWorkoutLogsUseCase(repository)
    )
}

private class FakeExerciseCatalogRepository : TrainingRepository {
    val exercises = MutableStateFlow<List<Exercise>>(emptyList())
    val latestLogs = MutableStateFlow<List<WorkoutLog>>(emptyList())

    fun reset() {
        exercises.value = emptyList()
        latestLogs.value = emptyList()
    }

    override fun observeExercises(): Flow<List<Exercise>> = exercises

    override fun observeLatestWorkoutLogs(): Flow<List<WorkoutLog>> = latestLogs

    override fun observePlanTemplates(): Flow<List<PlanTemplate>> = unused()

    override fun observeCustomRoutines(): Flow<List<PlanTemplate>> = unused()

    override fun observeCurrentWeeklyPlan(weekStartDate: LocalDate): Flow<WeeklyPlan> = unused()

    override fun observeRoutineProgress(): Flow<RoutineProgress> = unused()

    override fun observeWorkoutLogs(weekStartDate: LocalDate): Flow<List<WorkoutLog>> = unused()

    override fun observeWeeklySummary(weekStartDate: LocalDate): Flow<WeeklySummary> = unused()

    override suspend fun getExercise(id: ExerciseId): Exercise? = unused()

    override suspend fun getLatestWorkoutLog(exerciseId: ExerciseId): WorkoutLog? = unused()

    override suspend fun selectPlanTemplate(templateId: String): Result<Unit> = unused()

    override suspend fun startRoutine(templateId: String): Result<Unit> = unused()

    override suspend fun saveCustomRoutine(input: CustomRoutineInput): Result<PlanTemplate> = unused()

    override suspend fun deleteCustomRoutine(templateId: String): Result<Unit> = unused()

    override suspend fun markRoutineDayCompleted(
        completedDayIndex: Int,
        nextDayIndex: Int,
        completedAt: Instant,
        newCycleStartedAt: Instant?
    ): Result<Unit> = unused()

    override suspend fun saveWorkoutLog(input: WorkoutLogInput): Result<Unit> = unused()

    private fun unused(): Nothing = throw UnsupportedOperationException("Not used by exercise catalog tests")
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
