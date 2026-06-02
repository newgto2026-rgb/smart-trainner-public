package com.smarttrainner.core.data

import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.database.CustomRoutineDayEntity
import com.smarttrainner.core.database.CustomRoutineDayWithExercises
import com.smarttrainner.core.database.CustomRoutineEntity
import com.smarttrainner.core.database.CustomRoutineExerciseEntity
import com.smarttrainner.core.database.CustomRoutineWithDays
import com.smarttrainner.core.model.DifficultyLevel
import com.smarttrainner.core.model.EquipmentType
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.RoutineFocus
import org.junit.Test

class CustomRoutineTemplateMappersTest {
    @Test
    fun customRoutineWithoutFocusDoesNotDefaultToFullBody() {
        val template = customRoutine(primaryFocus = "", focus = "").toPlanTemplate()

        assertThat(template.focusSummary).isEmpty()
        assertThat(template.days.single().primaryFocus).isNull()
        assertThat(template.days.single().focus).isEmpty()
    }

    @Test
    fun customRoutineExplicitFocusIsMapped() {
        val template = customRoutine(primaryFocus = "BACK", focus = "BACK").toPlanTemplate()

        assertThat(template.focusSummary).containsExactly(RoutineFocus.BACK)
        assertThat(template.days.single().primaryFocus).isEqualTo(RoutineFocus.BACK)
    }

    @Test
    fun customRoutineSessionMinutesUsesRepDurationAndRestFormula() {
        val template = customRoutine(
            primaryFocus = "BACK",
            focus = "BACK",
            sets = 5,
            restSeconds = 180
        ).toPlanTemplate(
            exercises = listOf(exercise("back_pull", repDurationSeconds = 6))
        )

        assertThat(template.days.single().exercises.single().repDurationSeconds).isEqualTo(6)
        assertThat(template.sessionMinutes).isEqualTo(21)
    }

    private fun customRoutine(
        primaryFocus: String,
        focus: String,
        sets: Int = 3,
        restSeconds: Int = 90
    ) = CustomRoutineWithDays(
        routine = CustomRoutineEntity(
            id = "custom-1",
            sessionId = "local-default",
            name = "My routine",
            description = "",
            createdAt = "2026-05-30T00:00:00Z",
            updatedAt = "2026-05-30T00:00:00Z"
        ),
        days = listOf(
            CustomRoutineDayWithExercises(
                day = CustomRoutineDayEntity(
                    id = "custom-1-day-1",
                    routineId = "custom-1",
                    dayIndex = 0,
                    title = "1일차",
                    focus = focus,
                    primaryFocus = primaryFocus,
                    secondaryFocuses = "",
                    minRecoveryHours = 24
                ),
                exercises = listOf(
                    CustomRoutineExerciseEntity(
                        id = "custom-1-day-1-slot-1",
                        dayId = "custom-1-day-1",
                        slotIndex = 0,
                        exerciseId = "back_pull",
                        sets = sets,
                        repRangeStart = 8,
                        repRangeEnd = 12,
                        durationMinutes = null,
                        restSeconds = restSeconds,
                        note = ""
                    )
                )
            )
        )
    )

    private fun exercise(id: String, repDurationSeconds: Int) = Exercise(
        id = ExerciseId(id),
        name = id,
        muscleGroup = MuscleGroup.BACK,
        equipment = EquipmentType.MACHINE,
        difficulty = DifficultyLevel.INTERMEDIATE,
        imageKey = id,
        summary = "",
        instructions = emptyList(),
        safetyCues = emptyList(),
        defaultSets = 3,
        defaultRepRange = 8..12,
        defaultDurationMinutes = null,
        restSeconds = 90,
        defaultRepDurationSeconds = repDurationSeconds
    )
}
