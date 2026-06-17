package com.smarttrainner.core.model

import java.time.Instant

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

enum class ExerciseLoadType {
    EXTERNAL_LOAD,
    ASSISTANCE_LOAD
}

enum class ExerciseSource {
    SYSTEM,
    USER_CREATED,
    SHARED
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
    val catalogOrder: Int = Int.MAX_VALUE,
    val loadType: ExerciseLoadType = ExerciseLoadType.EXTERNAL_LOAD,
    val source: ExerciseSource = ExerciseSource.SYSTEM,
    val ownerSessionId: UserSessionId? = null,
    val originExerciseId: ExerciseId? = null,
    val imageUri: String? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
    val archivedAt: Instant? = null
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

data class CustomExerciseInput(
    val id: ExerciseId? = null,
    val name: String,
    val muscleGroup: MuscleGroup,
    val equipment: EquipmentType,
    val difficulty: DifficultyLevel,
    val imageUri: String? = null,
    val summary: String = "",
    val instructions: List<String>,
    val safetyCues: List<String>,
    val defaultSets: Int,
    val repRangeStart: Int?,
    val repRangeEnd: Int?,
    val defaultDurationMinutes: Int?,
    val restSeconds: Int
)

fun Exercise.targetsMuscleGroup(group: MuscleGroup): Boolean =
    group in muscleGroups

fun Exercise.targetsAnyMuscleGroup(groups: Collection<MuscleGroup>): Boolean =
    muscleGroups.any { it in groups }
