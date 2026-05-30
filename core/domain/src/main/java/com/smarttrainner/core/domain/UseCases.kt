package com.smarttrainner.core.domain

import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.CustomRoutineInput
import com.smarttrainner.core.model.WorkoutLogInput
import java.time.LocalDate
import javax.inject.Inject

class ObserveExercisesUseCase @Inject constructor(
    private val repository: TrainingRepository
) {
    operator fun invoke() = repository.observeExercises()
}

class ObservePlanTemplatesUseCase @Inject constructor(
    private val repository: TrainingRepository
) {
    operator fun invoke() = repository.observePlanTemplates()
}

class ObserveCustomRoutinesUseCase @Inject constructor(
    private val repository: TrainingRepository
) {
    operator fun invoke() = repository.observeCustomRoutines()
}

class ObserveCurrentWeeklyPlanUseCase @Inject constructor(
    private val repository: TrainingRepository
) {
    operator fun invoke(weekStartDate: LocalDate) = repository.observeCurrentWeeklyPlan(weekStartDate)
}

class ObserveWorkoutLogsUseCase @Inject constructor(
    private val repository: TrainingRepository
) {
    operator fun invoke(weekStartDate: LocalDate) = repository.observeWorkoutLogs(weekStartDate)
}

class ObserveLatestWorkoutLogsUseCase @Inject constructor(
    private val repository: TrainingRepository
) {
    operator fun invoke() = repository.observeLatestWorkoutLogs()
}

class ObserveWeeklySummaryUseCase @Inject constructor(
    private val repository: TrainingRepository
) {
    operator fun invoke(weekStartDate: LocalDate) = repository.observeWeeklySummary(weekStartDate)
}

class GetExerciseUseCase @Inject constructor(
    private val repository: TrainingRepository
) {
    suspend operator fun invoke(id: ExerciseId) = repository.getExercise(id)
}

class GetLatestWorkoutLogUseCase @Inject constructor(
    private val repository: TrainingRepository
) {
    suspend operator fun invoke(exerciseId: ExerciseId) = repository.getLatestWorkoutLog(exerciseId)
}

class SaveWorkoutLogUseCase @Inject constructor(
    private val repository: TrainingRepository
) {
    suspend operator fun invoke(input: WorkoutLogInput) = repository.saveWorkoutLog(input)
}

class SelectPlanTemplateUseCase @Inject constructor(
    private val repository: TrainingRepository
) {
    suspend operator fun invoke(templateId: String) = repository.selectPlanTemplate(templateId)
}

class SaveCustomRoutineUseCase @Inject constructor(
    private val repository: TrainingRepository,
    private val validateCustomRoutine: ValidateCustomRoutineUseCase
) {
    suspend operator fun invoke(
        input: CustomRoutineInput,
        availableExerciseIds: Set<ExerciseId>
    ) = validateCustomRoutine(input, availableExerciseIds)?.let { error ->
        Result.failure(IllegalArgumentException(error.name))
    } ?: repository.saveCustomRoutine(input)
}

class DeleteCustomRoutineUseCase @Inject constructor(
    private val repository: TrainingRepository
) {
    suspend operator fun invoke(templateId: String) = repository.deleteCustomRoutine(templateId)
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
