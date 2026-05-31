package com.smarttrainner.core.data

import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.database.CustomRoutineDayEntity
import com.smarttrainner.core.database.CustomRoutineDayWithExercises
import com.smarttrainner.core.database.CustomRoutineEntity
import com.smarttrainner.core.database.CustomRoutineExerciseEntity
import com.smarttrainner.core.database.CustomRoutineWithDays
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

    private fun customRoutine(
        primaryFocus: String,
        focus: String
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
                        sets = 3,
                        repRangeStart = 8,
                        repRangeEnd = 12,
                        durationMinutes = null,
                        restSeconds = 90,
                        note = ""
                    )
                )
            )
        )
    )
}
