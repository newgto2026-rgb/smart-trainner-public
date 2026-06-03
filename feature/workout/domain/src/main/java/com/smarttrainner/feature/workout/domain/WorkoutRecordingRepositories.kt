package com.smarttrainner.feature.workout.domain

import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.WorkoutLogInput

interface WorkoutRecordingRepository {
    suspend fun getLatestWorkoutLog(exerciseId: ExerciseId): WorkoutLog?
    suspend fun getLatestWorkoutLog(plannedExerciseId: PlannedExerciseId): WorkoutLog?
    suspend fun saveWorkoutLog(input: WorkoutLogInput): Result<Unit>
}
