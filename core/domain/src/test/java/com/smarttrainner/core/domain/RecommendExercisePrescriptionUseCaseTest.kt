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
    fun repExerciseRecommendationsUseBaselineVolumeAcrossExperienceLevels() {
        val compound = exercise(muscleGroup = MuscleGroup.CHEST)
        val isolation = exercise(muscleGroup = MuscleGroup.BICEPS)
        val expected = ExercisePrescription(sets = 3, repRange = 15..15, durationMinutes = null, restSeconds = 90)

        assertThat(useCase(compound, TrainingExperience.BEGINNER)).isEqualTo(expected)
        assertThat(useCase(compound, TrainingExperience.INTERMEDIATE)).isEqualTo(expected)
        assertThat(useCase(compound, TrainingExperience.ADVANCED)).isEqualTo(expected)
        assertThat(useCase(isolation, TrainingExperience.BEGINNER)).isEqualTo(expected)
        assertThat(useCase(isolation, TrainingExperience.ADVANCED)).isEqualTo(expected)
    }

    @Test
    fun durationExerciseRecommendationsKeepExerciseDefaultsAcrossExperienceLevels() {
        val cardio = exercise(
            muscleGroup = MuscleGroup.CARDIO,
            defaultRepRange = null,
            defaultDurationMinutes = 20
        )
        val expected = ExercisePrescription(sets = 3, repRange = null, durationMinutes = 20, restSeconds = 90)

        assertThat(useCase(cardio, TrainingExperience.BEGINNER)).isEqualTo(expected)
        assertThat(useCase(cardio, TrainingExperience.INTERMEDIATE)).isEqualTo(expected)
        assertThat(useCase(cardio, TrainingExperience.ADVANCED)).isEqualTo(expected)
    }

    @Test
    fun durationExerciseRecommendationsUseFallbackAndClampSets() {
        val cardio = exercise(
            muscleGroup = MuscleGroup.CARDIO,
            defaultRepRange = null,
            defaultDurationMinutes = null,
            defaultSets = 10
        )

        val result = useCase(cardio, TrainingExperience.BEGINNER)

        assertThat(result).isEqualTo(
            ExercisePrescription(sets = 6, repRange = null, durationMinutes = 10, restSeconds = 90)
        )
    }
}

private fun exercise(
    muscleGroup: MuscleGroup,
    defaultRepRange: IntRange? = 8..12,
    defaultDurationMinutes: Int? = null,
    defaultSets: Int = 3
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
    defaultSets = defaultSets,
    defaultRepRange = defaultRepRange,
    defaultDurationMinutes = defaultDurationMinutes,
    restSeconds = 90
)
