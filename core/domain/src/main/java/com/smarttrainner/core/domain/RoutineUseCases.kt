package com.smarttrainner.core.domain

import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.CustomRoutineInput
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.RoutineFeeling
import com.smarttrainner.core.model.RoutineFocus
import com.smarttrainner.core.model.RoutineProgress
import com.smarttrainner.core.model.RoutineReadiness
import com.smarttrainner.core.model.RoutineRecommendation
import com.smarttrainner.core.model.RoutineRecommendationInput
import com.smarttrainner.core.model.RoutineSource
import com.smarttrainner.core.model.RoutineStructure
import com.smarttrainner.core.model.TrainingExperience
import com.smarttrainner.core.model.WorkoutLog
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

class RecommendRoutineUseCase @Inject constructor() {
    operator fun invoke(
        input: RoutineRecommendationInput,
        templates: List<PlanTemplate>
    ): RoutineRecommendation {
        val systemTemplates = templates.filter { it.source == RoutineSource.SYSTEM }
        require(systemTemplates.isNotEmpty()) { "At least one system routine template is required." }
        val eligible = systemTemplates.filter { template ->
            template.daysPerWeek <= input.daysPerWeek &&
                template.sessionMinutes <= input.sessionMinutes &&
                isExperienceEligible(input.experience, template)
        }.ifEmpty { systemTemplates.filter { isExperienceEligible(input.experience, it) } }
            .ifEmpty { systemTemplates }

        val primary = when {
            input.experience == TrainingExperience.BEGINNER && input.daysPerWeek <= 2 -> {
                eligible.bestFullBody(daysPerWeek = 2)
            }
            input.experience == TrainingExperience.BEGINNER -> {
                eligible.bestFullBody(daysPerWeek = input.daysPerWeek.coerceAtMost(3))
            }
            input.feeling == RoutineFeeling.FOCUSED_BODY_PART && input.daysPerWeek >= 4 -> {
                eligible.bestBodyPart(input) ?: eligible.bestBalanced(input)
            }
            input.feeling == RoutineFeeling.BALANCED_FULL_BODY -> {
                eligible.bestFullBody(daysPerWeek = input.daysPerWeek.coerceAtMost(3))
                    ?: eligible.bestBalanced(input)
            }
            input.daysPerWeek >= 4 -> {
                eligible.bestBodyPart(input) ?: eligible.bestBalanced(input)
            }
            else -> eligible.bestBalanced(input) ?: eligible.bestFullBody(daysPerWeek = input.daysPerWeek)
        } ?: eligible.first()

        val alternatives = eligible
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

    private fun isExperienceEligible(
        experience: TrainingExperience,
        template: PlanTemplate
    ): Boolean = when (experience) {
        TrainingExperience.BEGINNER -> {
            template.recommendedExperience == TrainingExperience.BEGINNER &&
                !(template.structure == RoutineStructure.BODY_PART_SPLIT && template.daysPerWeek >= 5)
        }
        TrainingExperience.INTERMEDIATE -> true
    }

    private fun List<PlanTemplate>.bestFullBody(daysPerWeek: Int): PlanTemplate? =
        filter { it.structure == RoutineStructure.FULL_BODY }
            .minWithOrNull(compareBy<PlanTemplate> { kotlin.math.abs(it.daysPerWeek - daysPerWeek) }
                .thenBy { it.daysPerWeek })

    private fun List<PlanTemplate>.bestBalanced(input: RoutineRecommendationInput): PlanTemplate? =
        filter { it.structure == RoutineStructure.BALANCED_SPLIT }
            .minWithOrNull(templateFitComparator(input))

    private fun List<PlanTemplate>.bestBodyPart(input: RoutineRecommendationInput): PlanTemplate? =
        filter { it.structure == RoutineStructure.BODY_PART_SPLIT }
            .filter { it.focusSummary.containsAll(BODY_PART_BASE_FOCUS) }
            .minWithOrNull(templateFitComparator(input))

    private fun templateFitComparator(input: RoutineRecommendationInput): Comparator<PlanTemplate> =
        compareBy<PlanTemplate> {
            kotlin.math.abs(it.daysPerWeek - input.daysPerWeek)
        }.thenBy {
            kotlin.math.abs(it.sessionMinutes - input.sessionMinutes)
        }.thenByDescending {
            it.sessionMinutes
        }

    private fun routineAlternativeComparator(
        input: RoutineRecommendationInput,
        primary: PlanTemplate
    ): Comparator<PlanTemplate> = compareBy<PlanTemplate> {
        if (it.structure == primary.structure) 1 else 0
    }.thenBy {
        kotlin.math.abs(it.daysPerWeek - input.daysPerWeek)
    }.thenByDescending {
        it.sessionMinutes <= input.sessionMinutes
    }

    private companion object {
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

enum class CustomRoutineValidationError {
    NAME,
    DAYS,
    EMPTY_DAY,
    UNKNOWN_EXERCISE,
    SETS,
    REPS,
    DURATION,
    REST
}

class ValidateCustomRoutineUseCase @Inject constructor() {
    operator fun invoke(
        input: CustomRoutineInput,
        availableExerciseIds: Set<ExerciseId>
    ): CustomRoutineValidationError? {
        if (input.name.trim().isEmpty() || input.name.trim().length > MAX_ROUTINE_NAME_LENGTH) {
            return CustomRoutineValidationError.NAME
        }
        if (input.days.isEmpty() || input.days.size > MAX_ROUTINE_DAYS) {
            return CustomRoutineValidationError.DAYS
        }
        input.days.forEach { day ->
            if (day.exercises.isEmpty()) return CustomRoutineValidationError.EMPTY_DAY
            if (day.minRecoveryHours !in 0..168) return CustomRoutineValidationError.REST
            day.exercises.forEach { exercise ->
                if (exercise.exerciseId !in availableExerciseIds) {
                    return CustomRoutineValidationError.UNKNOWN_EXERCISE
                }
                if (exercise.sets !in 1..12) return CustomRoutineValidationError.SETS
                if (exercise.restSeconds !in 0..600) return CustomRoutineValidationError.REST
                val hasReps = exercise.repRangeStart != null || exercise.repRangeEnd != null
                val hasDuration = exercise.durationMinutes != null
                if (!hasReps && !hasDuration) return CustomRoutineValidationError.REPS
                if (hasReps) {
                    val start = exercise.repRangeStart ?: return CustomRoutineValidationError.REPS
                    val end = exercise.repRangeEnd ?: return CustomRoutineValidationError.REPS
                    if (start !in 1..50 || end !in 1..50 || start > end) {
                        return CustomRoutineValidationError.REPS
                    }
                }
                if (exercise.durationMinutes?.let { it !in 1..240 } == true) {
                    return CustomRoutineValidationError.DURATION
                }
            }
        }
        return null
    }

    private companion object {
        const val MAX_ROUTINE_DAYS = 7
        const val MAX_ROUTINE_NAME_LENGTH = 60
    }
}

class ObserveRoutineProgressUseCase @Inject constructor(
    private val repository: TrainingRepository
) {
    operator fun invoke() = repository.observeRoutineProgress()
}

class StartRoutineUseCase @Inject constructor(
    private val repository: TrainingRepository
) {
    suspend operator fun invoke(templateId: String) = repository.startRoutine(templateId)
}

class AdvanceRoutineDayUseCase @Inject constructor() {
    operator fun invoke(completedDayIndex: Int, cycleLength: Int): Int {
        require(cycleLength > 0) { "Cycle length must be positive." }
        require(completedDayIndex in 0 until cycleLength) { "Completed day index is outside the routine cycle." }
        return (completedDayIndex + 1) % cycleLength
    }
}

class CompleteRoutineDayUseCase @Inject constructor(
    private val repository: TrainingRepository,
    private val advanceRoutineDay: AdvanceRoutineDayUseCase
) {
    suspend operator fun invoke(
        template: PlanTemplate,
        completedDayIndex: Int,
        completedAt: Instant
    ): Result<Unit> {
        val nextDayIndex = advanceRoutineDay(completedDayIndex, template.cycleLength)
        val newCycleStartedAt = if (nextDayIndex == 0) completedAt else null
        return repository.markRoutineDayCompleted(
            completedDayIndex = completedDayIndex,
            nextDayIndex = nextDayIndex,
            completedAt = completedAt,
            newCycleStartedAt = newCycleStartedAt
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

class ResolveRoutineCycleCompletionUseCase @Inject constructor() {
    operator fun invoke(
        logs: List<WorkoutLog>,
        progress: RoutineProgress,
        zone: ZoneId
    ): Set<PlannedExerciseId> {
        val cycleStartedAt = progress.cycleStartedAt?.let { LocalDateTime.ofInstant(it, zone) }
        return logs.asSequence()
            .filter { it.completed }
            .filter { log -> cycleStartedAt == null || !log.performedAt.isBefore(cycleStartedAt) }
            .map { it.plannedExerciseId }
            .toSet()
    }
}
