package com.smarttrainner.feature.calendar.domain

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.domain.ExerciseRepository
import com.smarttrainner.core.domain.WorkoutLogRepository
import com.smarttrainner.core.model.DifficultyLevel
import com.smarttrainner.core.model.EquipmentType
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.UserSessionId
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.WorkoutLogId
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ObserveWorkoutCalendarMonthUseCaseTest {
    @Test
    fun invoke_groupsLogsByDateInsideRequestedMonth() = runTest {
        val repository = FakeWorkoutCalendarRepository()
        repository.logs.value = listOf(
            workoutLog(
                id = 1,
                exerciseId = "bench",
                performedAt = LocalDateTime.of(2026, 5, 9, 18, 0),
                sets = 3,
                reps = 10,
                weightKg = 40.0
            ),
            workoutLog(
                id = 2,
                exerciseId = "row",
                performedAt = LocalDateTime.of(2026, 5, 9, 19, 0),
                sets = 4,
                reps = 8,
                weightKg = 35.0
            ),
            workoutLog(
                id = 3,
                exerciseId = "bench",
                performedAt = LocalDateTime.of(2026, 6, 1, 10, 0)
            )
        )
        val useCase = ObserveWorkoutCalendarMonthUseCase(repository, repository)

        useCase(
            month = YearMonth.of(2026, 5),
            today = LocalDate.of(2026, 5, 9)
        ).test {
            val month = awaitItem()

            assertThat(month.summariesByDate.keys).containsExactly(LocalDate.of(2026, 5, 9))
            assertThat(month.todayWorkoutCount).isEqualTo(2)
            assertThat(month.summariesByDate.getValue(LocalDate.of(2026, 5, 9)).totalSetCount)
                .isEqualTo(7)
            assertThat(month.logsByDate.getValue(LocalDate.of(2026, 5, 9)).map { it.exerciseName })
                .containsExactly("Row", "Bench press")
                .inOrder()
            cancelAndIgnoreRemainingEvents()
        }
    }
}

private class FakeWorkoutCalendarRepository : WorkoutLogRepository, ExerciseRepository {
    val logs = MutableStateFlow<List<WorkoutLog>>(emptyList())
    private val exercises = MutableStateFlow(
        listOf(
            exercise("bench", "Bench press", MuscleGroup.CHEST),
            exercise("row", "Row", MuscleGroup.BACK)
        )
    )

    override fun observeLatestWorkoutLogs(): Flow<List<WorkoutLog>> = logs

    override fun observeAllWorkoutLogs(): Flow<List<WorkoutLog>> = logs

    override fun observeExercises(): Flow<List<Exercise>> = exercises

    override suspend fun getExercise(id: ExerciseId): Exercise? =
        exercises.value.firstOrNull { it.id == id }
}

private fun workoutLog(
    id: Long,
    exerciseId: String,
    performedAt: LocalDateTime,
    sets: Int = 3,
    reps: Int? = 10,
    weightKg: Double? = 20.0,
    durationMinutes: Int? = null
) = WorkoutLog(
    id = WorkoutLogId(id),
    sessionId = UserSessionId("session"),
    plannedExerciseId = com.smarttrainner.core.model.PlannedExerciseId("planned_$id"),
    exerciseId = ExerciseId(exerciseId),
    performedAt = performedAt,
    sets = sets,
    reps = reps,
    weightKg = weightKg,
    durationMinutes = durationMinutes,
    memo = "",
    completed = true
)

private fun exercise(
    id: String,
    name: String,
    muscleGroup: MuscleGroup
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
