package com.smarttrainner.feature.workout.data

import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.WorkoutLogInput
import com.smarttrainner.core.model.WorkoutSetLog
import java.time.LocalDateTime
import org.junit.Test

class WorkoutLogBackupMapperTest {
    @Test
    fun toBackupRequest_mapsSetLevelWorkoutLog() {
        val request = WorkoutLogInput(
            plannedExerciseId = PlannedExerciseId("plan-day-1-squat"),
            exerciseId = ExerciseId("bodyweight_squat"),
            performedAt = LocalDateTime.of(2026, 5, 31, 20, 15),
            sets = 2,
            reps = 10,
            weightKg = 20.0,
            durationMinutes = null,
            memo = "Felt solid",
            completed = true,
            setEntries = listOf(
                WorkoutSetLog(order = 2, reps = 8, weightKg = 25.0, durationMinutes = null),
                WorkoutSetLog(order = 1, reps = 10, weightKg = 20.0, durationMinutes = null)
            )
        ).toBackupRequest()

        assertThat(request.exerciseId).isEqualTo("bodyweight_squat")
        assertThat(request.plannedExerciseId).isEqualTo("plan-day-1-squat")
        assertThat(request.notes).isEqualTo("Felt solid")
        assertThat(request.date).contains("2026-05-31T20:15")
        assertThat(request.sets.map { it.setIndex }).containsExactly(1, 2).inOrder()
        assertThat(request.sets.first().reps).isEqualTo(10)
        assertThat(request.sets.first().weightKg).isEqualTo(20.0)
    }
}
