package com.smarttrainner.core.domain

import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.model.CustomExerciseInput
import com.smarttrainner.core.model.DifficultyLevel
import com.smarttrainner.core.model.EquipmentType
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.MuscleGroup
import org.junit.Test

class CustomExerciseUseCasesTest {
    private val validate = ValidateCustomExerciseUseCase()

    @Test
    fun validateCustomExerciseAcceptsRepRangeOrDurationTarget() {
        assertThat(validate(validInput(repRangeStart = 8, repRangeEnd = 12, duration = null))).isNull()
        assertThat(validate(validInput(repRangeStart = null, repRangeEnd = null, duration = 15))).isNull()
    }

    @Test
    fun validateCustomExerciseRequiresAtLeastOneTarget() {
        val error = validate(validInput(repRangeStart = null, repRangeEnd = null, duration = null))

        assertThat(error).isEqualTo(CustomExerciseValidationError.TARGET)
    }

    @Test
    fun validateCustomExerciseRejectsInvalidRepRangeAndDuplicateId() {
        val duplicateId = ExerciseId("custom-exercise-1")

        assertThat(validate(validInput(repRangeStart = 15, repRangeEnd = 8)))
            .isEqualTo(CustomExerciseValidationError.REPS)
        assertThat(validate(validInput(id = duplicateId), existingExerciseIds = setOf(duplicateId)))
            .isEqualTo(CustomExerciseValidationError.ID)
    }

    private fun validInput(
        id: ExerciseId? = null,
        repRangeStart: Int? = 8,
        repRangeEnd: Int? = 12,
        duration: Int? = null
    ) = CustomExerciseInput(
        id = id,
        name = "Hotel Cable Row",
        muscleGroup = MuscleGroup.BACK,
        equipment = EquipmentType.CABLE,
        difficulty = DifficultyLevel.INTERMEDIATE,
        imageUri = null,
        summary = "A custom row.",
        instructions = listOf("Pull toward the ribs."),
        safetyCues = listOf("Keep the shoulders down."),
        defaultSets = 3,
        repRangeStart = repRangeStart,
        repRangeEnd = repRangeEnd,
        defaultDurationMinutes = duration,
        restSeconds = 90
    )
}
