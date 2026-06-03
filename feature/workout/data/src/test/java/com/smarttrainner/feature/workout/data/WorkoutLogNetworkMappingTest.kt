package com.smarttrainner.feature.workout.data

import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.WorkoutLogInput
import com.smarttrainner.core.model.WorkoutSetLog
import java.time.LocalDateTime
import org.junit.Test

class WorkoutLogNetworkMappingTest {
    @Test
    fun workoutLogRequestKeepsStableClientIdAndSetDetails() {
        val input = WorkoutLogInput(
            plannedExerciseId = PlannedExerciseId("routine-day-1-leg_press"),
            exerciseId = ExerciseId("leg_press"),
            performedAt = LocalDateTime.parse("2026-06-01T09:30:00"),
            sets = 2,
            reps = 10,
            weightKg = 80.0,
            durationMinutes = null,
            memo = "heavy day",
            completed = true,
            setEntries = listOf(
                WorkoutSetLog(order = 1, reps = 10, weightKg = 80.0, durationMinutes = null, restSeconds = 90),
                WorkoutSetLog(order = 2, reps = 8, weightKg = 85.0, durationMinutes = null, restSeconds = 120)
            )
        )

        val clientLogId = input.clientLogId("google-user-1")
        val request = input.toNetworkRequest(clientLogId)

        assertThat(clientLogId).startsWith("android-")
        assertThat(clientLogId).isEqualTo(input.clientLogId("google-user-1"))
        assertThat(request.id).isEqualTo(clientLogId)
        assertThat(request.plannedExerciseId).isEqualTo("routine-day-1-leg_press")
        assertThat(request.notes).isEqualTo("heavy day")
        assertThat(request.sets.map { it.restSeconds }).containsExactly(90, 120).inOrder()
        assertThat(request.sets.map { it.weightKg }).containsExactly(80.0, 85.0).inOrder()
    }
}
