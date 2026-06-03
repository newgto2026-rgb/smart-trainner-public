package com.smarttrainner.feature.routine.impl

import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.model.DifficultyLevel
import com.smarttrainner.core.model.EquipmentType
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.ExerciseMovementPattern
import com.smarttrainner.core.model.ExerciseMuscleRole
import com.smarttrainner.core.model.MuscleGroup
import org.junit.Test

class CustomRoutineExercisePickerGroupingTest {
    @Test
    fun customRoutineExercisePickerGroups_includesSecondaryMatchesInEachMatchingCategory() {
        val groups = customRoutineExercisePickerGroups(
            exercises = listOf(
                exercise(
                    id = "lat_pulldown",
                    muscleGroup = MuscleGroup.BACK,
                    movementPattern = ExerciseMovementPattern.VERTICAL_PULL,
                    popularityRank = 3,
                    variantRank = 3,
                    catalogOrder = 3
                ),
                exercise(
                    id = "conventional_deadlift",
                    muscleGroup = MuscleGroup.FULL_BODY,
                    muscleGroups = listOf(
                        MuscleGroup.FULL_BODY,
                        MuscleGroup.LOWER_BODY,
                        MuscleGroup.BACK,
                        MuscleGroup.CORE
                    ),
                    movementPattern = ExerciseMovementPattern.HINGE,
                    popularityRank = 1,
                    variantRank = 1,
                    catalogOrder = 1
                )
            ),
            allowedGroups = setOf(MuscleGroup.LOWER_BODY, MuscleGroup.BACK, MuscleGroup.CORE)
        )

        val lowerBody = groups.single { it.muscleGroup == MuscleGroup.LOWER_BODY }
        val back = groups.single { it.muscleGroup == MuscleGroup.BACK }
        val core = groups.single { it.muscleGroup == MuscleGroup.CORE }

        assertThat(lowerBody.items.map { it.exercise.id.value })
            .containsExactly("conventional_deadlift")
        assertThat(core.items.map { it.exercise.id.value })
            .containsExactly("conventional_deadlift")
        assertThat(back.items.map { it.exercise.id.value })
            .containsExactly("lat_pulldown", "conventional_deadlift")
            .inOrder()
        assertThat(back.items.map { it.role })
            .containsExactly(ExerciseMuscleRole.PRIMARY, ExerciseMuscleRole.SECONDARY)
            .inOrder()
    }

    @Test
    fun customRoutineExercisePickerGroups_keepsLegPressSeparateFromSquatVariantsForSorting() {
        val groups = customRoutineExercisePickerGroups(
            exercises = listOf(
                exercise(
                    id = "leg_press",
                    muscleGroup = MuscleGroup.LOWER_BODY,
                    movementPattern = ExerciseMovementPattern.LEG_PRESS,
                    popularityRank = 2,
                    variantRank = 1,
                    catalogOrder = 2
                ),
                exercise(
                    id = "box_squat",
                    muscleGroup = MuscleGroup.LOWER_BODY,
                    movementPattern = ExerciseMovementPattern.SQUAT,
                    popularityRank = 3,
                    variantRank = 3,
                    catalogOrder = 3
                ),
                exercise(
                    id = "barbell_back_squat",
                    muscleGroup = MuscleGroup.LOWER_BODY,
                    movementPattern = ExerciseMovementPattern.SQUAT,
                    popularityRank = 1,
                    variantRank = 1,
                    catalogOrder = 1
                )
            ),
            allowedGroups = setOf(MuscleGroup.LOWER_BODY)
        )

        assertThat(groups.single().items.map { it.exercise.id.value })
            .containsExactly("barbell_back_squat", "box_squat", "leg_press")
            .inOrder()
    }

    private fun exercise(
        id: String,
        muscleGroup: MuscleGroup,
        muscleGroups: List<MuscleGroup> = listOf(muscleGroup),
        movementPattern: ExerciseMovementPattern,
        popularityRank: Int,
        variantRank: Int,
        catalogOrder: Int
    ): Exercise = Exercise(
        id = ExerciseId(id),
        name = id,
        muscleGroup = muscleGroup,
        muscleGroups = muscleGroups,
        equipment = EquipmentType.MACHINE,
        difficulty = DifficultyLevel.BEGINNER,
        imageKey = id,
        summary = "",
        instructions = emptyList(),
        safetyCues = emptyList(),
        defaultSets = 3,
        defaultRepRange = 10..12,
        defaultDurationMinutes = null,
        restSeconds = 90,
        movementPattern = movementPattern,
        popularityRank = popularityRank,
        variantRank = variantRank,
        catalogOrder = catalogOrder
    )
}
