package com.smarttrainner.core.domain

import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.model.CustomExerciseInput
import com.smarttrainner.core.model.DifficultyLevel
import com.smarttrainner.core.model.EquipmentType
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.ExerciseSource
import com.smarttrainner.core.model.MuscleGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
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

        assertThat(validate(validInput(repRangeStart = null, repRangeEnd = 12)))
            .isEqualTo(CustomExerciseValidationError.REPS)
        assertThat(validate(validInput(repRangeStart = 8, repRangeEnd = null)))
            .isEqualTo(CustomExerciseValidationError.REPS)
        assertThat(validate(validInput(repRangeStart = 0, repRangeEnd = 8)))
            .isEqualTo(CustomExerciseValidationError.REPS)
        assertThat(validate(validInput(repRangeStart = 8, repRangeEnd = 101)))
            .isEqualTo(CustomExerciseValidationError.REPS)
        assertThat(validate(validInput(repRangeStart = 15, repRangeEnd = 8)))
            .isEqualTo(CustomExerciseValidationError.REPS)
        assertThat(validate(validInput(id = duplicateId), existingExerciseIds = setOf(duplicateId)))
            .isEqualTo(CustomExerciseValidationError.ID)
    }

    @Test
    fun validateCustomExerciseRejectsRequiredTextAndTargetBounds() {
        assertThat(validate(validInput(name = ""))).isEqualTo(CustomExerciseValidationError.NAME)
        assertThat(validate(validInput(name = "x".repeat(61)))).isEqualTo(CustomExerciseValidationError.NAME)
        assertThat(validate(validInput(instructions = listOf("  ")))).isEqualTo(CustomExerciseValidationError.INSTRUCTIONS)
        assertThat(validate(validInput(safetyCues = listOf("  ")))).isEqualTo(CustomExerciseValidationError.SAFETY)
        assertThat(validate(validInput(sets = 0))).isEqualTo(CustomExerciseValidationError.SETS)
        assertThat(validate(validInput(sets = 13))).isEqualTo(CustomExerciseValidationError.SETS)
        assertThat(validate(validInput(restSeconds = -1))).isEqualTo(CustomExerciseValidationError.REST)
        assertThat(validate(validInput(restSeconds = 601))).isEqualTo(CustomExerciseValidationError.REST)
        assertThat(validate(validInput(duration = 0))).isEqualTo(CustomExerciseValidationError.DURATION)
        assertThat(validate(validInput(duration = 181))).isEqualTo(CustomExerciseValidationError.DURATION)
    }

    @Test
    fun saveCustomExerciseReturnsValidationFailureWithoutCallingRepository() = runTest {
        val repository = FakeExerciseRepository()
        val useCase = SaveCustomExerciseUseCase(repository, validate)

        val result = useCase(validInput(instructions = listOf("")))

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).hasMessageThat().isEqualTo(CustomExerciseValidationError.INSTRUCTIONS.name)
        assertThat(repository.savedInput).isNull()
    }

    @Test
    fun saveCustomExerciseDelegatesValidInputToRepository() = runTest {
        val repository = FakeExerciseRepository()
        val useCase = SaveCustomExerciseUseCase(repository, validate)
        val input = validInput()

        val result = useCase(input)

        assertThat(result.getOrNull()?.name).isEqualTo(input.name)
        assertThat(repository.savedInput).isEqualTo(input)
    }

    @Test
    fun archiveCustomExerciseDelegatesToRepository() = runTest {
        val repository = FakeExerciseRepository()
        val useCase = ArchiveCustomExerciseUseCase(repository)
        val exerciseId = ExerciseId("custom-exercise-1")

        val result = useCase(exerciseId)

        assertThat(result.isSuccess).isTrue()
        assertThat(repository.archivedId).isEqualTo(exerciseId)
    }

    private fun validInput(
        id: ExerciseId? = null,
        name: String = "Hotel Cable Row",
        instructions: List<String> = listOf("Pull toward the ribs."),
        safetyCues: List<String> = listOf("Keep the shoulders down."),
        sets: Int = 3,
        repRangeStart: Int? = 8,
        repRangeEnd: Int? = 12,
        duration: Int? = null,
        restSeconds: Int = 90
    ) = CustomExerciseInput(
        id = id,
        name = name,
        muscleGroup = MuscleGroup.BACK,
        equipment = EquipmentType.CABLE,
        difficulty = DifficultyLevel.INTERMEDIATE,
        imageUri = null,
        summary = "A custom row.",
        instructions = instructions,
        safetyCues = safetyCues,
        defaultSets = sets,
        repRangeStart = repRangeStart,
        repRangeEnd = repRangeEnd,
        defaultDurationMinutes = duration,
        restSeconds = restSeconds
    )
}

private class FakeExerciseRepository : ExerciseRepository {
    var savedInput: CustomExerciseInput? = null
    var archivedId: ExerciseId? = null

    override fun observeExercises(): Flow<List<Exercise>> = flowOf(emptyList())

    override suspend fun getExercise(id: ExerciseId): Exercise? = null

    override suspend fun saveCustomExercise(input: CustomExerciseInput): Result<Exercise> {
        savedInput = input
        return Result.success(input.toExercise())
    }

    override suspend fun archiveCustomExercise(id: ExerciseId): Result<Unit> {
        archivedId = id
        return Result.success(Unit)
    }
}

private fun CustomExerciseInput.toExercise() = Exercise(
    id = id ?: ExerciseId("custom-exercise-1"),
    name = name,
    muscleGroup = muscleGroup,
    equipment = equipment,
    difficulty = difficulty,
    imageKey = "custom",
    summary = summary,
    instructions = instructions,
    safetyCues = safetyCues,
    defaultSets = defaultSets,
    defaultRepRange = repRangeStart?.let { start -> repRangeEnd?.let { end -> start..end } },
    defaultDurationMinutes = defaultDurationMinutes,
    restSeconds = restSeconds,
    source = ExerciseSource.USER_CREATED,
    imageUri = imageUri
)
