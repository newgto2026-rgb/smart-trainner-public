package com.smarttrainner.core.domain

import java.time.LocalDate
import javax.inject.Inject

class ObserveWorkoutLogsUseCase @Inject constructor(
    private val repository: WorkoutLogRepository
) {
    operator fun invoke(weekStartDate: LocalDate) = repository.observeWorkoutLogs(weekStartDate)
}

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
