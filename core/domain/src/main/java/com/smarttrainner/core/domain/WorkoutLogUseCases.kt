package com.smarttrainner.core.domain

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
