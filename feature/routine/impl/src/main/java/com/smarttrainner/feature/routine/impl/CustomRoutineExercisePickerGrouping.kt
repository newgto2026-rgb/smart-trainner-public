package com.smarttrainner.feature.routine.impl

import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseMovementPattern
import com.smarttrainner.core.model.ExerciseMuscleRole
import com.smarttrainner.core.model.MuscleGroup

internal data class CustomRoutineExercisePickerGroup(
    val muscleGroup: MuscleGroup,
    val items: List<CustomRoutineExercisePickerItem>
)

internal data class CustomRoutineExercisePickerItem(
    val exercise: Exercise,
    val muscleGroup: MuscleGroup,
    val role: ExerciseMuscleRole
)

internal fun customRoutineExercisePickerGroups(
    exercises: List<Exercise>,
    allowedGroups: Set<MuscleGroup>
): List<CustomRoutineExercisePickerGroup> =
    exercises
        .flatMap { exercise -> exercise.toCustomRoutinePickerItems(allowedGroups) }
        .groupBy { it.muscleGroup }
        .map { (muscleGroup, items) ->
            CustomRoutineExercisePickerGroup(
                muscleGroup = muscleGroup,
                items = items.sortedWith(customRoutineExerciseComparator)
            )
        }
        .sortedBy { MuscleGroup.entries.indexOf(it.muscleGroup) }

private fun Exercise.toCustomRoutinePickerItems(
    allowedGroups: Set<MuscleGroup>
): List<CustomRoutineExercisePickerItem> =
    involvedMuscleGroups.mapNotNull { group ->
        roleFor(group)
            ?.takeIf { group in allowedGroups }
            ?.let { role ->
                CustomRoutineExercisePickerItem(
                    exercise = this,
                    muscleGroup = group,
                    role = role
                )
            }
    }

private val customRoutineExerciseComparator = compareBy<CustomRoutineExercisePickerItem>(
    { it.role.sortRank },
    { it.exercise.movementPattern.sortRank },
    { it.exercise.variantRank },
    { it.exercise.popularityRank },
    { it.exercise.catalogOrder },
    { it.exercise.id.value }
)

private val ExerciseMuscleRole.sortRank: Int
    get() = when (this) {
        ExerciseMuscleRole.PRIMARY -> 0
        ExerciseMuscleRole.SECONDARY -> 1
    }

private val ExerciseMovementPattern.sortRank: Int
    get() = when (this) {
        ExerciseMovementPattern.SQUAT -> 0
        ExerciseMovementPattern.LEG_PRESS -> 1
        ExerciseMovementPattern.HINGE -> 2
        ExerciseMovementPattern.LUNGE -> 3
        ExerciseMovementPattern.STEP_UP -> 4
        ExerciseMovementPattern.HIP_EXTENSION -> 5
        ExerciseMovementPattern.KNEE_EXTENSION -> 6
        ExerciseMovementPattern.KNEE_FLEXION -> 7
        ExerciseMovementPattern.CALF_RAISE -> 8
        ExerciseMovementPattern.VERTICAL_PULL -> 9
        ExerciseMovementPattern.HORIZONTAL_PULL -> 10
        ExerciseMovementPattern.HORIZONTAL_PUSH -> 11
        ExerciseMovementPattern.VERTICAL_PUSH -> 12
        ExerciseMovementPattern.CHEST_ISOLATION -> 13
        ExerciseMovementPattern.SHOULDER_ISOLATION -> 14
        ExerciseMovementPattern.ARM_ISOLATION -> 15
        ExerciseMovementPattern.CORE_STABILITY -> 16
        ExerciseMovementPattern.CORE_FLEXION -> 17
        ExerciseMovementPattern.CORE_ROTATION -> 18
        ExerciseMovementPattern.CARRY -> 19
        ExerciseMovementPattern.CONDITIONING -> 20
        ExerciseMovementPattern.CARDIO -> 21
        ExerciseMovementPattern.ACCESSORY -> 22
    }
