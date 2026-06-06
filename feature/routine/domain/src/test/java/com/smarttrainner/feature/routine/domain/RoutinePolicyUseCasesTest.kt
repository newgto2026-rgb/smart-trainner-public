package com.smarttrainner.feature.routine.domain

import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlanLevel
import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.PlanTemplateDay
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.RoutineFeeling
import com.smarttrainner.core.model.RoutineFocus
import com.smarttrainner.core.model.RoutineProgress
import com.smarttrainner.core.model.RoutineRecommendationInput
import com.smarttrainner.core.model.RoutineSource
import com.smarttrainner.core.model.RoutineStructure
import com.smarttrainner.core.model.TemplateExercise
import com.smarttrainner.core.model.TrainingExperience
import com.smarttrainner.core.model.UserSessionId
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.WorkoutLogId
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import org.junit.Test

class RoutinePolicyUseCasesTest {
    private val recommendRoutine = RecommendRoutineUseCase()
    private val evaluateReadiness = EvaluateRoutineReadinessUseCase()
    private val resolveRoutineCycleCompletion = ResolveRoutineCycleCompletionUseCase()

    @Test
    fun recommendRoutine_beginnerTwoDaysRecommendsFullBody() {
        val recommendation = recommendRoutine(
            input = RoutineRecommendationInput(
                cycleLength = 2,
                sessionMinutes = 45,
                experience = TrainingExperience.BEGINNER,
                feeling = RoutineFeeling.APP_RECOMMENDED
            ),
            templates = templates
        )

        assertThat(recommendation.primaryTemplateId).isEqualTo("beginner-full-body-2day")
    }

    @Test
    fun recommendRoutine_beginnerFiveDaysDoesNotDefaultToBodyPartSplit() {
        val recommendation = recommendRoutine(
            input = RoutineRecommendationInput(
                cycleLength = 5,
                sessionMinutes = 60,
                experience = TrainingExperience.BEGINNER,
                feeling = RoutineFeeling.FOCUSED_BODY_PART
            ),
            templates = templates
        )

        assertThat(recommendation.primaryTemplateId).isNotEqualTo("intermediate-body-part-5day")
        assertThat(templates.first { it.id == recommendation.primaryTemplateId }.structure)
            .isEqualTo(RoutineStructure.FULL_BODY)
    }

    @Test
    fun recommendRoutine_intermediateFocusedFourDaysRecommendsBodyPartSplit() {
        val recommendation = recommendRoutine(
            input = RoutineRecommendationInput(
                cycleLength = 4,
                sessionMinutes = 60,
                experience = TrainingExperience.INTERMEDIATE,
                feeling = RoutineFeeling.FOCUSED_BODY_PART
            ),
            templates = templates
        )

        assertThat(recommendation.primaryTemplateId).isEqualTo("intermediate-body-part-4day-60")
    }

    @Test
    fun recommendRoutine_balancedFullBodyKeepsIntermediateFourDayAwayFromBodyPartSplit() {
        val recommendation = recommendRoutine(
            input = RoutineRecommendationInput(
                cycleLength = 4,
                sessionMinutes = 60,
                experience = TrainingExperience.INTERMEDIATE,
                feeling = RoutineFeeling.BALANCED_FULL_BODY
            ),
            templates = templates
        )

        assertThat(recommendation.primaryTemplateId).isEqualTo("intermediate-balanced-4day")
    }

    @Test
    fun recommendRoutine_focusedBodyPartHonorsSessionLengthVariants() {
        val inputs = listOf(30, 45, 60).map { minutes ->
            RoutineRecommendationInput(
                cycleLength = 4,
                sessionMinutes = minutes,
                experience = TrainingExperience.INTERMEDIATE,
                feeling = RoutineFeeling.FOCUSED_BODY_PART
            )
        }

        val recommendations = inputs.map { recommendRoutine(it, templates).primaryTemplateId }

        assertThat(recommendations).containsExactly(
            "intermediate-body-part-4day-30",
            "intermediate-body-part-4day",
            "intermediate-body-part-4day-60"
        ).inOrder()
    }

    @Test
    fun recommendRoutine_excludesCustomRoutinesFromRecommendationCandidates() {
        val customTemplate = templates.first { it.id == "intermediate-body-part-4day-60" }
            .copy(id = "custom-lift-party", source = RoutineSource.CUSTOM)

        val recommendation = recommendRoutine(
            input = RoutineRecommendationInput(
                cycleLength = 4,
                sessionMinutes = 60,
                experience = TrainingExperience.INTERMEDIATE,
                feeling = RoutineFeeling.FOCUSED_BODY_PART
            ),
            templates = listOf(customTemplate) + templates
        )

        assertThat(recommendation.primaryTemplateId).isEqualTo("intermediate-body-part-4day-60")
        assertThat(recommendation.alternativeTemplateIds).doesNotContain("custom-lift-party")
    }

    @Test
    fun recommendRoutine_excludesTemplatesOverTargetByEstimatedSessionMinutes() {
        val overBudget = template(
            id = "over-budget-45-label",
            cycleLength = 4,
            sessionMinutes = 45,
            structure = RoutineStructure.BODY_PART_SPLIT,
            experience = TrainingExperience.INTERMEDIATE,
            focusSummary = bodyPartFocus,
            days = listOf(estimatedDurationTemplateDay(minutes = 58))
        )
        val withinBudget = template(
            id = "within-budget-45",
            cycleLength = 4,
            sessionMinutes = 45,
            structure = RoutineStructure.BODY_PART_SPLIT,
            experience = TrainingExperience.INTERMEDIATE,
            focusSummary = bodyPartFocus,
            days = listOf(estimatedDurationTemplateDay(minutes = 45))
        )

        val recommendation = recommendRoutine(
            input = RoutineRecommendationInput(
                cycleLength = 4,
                sessionMinutes = 45,
                experience = TrainingExperience.INTERMEDIATE,
                feeling = RoutineFeeling.FOCUSED_BODY_PART
            ),
            templates = listOf(overBudget, withinBudget)
        )

        assertThat(recommendation.primaryTemplateId).isEqualTo("within-budget-45")
    }

    @Test
    fun recommendRoutine_usesTenMinuteToleranceForSessionCandidates() {
        val outsideTolerance = template(
            id = "outside-tolerance-45",
            cycleLength = 4,
            sessionMinutes = 45,
            structure = RoutineStructure.BODY_PART_SPLIT,
            experience = TrainingExperience.INTERMEDIATE,
            focusSummary = bodyPartFocus,
            days = listOf(estimatedDurationTemplateDay(minutes = 56))
        )
        val insideTolerance = template(
            id = "inside-tolerance-45",
            cycleLength = 4,
            sessionMinutes = 45,
            structure = RoutineStructure.BODY_PART_SPLIT,
            experience = TrainingExperience.INTERMEDIATE,
            focusSummary = bodyPartFocus,
            days = listOf(estimatedDurationTemplateDay(minutes = 55))
        )

        val recommendation = recommendRoutine(
            input = RoutineRecommendationInput(
                cycleLength = 4,
                sessionMinutes = 45,
                experience = TrainingExperience.INTERMEDIATE,
                feeling = RoutineFeeling.FOCUSED_BODY_PART
            ),
            templates = listOf(outsideTolerance, insideTolerance)
        )

        assertThat(recommendation.primaryTemplateId).isEqualTo("inside-tolerance-45")
    }

    @Test
    fun recommendRoutine_keepsRequestedFrequencyWhenExactMatchExceedsTargetMinutes() {
        val twoDayWithinBudget = template(
            id = "two-day-within-budget",
            cycleLength = 2,
            sessionMinutes = 30,
            structure = RoutineStructure.FULL_BODY,
            experience = TrainingExperience.BEGINNER,
            focusSummary = listOf(RoutineFocus.FULL_BODY)
        )
        val threeDayOverBudget = template(
            id = "three-day-over-budget",
            cycleLength = 3,
            sessionMinutes = 45,
            structure = RoutineStructure.FULL_BODY,
            experience = TrainingExperience.BEGINNER,
            focusSummary = listOf(RoutineFocus.FULL_BODY),
            days = listOf(estimatedTemplateDay(exerciseCount = 4))
        )

        val recommendation = recommendRoutine(
            input = RoutineRecommendationInput(
                cycleLength = 3,
                sessionMinutes = 45,
                experience = TrainingExperience.BEGINNER,
                feeling = RoutineFeeling.APP_RECOMMENDED
            ),
            templates = listOf(twoDayWithinBudget, threeDayOverBudget)
        )

        assertThat(recommendation.primaryTemplateId).isEqualTo("three-day-over-budget")
    }

    @Test
    fun recommendRoutine_appRecommendedPrefersRequestedFrequencyBeforeBroaderFallback() {
        val threeDayFullBody = template(
            id = "exact-three-day-full-body",
            cycleLength = 3,
            sessionMinutes = 45,
            structure = RoutineStructure.FULL_BODY,
            experience = TrainingExperience.INTERMEDIATE,
            focusSummary = listOf(RoutineFocus.FULL_BODY)
        )
        val fourDayBalanced = template(
            id = "broader-four-day-balanced",
            cycleLength = 4,
            sessionMinutes = 45,
            structure = RoutineStructure.BALANCED_SPLIT,
            experience = TrainingExperience.INTERMEDIATE,
            focusSummary = listOf(RoutineFocus.UPPER_BODY, RoutineFocus.LOWER_BODY)
        )

        val recommendation = recommendRoutine(
            input = RoutineRecommendationInput(
                cycleLength = 3,
                sessionMinutes = 45,
                experience = TrainingExperience.INTERMEDIATE,
                feeling = RoutineFeeling.APP_RECOMMENDED
            ),
            templates = listOf(threeDayFullBody, fourDayBalanced)
        )

        assertThat(recommendation.primaryTemplateId).isEqualTo("exact-three-day-full-body")
    }

    @Test
    fun resolveRoutineCycleCompletion_excludesLogsBeforeCurrentCycle() {
        val currentCycleStart = Instant.parse("2026-05-24T12:00:00Z")
        val progress = RoutineProgress(
            templateId = "intermediate-body-part-4day",
            dayIndex = 0,
            lastCompletedDayIndex = 3,
            lastCompletedAt = currentCycleStart,
            startedAt = Instant.parse("2026-05-20T00:00:00Z"),
            cycleStartedAt = currentCycleStart
        )

        val result = resolveRoutineCycleCompletion(
            logs = listOf(
                completedLog(
                    id = 1,
                    plannedExerciseId = "day-1-exercise",
                    performedAt = LocalDateTime.of(2026, 5, 24, 11, 59)
                ),
                completedLog(
                    id = 2,
                    plannedExerciseId = "day-2-exercise",
                    performedAt = LocalDateTime.of(2026, 5, 24, 12, 1)
                )
            ),
            progress = progress,
            zone = ZoneOffset.UTC
        )

        assertThat(result).containsExactly(PlannedExerciseId("day-2-exercise"))
    }

    @Test
    fun resolveRoutineCycleCompletion_includesMatchingRoutineDayInstanceBeforeCurrentCycle() {
        val currentCycleStart = Instant.parse("2026-05-24T12:00:00Z")
        val progress = RoutineProgress(
            templateId = "intermediate-body-part-4day",
            dayIndex = 0,
            lastCompletedDayIndex = null,
            lastCompletedAt = null,
            startedAt = currentCycleStart,
            cycleStartedAt = currentCycleStart
        )
        val dayOneExercise = PlannedExerciseId("day-1-exercise")
        val routineDayInstanceId = "routine-day|intermediate-body-part-4day|cycle1|day1"

        val result = resolveRoutineCycleCompletion(
            logs = listOf(
                completedLog(
                    id = 1,
                    plannedExerciseId = "stale-exercise",
                    performedAt = LocalDateTime.of(2026, 5, 24, 11, 30)
                ),
                completedLog(
                    id = 2,
                    plannedExerciseId = dayOneExercise.value,
                    performedAt = LocalDateTime.of(2026, 5, 24, 11, 59),
                    routineDayInstanceId = routineDayInstanceId
                )
            ),
            progress = progress,
            zone = ZoneOffset.UTC,
            routineDayInstanceId = routineDayInstanceId,
            currentDayPlannedExerciseIds = setOf(dayOneExercise)
        )

        assertThat(result).containsExactly(dayOneExercise)
    }

    @Test
    fun resolveRoutineCycleCompletion_excludesRoutineLogsFromOtherTemplatesOrCycles() {
        val currentCycleStart = Instant.parse("2026-05-24T12:00:00Z")
        val progress = RoutineProgress(
            templateId = "intermediate-body-part-4day",
            dayIndex = 0,
            lastCompletedDayIndex = null,
            lastCompletedAt = null,
            cycleNumber = 2,
            startedAt = currentCycleStart,
            cycleStartedAt = currentCycleStart
        )

        val result = resolveRoutineCycleCompletion(
            logs = listOf(
                completedLog(
                    id = 1,
                    plannedExerciseId = "current-cycle-exercise",
                    performedAt = LocalDateTime.of(2026, 5, 24, 13, 0),
                    routineDayInstanceId = "routine-day|intermediate-body-part-4day|cycle2|day1"
                ),
                completedLog(
                    id = 2,
                    plannedExerciseId = "previous-cycle-exercise",
                    performedAt = LocalDateTime.of(2026, 5, 24, 14, 0),
                    routineDayInstanceId = "routine-day|intermediate-body-part-4day|cycle1|day1"
                ),
                completedLog(
                    id = 3,
                    plannedExerciseId = "other-template-exercise",
                    performedAt = LocalDateTime.of(2026, 5, 24, 15, 0),
                    routineDayInstanceId = "routine-day|other-template|cycle2|day1"
                ),
                completedLog(
                    id = 4,
                    plannedExerciseId = "ad-hoc-exercise",
                    performedAt = LocalDateTime.of(2026, 5, 24, 16, 0)
                )
            ),
            progress = progress,
            zone = ZoneOffset.UTC
        )

        assertThat(result).containsExactly(
            PlannedExerciseId("current-cycle-exercise"),
            PlannedExerciseId("ad-hoc-exercise")
        )
    }

    @Test
    fun resolveRoutineCycleCompletion_usesRoutineDayInstanceForRepeatedCurrentDay() {
        val currentCycleStart = Instant.parse("2026-05-24T12:00:00Z")
        val progress = RoutineProgress(
            templateId = "intermediate-body-part-4day",
            dayIndex = 0,
            lastCompletedDayIndex = null,
            lastCompletedAt = null,
            cycleNumber = 1,
            startedAt = currentCycleStart,
            cycleStartedAt = currentCycleStart
        )
        val dayOneExercise = PlannedExerciseId("2026-05-25_bench_press")
        val dayOneSecondExercise = PlannedExerciseId("2026-05-25_incline_press")
        val previousDayExercise = PlannedExerciseId("2026-05-24_row")

        val result = resolveRoutineCycleCompletion(
            logs = listOf(
                completedLog(
                    id = 1,
                    plannedExerciseId = dayOneSecondExercise.value,
                    performedAt = LocalDateTime.of(2026, 5, 25, 10, 0)
                ),
                completedLog(
                    id = 2,
                    plannedExerciseId = previousDayExercise.value,
                    performedAt = LocalDateTime.of(2026, 5, 24, 13, 0)
                ),
                completedLog(
                    id = 3,
                    plannedExerciseId = dayOneExercise.value,
                    performedAt = LocalDateTime.of(2026, 5, 25, 11, 0),
                    routineDayInstanceId = "routine-day|intermediate-body-part-4day|cycle1|day1"
                )
            ),
            progress = progress,
            zone = ZoneOffset.UTC,
            routineDayInstanceId = "routine-day|intermediate-body-part-4day|cycle1|day1",
            currentDayPlannedExerciseIds = setOf(dayOneExercise, dayOneSecondExercise)
        )

        assertThat(result).containsExactly(dayOneExercise, previousDayExercise)
    }

    @Test
    fun evaluateReadiness_returnsWarningBeforeMinimumRecovery() {
        val result = evaluateReadiness(
            lastCompletedAt = Instant.parse("2026-05-24T09:00:00Z"),
            now = Instant.parse("2026-05-24T20:00:00Z"),
            minRecoveryHours = 24
        )

        assertThat(result.ready).isFalse()
        assertThat(result.remainingRecoveryHours).isEqualTo(13)
        assertThat(result.warningCode).isEqualTo("minimum_recovery_not_met")
    }

    private val bodyPartFocus = listOf(
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
            focusSummary = bodyPartFocus
        ),
        template(
            id = "intermediate-body-part-4day",
            cycleLength = 4,
            sessionMinutes = 45,
            structure = RoutineStructure.BODY_PART_SPLIT,
            experience = TrainingExperience.INTERMEDIATE,
            focusSummary = bodyPartFocus
        ),
        template(
            id = "intermediate-body-part-4day-60",
            cycleLength = 4,
            sessionMinutes = 60,
            structure = RoutineStructure.BODY_PART_SPLIT,
            experience = TrainingExperience.INTERMEDIATE,
            focusSummary = bodyPartFocus
        ),
        template(
            id = "intermediate-body-part-5day",
            cycleLength = 5,
            sessionMinutes = 60,
            structure = RoutineStructure.BODY_PART_SPLIT,
            experience = TrainingExperience.INTERMEDIATE,
            focusSummary = bodyPartFocus
        )
    )

    private fun template(
        id: String,
        cycleLength: Int,
        sessionMinutes: Int,
        structure: RoutineStructure,
        experience: TrainingExperience,
        focusSummary: List<RoutineFocus>,
        days: List<PlanTemplateDay> = emptyList()
    ) = PlanTemplate(
        id = id,
        name = id,
        level = experience.toPlanLevel(),
        cycleLength = cycleLength,
        description = id,
        days = days.ifEmpty { listOf(estimatedDurationTemplateDay(minutes = sessionMinutes)) },
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

    private fun estimatedDurationTemplateDay(minutes: Int) = PlanTemplateDay(
        dayOffset = 0,
        title = "Test day",
        focus = "Test",
        exercises = listOf(
            TemplateExercise(
                exerciseId = ExerciseId("duration-exercise-$minutes"),
                sets = 1,
                repRange = null,
                durationMinutes = minutes,
                restSeconds = 0,
                note = ""
            )
        )
    )

    private fun estimatedTemplateDay(exerciseCount: Int) = PlanTemplateDay(
        dayOffset = 0,
        title = "Test day",
        focus = "Test",
        exercises = List(exerciseCount) { index ->
            TemplateExercise(
                exerciseId = ExerciseId("exercise-$index"),
                sets = 4,
                repRange = 10..12,
                durationMinutes = null,
                restSeconds = 120,
                note = "",
                repDurationSeconds = 10
            )
        }
    )

    private fun completedLog(
        id: Long,
        plannedExerciseId: String,
        performedAt: LocalDateTime,
        routineDayInstanceId: String? = null
    ) = WorkoutLog(
        id = WorkoutLogId(id),
        sessionId = UserSessionId("session"),
        plannedExerciseId = PlannedExerciseId(plannedExerciseId),
        exerciseId = ExerciseId("exercise-$id"),
        performedAt = performedAt,
        sets = 3,
        reps = 10,
        weightKg = null,
        durationMinutes = null,
        memo = "",
        completed = true,
        routineDayInstanceId = routineDayInstanceId
    )
}
