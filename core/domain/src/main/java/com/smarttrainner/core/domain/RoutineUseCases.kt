package com.smarttrainner.core.domain

import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.CustomRoutineInput
import com.smarttrainner.core.model.ExerciseId
import java.time.Instant
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

class ObserveRoutineProgressUseCase @Inject constructor(
    private val repository: RoutineProgressRepository
) {
    operator fun invoke() = repository.observeRoutineProgress()
}

class StartRoutineUseCase @Inject constructor(
    private val repository: RoutineProgressRepository
) {
    suspend operator fun invoke(templateId: String) = repository.startRoutine(templateId)
}

class AdvanceRoutineDayUseCase @Inject constructor() {
    operator fun invoke(completedDayIndex: Int, cycleLength: Int): Int {
        require(cycleLength > 0) { "Cycle length must be positive." }
        require(completedDayIndex in 0 until cycleLength) { "Completed day index is outside the routine cycle." }
        return (completedDayIndex + 1) % cycleLength
    }
}

class CompleteRoutineDayUseCase @Inject constructor(
    private val repository: RoutineProgressRepository,
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
