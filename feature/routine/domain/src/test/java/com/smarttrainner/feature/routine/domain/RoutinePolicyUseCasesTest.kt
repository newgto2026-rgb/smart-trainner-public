package com.smarttrainner.feature.routine.domain

import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlanLevel
import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.RoutineFeeling
import com.smarttrainner.core.model.RoutineFocus
import com.smarttrainner.core.model.RoutineProgress
import com.smarttrainner.core.model.RoutineRecommendationInput
import com.smarttrainner.core.model.RoutineSource
import com.smarttrainner.core.model.RoutineStructure
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
                daysPerWeek = 2,
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
                daysPerWeek = 5,
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
                daysPerWeek = 4,
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
                daysPerWeek = 4,
                sessionMinutes = 60,
                experience = TrainingExperience.INTERMEDIATE,
                feeling = RoutineFeeling.BALANCED_FULL_BODY
            ),
            templates = templates
        )

        assertThat(recommendation.primaryTemplateId).isEqualTo("beginner-full-body-3day")
    }

    @Test
    fun recommendRoutine_focusedBodyPartHonorsSessionLengthVariants() {
        val inputs = listOf(30, 45, 60).map { minutes ->
            RoutineRecommendationInput(
                daysPerWeek = 4,
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
                daysPerWeek = 4,
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
            focusSummary = bodyPartFocus
        ),
        template(
            id = "intermediate-body-part-4day",
            daysPerWeek = 4,
            sessionMinutes = 45,
            structure = RoutineStructure.BODY_PART_SPLIT,
            experience = TrainingExperience.INTERMEDIATE,
            focusSummary = bodyPartFocus
        ),
        template(
            id = "intermediate-body-part-4day-60",
            daysPerWeek = 4,
            sessionMinutes = 60,
            structure = RoutineStructure.BODY_PART_SPLIT,
            experience = TrainingExperience.INTERMEDIATE,
            focusSummary = bodyPartFocus
        ),
        template(
            id = "intermediate-body-part-5day",
            daysPerWeek = 5,
            sessionMinutes = 60,
            structure = RoutineStructure.BODY_PART_SPLIT,
            experience = TrainingExperience.INTERMEDIATE,
            focusSummary = bodyPartFocus
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
        level = if (experience == TrainingExperience.BEGINNER) PlanLevel.BEGINNER else PlanLevel.INTERMEDIATE,
        daysPerWeek = daysPerWeek,
        description = id,
        days = emptyList(),
        structure = structure,
        recommendedExperience = experience,
        cycleLength = daysPerWeek,
        sessionMinutes = sessionMinutes,
        focusSummary = focusSummary
    )

    private fun completedLog(
        id: Long,
        plannedExerciseId: String,
        performedAt: LocalDateTime
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
        completed = true
    )
}
