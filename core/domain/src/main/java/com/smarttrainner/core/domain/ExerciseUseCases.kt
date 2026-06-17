package com.smarttrainner.core.domain

import com.smarttrainner.core.model.CustomExerciseInput
import com.smarttrainner.core.model.ExerciseId
import javax.inject.Inject

class ObserveExercisesUseCase @Inject constructor(
    private val repository: ExerciseRepository
) {
    operator fun invoke() = repository.observeExercises()
}

enum class CustomExerciseValidationError {
    NAME,
    INSTRUCTIONS,
    SAFETY,
    SETS,
    TARGET,
    REPS,
    DURATION,
    REST,
    ID
}

class ValidateCustomExerciseUseCase @Inject constructor() {
    operator fun invoke(
        input: CustomExerciseInput,
        existingExerciseIds: Set<ExerciseId> = emptySet()
    ): CustomExerciseValidationError? {
        if (input.name.trim().length !in 1..60) return CustomExerciseValidationError.NAME
        if (input.id != null && input.id in existingExerciseIds) return CustomExerciseValidationError.ID
        if (input.instructions.map { it.trim() }.none { it.isNotEmpty() }) {
            return CustomExerciseValidationError.INSTRUCTIONS
        }
        if (input.safetyCues.map { it.trim() }.none { it.isNotEmpty() }) {
            return CustomExerciseValidationError.SAFETY
        }
        if (input.defaultSets !in 1..12) return CustomExerciseValidationError.SETS
        if (input.restSeconds !in 0..600) return CustomExerciseValidationError.REST

        val hasReps = input.repRangeStart != null || input.repRangeEnd != null
        val hasDuration = input.defaultDurationMinutes != null
        if (!hasReps && !hasDuration) return CustomExerciseValidationError.TARGET
        if (hasReps) {
            val start = input.repRangeStart ?: return CustomExerciseValidationError.REPS
            val end = input.repRangeEnd ?: return CustomExerciseValidationError.REPS
            if (start !in 1..100 || end !in 1..100 || start > end) {
                return CustomExerciseValidationError.REPS
            }
        }
        if (hasDuration && input.defaultDurationMinutes !in 1..180) {
            return CustomExerciseValidationError.DURATION
        }
        return null
    }
}

class SaveCustomExerciseUseCase @Inject constructor(
    private val repository: ExerciseRepository,
    private val validateCustomExercise: ValidateCustomExerciseUseCase
) {
    suspend operator fun invoke(
        input: CustomExerciseInput,
        existingExerciseIds: Set<ExerciseId> = emptySet()
    ) = validateCustomExercise(input, existingExerciseIds)?.let { error ->
        Result.failure(IllegalArgumentException(error.name))
    } ?: repository.saveCustomExercise(input)
}

class ArchiveCustomExerciseUseCase @Inject constructor(
    private val repository: ExerciseRepository
) {
    suspend operator fun invoke(id: ExerciseId) = repository.archiveCustomExercise(id)
}
