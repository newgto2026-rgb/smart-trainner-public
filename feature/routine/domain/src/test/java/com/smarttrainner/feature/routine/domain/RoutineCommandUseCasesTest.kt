package com.smarttrainner.feature.routine.domain

import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.CustomRoutineDayInput
import com.smarttrainner.core.model.CustomRoutineExerciseInput
import com.smarttrainner.core.model.CustomRoutineInput
import com.smarttrainner.core.model.PlanLevel
import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.RoutineProgress
import com.smarttrainner.core.model.RoutineFocus
import com.smarttrainner.core.model.RoutineStructure
import com.smarttrainner.core.model.TrainingExperience
import com.smarttrainner.core.model.WorkoutDayPlan
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
    fun advanceRoutineDay_wrapsToFirstDayAfterLastDay() {
        val nextDayIndex = advanceRoutineDay(completedDayIndex = 3, cycleLength = 4)

        assertThat(nextDayIndex).isEqualTo(0)
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

    private val templates = listOf(
        template(
            id = "beginner-full-body-2day",
            daysPerWeek = 2,
            sessionMinutes = 30,
            structure = RoutineStructure.FULL_BODY,
            experience = TrainingExperience.BEGINNER,
            focusSummary = listOf(RoutineFocus.FULL_BODY)
        ),
        template(
            id = "beginner-full-body-3day",
            daysPerWeek = 3,
            sessionMinutes = 45,
            structure = RoutineStructure.FULL_BODY,
            experience = TrainingExperience.BEGINNER,
            focusSummary = listOf(RoutineFocus.FULL_BODY)
        ),
        template(
            id = "intermediate-balanced-4day",
            daysPerWeek = 4,
            sessionMinutes = 45,
            structure = RoutineStructure.BALANCED_SPLIT,
            experience = TrainingExperience.INTERMEDIATE,
            focusSummary = listOf(RoutineFocus.UPPER_BODY, RoutineFocus.LOWER_BODY)
        ),
        template(
            id = "intermediate-body-part-4day-30",
            daysPerWeek = 4,
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
            daysPerWeek = 4,
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
            daysPerWeek = 4,
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
            daysPerWeek = 5,
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
        daysPerWeek: Int,
        sessionMinutes: Int,
        structure: RoutineStructure,
        experience: TrainingExperience,
        focusSummary: List<RoutineFocus>
    ) = PlanTemplate(
        id = id,
        name = id,
        level = experience.toPlanLevel(),
        daysPerWeek = daysPerWeek,
        description = id,
        days = emptyList(),
        structure = structure,
        recommendedExperience = experience,
        cycleLength = daysPerWeek,
        sessionMinutes = sessionMinutes,
        focusSummary = focusSummary
    )

    private fun TrainingExperience.toPlanLevel(): PlanLevel = when (this) {
        TrainingExperience.BEGINNER -> PlanLevel.BEGINNER
        TrainingExperience.INTERMEDIATE -> PlanLevel.INTERMEDIATE
        TrainingExperience.ADVANCED -> PlanLevel.ADVANCED
    }

    private fun customRoutine(
        exercises: List<CustomRoutineExerciseInput>
    ) = CustomRoutineInput(
        name = "My routine",
        days = listOf(
            CustomRoutineDayInput(
                title = "1일차",
                focus = "하체",
                primaryFocus = RoutineFocus.LOWER_BODY,
                exercises = exercises
            )
        )
    )

    private fun customExercise(
        exerciseId: String,
        repRangeStart: Int? = 8,
        repRangeEnd: Int? = 12
    ) = CustomRoutineExerciseInput(
        exerciseId = ExerciseId(exerciseId),
        sets = 3,
        repRangeStart = repRangeStart,
        repRangeEnd = repRangeEnd,
        durationMinutes = null,
        restSeconds = 90
    )
}

private class CapturingRoutineProgressRepository : RoutineProgressCommandRepository {
    var nextDayIndex: Int? = null
    var newCycleStartedAt: Instant? = null
    var restoredDayIndex: Int? = null
    var restoredCycleNumber: Int? = null
    var remainingLatestCompletion: RoutineCompletionSnapshot? = null
    var routineDayInstanceId: String? = null
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
        return Result.success(Unit)
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
        this.additionalExerciseIdPrefix = additionalExerciseIdPrefix
        return Result.success(Unit)
    }
}
