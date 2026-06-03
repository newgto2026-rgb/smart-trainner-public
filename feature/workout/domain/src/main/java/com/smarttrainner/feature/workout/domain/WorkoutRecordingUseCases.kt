package com.smarttrainner.feature.workout.domain

import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.WorkoutLogInput
import javax.inject.Inject

class GetLatestWorkoutLogUseCase @Inject constructor(
    private val repository: WorkoutRecordingRepository
) {
    suspend operator fun invoke(exerciseId: ExerciseId) = repository.getLatestWorkoutLog(exerciseId)
    suspend operator fun invoke(plannedExerciseId: PlannedExerciseId) =
        repository.getLatestWorkoutLog(plannedExerciseId)
}

class SaveWorkoutLogUseCase @Inject constructor(
    private val repository: WorkoutRecordingRepository
) {
    suspend operator fun invoke(input: WorkoutLogInput) = repository.saveWorkoutLog(input)
}
