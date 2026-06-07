package com.smarttrainner.feature.routine.domain

import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlanLevel
import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.PlanTemplateDay
import com.smarttrainner.core.model.RoutineFeeling
import com.smarttrainner.core.model.RoutineFocus
import com.smarttrainner.core.model.RoutineRecommendationInput
import com.smarttrainner.core.model.RoutineSource
import com.smarttrainner.core.model.RoutineStructure
import com.smarttrainner.core.model.TemplateExercise
import com.smarttrainner.core.model.TrainingExperience
import java.time.Instant
import org.junit.Test

class RoutinePolicyUseCasesTest {
    private val recommendRoutine = RecommendRoutineUseCase()
    private val evaluateReadiness = EvaluateRoutineReadinessUseCase()

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
    fun recommendRoutine_rejectsWhenNoSystemTemplatesExist() {
        val customTemplate = templates.first().copy(source = RoutineSource.CUSTOM)

        val result = runCatching {
            recommendRoutine(
                input = RoutineRecommendationInput(
                    cycleLength = 3,
                    sessionMinutes = 45,
                    experience = TrainingExperience.BEGINNER,
                    feeling = RoutineFeeling.APP_RECOMMENDED
                ),
                templates = listOf(customTemplate)
            )
        }

        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun recommendRoutine_advancedExperienceUsesAdvancedEligibleTemplate() {
        val advancedTemplate = template(
            id = "advanced-balanced-5day",
            cycleLength = 5,
            sessionMinutes = 60,
            structure = RoutineStructure.BALANCED_SPLIT,
            experience = TrainingExperience.ADVANCED,
            focusSummary = listOf(RoutineFocus.UPPER_BODY, RoutineFocus.LOWER_BODY)
        )

        val recommendation = recommendRoutine(
            input = RoutineRecommendationInput(
                cycleLength = 5,
                sessionMinutes = 60,
                experience = TrainingExperience.ADVANCED,
                feeling = RoutineFeeling.APP_RECOMMENDED
            ),
            templates = templates + advancedTemplate
        )

        assertThat(recommendation.primaryTemplateId).isEqualTo("advanced-balanced-5day")
        assertThat(recommendation.reasonCode).isEqualTo("balanced_recovery")
    }

    @Test
    fun recommendRoutine_focusedBodyPartFallsBackThroughBalancedAndFullBody() {
        val balanced = template(
            id = "balanced-without-body-part",
            cycleLength = 4,
            sessionMinutes = 45,
            structure = RoutineStructure.BALANCED_SPLIT,
            experience = TrainingExperience.INTERMEDIATE,
            focusSummary = listOf(RoutineFocus.UPPER_BODY, RoutineFocus.LOWER_BODY)
        )
        val fullBody = template(
            id = "full-body-fallback",
            cycleLength = 4,
            sessionMinutes = 45,
            structure = RoutineStructure.FULL_BODY,
            experience = TrainingExperience.INTERMEDIATE,
            focusSummary = listOf(RoutineFocus.FULL_BODY)
        )

        val balancedRecommendation = recommendRoutine(
            input = RoutineRecommendationInput(
                cycleLength = 4,
                sessionMinutes = 45,
                experience = TrainingExperience.INTERMEDIATE,
                feeling = RoutineFeeling.FOCUSED_BODY_PART
            ),
            templates = listOf(balanced, fullBody)
        )
        val fullBodyRecommendation = recommendRoutine(
            input = RoutineRecommendationInput(
                cycleLength = 4,
                sessionMinutes = 45,
                experience = TrainingExperience.INTERMEDIATE,
                feeling = RoutineFeeling.FOCUSED_BODY_PART
            ),
            templates = listOf(fullBody)
        )

        assertThat(balancedRecommendation.primaryTemplateId).isEqualTo("balanced-without-body-part")
        assertThat(fullBodyRecommendation.primaryTemplateId).isEqualTo("full-body-fallback")
    }

    @Test
    fun recommendRoutine_appRecommendedUsesCycleLengthFallbacks() {
        val fullBody = template(
            id = "short-cycle-full-body",
            cycleLength = 2,
            sessionMinutes = 45,
            structure = RoutineStructure.FULL_BODY,
            experience = TrainingExperience.INTERMEDIATE,
            focusSummary = listOf(RoutineFocus.FULL_BODY)
        )
        val balanced = template(
            id = "longer-balanced",
            cycleLength = 4,
            sessionMinutes = 45,
            structure = RoutineStructure.BALANCED_SPLIT,
            experience = TrainingExperience.INTERMEDIATE,
            focusSummary = listOf(RoutineFocus.UPPER_BODY, RoutineFocus.LOWER_BODY)
        )
        val bodyPartWithoutBaseFocus = template(
            id = "body-part-without-base-focus",
            cycleLength = 5,
            sessionMinutes = 45,
            structure = RoutineStructure.BODY_PART_SPLIT,
            experience = TrainingExperience.INTERMEDIATE,
            focusSummary = listOf(RoutineFocus.CHEST)
        )

        val longCycleRecommendation = recommendRoutine(
            input = RoutineRecommendationInput(
                cycleLength = 5,
                sessionMinutes = 45,
                experience = TrainingExperience.INTERMEDIATE,
                feeling = RoutineFeeling.APP_RECOMMENDED
            ),
            templates = listOf(fullBody, balanced, bodyPartWithoutBaseFocus)
        )
        val shortCycleRecommendation = recommendRoutine(
            input = RoutineRecommendationInput(
                cycleLength = 2,
                sessionMinutes = 45,
                experience = TrainingExperience.INTERMEDIATE,
                feeling = RoutineFeeling.APP_RECOMMENDED
            ),
            templates = listOf(fullBody, balanced)
        )

        assertThat(longCycleRecommendation.primaryTemplateId).isEqualTo("longer-balanced")
        assertThat(shortCycleRecommendation.primaryTemplateId).isEqualTo("short-cycle-full-body")
    }

    @Test
    fun recommendRoutine_fallsBackToSystemTemplatesWhenExperienceHasNoEligibleTemplate() {
        val intermediateBalanced = templates.first { it.id == "intermediate-balanced-4day" }

        val recommendation = recommendRoutine(
            input = RoutineRecommendationInput(
                cycleLength = 4,
                sessionMinutes = 45,
                experience = TrainingExperience.ADVANCED,
                feeling = RoutineFeeling.APP_RECOMMENDED
            ),
            templates = listOf(intermediateBalanced)
        )

        assertThat(recommendation.primaryTemplateId).isEqualTo("intermediate-balanced-4day")
    }

    @Test
    fun recommendRoutine_focusedBodyPartUsesFallbackCandidatesWhenExactCycleHasNoFit() {
        val exactUnfitBodyPart = template(
            id = "exact-unfit-body-part",
            cycleLength = 3,
            sessionMinutes = 45,
            structure = RoutineStructure.BODY_PART_SPLIT,
            experience = TrainingExperience.INTERMEDIATE,
            focusSummary = listOf(RoutineFocus.CHEST)
        )
        val fallbackBodyPart = template(
            id = "fallback-body-part",
            cycleLength = 4,
            sessionMinutes = 45,
            structure = RoutineStructure.BODY_PART_SPLIT,
            experience = TrainingExperience.INTERMEDIATE,
            focusSummary = bodyPartFocus
        )
        val fallbackBalanced = template(
            id = "fallback-balanced",
            cycleLength = 4,
            sessionMinutes = 45,
            structure = RoutineStructure.BALANCED_SPLIT,
            experience = TrainingExperience.INTERMEDIATE,
            focusSummary = listOf(RoutineFocus.UPPER_BODY, RoutineFocus.LOWER_BODY)
        )
        val fallbackFullBody = template(
            id = "fallback-full-body",
            cycleLength = 4,
            sessionMinutes = 45,
            structure = RoutineStructure.FULL_BODY,
            experience = TrainingExperience.INTERMEDIATE,
            focusSummary = listOf(RoutineFocus.FULL_BODY)
        )
        val input = RoutineRecommendationInput(
            cycleLength = 3,
            sessionMinutes = 45,
            experience = TrainingExperience.INTERMEDIATE,
            feeling = RoutineFeeling.FOCUSED_BODY_PART
        )

        val bodyPartRecommendation = recommendRoutine(input, listOf(exactUnfitBodyPart, fallbackBodyPart))
        val balancedRecommendation = recommendRoutine(input, listOf(exactUnfitBodyPart, fallbackBalanced))
        val fullBodyRecommendation = recommendRoutine(input, listOf(exactUnfitBodyPart, fallbackFullBody))

        assertThat(bodyPartRecommendation.primaryTemplateId).isEqualTo("fallback-body-part")
        assertThat(balancedRecommendation.primaryTemplateId).isEqualTo("fallback-balanced")
        assertThat(fullBodyRecommendation.primaryTemplateId).isEqualTo("fallback-full-body")
    }

    @Test
    fun recommendRoutine_beginnerUsesBalancedBodyPartAndFallbackCandidates() {
        val exactBalanced = template(
            id = "beginner-exact-balanced",
            cycleLength = 3,
            sessionMinutes = 45,
            structure = RoutineStructure.BALANCED_SPLIT,
            experience = TrainingExperience.BEGINNER,
            focusSummary = listOf(RoutineFocus.UPPER_BODY, RoutineFocus.LOWER_BODY)
        )
        val exactBodyPart = template(
            id = "beginner-exact-body-part",
            cycleLength = 3,
            sessionMinutes = 45,
            structure = RoutineStructure.BODY_PART_SPLIT,
            experience = TrainingExperience.BEGINNER,
            focusSummary = bodyPartFocus
        )
        val exactUnfitBodyPart = template(
            id = "beginner-exact-unfit-body-part",
            cycleLength = 3,
            sessionMinutes = 45,
            structure = RoutineStructure.BODY_PART_SPLIT,
            experience = TrainingExperience.BEGINNER,
            focusSummary = listOf(RoutineFocus.CHEST)
        )
        val fallbackFullBody = template(
            id = "beginner-fallback-full-body",
            cycleLength = 4,
            sessionMinutes = 45,
            structure = RoutineStructure.FULL_BODY,
            experience = TrainingExperience.BEGINNER,
            focusSummary = listOf(RoutineFocus.FULL_BODY)
        )
        val fallbackBalanced = exactBalanced.copy(id = "beginner-fallback-balanced", cycleLength = 4)
        val fallbackBodyPart = exactBodyPart.copy(id = "beginner-fallback-body-part", cycleLength = 4)
        val input = RoutineRecommendationInput(
            cycleLength = 3,
            sessionMinutes = 45,
            experience = TrainingExperience.BEGINNER,
            feeling = RoutineFeeling.APP_RECOMMENDED
        )

        val balancedRecommendation = recommendRoutine(input, listOf(exactBalanced))
        val bodyPartRecommendation = recommendRoutine(input, listOf(exactBodyPart))
        val fallbackFullBodyRecommendation = recommendRoutine(input, listOf(exactUnfitBodyPart, fallbackFullBody))
        val fallbackBalancedRecommendation = recommendRoutine(input, listOf(exactUnfitBodyPart, fallbackBalanced))
        val fallbackBodyPartRecommendation = recommendRoutine(input, listOf(exactUnfitBodyPart, fallbackBodyPart))

        assertThat(balancedRecommendation.primaryTemplateId).isEqualTo("beginner-exact-balanced")
        assertThat(bodyPartRecommendation.primaryTemplateId).isEqualTo("beginner-exact-body-part")
        assertThat(fallbackFullBodyRecommendation.primaryTemplateId).isEqualTo("beginner-fallback-full-body")
        assertThat(fallbackBalancedRecommendation.primaryTemplateId).isEqualTo("beginner-fallback-balanced")
        assertThat(fallbackBodyPartRecommendation.primaryTemplateId).isEqualTo("beginner-fallback-body-part")
    }

    @Test
    fun recommendRoutine_appRecommendedUsesFallbackCandidatesAndFirstCandidateWhenNoStructureFits() {
        val exactUnfitBodyPart = template(
            id = "app-exact-unfit-body-part",
            cycleLength = 3,
            sessionMinutes = 45,
            structure = RoutineStructure.BODY_PART_SPLIT,
            experience = TrainingExperience.INTERMEDIATE,
            focusSummary = listOf(RoutineFocus.CHEST)
        )
        val fallbackBalanced = template(
            id = "app-fallback-balanced",
            cycleLength = 4,
            sessionMinutes = 45,
            structure = RoutineStructure.BALANCED_SPLIT,
            experience = TrainingExperience.INTERMEDIATE,
            focusSummary = listOf(RoutineFocus.UPPER_BODY, RoutineFocus.LOWER_BODY)
        )
        val fallbackFullBody = template(
            id = "app-fallback-full-body",
            cycleLength = 4,
            sessionMinutes = 45,
            structure = RoutineStructure.FULL_BODY,
            experience = TrainingExperience.INTERMEDIATE,
            focusSummary = listOf(RoutineFocus.FULL_BODY)
        )
        val input = RoutineRecommendationInput(
            cycleLength = 3,
            sessionMinutes = 45,
            experience = TrainingExperience.INTERMEDIATE,
            feeling = RoutineFeeling.APP_RECOMMENDED
        )

        val balancedRecommendation = recommendRoutine(input, listOf(exactUnfitBodyPart, fallbackBalanced))
        val fullBodyRecommendation = recommendRoutine(input, listOf(exactUnfitBodyPart, fallbackFullBody))
        val firstCandidateRecommendation = recommendRoutine(input, listOf(exactUnfitBodyPart))

        assertThat(balancedRecommendation.primaryTemplateId).isEqualTo("app-fallback-balanced")
        assertThat(fullBodyRecommendation.primaryTemplateId).isEqualTo("app-fallback-full-body")
        assertThat(firstCandidateRecommendation.primaryTemplateId).isEqualTo("app-exact-unfit-body-part")
    }

    @Test
    fun recommendRoutine_usesFrequencyFallbackWhenExactCycleIsMissing() {
        val shorterFullBody = template(
            id = "shorter-full-body",
            cycleLength = 2,
            sessionMinutes = 45,
            structure = RoutineStructure.FULL_BODY,
            experience = TrainingExperience.INTERMEDIATE,
            focusSummary = listOf(RoutineFocus.FULL_BODY)
        )
        val longerBalanced = template(
            id = "longer-balanced-only",
            cycleLength = 4,
            sessionMinutes = 45,
            structure = RoutineStructure.BALANCED_SPLIT,
            experience = TrainingExperience.INTERMEDIATE,
            focusSummary = listOf(RoutineFocus.UPPER_BODY, RoutineFocus.LOWER_BODY)
        )

        val shorterFallbackRecommendation = recommendRoutine(
            input = RoutineRecommendationInput(
                cycleLength = 3,
                sessionMinutes = 45,
                experience = TrainingExperience.INTERMEDIATE,
                feeling = RoutineFeeling.APP_RECOMMENDED
            ),
            templates = listOf(shorterFullBody, longerBalanced)
        )
        val allCandidatesFallbackRecommendation = recommendRoutine(
            input = RoutineRecommendationInput(
                cycleLength = 1,
                sessionMinutes = 45,
                experience = TrainingExperience.INTERMEDIATE,
                feeling = RoutineFeeling.APP_RECOMMENDED
            ),
            templates = listOf(longerBalanced)
        )

        assertThat(shorterFallbackRecommendation.primaryTemplateId).isEqualTo("shorter-full-body")
        assertThat(allCandidatesFallbackRecommendation.primaryTemplateId).isEqualTo("longer-balanced-only")
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

    @Test
    fun evaluateReadiness_isReadyWithoutCompletionOrAfterRecovery() {
        val noCompletion = evaluateReadiness(
            lastCompletedAt = null,
            now = Instant.parse("2026-05-24T20:00:00Z"),
            minRecoveryHours = 24
        )
        val afterRecovery = evaluateReadiness(
            lastCompletedAt = Instant.parse("2026-05-23T09:00:00Z"),
            now = Instant.parse("2026-05-24T20:00:00Z"),
            minRecoveryHours = 24
        )

        assertThat(noCompletion.ready).isTrue()
        assertThat(noCompletion.warningCode).isNull()
        assertThat(afterRecovery.ready).isTrue()
        assertThat(afterRecovery.remainingRecoveryHours).isEqualTo(0)
        assertThat(afterRecovery.warningCode).isNull()
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

}
