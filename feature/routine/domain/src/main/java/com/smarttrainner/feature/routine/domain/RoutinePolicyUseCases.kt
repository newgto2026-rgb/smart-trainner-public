package com.smarttrainner.feature.routine.domain

import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.RoutineFeeling
import com.smarttrainner.core.model.RoutineFocus
import com.smarttrainner.core.model.RoutineReadiness
import com.smarttrainner.core.model.RoutineRecommendation
import com.smarttrainner.core.model.RoutineRecommendationInput
import com.smarttrainner.core.model.RoutineSource
import com.smarttrainner.core.model.RoutineStructure
import com.smarttrainner.core.model.TrainingExperience
import com.smarttrainner.core.model.estimatedSessionMinutes
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

class RecommendRoutineUseCase @Inject constructor() {
    operator fun invoke(
        input: RoutineRecommendationInput,
        templates: List<PlanTemplate>
    ): RoutineRecommendation {
        val systemTemplates = templates.filter { it.source == RoutineSource.SYSTEM }
        require(systemTemplates.isNotEmpty()) { "At least one system routine template is required." }
        val experienceEligible = systemTemplates.filter { isExperienceEligible(input.experience, it) }
            .ifEmpty { systemTemplates }
        val frequencyCandidates = experienceEligible.preferRequestedFrequency(input)
        val sessionCandidates = frequencyCandidates.filter { it.isWithinSessionTolerance(input.sessionMinutes) }
        val primaryCandidates = sessionCandidates.ifEmpty { frequencyCandidates }

        val primary = selectPrimary(
            input = input,
            primaryCandidates = primaryCandidates,
            fallbackCandidates = experienceEligible
        ) ?: primaryCandidates.first()

        val alternativeCandidates = (primaryCandidates + frequencyCandidates + experienceEligible)
            .distinctBy { it.id }

        val alternatives = alternativeCandidates
            .filterNot { it.id == primary.id }
            .sortedWith(routineAlternativeComparator(input, primary))
            .take(2)
            .map { it.id }

        return RoutineRecommendation(
            primaryTemplateId = primary.id,
            alternativeTemplateIds = alternatives,
            reasonCode = when (primary.structure) {
                RoutineStructure.FULL_BODY -> "full_body_frequency"
                RoutineStructure.BALANCED_SPLIT -> "balanced_recovery"
                RoutineStructure.BODY_PART_SPLIT -> "focused_body_part_cycle"
            }
        )
    }

    private fun selectPrimary(
        input: RoutineRecommendationInput,
        primaryCandidates: List<PlanTemplate>,
        fallbackCandidates: List<PlanTemplate>
    ): PlanTemplate? = when {
        input.feeling == RoutineFeeling.FOCUSED_BODY_PART -> {
            primaryCandidates.bestBodyPart(input)
                ?: primaryCandidates.bestBalanced(input)
                ?: primaryCandidates.bestFullBody(input)
                ?: fallbackCandidates.bestBodyPart(input)
                ?: fallbackCandidates.bestBalanced(input)
                ?: fallbackCandidates.bestFullBody(input)
        }
        input.feeling == RoutineFeeling.BALANCED_FULL_BODY -> {
            primaryCandidates.bestFullBody(input)
                ?: fallbackCandidates.bestFullBody(input)
                ?: primaryCandidates.bestBalanced(input)
                ?: fallbackCandidates.bestBalanced(input)
        }
        input.experience == TrainingExperience.BEGINNER -> {
            primaryCandidates.bestFullBody(input)
                ?: primaryCandidates.bestBalanced(input)
                ?: primaryCandidates.bestBodyPart(input)
                ?: fallbackCandidates.bestFullBody(input)
                ?: fallbackCandidates.bestBalanced(input)
                ?: fallbackCandidates.bestBodyPart(input)
        }
        input.cycleLength >= 4 -> {
            primaryCandidates.bestBodyPart(input)
                ?: primaryCandidates.bestBalanced(input)
                ?: fallbackCandidates.bestBodyPart(input)
                ?: fallbackCandidates.bestBalanced(input)
        }
        else -> {
            primaryCandidates.bestBalanced(input)
                ?: primaryCandidates.bestFullBody(input)
                ?: fallbackCandidates.bestBalanced(input)
                ?: fallbackCandidates.bestFullBody(input)
        }
    }

    private fun isExperienceEligible(
        experience: TrainingExperience,
        template: PlanTemplate
    ): Boolean = when (experience) {
        TrainingExperience.BEGINNER -> {
            template.recommendedExperience == TrainingExperience.BEGINNER
        }
        TrainingExperience.INTERMEDIATE -> {
            template.recommendedExperience == TrainingExperience.INTERMEDIATE
        }
        TrainingExperience.ADVANCED -> {
            template.recommendedExperience == TrainingExperience.ADVANCED
        }
    }

    private fun List<PlanTemplate>.preferRequestedFrequency(
        input: RoutineRecommendationInput
    ): List<PlanTemplate> {
        val exactFrequency = filter { it.cycleLength == input.cycleLength }
        if (exactFrequency.isNotEmpty()) {
            return exactFrequency
        }
        return filter { it.cycleLength <= input.cycleLength }.ifEmpty { this }
    }

    private fun PlanTemplate.isWithinSessionTolerance(sessionMinutes: Int): Boolean =
        this.sessionMinutes == sessionMinutes &&
            kotlin.math.abs(estimatedSessionMinutes - sessionMinutes) <= SESSION_TOLERANCE_MINUTES

    private fun List<PlanTemplate>.bestFullBody(input: RoutineRecommendationInput): PlanTemplate? =
        filter { it.structure == RoutineStructure.FULL_BODY }
            .minWithOrNull(templateFitComparator(input))

    private fun List<PlanTemplate>.bestBalanced(input: RoutineRecommendationInput): PlanTemplate? =
        filter { it.structure == RoutineStructure.BALANCED_SPLIT }
            .minWithOrNull(templateFitComparator(input))

    private fun List<PlanTemplate>.bestBodyPart(input: RoutineRecommendationInput): PlanTemplate? =
        filter { it.structure == RoutineStructure.BODY_PART_SPLIT }
            .filter { it.focusSummary.containsAll(BODY_PART_BASE_FOCUS) }
            .minWithOrNull(templateFitComparator(input))

    private fun templateFitComparator(input: RoutineRecommendationInput): Comparator<PlanTemplate> =
        compareBy<PlanTemplate> {
            kotlin.math.abs(it.cycleLength - input.cycleLength)
        }.thenBy {
            kotlin.math.abs(it.estimatedSessionMinutes - input.sessionMinutes)
        }.thenByDescending {
            it.estimatedSessionMinutes
        }

    private fun routineAlternativeComparator(
        input: RoutineRecommendationInput,
        primary: PlanTemplate
    ): Comparator<PlanTemplate> = compareBy<PlanTemplate> {
        if (it.structure == primary.structure) 1 else 0
    }.thenBy {
        kotlin.math.abs(it.cycleLength - input.cycleLength)
    }.thenByDescending {
        it.isWithinSessionTolerance(input.sessionMinutes)
    }.thenBy {
        kotlin.math.abs(it.estimatedSessionMinutes - input.sessionMinutes)
    }

    private companion object {
        private const val SESSION_TOLERANCE_MINUTES = 10

        val BODY_PART_BASE_FOCUS = listOf(
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
    }
}

class EvaluateRoutineReadinessUseCase @Inject constructor() {
    operator fun invoke(
        lastCompletedAt: Instant?,
        now: Instant,
        minRecoveryHours: Int
    ): RoutineReadiness {
        if (lastCompletedAt == null) {
            return RoutineReadiness(ready = true, remainingRecoveryHours = 0, warningCode = null)
        }
        val elapsedHours = Duration.between(lastCompletedAt, now).toHours().coerceAtLeast(0)
        val remaining = (minRecoveryHours - elapsedHours).coerceAtLeast(0)
        return RoutineReadiness(
            ready = remaining == 0L,
            remainingRecoveryHours = remaining,
            warningCode = if (remaining == 0L) null else "minimum_recovery_not_met"
        )
    }
}
