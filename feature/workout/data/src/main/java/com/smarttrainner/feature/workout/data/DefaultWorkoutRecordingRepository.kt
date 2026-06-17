package com.smarttrainner.feature.workout.data

import com.smarttrainner.core.domain.WorkoutLogRepository
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.WorkoutLogInput
import com.smarttrainner.feature.workout.domain.WorkoutRecordingRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultWorkoutRecordingRepository @Inject constructor(
    private val workoutLogRepository: WorkoutLogRepository
) : WorkoutRecordingRepository {
    override suspend fun getLatestWorkoutLog(exerciseId: ExerciseId): WorkoutLog? =
        workoutLogRepository.getLatestWorkoutLog(exerciseId)

    override suspend fun getLatestWorkoutLog(plannedExerciseId: PlannedExerciseId): WorkoutLog? =
        workoutLogRepository.getLatestWorkoutLog(plannedExerciseId)

    override suspend fun saveWorkoutLog(input: WorkoutLogInput): Result<Unit> =
        workoutLogRepository.saveWorkoutLog(input)
}
