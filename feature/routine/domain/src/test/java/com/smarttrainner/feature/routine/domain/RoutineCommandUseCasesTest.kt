package com.smarttrainner.feature.routine.domain

import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.model.CustomRoutineDayInput
import com.smarttrainner.core.model.CustomRoutineExerciseInput
import com.smarttrainner.core.model.CustomRoutineInput
import com.smarttrainner.core.model.DifficultyLevel
import com.smarttrainner.core.model.EquipmentType
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.PlanLevel
import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.RoutineProgress
import com.smarttrainner.core.model.RoutineFocus
import com.smarttrainner.core.model.RoutineStructure
import com.smarttrainner.core.model.TrainingExperience
import com.smarttrainner.core.model.WorkoutDayPlan
import com.smarttrainner.core.model.isRoutineAdditionalExerciseId
import com.smarttrainner.core.model.routineAdditionalExerciseIdPrefix
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RoutineCommandUseCasesTest {
    private val advanceRoutineDay = AdvanceRoutineDayUseCase()
    private val validateCustomRoutine = ValidateCustomRoutineUseCase()

    @Test
    fun validateCustomRoutine_acceptsCatalogExercisesAndRepeatedExerciseSlots() {
        val result = validateCustomRoutine(
            input = customRoutine(
                exercises = listOf(
                    customExercise("squat"),
                    customExercise("squat")
                )
            ),
            availableExerciseIds = setOf(ExerciseId("squat"))
        )

        assertThat(result).isNull()
    }

    @Test
    fun validateCustomRoutine_rejectsEmptyDay() {
        val result = validateCustomRoutine(
            input = customRoutine(exercises = emptyList()),
            availableExerciseIds = setOf(ExerciseId("squat"))
        )

        assertThat(result).isEqualTo(CustomRoutineValidationError.EMPTY_DAY)
    }

    @Test
    fun validateCustomRoutine_rejectsUnknownExercise() {
        val result = validateCustomRoutine(
            input = customRoutine(exercises = listOf(customExercise("unknown"))),
            availableExerciseIds = setOf(ExerciseId("squat"))
        )

        assertThat(result).isEqualTo(CustomRoutineValidationError.UNKNOWN_EXERCISE)
    }

    @Test
    fun validateCustomRoutine_rejectsInvalidRepRange() {
        val result = validateCustomRoutine(
            input = customRoutine(
                exercises = listOf(customExercise("squat", repRangeStart = 12, repRangeEnd = 8))
            ),
            availableExerciseIds = setOf(ExerciseId("squat"))
        )

        assertThat(result).isEqualTo(CustomRoutineValidationError.REPS)
    }

    @Test
    fun validateCustomRoutine_rejectsRoutineAndExerciseBoundaryViolations() {
        val cases = listOf(
            customRoutine(name = " ") to CustomRoutineValidationError.NAME,
            customRoutine(name = "a".repeat(61)) to CustomRoutineValidationError.NAME,
            customRoutine(days = emptyList()) to CustomRoutineValidationError.DAYS,
            customRoutine(days = List(8) { customDay() }) to CustomRoutineValidationError.DAYS,
            customRoutine(days = listOf(customDay(minRecoveryHours = -1))) to CustomRoutineValidationError.REST,
            customRoutine(exercises = listOf(customExercise("squat", sets = 0))) to CustomRoutineValidationError.SETS,
            customRoutine(exercises = listOf(customExercise("squat", restSeconds = 601))) to CustomRoutineValidationError.REST,
            customRoutine(
                exercises = listOf(customExercise("squat", repRangeStart = null, repRangeEnd = null))
            ) to CustomRoutineValidationError.REPS,
            customRoutine(
                exercises = listOf(customExercise("squat", repRangeStart = null, repRangeEnd = 12))
            ) to CustomRoutineValidationError.REPS,
            customRoutine(
                exercises = listOf(customExercise("squat", repRangeStart = 8, repRangeEnd = null))
            ) to CustomRoutineValidationError.REPS,
            customRoutine(
                exercises = listOf(
                    customExercise(
                        "squat",
                        repRangeStart = null,
                        repRangeEnd = null,
                        durationMinutes = 241
                    )
                )
            ) to CustomRoutineValidationError.DURATION
        )

        cases.forEach { (input, expected) ->
            assertThat(validateCustomRoutine(input, setOf(ExerciseId("squat")))).isEqualTo(expected)
        }
    }

    @Test
    fun validateCustomRoutine_acceptsDurationOnlyExercise() {
        val result = validateCustomRoutine(
            input = customRoutine(
                exercises = listOf(
                    customExercise(
                        "plank",
                        repRangeStart = null,
                        repRangeEnd = null,
                        durationMinutes = 3
                    )
                )
            ),
            availableExerciseIds = setOf(ExerciseId("plank"))
        )

        assertThat(result).isNull()
    }

    @Test
    fun saveCustomRoutine_returnsValidationFailureWithoutCallingRepository() = runTest {
        val repository = CapturingRoutinePlanRepository()
        val saveCustomRoutine = SaveCustomRoutineUseCase(repository, validateCustomRoutine)

        val result = saveCustomRoutine(
            input = customRoutine(exercises = emptyList()),
            availableExerciseIds = setOf(ExerciseId("squat"))
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo(CustomRoutineValidationError.EMPTY_DAY.name)
        assertThat(repository.savedInput).isNull()
    }

    @Test
    fun saveCustomRoutine_delegatesValidInputToRepository() = runTest {
        val repository = CapturingRoutinePlanRepository()
        val saveCustomRoutine = SaveCustomRoutineUseCase(repository, validateCustomRoutine)
        val input = customRoutine(exercises = listOf(customExercise("squat")))

        val result = saveCustomRoutine(
            input = input,
            availableExerciseIds = setOf(ExerciseId("squat"))
        )

        assertThat(result.getOrThrow().id).isEqualTo("saved-custom")
        assertThat(repository.savedInput).isEqualTo(input)
    }

    @Test
    fun saveCustomRoutine_propagatesRepositoryFailureForValidInput() = runTest {
        val failure = IllegalStateException("save failed")
        val repository = CapturingRoutinePlanRepository(saveResult = Result.failure(failure))
        val saveCustomRoutine = SaveCustomRoutineUseCase(repository, validateCustomRoutine)
        val input = customRoutine(exercises = listOf(customExercise("squat")))

        val result = saveCustomRoutine(
            input = input,
            availableExerciseIds = setOf(ExerciseId("squat"))
        )

        assertThat(result.exceptionOrNull()).isSameInstanceAs(failure)
        assertThat(repository.savedInput).isEqualTo(input)
    }

    @Test
    fun advanceRoutineDay_wrapsToFirstDayAfterLastDay() {
        val nextDayIndex = advanceRoutineDay(completedDayIndex = 3, cycleLength = 4)

        assertThat(nextDayIndex).isEqualTo(0)
    }

    @Test
    fun advanceRoutineDay_rejectsInvalidCycleInputs() {
        val invalidCycle = runCatching { advanceRoutineDay(completedDayIndex = 0, cycleLength = 0) }
        val invalidDay = runCatching { advanceRoutineDay(completedDayIndex = 2, cycleLength = 2) }

        assertThat(invalidCycle.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(invalidDay.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun completeRoutineDay_marksNewCycleStartWhenRoutineWraps() = runTest {
        val repository = CapturingRoutineProgressRepository()
        val completedAt = Instant.parse("2026-05-24T12:00:00Z")
        val completeRoutineDay = CompleteRoutineDayUseCase(repository, advanceRoutineDay)

        completeRoutineDay(
            template = templates.first { it.id == "intermediate-body-part-4day" },
            completedDayIndex = 3,
            completedAt = completedAt
        )

        assertThat(repository.nextDayIndex).isEqualTo(0)
        assertThat(repository.newCycleStartedAt).isEqualTo(completedAt)
        assertThat(repository.markCompletedResult?.isSuccess).isTrue()
    }

    @Test
    fun completeRoutineDay_keepsCycleStartWhenRoutineContinues() = runTest {
        val repository = CapturingRoutineProgressRepository()
        val completedAt = Instant.parse("2026-05-24T12:00:00Z")
        val completeRoutineDay = CompleteRoutineDayUseCase(repository, advanceRoutineDay)

        completeRoutineDay(
            template = templates.first { it.id == "intermediate-body-part-4day" },
            completedDayIndex = 1,
            completedAt = completedAt
        )

        assertThat(repository.nextDayIndex).isEqualTo(2)
        assertThat(repository.newCycleStartedAt).isNull()
    }

    @Test
    fun completeRoutineDay_propagatesRepositoryFailureAfterComputingNextDay() = runTest {
        val failure = IllegalStateException("completion failed")
        val repository = CapturingRoutineProgressRepository(
            markCompletedResultToReturn = Result.failure(failure)
        )
        val completedAt = Instant.parse("2026-05-24T12:00:00Z")
        val completeRoutineDay = CompleteRoutineDayUseCase(repository, advanceRoutineDay)

        val result = completeRoutineDay(
            template = templates.first { it.id == "intermediate-body-part-4day" },
            completedDayIndex = 0,
            completedAt = completedAt
        )

        assertThat(result.exceptionOrNull()).isSameInstanceAs(failure)
        assertThat(repository.nextDayIndex).isEqualTo(1)
        assertThat(repository.newCycleStartedAt).isNull()
    }

    @Test
    fun cancelLatestRoutineDayCompletion_failsWhenThereIsNoLatestCompletion() = runTest {
        val repository = CapturingRoutineProgressRepository()
        val cancelLatest = CancelLatestRoutineDayCompletionUseCase(repository)

        val result = cancelLatest(
            template = templates.first { it.id == "intermediate-body-part-4day" },
            progress = RoutineProgress(
                templateId = "intermediate-body-part-4day",
                dayIndex = 0,
                lastCompletedDayIndex = null,
                lastCompletedAt = null,
                startedAt = Instant.parse("2026-05-20T00:00:00Z")
            ),
            completedDay = WorkoutDayPlan(
                date = LocalDate.of(2026, 5, 20),
                title = "Day 1",
                focus = "Chest",
                exercises = emptyList(),
                dayNumber = 1
            )
        )

        assertThat(result.isFailure).isTrue()
        assertThat(repository.restoredDayIndex).isNull()
    }

    @Test
    fun cancelLatestRoutineDayCompletion_rejectsMismatchedCompletedDay() = runTest {
        val repository = CapturingRoutineProgressRepository()
        val cancelLatest = CancelLatestRoutineDayCompletionUseCase(repository)

        val result = runCatching {
            cancelLatest(
                template = templates.first { it.id == "intermediate-body-part-4day" },
                progress = RoutineProgress(
                    templateId = "intermediate-body-part-4day",
                    dayIndex = 1,
                    lastCompletedDayIndex = 0,
                    lastCompletedAt = Instant.parse("2026-05-24T12:00:00Z"),
                    startedAt = Instant.parse("2026-05-20T00:00:00Z")
                ),
                completedDay = WorkoutDayPlan(
                    date = LocalDate.of(2026, 5, 20),
                    title = "Day 2",
                    focus = "Back",
                    exercises = emptyList(),
                    dayNumber = 2
                )
            )
        }

        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(repository.restoredDayIndex).isNull()
    }

    @Test
    fun cancelLatestRoutineDayCompletion_restoresFirstDayWithoutRemainingLatestCompletion() = runTest {
        val repository = CapturingRoutineProgressRepository()
        val cancelLatest = CancelLatestRoutineDayCompletionUseCase(repository)

        cancelLatest(
            template = templates.first { it.id == "intermediate-body-part-4day" },
            progress = RoutineProgress(
                templateId = "intermediate-body-part-4day",
                dayIndex = 1,
                lastCompletedDayIndex = 0,
                lastCompletedAt = Instant.parse("2026-05-24T12:00:00Z"),
                cycleNumber = 2,
                lastCompletedCycleNumber = null,
                startedAt = Instant.parse("2026-05-20T00:00:00Z"),
                cycleStartedAt = Instant.parse("2026-05-20T00:00:00Z")
            ),
            completedDay = WorkoutDayPlan(
                date = LocalDate.of(2026, 5, 20),
                title = "Day 1",
                focus = "Chest",
                exercises = emptyList(),
                dayNumber = 1
            )
        )

        assertThat(repository.restoredCycleNumber).isEqualTo(2)
        assertThat(repository.remainingLatestCompletion).isNull()
    }

    @Test
    fun cancelLatestRoutineDayCompletion_restoresLatestDayAndUsesRoutineDayScope() = runTest {
        val repository = CapturingRoutineProgressRepository()
        val cancelLatest = CancelLatestRoutineDayCompletionUseCase(repository)
        val progress = RoutineProgress(
            templateId = "intermediate-body-part-4day",
            dayIndex = 2,
            lastCompletedDayIndex = 1,
            lastCompletedAt = Instant.parse("2026-05-24T12:00:00Z"),
            cycleNumber = 1,
            lastCompletedCycleNumber = 1,
            lastCompletedPreviousCycleStartedAt = Instant.parse("2026-05-20T00:00:00Z"),
            startedAt = Instant.parse("2026-05-20T00:00:00Z"),
            cycleStartedAt = Instant.parse("2026-05-20T00:00:00Z")
        )
        val completedDay = WorkoutDayPlan(
            date = LocalDate.of(2026, 5, 20),
            title = "Day 2",
            focus = "Back",
            exercises = emptyList(),
            dayNumber = 2
        )

        cancelLatest(
            template = templates.first { it.id == "intermediate-body-part-4day" },
            progress = progress,
            completedDay = completedDay
        )

        assertThat(repository.restoredDayIndex).isEqualTo(1)
        assertThat(repository.restoredCycleNumber).isEqualTo(1)
        assertThat(repository.routineDayInstanceId)
            .isEqualTo("routine-day|intermediate-body-part-4day|cycle1|day2")
        assertThat(repository.additionalExerciseIdPrefix)
            .isEqualTo("routine-added|intermediate-body-part-4day|cycle1|day2|")
    }

    @Test
    fun cancelLatestRoutineDayCompletion_passesCompletedDayExerciseScope() = runTest {
        val repository = CapturingRoutineProgressRepository()
        val cancelLatest = CancelLatestRoutineDayCompletionUseCase(repository)
        val planned = plannedExercise("bench-press")

        cancelLatest(
            template = templates.first { it.id == "intermediate-body-part-4day" },
            progress = RoutineProgress(
                templateId = "intermediate-body-part-4day",
                dayIndex = 1,
                lastCompletedDayIndex = 0,
                lastCompletedAt = Instant.parse("2026-05-24T12:00:00Z"),
                cycleNumber = 1,
                lastCompletedCycleNumber = 1,
                startedAt = Instant.parse("2026-05-20T00:00:00Z"),
                cycleStartedAt = Instant.parse("2026-05-20T00:00:00Z")
            ),
            completedDay = WorkoutDayPlan(
                date = LocalDate.of(2026, 5, 20),
                title = "Day 1",
                focus = "Chest",
                exercises = listOf(planned),
                dayNumber = 1
            )
        )

        assertThat(repository.plannedExerciseIds).containsExactly(planned.id)
    }

    @Test
    fun isRoutineAdditionalExerciseId_matchesOnlyRoutineAdditionalExerciseIds() {
        val additionalId = PlannedExerciseId(
            routineAdditionalExerciseIdPrefix(
                templateId = "intermediate-body-part-4day",
                cycleNumber = 1,
                dayNumber = 2
            ) + "bench-press"
        )
        val plannedId = PlannedExerciseId("intermediate-body-part-4day-day2-bench-press")

        assertThat(additionalId.isRoutineAdditionalExerciseId()).isTrue()
        assertThat(plannedId.isRoutineAdditionalExerciseId()).isFalse()
    }

    @Test
    fun cancelLatestRoutineDayCompletion_keepsPreviousSameCycleDayCancelable() = runTest {
        val repository = CapturingRoutineProgressRepository()
        val cancelLatest = CancelLatestRoutineDayCompletionUseCase(repository)
        val cycleStartedAt = Instant.parse("2026-05-20T00:00:00Z")
        val progress = RoutineProgress(
            templateId = "intermediate-body-part-4day",
            dayIndex = 3,
            lastCompletedDayIndex = 2,
            lastCompletedAt = Instant.parse("2026-05-24T12:00:00Z"),
            cycleNumber = 1,
            lastCompletedCycleNumber = 1,
            lastCompletedPreviousCycleStartedAt = cycleStartedAt,
            startedAt = cycleStartedAt,
            cycleStartedAt = cycleStartedAt
        )
        val completedDay = WorkoutDayPlan(
            date = LocalDate.of(2026, 5, 22),
            title = "Day 3",
            focus = "Shoulders",
            exercises = emptyList(),
            dayNumber = 3
        )

        cancelLatest(
            template = templates.first { it.id == "intermediate-body-part-4day" },
            progress = progress,
            completedDay = completedDay
        )

        assertThat(repository.remainingLatestCompletion?.dayIndex).isEqualTo(1)
        assertThat(repository.remainingLatestCompletion?.cycleNumber).isEqualTo(1)
        assertThat(repository.remainingLatestCompletion?.previousCycleStartedAt).isEqualTo(cycleStartedAt)
    }

    @Test
    fun cancelLatestRoutineDayCompletion_rejectsPreviousCycleCompletion() = runTest {
        val repository = CapturingRoutineProgressRepository()
        val cancelLatest = CancelLatestRoutineDayCompletionUseCase(repository)
        val progress = RoutineProgress(
            templateId = "beginner-full-body-3day",
            dayIndex = 0,
            lastCompletedDayIndex = 2,
            lastCompletedAt = Instant.parse("2026-05-24T12:00:00Z"),
            cycleNumber = 2,
            lastCompletedCycleNumber = 1,
            startedAt = Instant.parse("2026-05-20T00:00:00Z"),
            cycleStartedAt = Instant.parse("2026-05-24T12:00:00Z")
        )
        val completedDay = WorkoutDayPlan(
            date = LocalDate.of(2026, 5, 24),
            title = "Day 3",
            focus = "Full body",
            exercises = emptyList(),
            dayNumber = 3
        )

        val result = cancelLatest(
            template = templates.first { it.id == "beginner-full-body-3day" },
            progress = progress,
            completedDay = completedDay
        )

        assertThat(result.isFailure).isTrue()
        assertThat(repository.restoredDayIndex).isNull()
        assertThat(repository.routineDayInstanceId).isNull()
    }

    private val templates = listOf(
        template(
            id = "beginner-full-body-2day",
            cycleLength = 2,
            sessionMinutes = 30,
            structure = RoutineStructure.FULL_BODY,
            experience = TrainingExperience.BEGINNER,
            focusSummary = listOf(RoutineFocus.FULL_BODY)
        ),
        template(
            id = "beginner-full-body-3day",
            cycleLength = 3,
            sessionMinutes = 45,
            structure = RoutineStructure.FULL_BODY,
            experience = TrainingExperience.BEGINNER,
            focusSummary = listOf(RoutineFocus.FULL_BODY)
        ),
        template(
            id = "intermediate-balanced-4day",
            cycleLength = 4,
            sessionMinutes = 45,
            structure = RoutineStructure.BALANCED_SPLIT,
            experience = TrainingExperience.INTERMEDIATE,
            focusSummary = listOf(RoutineFocus.UPPER_BODY, RoutineFocus.LOWER_BODY)
        ),
        template(
            id = "intermediate-body-part-4day-30",
            cycleLength = 4,
            sessionMinutes = 30,
            structure = RoutineStructure.BODY_PART_SPLIT,
            experience = TrainingExperience.INTERMEDIATE,
            focusSummary = listOf(
                RoutineFocus.BACK,
                RoutineFocus.CHEST,
                RoutineFocus.LOWER_BODY,
                RoutineFocus.SHOULDERS,
                RoutineFocus.ARMS,
                RoutineFocus.BICEPS,
                RoutineFocus.TRICEPS,
                RoutineFocus.PUSH,
                RoutineFocus.PULL
            )
        ),
        template(
            id = "intermediate-body-part-4day",
            cycleLength = 4,
            sessionMinutes = 45,
            structure = RoutineStructure.BODY_PART_SPLIT,
            experience = TrainingExperience.INTERMEDIATE,
            focusSummary = listOf(
                RoutineFocus.BACK,
                RoutineFocus.CHEST,
                RoutineFocus.LOWER_BODY,
                RoutineFocus.SHOULDERS,
                RoutineFocus.ARMS,
                RoutineFocus.BICEPS,
                RoutineFocus.TRICEPS,
                RoutineFocus.PUSH,
                RoutineFocus.PULL
            )
        ),
        template(
            id = "intermediate-body-part-4day-60",
            cycleLength = 4,
            sessionMinutes = 60,
            structure = RoutineStructure.BODY_PART_SPLIT,
            experience = TrainingExperience.INTERMEDIATE,
            focusSummary = listOf(
                RoutineFocus.BACK,
                RoutineFocus.CHEST,
                RoutineFocus.LOWER_BODY,
                RoutineFocus.SHOULDERS,
                RoutineFocus.ARMS,
                RoutineFocus.BICEPS,
                RoutineFocus.TRICEPS,
                RoutineFocus.PUSH,
                RoutineFocus.PULL
            )
        ),
        template(
            id = "intermediate-body-part-5day",
            cycleLength = 5,
            sessionMinutes = 60,
            structure = RoutineStructure.BODY_PART_SPLIT,
            experience = TrainingExperience.INTERMEDIATE,
            focusSummary = listOf(
                RoutineFocus.BACK,
                RoutineFocus.CHEST,
                RoutineFocus.LOWER_BODY,
                RoutineFocus.SHOULDERS,
                RoutineFocus.ARMS,
                RoutineFocus.BICEPS,
                RoutineFocus.TRICEPS,
                RoutineFocus.PUSH,
                RoutineFocus.PULL
            )
        )
    )

    private fun template(
        id: String,
        cycleLength: Int,
        sessionMinutes: Int,
        structure: RoutineStructure,
        experience: TrainingExperience,
        focusSummary: List<RoutineFocus>
    ) = PlanTemplate(
        id = id,
        name = id,
        level = experience.toPlanLevel(),
        cycleLength = cycleLength,
        description = id,
        days = emptyList(),
        structure = structure,
        recommendedExperience = experience,
        sessionMinutes = sessionMinutes,
        focusSummary = focusSummary
    )

    private fun TrainingExperience.toPlanLevel(): PlanLevel = when (this) {
        TrainingExperience.BEGINNER -> PlanLevel.BEGINNER
        TrainingExperience.INTERMEDIATE -> PlanLevel.INTERMEDIATE
        TrainingExperience.ADVANCED -> PlanLevel.ADVANCED
    }

    private fun customRoutine(
        name: String = "My routine",
        days: List<CustomRoutineDayInput>? = null,
        exercises: List<CustomRoutineExerciseInput> = listOf(customExercise("squat"))
    ) = CustomRoutineInput(
        name = name,
        days = days ?: listOf(customDay(exercises = exercises))
    )

    private fun customDay(
        exercises: List<CustomRoutineExerciseInput> = listOf(customExercise("squat")),
        minRecoveryHours: Int = 24
    ) = CustomRoutineDayInput(
        title = "1일차",
        focus = "하체",
        primaryFocus = RoutineFocus.LOWER_BODY,
        exercises = exercises,
        minRecoveryHours = minRecoveryHours
    )

    private fun customExercise(
        exerciseId: String,
        sets: Int = 3,
        repRangeStart: Int? = 8,
        repRangeEnd: Int? = 12,
        durationMinutes: Int? = null,
        restSeconds: Int = 90
    ) = CustomRoutineExerciseInput(
        exerciseId = ExerciseId(exerciseId),
        sets = sets,
        repRangeStart = repRangeStart,
        repRangeEnd = repRangeEnd,
        durationMinutes = durationMinutes,
        restSeconds = restSeconds
    )

    private fun plannedExercise(id: String) = PlannedExercise(
        id = PlannedExerciseId(id),
        exercise = Exercise(
            id = ExerciseId(id),
            name = id,
            muscleGroup = MuscleGroup.CHEST,
            equipment = EquipmentType.BARBELL,
            difficulty = DifficultyLevel.BEGINNER,
            imageKey = id,
            summary = "",
            instructions = emptyList(),
            safetyCues = emptyList(),
            defaultSets = 3,
            defaultRepRange = 8..12,
            defaultDurationMinutes = null,
            restSeconds = 90
        ),
        sets = 3,
        repRange = 8..12,
        durationMinutes = null,
        restSeconds = 90,
        note = ""
    )
}

private class CapturingRoutinePlanRepository(
    private val saveResult: Result<PlanTemplate> = Result.success(
        PlanTemplate(
            id = "saved-custom",
            name = "saved-custom",
            level = PlanLevel.BEGINNER,
            cycleLength = 1,
            description = "saved-custom",
            days = emptyList(),
            structure = RoutineStructure.FULL_BODY,
            recommendedExperience = TrainingExperience.BEGINNER,
            sessionMinutes = 30
        )
    )
) : RoutinePlanCommandRepository {
    var savedInput: CustomRoutineInput? = null

    override suspend fun selectPlanTemplate(templateId: String): Result<Unit> = error("Not used")

    override suspend fun saveCustomRoutine(input: CustomRoutineInput): Result<PlanTemplate> {
        savedInput = input
        return saveResult
    }

    override suspend fun deleteCustomRoutine(templateId: String): Result<Unit> = error("Not used")
}

private class CapturingRoutineProgressRepository(
    private val markCompletedResultToReturn: Result<Unit> = Result.success(Unit)
) : RoutineProgressCommandRepository {
    var nextDayIndex: Int? = null
    var newCycleStartedAt: Instant? = null
    var markCompletedResult: Result<Unit>? = null
    var restoredDayIndex: Int? = null
    var restoredCycleNumber: Int? = null
    var remainingLatestCompletion: RoutineCompletionSnapshot? = null
    var routineDayInstanceId: String? = null
    var plannedExerciseIds: Set<PlannedExerciseId>? = null
    var assignedDate: LocalDate? = null
    var additionalExerciseIdPrefix: String? = null

    override suspend fun startRoutine(templateId: String): Result<Unit> = error("Not used")

    override suspend fun switchRoutineTemplate(templateId: String): Result<Unit> = error("Not used")

    override suspend fun setRoutineDayDate(
        routineDayInstanceId: String,
        assignedDate: LocalDate,
        cycleStartedAt: Instant?
    ): Result<Unit> {
        this.routineDayInstanceId = routineDayInstanceId
        this.assignedDate = assignedDate
        this.newCycleStartedAt = cycleStartedAt
        return Result.success(Unit)
    }

    override suspend fun markRoutineDayCompleted(
        completedDayIndex: Int,
        nextDayIndex: Int,
        completedAt: Instant,
        newCycleStartedAt: Instant?
    ): Result<Unit> {
        this.nextDayIndex = nextDayIndex
        this.newCycleStartedAt = newCycleStartedAt
        return markCompletedResultToReturn.also { markCompletedResult = it }
    }

    override suspend fun cancelLatestRoutineDayCompletion(
        restoredDayIndex: Int,
        restoredCycleNumber: Int,
        restoredCycleStartedAt: Instant?,
        remainingLatestCompletion: RoutineCompletionSnapshot?,
        routineDayInstanceId: String,
        plannedExerciseIds: Set<PlannedExerciseId>,
        additionalExerciseIdPrefix: String
    ): Result<Unit> {
        this.restoredDayIndex = restoredDayIndex
        this.restoredCycleNumber = restoredCycleNumber
        this.remainingLatestCompletion = remainingLatestCompletion
        this.routineDayInstanceId = routineDayInstanceId
        this.plannedExerciseIds = plannedExerciseIds
        this.additionalExerciseIdPrefix = additionalExerciseIdPrefix
        return Result.success(Unit)
    }
}
