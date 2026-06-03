package com.smarttrainner.feature.exercise.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.domain.ExerciseRepository
import com.smarttrainner.core.domain.ObserveExercisesUseCase
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
    fun uiState_loadsExercisesAndLatestLogs() = runTest {
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
            advanceUntilIdle()

            val state = awaitItem()
            assertThat(state.exercises).containsExactlyElementsIn(exercises).inOrder()
            assertThat(state.latestWorkoutLogs).containsExactly(latestLog)
            assertThat(state.selectedExerciseId).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateSearchQuery_matchesKoreanTokensInAnyOrder() = runTest {
        val legPress = exercise("leg_press", MuscleGroup.LOWER_BODY, name = "레그 프레스")
        val chestPress = exercise("machine_chest_press", MuscleGroup.CHEST, name = "머신 체스트 프레스")
        val legCurl = exercise("leg_curl", MuscleGroup.LOWER_BODY, name = "레그 컬")
        repository.exercises.value = listOf(legPress, chestPress, legCurl)
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            advanceUntilIdle()
            awaitItem()

            viewModel.updateSearchQuery("프레스 레그")

            val state = awaitItem()
            assertThat(state.searchQuery).isEqualTo("프레스 레그")
            assertThat(state.exercises).containsExactly(legPress)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateSearchQuery_matchesEnglishExerciseIdTokensInAnyOrder() = runTest {
        val legPress = exercise("leg_press", MuscleGroup.LOWER_BODY, name = "레그 프레스")
        val chestPress = exercise("machine_chest_press", MuscleGroup.CHEST, name = "머신 체스트 프레스")
        val legCurl = exercise("leg_curl", MuscleGroup.LOWER_BODY, name = "레그 컬")
        repository.exercises.value = listOf(legPress, chestPress, legCurl)
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            advanceUntilIdle()
            awaitItem()

            viewModel.updateSearchQuery("press leg")

            val state = awaitItem()
            assertThat(state.searchQuery).isEqualTo("press leg")
            assertThat(state.exercises).containsExactly(legPress)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun viewModel() = ExerciseCatalogViewModel(
        observeExercises = ObserveExercisesUseCase(repository),
        observeLatestWorkoutLogs = ObserveLatestWorkoutLogsUseCase(repository)
    )
}

private class FakeExerciseCatalogRepository :
    ExerciseRepository,
    WorkoutLogRepository {
    val exercises = MutableStateFlow<List<Exercise>>(emptyList())
    val latestLogs = MutableStateFlow<List<WorkoutLog>>(emptyList())

    fun reset() {
        exercises.value = emptyList()
        latestLogs.value = emptyList()
    }

    override fun observeExercises(): Flow<List<Exercise>> = exercises

    override fun observeLatestWorkoutLogs(): Flow<List<WorkoutLog>> = latestLogs

    override fun observeWorkoutLogs(weekStartDate: LocalDate): Flow<List<WorkoutLog>> = unused()

    override suspend fun getExercise(id: ExerciseId): Exercise? = unused()

    private fun unused(): Nothing = throw UnsupportedOperationException("Not used by exercise catalog tests")
}

private fun exercise(
    id: String,
    muscleGroup: MuscleGroup,
    name: String = id
) = Exercise(
    id = ExerciseId(id),
    name = name,
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
