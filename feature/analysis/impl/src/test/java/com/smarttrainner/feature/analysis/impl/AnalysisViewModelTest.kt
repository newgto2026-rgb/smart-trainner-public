package com.smarttrainner.feature.analysis.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.domain.ExerciseRepository
import com.smarttrainner.core.domain.ObserveExercisesUseCase
import com.smarttrainner.core.domain.ObserveAllWorkoutLogsUseCase
import com.smarttrainner.core.domain.ObserveRoutineProgressUseCase
import com.smarttrainner.core.domain.RoutineProgressRepository
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
import com.smarttrainner.core.model.CyclePlan
import com.smarttrainner.core.model.CycleSummary
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.WorkoutLogId
import com.smarttrainner.feature.analysis.domain.ObserveCycleSummaryUseCase
import com.smarttrainner.feature.analysis.domain.CycleSummaryRepository
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class AnalysisViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fixedClock = Clock.fixed(Instant.parse("2026-05-24T12:00:00Z"), ZoneOffset.UTC)
    private val repository = FakeAnalysisRepository()

    @Before
    fun setUp() {
        repository.reset()
    }

    @Test
    fun uiState_exposesAllRecentLogsSortedByLatest() = runTest {
        val oldest = workoutLog(
            id = 1,
            exerciseId = "chest_press",
            performedAt = LocalDateTime.of(2026, 5, 21, 9, 0)
        )
        val secondLatest = workoutLog(
            id = 2,
            exerciseId = "back_pull",
            performedAt = LocalDateTime.of(2026, 5, 23, 9, 0)
        )
        val latest = workoutLog(
            id = 3,
            exerciseId = "leg_press",
            performedAt = LocalDateTime.of(2026, 5, 24, 9, 0)
        )
        val thirdLatest = workoutLog(
            id = 4,
            exerciseId = "shoulder_raise",
            performedAt = LocalDateTime.of(2026, 5, 22, 9, 0)
        )
        repository.logs.value = listOf(oldest, secondLatest, latest, thirdLatest)
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()

            assertThat(state.recentLogs.map { it.log.performedAt })
                .containsExactly(
                    latest.performedAt,
                    secondLatest.performedAt,
                    thirdLatest.performedAt,
                    oldest.performedAt
                )
                .inOrder()
            assertThat(state.recentLogs.map { it.exercise?.id })
                .containsExactly(
                    latest.exerciseId,
                    secondLatest.exerciseId,
                    thirdLatest.exerciseId,
                    oldest.exerciseId
                )
                .inOrder()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_requestsCycleSummaryForCurrentCycle() = runTest {
        repository.progress.value = repository.progress.value.copy(cycleNumber = 4)
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()

            assertThat(repository.requestedCycleStartDates).containsExactly(LocalDate.of(2026, 5, 18))
            assertThat(state.summary?.cycleStartDate).isEqualTo(LocalDate.of(2026, 5, 18))
            assertThat(repository.requestedCycleNumbers).containsExactly(4)
            assertThat(state.cycleNumber).isEqualTo(4)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_requestsCycleSummaryFromRoutineStartWhenCycleStartIsMissing() = runTest {
        repository.progress.value = repository.progress.value.copy(
            startedAt = Instant.parse("2026-05-20T00:00:00Z"),
            cycleStartedAt = null
        )
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()

            assertThat(repository.requestedCycleStartDates).containsExactly(LocalDate.of(2026, 5, 20))
            assertThat(state.summary?.cycleStartDate).isEqualTo(LocalDate.of(2026, 5, 20))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_requestsNewCycleSummaryWhenProgressCycleStartChanges() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            assertThat(awaitItem().summary?.cycleStartDate).isEqualTo(LocalDate.of(2026, 5, 18))

            repository.progress.value = repository.progress.value.copy(
                cycleNumber = 2,
                cycleStartedAt = Instant.parse("2026-05-25T00:00:00Z")
            )
            val state = awaitItem()
            assertThat(state.summary?.cycleStartDate).isEqualTo(LocalDate.of(2026, 5, 25))
            assertThat(state.cycleNumber).isEqualTo(2)
            assertThat(repository.requestedCycleStartDates)
                .containsAtLeast(LocalDate.of(2026, 5, 18), LocalDate.of(2026, 5, 25))
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun viewModel(clock: Clock = fixedClock) = AnalysisViewModel(
        observeAllWorkoutLogs = ObserveAllWorkoutLogsUseCase(repository),
        observeCycleSummary = ObserveCycleSummaryUseCase(repository),
        observeExercises = ObserveExercisesUseCase(repository),
        observeRoutineProgress = ObserveRoutineProgressUseCase(repository),
        clock = clock
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

private class FakeAnalysisRepository :
    ExerciseRepository,
    RoutineProgressRepository,
    CycleSummaryRepository,
    WorkoutLogRepository {
    val logs = MutableStateFlow<List<WorkoutLog>>(emptyList())
    val progress = MutableStateFlow(
        RoutineProgress(
            templateId = "beginner",
            dayIndex = 0,
            lastCompletedDayIndex = null,
            lastCompletedAt = null,
            cycleNumber = 1,
            startedAt = Instant.parse("2026-05-18T00:00:00Z"),
            cycleStartedAt = Instant.parse("2026-05-18T00:00:00Z")
        )
    )
    val requestedCycleStartDates = mutableListOf<LocalDate>()
    val requestedCycleNumbers = mutableListOf<Int>()

    private val exercises = MutableStateFlow(
        listOf(
            exercise("back_pull", MuscleGroup.BACK),
            exercise("chest_press", MuscleGroup.CHEST),
            exercise("leg_press", MuscleGroup.LOWER_BODY),
            exercise("shoulder_raise", MuscleGroup.SHOULDERS)
        )
    )
    private val summary = MutableStateFlow(summary(LocalDate.of(2026, 5, 18)))

    fun reset() {
        logs.value = emptyList()
        progress.value = progress.value.copy(cycleNumber = 1)
        requestedCycleStartDates.clear()
        requestedCycleNumbers.clear()
        summary.value = summary(LocalDate.of(2026, 5, 18))
    }

    override fun observeExercises(): Flow<List<Exercise>> = exercises

    override fun observeRoutineProgress(): Flow<RoutineProgress> = progress

    override fun observeLatestWorkoutLogs(): Flow<List<WorkoutLog>> = logs

    override fun observeAllWorkoutLogs(): Flow<List<WorkoutLog>> = logs

    override fun observeCycleSummary(
        progress: RoutineProgress,
        zone: ZoneId
    ): Flow<CycleSummary> {
        val cycleStartDate = (progress.cycleStartedAt ?: progress.startedAt)
            ?.atZone(zone)
            ?.toLocalDate()
            ?: LocalDate.now(zone)
        requestedCycleStartDates += cycleStartDate
        requestedCycleNumbers += progress.cycleNumber
        summary.value = summary(cycleStartDate)
        return summary
    }

    override suspend fun getExercise(id: ExerciseId): Exercise? = unused()

    private fun unused(): Nothing = error("Not used")
}

private fun workoutLog(
    id: Long,
    exerciseId: String,
    performedAt: LocalDateTime
) = WorkoutLog(
    id = WorkoutLogId(id),
    sessionId = UserSessionId("local-default"),
    plannedExerciseId = PlannedExerciseId("planned_$exerciseId"),
    exerciseId = ExerciseId(exerciseId),
    performedAt = performedAt,
    sets = 3,
    reps = 10,
    weightKg = null,
    durationMinutes = null,
    memo = "",
    completed = true,
    setEntries = emptyList()
)

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

private fun summary(cycleStartDate: LocalDate) = CycleSummary(
    cycleStartDate = cycleStartDate,
    plannedExerciseCount = 4,
    completedExerciseCount = 0,
    totalSets = 0,
    totalVolumeKg = 0.0,
    totalMinutes = 0,
    streakDays = 0,
    muscleBalance = emptyMap(),
    insight = ""
)
