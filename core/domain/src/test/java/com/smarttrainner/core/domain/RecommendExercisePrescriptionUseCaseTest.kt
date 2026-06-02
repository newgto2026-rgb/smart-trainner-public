package com.smarttrainner.core.domain

import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.model.DifficultyLevel
import com.smarttrainner.core.model.EquipmentType
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.TrainingExperience
import org.junit.Test

class RecommendExercisePrescriptionUseCaseTest {
    private val useCase = RecommendExercisePrescriptionUseCase()

    @Test
    fun repExerciseRecommendationsScaleByExperienceAndMuscleGroup() {
        val compound = exercise(muscleGroup = MuscleGroup.CHEST)
        val isolation = exercise(muscleGroup = MuscleGroup.BICEPS)

        assertThat(useCase(compound, TrainingExperience.BEGINNER))
            .isEqualTo(ExercisePrescription(sets = 2, repRange = 10..12, durationMinutes = null, restSeconds = 90))
        assertThat(useCase(compound, TrainingExperience.INTERMEDIATE))
            .isEqualTo(ExercisePrescription(sets = 3, repRange = 8..12, durationMinutes = null, restSeconds = 90))
        assertThat(useCase(compound, TrainingExperience.ADVANCED))
            .isEqualTo(ExercisePrescription(sets = 4, repRange = 6..10, durationMinutes = null, restSeconds = 90))

        assertThat(useCase(isolation, TrainingExperience.BEGINNER).repRange).isEqualTo(12..15)
        assertThat(useCase(isolation, TrainingExperience.ADVANCED).repRange).isEqualTo(8..12)
    }

    @Test
    fun durationExerciseRecommendationsScaleDurationAndSetsByExperience() {
        val cardio = exercise(
            muscleGroup = MuscleGroup.CARDIO,
            defaultRepRange = null,
            defaultDurationMinutes = 20
        )

        assertThat(useCase(cardio, TrainingExperience.BEGINNER))
            .isEqualTo(ExercisePrescription(sets = 2, repRange = null, durationMinutes = 16, restSeconds = 90))
        assertThat(useCase(cardio, TrainingExperience.INTERMEDIATE))
            .isEqualTo(ExercisePrescription(sets = 3, repRange = null, durationMinutes = 20, restSeconds = 90))
        assertThat(useCase(cardio, TrainingExperience.ADVANCED))
            .isEqualTo(ExercisePrescription(sets = 4, repRange = null, durationMinutes = 25, restSeconds = 90))
    }
}

private fun exercise(
    muscleGroup: MuscleGroup,
    defaultRepRange: IntRange? = 8..12,
    defaultDurationMinutes: Int? = null
) = Exercise(
    id = ExerciseId("exercise"),
    name = "Exercise",
    muscleGroup = muscleGroup,
    equipment = EquipmentType.MACHINE,
    difficulty = DifficultyLevel.INTERMEDIATE,
    imageKey = "exercise",
    summary = "",
    instructions = emptyList(),
    safetyCues = emptyList(),
    defaultSets = 3,
    defaultRepRange = defaultRepRange,
    defaultDurationMinutes = defaultDurationMinutes,
    restSeconds = 90
)
