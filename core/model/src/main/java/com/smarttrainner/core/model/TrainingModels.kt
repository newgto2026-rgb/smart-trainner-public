package com.smarttrainner.core.model

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
    val nickname: String,
    val email: String?,
    val provider: AuthProvider,
    val providerAccountId: String?,
    val avatarUrl: String?,
    val linkedAt: String?
) {
    val isLinked: Boolean
        get() = provider != AuthProvider.LOCAL
}

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

enum class PlanLevel(val displayName: String) {
    INTRO("입문"),
    BEGINNER("초보"),
    INTERMEDIATE("초중급")
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
    INTERMEDIATE
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
    val equipment: EquipmentType,
    val difficulty: DifficultyLevel,
    val imageKey: String,
    val summary: String,
    val instructions: List<String>,
    val safetyCues: List<String>,
    val defaultSets: Int,
    val defaultRepRange: IntRange?,
    val defaultDurationMinutes: Int?,
    val restSeconds: Int
) {
    val targetText: String
        get() = if (defaultRepRange != null) {
            "${defaultSets}세트 x ${defaultRepRange.first}-${defaultRepRange.last}회"
        } else {
            "${defaultSets}세트 x ${defaultDurationMinutes ?: 10}분"
        }
}

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
    val note: String
)

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
    val startedAt: String?,
    val cycleStartedAt: String?,
    val lastCompletedDayIndex: Int?,
    val lastCompletedAt: String?
)

data class RoutineProgress(
    val templateId: String,
    val dayIndex: Int,
    val lastCompletedDayIndex: Int?,
    val lastCompletedAt: Instant?,
    val startedAt: Instant? = null,
    val cycleStartedAt: Instant? = startedAt
)

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
            "${sets}세트 x ${repRange.first}-${repRange.last}회"
        } else {
            "${sets}세트 x ${durationMinutes ?: 10}분"
        }
}

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
