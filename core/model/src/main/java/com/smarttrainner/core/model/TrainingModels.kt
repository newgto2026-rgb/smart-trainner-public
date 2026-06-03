package com.smarttrainner.core.model

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

@JvmInline
value class ExerciseId(val value: String)

@JvmInline
value class PlanId(val value: String)

@JvmInline
value class PlannedExerciseId(val value: String)

@JvmInline
value class WorkoutLogId(val value: Long)

@JvmInline
value class UserSessionId(val value: String)

enum class AuthProvider {
    LOCAL,
    GOOGLE
}

data class UserSession(
    val id: UserSessionId,
    val displayName: String,
    val nickname: String = displayName,
    val email: String?,
    val provider: AuthProvider,
    val linkedAt: String?
) {
    val isLinked: Boolean
        get() = provider != AuthProvider.LOCAL
}

data class NicknameAvailability(
    val nickname: String,
    val available: Boolean
)

enum class MuscleGroup(val displayName: String) {
    LOWER_BODY("하체"),
    BACK("등"),
    CHEST("가슴"),
    SHOULDERS("어깨"),
    ARMS("팔"),
    BICEPS("이두"),
    TRICEPS("삼두"),
    FOREARMS("전완근"),
    CORE("코어"),
    CARDIO("유산소"),
    FULL_BODY("전신")
}

enum class EquipmentType(val displayName: String) {
    BODYWEIGHT("맨몸"),
    DUMBBELL("덤벨"),
    KETTLEBELL("케틀벨"),
    BARBELL("바벨"),
    MACHINE("머신"),
    CABLE("케이블"),
    BENCH("벤치"),
    CARDIO_MACHINE("유산소 머신")
}

enum class DifficultyLevel(val displayName: String) {
    BEGINNER("초보"),
    INTERMEDIATE("초중급"),
    ADVANCED("숙련")
}

enum class ExerciseMuscleRole {
    PRIMARY,
    SECONDARY
}

enum class ExerciseMovementPattern(val sortRank: Int) {
    SQUAT(0),
    LEG_PRESS(1),
    HINGE(2),
    LUNGE(3),
    STEP_UP(4),
    HIP_EXTENSION(5),
    KNEE_EXTENSION(6),
    KNEE_FLEXION(7),
    CALF_RAISE(8),
    VERTICAL_PULL(9),
    HORIZONTAL_PULL(10),
    HORIZONTAL_PUSH(11),
    VERTICAL_PUSH(12),
    CHEST_ISOLATION(13),
    SHOULDER_ISOLATION(14),
    ARM_ISOLATION(15),
    CORE_STABILITY(16),
    CORE_FLEXION(17),
    CORE_ROTATION(18),
    CARRY(19),
    CONDITIONING(20),
    CARDIO(21),
    ACCESSORY(22)
}

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

data class Exercise(
    val id: ExerciseId,
    val name: String,
    val muscleGroup: MuscleGroup,
    val muscleGroups: List<MuscleGroup> = listOf(muscleGroup),
    val equipment: EquipmentType,
    val difficulty: DifficultyLevel,
    val imageKey: String,
    val summary: String,
    val instructions: List<String>,
    val safetyCues: List<String>,
    val defaultSets: Int,
    val defaultRepRange: IntRange?,
    val defaultDurationMinutes: Int?,
    val restSeconds: Int,
    val defaultRepDurationSeconds: Int = DEFAULT_REP_DURATION_SECONDS,
    val movementPattern: ExerciseMovementPattern = ExerciseMovementPattern.ACCESSORY,
    val popularityRank: Int = Int.MAX_VALUE,
    val variantRank: Int = Int.MAX_VALUE,
    val catalogOrder: Int = Int.MAX_VALUE
) {
    val targetText: String
        get() = if (defaultRepRange != null) {
            val displayRepRange = defaultRepRange.toRecommendedDisplayRepRange()
            if (displayRepRange.first == displayRepRange.last) {
                "${defaultSets}세트 x ${displayRepRange.first}회"
            } else {
                "${defaultSets}세트 x ${displayRepRange.first}-${displayRepRange.last}회"
            }
        } else {
            "${defaultSets}세트 x ${defaultDurationMinutes ?: 10}분"
    }

    val secondaryMuscleGroups: List<MuscleGroup>
        get() = muscleGroups.filterNot { it == muscleGroup }

    val involvedMuscleGroups: List<MuscleGroup>
        get() = (listOf(muscleGroup) + secondaryMuscleGroups).distinct()

    fun roleFor(group: MuscleGroup): ExerciseMuscleRole? = when {
        group == muscleGroup -> ExerciseMuscleRole.PRIMARY
        group in secondaryMuscleGroups -> ExerciseMuscleRole.SECONDARY
        else -> null
    }
}

fun Exercise.targetsMuscleGroup(group: MuscleGroup): Boolean =
    group in muscleGroups

fun Exercise.targetsAnyMuscleGroup(groups: Collection<MuscleGroup>): Boolean =
    muscleGroups.any { it in groups }

fun IntRange.toRecommendedDisplayRepRange(): IntRange =
    if (first == FIXED_REP_TARGET && last == FIXED_REP_TARGET) {
        RECOMMENDED_REP_DISPLAY_START..FIXED_REP_TARGET
    } else {
        this
    }

private const val FIXED_REP_TARGET = 15
private const val RECOMMENDED_REP_DISPLAY_START = 12

data class PlanTemplate(
    val id: String,
    val name: String,
    val level: PlanLevel,
    val daysPerWeek: Int,
    val description: String,
    val days: List<PlanTemplateDay>,
    val structure: RoutineStructure = RoutineStructure.FULL_BODY,
    val recommendedExperience: TrainingExperience = TrainingExperience.BEGINNER,
    val cycleLength: Int = days.size,
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

data class WeeklyPlan(
    val id: PlanId,
    val templateId: String,
    val name: String,
    val weekStartDate: LocalDate,
    val days: List<WorkoutDayPlan>
)

data class WorkoutDayPlan(
    val date: LocalDate,
    val title: String,
    val focus: String,
    val exercises: List<PlannedExercise>,
    val dayNumber: Int = 1,
    val primaryFocus: RoutineFocus? = RoutineFocus.FULL_BODY,
    val secondaryFocuses: List<RoutineFocus> = emptyList(),
    val minRecoveryHours: Int = 24
)

data class RoutineRecommendationInput(
    val daysPerWeek: Int,
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
    val lastCompletedCycleDurationDays: Int? = null
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
    val lastCompletedCycleDurationDays: Int? = null
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

private const val MILLIS_PER_DAY = 86_400_000L

data class RoutineReadiness(
    val ready: Boolean,
    val remainingRecoveryHours: Long,
    val warningCode: String?
)

data class PlannedExercise(
    val id: PlannedExerciseId,
    val exercise: Exercise,
    val sets: Int,
    val repRange: IntRange?,
    val durationMinutes: Int?,
    val restSeconds: Int,
    val note: String
) {
    val targetText: String
        get() = if (repRange != null) {
            val displayRepRange = repRange.toRecommendedDisplayRepRange()
            "${sets}세트 x ${displayRepRange.first}-${displayRepRange.last}회"
        } else {
            "${sets}세트 x ${durationMinutes ?: 10}분"
        }

    val estimatedActiveSecondsPerSet: Int
        get() = durationMinutes?.let { it * SECONDS_PER_MINUTE }
            ?: ((repRange?.last ?: 0) * exercise.defaultRepDurationSeconds)

    val estimatedSecondsPerSet: Int
        get() = estimatedActiveSecondsPerSet + restSeconds

    val estimatedTotalSeconds: Int
        get() = estimateExerciseSeconds(
            sets = sets,
            repRange = repRange,
            durationMinutes = durationMinutes,
            restSeconds = restSeconds,
            repDurationSeconds = exercise.defaultRepDurationSeconds
        )

    val estimatedMinutes: Int
        get() = estimatedTotalSeconds.roundUpToMinutes()
}

val WorkoutDayPlan.estimatedSessionMinutes: Int
    get() = exercises.sumOf { it.estimatedTotalSeconds }.roundUpToMinutes()

data class WorkoutLog(
    val id: WorkoutLogId,
    val sessionId: UserSessionId,
    val plannedExerciseId: PlannedExerciseId,
    val exerciseId: ExerciseId,
    val performedAt: LocalDateTime,
    val sets: Int,
    val reps: Int?,
    val weightKg: Double?,
    val durationMinutes: Int?,
    val memo: String,
    val completed: Boolean,
    val setEntries: List<WorkoutSetLog> = emptyList()
) {
    val volumeKg: Double
        get() = if (setEntries.isNotEmpty()) {
            setEntries.sumOf { it.volumeKg }
        } else if (reps != null && weightKg != null) {
            sets * reps * weightKg
        } else {
            0.0
        }
}

data class WorkoutLogInput(
    val plannedExerciseId: PlannedExerciseId,
    val exerciseId: ExerciseId,
    val performedAt: LocalDateTime,
    val sets: Int,
    val reps: Int?,
    val weightKg: Double?,
    val durationMinutes: Int?,
    val memo: String,
    val completed: Boolean,
    val setEntries: List<WorkoutSetLog> = emptyList()
)

data class WorkoutSetLog(
    val order: Int,
    val reps: Int?,
    val weightKg: Double?,
    val durationMinutes: Int?,
    val restSeconds: Int? = null
) {
    val volumeKg: Double
        get() = if (reps != null && weightKg != null) reps * weightKg else 0.0
}

data class WeeklySummary(
    val weekStartDate: LocalDate,
    val plannedExerciseCount: Int,
    val completedExerciseCount: Int,
    val totalSets: Int,
    val totalVolumeKg: Double,
    val totalMinutes: Int,
    val streakDays: Int,
    val muscleBalance: Map<MuscleGroup, Int>,
    val insight: String
) {
    val completionRate: Int
        get() = if (plannedExerciseCount == 0) 0 else completedExerciseCount * 100 / plannedExerciseCount
}

fun estimateExerciseSeconds(
    sets: Int,
    repRange: IntRange?,
    durationMinutes: Int?,
    restSeconds: Int,
    repDurationSeconds: Int = DEFAULT_REP_DURATION_SECONDS
): Int {
    val activeSecondsPerSet = durationMinutes?.let { it * SECONDS_PER_MINUTE }
        ?: ((repRange?.last ?: 0) * repDurationSeconds)
    return sets * (activeSecondsPerSet + restSeconds)
}

const val DEFAULT_REP_DURATION_SECONDS = 5

private const val SECONDS_PER_MINUTE = 60

private fun Int.roundUpToMinutes(): Int =
    if (this <= 0) 0 else (this + SECONDS_PER_MINUTE - 1) / SECONDS_PER_MINUTE
