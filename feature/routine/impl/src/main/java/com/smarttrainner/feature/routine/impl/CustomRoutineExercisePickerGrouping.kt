package com.smarttrainner.feature.routine.impl

import com.smarttrainner.core.model.Exercise
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
