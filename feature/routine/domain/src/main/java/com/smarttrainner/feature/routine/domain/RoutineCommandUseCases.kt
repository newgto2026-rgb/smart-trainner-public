package com.smarttrainner.feature.routine.domain

import com.smarttrainner.core.model.CustomRoutineInput
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.RoutineProgress
import com.smarttrainner.core.model.WorkoutDayPlan
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

enum class CustomRoutineValidationError {
    NAME,
    DAYS,
    EMPTY_DAY,
    UNKNOWN_EXERCISE,
    SETS,
    REPS,
    DURATION,
    REST
}

class ValidateCustomRoutineUseCase @Inject constructor() {
    operator fun invoke(
        input: CustomRoutineInput,
        availableExerciseIds: Set<ExerciseId>
    ): CustomRoutineValidationError? {
        if (input.name.trim().isEmpty() || input.name.trim().length > MAX_ROUTINE_NAME_LENGTH) {
            return CustomRoutineValidationError.NAME
        }
        if (input.days.isEmpty() || input.days.size > MAX_ROUTINE_DAYS) {
            return CustomRoutineValidationError.DAYS
        }
        input.days.forEach { day ->
            if (day.exercises.isEmpty()) return CustomRoutineValidationError.EMPTY_DAY
            if (day.minRecoveryHours !in 0..168) return CustomRoutineValidationError.REST
            day.exercises.forEach { exercise ->
                if (exercise.exerciseId !in availableExerciseIds) {
                    return CustomRoutineValidationError.UNKNOWN_EXERCISE
                }
                if (exercise.sets !in 1..12) return CustomRoutineValidationError.SETS
                if (exercise.restSeconds !in 0..600) return CustomRoutineValidationError.REST
                val hasReps = exercise.repRangeStart != null || exercise.repRangeEnd != null
                val hasDuration = exercise.durationMinutes != null
                if (!hasReps && !hasDuration) return CustomRoutineValidationError.REPS
                if (hasReps) {
                    val start = exercise.repRangeStart ?: return CustomRoutineValidationError.REPS
                    val end = exercise.repRangeEnd ?: return CustomRoutineValidationError.REPS
                    if (start !in 1..50 || end !in 1..50 || start > end) {
                        return CustomRoutineValidationError.REPS
                    }
                }
                if (exercise.durationMinutes?.let { it !in 1..240 } == true) {
                    return CustomRoutineValidationError.DURATION
                }
            }
        }
        return null
    }

    private companion object {
        const val MAX_ROUTINE_DAYS = 7
        const val MAX_ROUTINE_NAME_LENGTH = 60
    }
}

class SelectPlanTemplateUseCase @Inject constructor(
    private val repository: RoutinePlanCommandRepository
) {
    suspend operator fun invoke(templateId: String) = repository.selectPlanTemplate(templateId)
}

class SaveCustomRoutineUseCase @Inject constructor(
    private val repository: RoutinePlanCommandRepository,
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
    private val repository: RoutinePlanCommandRepository
) {
    suspend operator fun invoke(templateId: String) = repository.deleteCustomRoutine(templateId)
}

class StartRoutineUseCase @Inject constructor(
    private val repository: RoutineProgressCommandRepository
) {
    suspend operator fun invoke(templateId: String) = repository.startRoutine(templateId)
}

class SwitchRoutineTemplateUseCase @Inject constructor(
    private val repository: RoutineProgressCommandRepository
) {
    suspend operator fun invoke(templateId: String) = repository.switchRoutineTemplate(templateId)
}

class SetRoutineDayDateUseCase @Inject constructor(
    private val repository: RoutineProgressCommandRepository
) {
    suspend operator fun invoke(
        routineDayInstanceId: String,
        assignedDate: LocalDate,
        cycleStartedAt: Instant?
    ) = repository.setRoutineDayDate(
        routineDayInstanceId = routineDayInstanceId,
        assignedDate = assignedDate,
        cycleStartedAt = cycleStartedAt
    )
}

class AdvanceRoutineDayUseCase @Inject constructor() {
    operator fun invoke(completedDayIndex: Int, cycleLength: Int): Int {
        require(cycleLength > 0) { "Cycle length must be positive." }
        require(completedDayIndex in 0 until cycleLength) { "Completed day index is outside the routine cycle." }
        return (completedDayIndex + 1) % cycleLength
    }
}

class CompleteRoutineDayUseCase @Inject constructor(
    private val repository: RoutineProgressCommandRepository,
    private val advanceRoutineDay: AdvanceRoutineDayUseCase
) {
    suspend operator fun invoke(
        template: PlanTemplate,
        completedDayIndex: Int,
        completedAt: Instant
    ): Result<Unit> {
        val nextDayIndex = advanceRoutineDay(completedDayIndex, template.cycleLength)
        val newCycleStartedAt = if (nextDayIndex == 0) completedAt else null
        return repository.markRoutineDayCompleted(
            completedDayIndex = completedDayIndex,
            nextDayIndex = nextDayIndex,
            completedAt = completedAt,
            newCycleStartedAt = newCycleStartedAt
        )
    }
}

class CancelLatestRoutineDayCompletionUseCase @Inject constructor(
    private val repository: RoutineProgressCommandRepository
) {
    suspend operator fun invoke(
        template: PlanTemplate,
        progress: RoutineProgress,
        completedDay: WorkoutDayPlan
    ): Result<Unit> {
        val completedDayIndex = progress.lastCompletedDayIndex
            ?: return Result.failure(IllegalStateException("No completed routine day can be canceled."))
        val completedCycleNumber = progress.lastCompletedCycleNumber
            ?: progress.cycleNumber
        if (completedCycleNumber != progress.cycleNumber) {
            return Result.failure(
                IllegalStateException("Completed routine days from previous cycles cannot be canceled.")
            )
        }
        val expectedDayNumber = completedDayIndex + 1
        require(completedDay.dayNumber == expectedDayNumber) {
            "Completed routine day does not match the latest completion."
        }
        val restoredCycleStartedAt = progress.lastCompletedPreviousCycleStartedAt ?: progress.cycleStartedAt
        val remainingLatestCompletion = (completedDayIndex - 1)
            .takeIf { it >= 0 }
            ?.let { previousDayIndex ->
                RoutineCompletionSnapshot(
                    dayIndex = previousDayIndex,
                    completedAt = null,
                    cycleNumber = completedCycleNumber,
                    previousCycleStartedAt = restoredCycleStartedAt
                )
            }
        return repository.cancelLatestRoutineDayCompletion(
            restoredDayIndex = completedDayIndex,
            restoredCycleNumber = completedCycleNumber,
            restoredCycleStartedAt = restoredCycleStartedAt,
            remainingLatestCompletion = remainingLatestCompletion,
            routineDayInstanceId = routineDayInstanceId(
                templateId = template.id,
                cycleNumber = completedCycleNumber,
                dayNumber = completedDay.dayNumber
            ),
            plannedExerciseIds = completedDay.exercises.map { it.id }.toSet(),
            additionalExerciseIdPrefix = routineAdditionalExerciseIdPrefix(
                templateId = template.id,
                cycleNumber = completedCycleNumber,
                dayNumber = completedDay.dayNumber
            )
        )
    }
}

fun routineDayInstanceId(
    templateId: String,
    cycleNumber: Int,
    dayNumber: Int
): String = "routine-day|$templateId|cycle$cycleNumber|day$dayNumber"

fun routineAdditionalExerciseIdPrefix(
    templateId: String,
    cycleNumber: Int,
    dayNumber: Int
): String = "$RoutineAdditionalExerciseIdPrefix$templateId|cycle$cycleNumber|day$dayNumber|"

fun PlannedExerciseId.isRoutineAdditionalExerciseId(): Boolean =
    value.startsWith(RoutineAdditionalExerciseIdPrefix)

private const val RoutineAdditionalExerciseIdPrefix = "routine-added|"
