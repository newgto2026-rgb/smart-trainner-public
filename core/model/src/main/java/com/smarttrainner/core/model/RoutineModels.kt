package com.smarttrainner.core.model

import java.time.Duration
import java.time.Instant
import java.time.LocalDate

enum class PlanLevel(val displayName: String) {
    INTRO("입문"),
    BEGINNER("초보"),
    INTERMEDIATE("초중급"),
    ADVANCED("고급")
}

enum class RoutineStructure {
    FULL_BODY,
    BALANCED_SPLIT,
    BODY_PART_SPLIT
}

enum class RoutineSource {
    SYSTEM,
    CUSTOM
}

enum class RoutineFocus {
    FULL_BODY,
    UPPER_BODY,
    PUSH,
    PULL,
    CHEST,
    BACK,
    LOWER_BODY,
    SHOULDERS,
    ARMS,
    BICEPS,
    TRICEPS,
    FOREARMS,
    CARDIO_CONDITIONING,
    CORE
}

enum class TrainingExperience {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}

enum class RoutineFeeling {
    BALANCED_FULL_BODY,
    FOCUSED_BODY_PART,
    APP_RECOMMENDED
}

data class PlanTemplate(
    val id: String,
    val name: String,
    val level: PlanLevel,
    val cycleLength: Int,
    val description: String,
    val days: List<PlanTemplateDay>,
    val structure: RoutineStructure = RoutineStructure.FULL_BODY,
    val recommendedExperience: TrainingExperience = TrainingExperience.BEGINNER,
    val sessionMinutes: Int = 45,
    val focusSummary: List<RoutineFocus> = listOf(RoutineFocus.FULL_BODY),
    val source: RoutineSource = RoutineSource.SYSTEM
)

data class PlanTemplateDay(
    val dayOffset: Int,
    val title: String,
    val focus: String,
    val exercises: List<TemplateExercise>,
    val dayNumber: Int = dayOffset + 1,
    val primaryFocus: RoutineFocus? = RoutineFocus.FULL_BODY,
    val secondaryFocuses: List<RoutineFocus> = emptyList(),
    val minRecoveryHours: Int = 24
)

data class TemplateExercise(
    val exerciseId: ExerciseId,
    val sets: Int,
    val repRange: IntRange?,
    val durationMinutes: Int?,
    val restSeconds: Int,
    val note: String,
    val repDurationSeconds: Int = DEFAULT_REP_DURATION_SECONDS
)

val TemplateExercise.estimatedTotalSeconds: Int
    get() = estimateExerciseSeconds(
        sets = sets,
        repRange = repRange,
        durationMinutes = durationMinutes,
        restSeconds = restSeconds,
        repDurationSeconds = repDurationSeconds
    )

val PlanTemplateDay.estimatedSessionMinutes: Int
    get() = exercises.sumOf { it.estimatedTotalSeconds }.roundUpToMinutes()

val PlanTemplate.estimatedSessionMinutes: Int
    get() = days.maxOfOrNull { it.estimatedSessionMinutes } ?: sessionMinutes

data class CustomRoutineInput(
    val id: String? = null,
    val name: String,
    val description: String = "",
    val days: List<CustomRoutineDayInput>
)

data class CustomRoutineDayInput(
    val title: String,
    val focus: String,
    val primaryFocus: RoutineFocus?,
    val secondaryFocuses: List<RoutineFocus> = emptyList(),
    val minRecoveryHours: Int = 24,
    val exercises: List<CustomRoutineExerciseInput>
)

data class CustomRoutineExerciseInput(
    val exerciseId: ExerciseId,
    val sets: Int,
    val repRangeStart: Int?,
    val repRangeEnd: Int?,
    val durationMinutes: Int?,
    val restSeconds: Int,
    val note: String = ""
)

data class RoutineRecommendationInput(
    val cycleLength: Int,
    val sessionMinutes: Int,
    val experience: TrainingExperience,
    val feeling: RoutineFeeling
)

data class RoutineRecommendation(
    val primaryTemplateId: String,
    val alternativeTemplateIds: List<String>,
    val reasonCode: String
)

data class RoutineProgressPreference(
    val templateId: String,
    val dayIndex: Int,
    val cycleNumber: Int,
    val startedAt: String?,
    val cycleStartedAt: String?,
    val lastCompletedDayIndex: Int?,
    val lastCompletedAt: String?,
    val lastCompletedCycleNumber: Int?,
    val lastCompletedPreviousCycleStartedAt: String?,
    val routineDayDates: Map<String, String> = emptyMap()
)

data class RoutineProgress(
    val templateId: String,
    val dayIndex: Int,
    val lastCompletedDayIndex: Int?,
    val lastCompletedAt: Instant?,
    val cycleNumber: Int = 1,
    val lastCompletedCycleNumber: Int? = null,
    val lastCompletedPreviousCycleStartedAt: Instant? = null,
    val startedAt: Instant? = null,
    val cycleStartedAt: Instant? = startedAt,
    val routineDayDates: Map<String, LocalDate> = emptyMap()
)

data class RoutineCycleCompletion(
    val id: String,
    val templateId: String,
    val cycleNumber: Int,
    val startedAt: Instant,
    val completedAt: Instant,
    val durationDays: Int,
    val completedDayIndex: Int,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null
)

fun completedCycleDurationDays(cycleStartedAt: Instant?, completedAt: Instant?): Int? {
    val start = cycleStartedAt ?: return null
    val end = completedAt ?: return null
    val elapsedMillis = Duration.between(start, end).toMillis()
    if (elapsedMillis < 0) return null
    return ((elapsedMillis + MILLIS_PER_DAY - 1) / MILLIS_PER_DAY)
        .toInt()
        .coerceAtLeast(1)
}

data class RoutineReadiness(
    val ready: Boolean,
    val remainingRecoveryHours: Long,
    val warningCode: String?
)

private const val MILLIS_PER_DAY = 86_400_000L
