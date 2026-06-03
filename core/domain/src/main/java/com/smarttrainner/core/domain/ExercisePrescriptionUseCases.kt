package com.smarttrainner.core.domain

import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.TrainingExperience
import javax.inject.Inject

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
        exercise.durationPrescription()
    } else {
        exercise.repPrescription()
    }

    private fun Exercise.repPrescription(): ExercisePrescription =
        ExercisePrescription(
            sets = DEFAULT_REP_SETS,
            repRange = DEFAULT_REP_RANGE,
            durationMinutes = null,
            restSeconds = restSeconds
        )

    private fun Exercise.durationPrescription(): ExercisePrescription {
        return ExercisePrescription(
            sets = defaultSets.coerceIn(1, MAX_RECOMMENDED_SETS),
            repRange = null,
            durationMinutes = (defaultDurationMinutes ?: DEFAULT_DURATION_MINUTES).coerceIn(1, MAX_DURATION_MINUTES),
            restSeconds = restSeconds
        )
    }

    private companion object {
        const val DEFAULT_REP_SETS = 3
        val DEFAULT_REP_RANGE = 15..15
        const val DEFAULT_DURATION_MINUTES = 10
        const val MAX_DURATION_MINUTES = 240
        const val MAX_RECOMMENDED_SETS = 6
    }
}
