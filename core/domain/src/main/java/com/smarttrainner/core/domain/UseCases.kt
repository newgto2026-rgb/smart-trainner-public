package com.smarttrainner.core.domain

import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.WorkoutLogInput
import java.time.LocalDate
import javax.inject.Inject

class ObserveExercisesUseCase @Inject constructor(
    private val repository: ExerciseRepository
) {
    operator fun invoke() = repository.observeExercises()
}

class ObserveCurrentWeeklyPlanUseCase @Inject constructor(
    private val repository: WeeklyPlanRepository
) {
    operator fun invoke(weekStartDate: LocalDate) = repository.observeCurrentWeeklyPlan(weekStartDate)
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

class ObserveWeeklySummaryUseCase @Inject constructor(
    private val repository: WeeklySummaryRepository
) {
    operator fun invoke(weekStartDate: LocalDate) = repository.observeWeeklySummary(weekStartDate)
}

class GetExerciseUseCase @Inject constructor(
    private val repository: ExerciseRepository
) {
    suspend operator fun invoke(id: ExerciseId) = repository.getExercise(id)
}

class GetLatestWorkoutLogUseCase @Inject constructor(
    private val repository: WorkoutLogRepository
) {
    suspend operator fun invoke(exerciseId: ExerciseId) = repository.getLatestWorkoutLog(exerciseId)
}

class SaveWorkoutLogUseCase @Inject constructor(
    private val repository: WorkoutLogRepository
) {
    suspend operator fun invoke(input: WorkoutLogInput) = repository.saveWorkoutLog(input)
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
