package com.smarttrainner.feature.routine.impl

import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.RoutineFeeling
import com.smarttrainner.core.model.RoutineSource
import com.smarttrainner.core.model.RoutineStructure
import com.smarttrainner.core.model.TrainingExperience
import com.smarttrainner.core.model.estimatedSessionMinutes

internal data class RoutineRecommendationFilterAvailability(
    val experiences: Set<TrainingExperience> = TrainingExperience.entries.toSet(),
    val cycleLength: Set<Int> = SUPPORTED_CYCLE_LENGTHS.toSet(),
    val sessionMinutes: Set<Int> = SUPPORTED_SESSION_MINUTES.toSet(),
    val feelings: Set<RoutineFeeling> = SUPPORTED_ROUTINE_FEELINGS.toSet()
)

internal data class RoutineFilterOption<T>(
    val value: T,
    val label: String,
    val enabled: Boolean
)

internal fun RoutineRecommendationFormState.availableFilterOptions(
    templates: List<PlanTemplate>
): RoutineRecommendationFilterAvailability {
    val systemTemplates = templates.systemRoutineTemplates()
    if (systemTemplates.isEmpty()) {
        return RoutineRecommendationFilterAvailability()
    }
    val enabledExperiences = TrainingExperience.entries
        .filter { experience ->
            systemTemplates.any { it.recommendedExperience == experience }
        }
        .toSet()
    val enabledDays = SUPPORTED_CYCLE_LENGTHS
        .filter { days ->
            systemTemplates.any {
                it.recommendedExperience == experience &&
                    it.cycleLength == days &&
                    it.matchesDirection(feeling)
            }
        }
        .toSet()
    val enabledSessionMinutes = SUPPORTED_SESSION_MINUTES
        .filter { minutes ->
            systemTemplates.any {
                it.recommendedExperience == experience &&
                    it.cycleLength == cycleLength &&
                    it.sessionMinutes == minutes &&
                    it.matchesDirection(feeling) &&
                    it.isWithinSessionTolerance(minutes)
            }
        }
        .toSet()
    val enabledFeelings = SUPPORTED_ROUTINE_FEELINGS
        .filter { routineFeeling ->
            systemTemplates.any {
                it.recommendedExperience == experience &&
                    it.cycleLength == cycleLength &&
                    it.sessionMinutes == sessionMinutes &&
                    it.matchesDirection(routineFeeling) &&
                    it.isWithinSessionTolerance(sessionMinutes)
            }
        }
        .toSet()

    return RoutineRecommendationFilterAvailability(
        experiences = enabledExperiences,
        cycleLength = enabledDays,
        sessionMinutes = enabledSessionMinutes,
        feelings = enabledFeelings
    )
}

internal fun RoutineRecommendationFormState.normalizedFor(
    templates: List<PlanTemplate>
): RoutineRecommendationFormState {
    val systemTemplates = templates.systemRoutineTemplates()
    if (systemTemplates.isEmpty()) {
        return this
    }

    val nextExperience = experience.takeIf { currentExperience ->
        systemTemplates.any { it.recommendedExperience == currentExperience }
    } ?: systemTemplates.first().recommendedExperience
    val experienceForm = copy(experience = nextExperience)

    val enabledFeelingsForExperience = SUPPORTED_ROUTINE_FEELINGS
        .filter { routineFeeling ->
            systemTemplates.any {
                it.recommendedExperience == nextExperience &&
                    it.matchesDirection(routineFeeling)
            }
        }
        .toSet()
    val nextInitialFeeling = feeling.takeIf { it in enabledFeelingsForExperience }
        ?: RoutineFeeling.APP_RECOMMENDED.takeIf { it in enabledFeelingsForExperience }
        ?: enabledFeelingsForExperience.firstOrNull()
        ?: feeling
    val directionForm = experienceForm.copy(feeling = nextInitialFeeling)

    val enabledDays = directionForm.availableFilterOptions(templates).cycleLength
    val nextDays = cycleLength.takeIf { it in enabledDays }
        ?: enabledDays.nearestTo(cycleLength)
        ?: cycleLength
    val daysForm = directionForm.copy(cycleLength = nextDays)

    val enabledSessionMinutes = daysForm.availableFilterOptions(templates).sessionMinutes
    val nextSessionMinutes = sessionMinutes.takeIf { it in enabledSessionMinutes }
        ?: enabledSessionMinutes.nearestTo(sessionMinutes)
        ?: sessionMinutes
    val sessionForm = daysForm.copy(sessionMinutes = nextSessionMinutes)

    val enabledFeelings = sessionForm.availableFilterOptions(templates).feelings
    val nextFeeling = feeling.takeIf { it in enabledFeelings }
        ?: RoutineFeeling.APP_RECOMMENDED.takeIf { it in enabledFeelings }
        ?: enabledFeelings.firstOrNull()
        ?: feeling

    return sessionForm.copy(feeling = nextFeeling)
}

private fun Set<Int>.nearestTo(value: Int): Int? =
    minWithOrNull(compareBy<Int> { kotlin.math.abs(it - value) }.thenBy { it })

private fun List<PlanTemplate>.systemRoutineTemplates(): List<PlanTemplate> =
    filter { it.source == RoutineSource.SYSTEM }

private fun PlanTemplate.matchesDirection(feeling: RoutineFeeling): Boolean = when (feeling) {
    RoutineFeeling.APP_RECOMMENDED -> true
    RoutineFeeling.BALANCED_FULL_BODY -> structure == RoutineStructure.FULL_BODY
    RoutineFeeling.FOCUSED_BODY_PART -> structure == RoutineStructure.BODY_PART_SPLIT
}

private fun PlanTemplate.isWithinSessionTolerance(sessionMinutes: Int): Boolean =
    kotlin.math.abs(estimatedSessionMinutes - sessionMinutes) <= SESSION_TOLERANCE_MINUTES

private val SUPPORTED_CYCLE_LENGTHS = 2..5
private val SUPPORTED_SESSION_MINUTES = listOf(30, 45, 60)
private val SUPPORTED_ROUTINE_FEELINGS = listOf(
    RoutineFeeling.APP_RECOMMENDED,
    RoutineFeeling.BALANCED_FULL_BODY,
    RoutineFeeling.FOCUSED_BODY_PART
)
private const val SESSION_TOLERANCE_MINUTES = 10
