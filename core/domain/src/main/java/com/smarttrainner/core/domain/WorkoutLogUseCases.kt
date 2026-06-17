package com.smarttrainner.core.domain

import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.WorkoutLogId
import com.smarttrainner.core.model.WorkoutLogInput
import javax.inject.Inject

class ObserveLatestWorkoutLogsUseCase @Inject constructor(
    private val repository: WorkoutLogRepository
) {
    operator fun invoke() = repository.observeLatestWorkoutLogs()
}

class ObserveAllWorkoutLogsUseCase @Inject constructor(
    private val repository: WorkoutLogRepository
) {
    operator fun invoke() = repository.observeAllWorkoutLogs()
}

class GetLatestWorkoutLogUseCase @Inject constructor(
    private val repository: WorkoutLogRepository
) {
    suspend operator fun invoke(exerciseId: ExerciseId) = repository.getLatestWorkoutLog(exerciseId)
    suspend operator fun invoke(plannedExerciseId: PlannedExerciseId) =
        repository.getLatestWorkoutLog(plannedExerciseId)
}

class SaveWorkoutLogUseCase @Inject constructor(
    private val repository: WorkoutLogRepository
) {
    suspend operator fun invoke(input: WorkoutLogInput) = repository.saveWorkoutLog(input)
}

class UpdateWorkoutLogUseCase @Inject constructor(
    private val repository: WorkoutLogRepository
) {
    suspend operator fun invoke(id: WorkoutLogId, input: WorkoutLogInput) =
        repository.updateWorkoutLog(id, input)
}
