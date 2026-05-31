package com.smarttrainner.core.domain

import com.smarttrainner.core.model.ExerciseId
import java.time.LocalDate
import javax.inject.Inject

class ObserveExercisesUseCase @Inject constructor(
    private val repository: ExerciseRepository
) {
    operator fun invoke() = repository.observeExercises()
}

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

class GetExerciseUseCase @Inject constructor(
    private val repository: ExerciseRepository
) {
    suspend operator fun invoke(id: ExerciseId) = repository.getExercise(id)
}

class ObserveActiveSessionUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    operator fun invoke() = repository.observeActiveSession()
}

class StartDefaultSessionUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    suspend operator fun invoke() = repository.startDefaultSession()
}

class SignOutUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    suspend operator fun invoke() = repository.signOut()
}
