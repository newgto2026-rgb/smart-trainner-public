package com.smarttrainner.core.domain

import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.TrainingExperience
import javax.inject.Inject
import kotlin.math.roundToInt

data class ExercisePrescription(
    val sets: Int,
    val repRange: IntRange?,
    val durationMinutes: Int?,
    val restSeconds: Int
)

class RecommendExercisePrescriptionUseCase @Inject constructor() {
    operator fun invoke(
        exercise: Exercise,
        experience: TrainingExperience
    ): ExercisePrescription = if (exercise.defaultRepRange == null) {
        exercise.durationPrescription(experience)
    } else {
        exercise.repPrescription(experience)
    }

    private fun Exercise.repPrescription(experience: TrainingExperience): ExercisePrescription {
        val (sets, reps) = when (experience) {
            TrainingExperience.BEGINNER -> beginnerRepPrescription()
            TrainingExperience.INTERMEDIATE -> defaultSets.coerceIn(1, MAX_RECOMMENDED_SETS) to
                requireNotNull(defaultRepRange)
            TrainingExperience.ADVANCED -> advancedRepPrescription()
        }
        return ExercisePrescription(
            sets = sets,
            repRange = reps.coerceRepRange(),
            durationMinutes = null,
            restSeconds = restSeconds
        )
    }

    private fun Exercise.durationPrescription(experience: TrainingExperience): ExercisePrescription {
        val duration = defaultDurationMinutes ?: DEFAULT_DURATION_MINUTES
        val (setDelta, durationMultiplier) = when (experience) {
            TrainingExperience.BEGINNER -> -1 to 0.8
            TrainingExperience.INTERMEDIATE -> 0 to 1.0
            TrainingExperience.ADVANCED -> 1 to 1.25
        }
        return ExercisePrescription(
            sets = (defaultSets + setDelta).coerceIn(1, MAX_RECOMMENDED_SETS),
            repRange = null,
            durationMinutes = (duration * durationMultiplier).roundToInt().coerceIn(1, MAX_DURATION_MINUTES),
            restSeconds = restSeconds
        )
    }

    private fun Exercise.beginnerRepPrescription(): Pair<Int, IntRange> {
        val sets = (defaultSets - 1).coerceAtLeast(1)
        val reps = when (muscleGroup) {
            MuscleGroup.CORE -> 8..10
            in isolationGroups -> 12..15
            MuscleGroup.CARDIO -> defaultRepRange ?: 10..15
            else -> 10..12
        }
        return sets to reps
    }

    private fun Exercise.advancedRepPrescription(): Pair<Int, IntRange> {
        val sets = (defaultSets + 1).coerceAtMost(MAX_RECOMMENDED_SETS)
        val reps = when (muscleGroup) {
            MuscleGroup.CORE -> 12..15
            in isolationGroups -> 8..12
            MuscleGroup.CARDIO -> defaultRepRange ?: 12..20
            else -> 6..10
        }
        return sets to reps
    }

    private fun IntRange.coerceRepRange(): IntRange {
        val first = first.coerceIn(1, MAX_REPS)
        val last = last.coerceIn(first, MAX_REPS)
        return first..last
    }

    private companion object {
        const val DEFAULT_DURATION_MINUTES = 10
        const val MAX_DURATION_MINUTES = 240
        const val MAX_RECOMMENDED_SETS = 6
        const val MAX_REPS = 50

        val isolationGroups = setOf(
            MuscleGroup.ARMS,
            MuscleGroup.BICEPS,
            MuscleGroup.TRICEPS,
            MuscleGroup.FOREARMS
        )
    }
}
