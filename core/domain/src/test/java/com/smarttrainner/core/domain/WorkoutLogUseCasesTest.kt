package com.smarttrainner.core.domain

import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.WorkoutLogId
import com.smarttrainner.core.model.WorkoutLogInput
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class WorkoutLogUseCasesTest {
    @Test
    fun updateWorkoutLog_delegatesToRepository() = runTest {
        val repository = FakeWorkoutLogRepository()
        val useCase = UpdateWorkoutLogUseCase(repository)
        val id = WorkoutLogId(42)
        val input = workoutInput()

        val result = useCase(id, input)

        assertThat(result.isSuccess).isTrue()
        assertThat(repository.updatedIds).containsExactly(id)
        assertThat(repository.updatedInputs).containsExactly(input)
    }
}

private class FakeWorkoutLogRepository : WorkoutLogRepository {
    private val logs = MutableStateFlow(emptyList<WorkoutLog>())
    val updatedIds = mutableListOf<WorkoutLogId>()
    val updatedInputs = mutableListOf<WorkoutLogInput>()

    override fun observeLatestWorkoutLogs(): Flow<List<WorkoutLog>> = logs

    override fun observeAllWorkoutLogs(): Flow<List<WorkoutLog>> = logs

    override suspend fun updateWorkoutLog(id: WorkoutLogId, input: WorkoutLogInput): Result<Unit> {
        updatedIds += id
        updatedInputs += input
        return Result.success(Unit)
    }
}

private fun workoutInput() = WorkoutLogInput(
    plannedExerciseId = PlannedExerciseId("routine-day-1-leg_press"),
    exerciseId = ExerciseId("leg_press"),
    performedAt = LocalDateTime.parse("2026-06-17T12:00:00"),
    sets = 1,
    reps = 10,
    weightKg = 80.0,
    durationMinutes = null,
    memo = "calendar edit",
    completed = true
)
