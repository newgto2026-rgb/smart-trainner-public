package com.smarttrainner.core.data

import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.model.RoutineSource
import java.time.LocalDate
import org.junit.Test

class TrainingSeedStoreTest {
    private val store = TrainingSeedStore()

    @Test
    fun buildWeeklyPlanKeepsSystemPlannedExerciseIdsStable() {
        val template = store.templates.first { it.source == RoutineSource.SYSTEM }
        val weekStartDate = LocalDate.parse("2026-05-25")
        val plan = store.buildWeeklyPlan(template, weekStartDate)
        val firstTemplateDay = template.days.first()
        val firstTemplateExercise = firstTemplateDay.exercises.first()
        val firstPlannedExercise = plan.days.first().exercises.first()

        assertThat(plan.id.value).isEqualTo("${template.id}_$weekStartDate")
        assertThat(firstPlannedExercise.id.value)
            .isEqualTo("${weekStartDate}_${firstTemplateExercise.exerciseId.value}")
    }

    @Test
    fun buildWeeklyPlanKeepsCustomPlannedExerciseIdsStable() {
        val systemTemplate = store.templates.first { it.days.isNotEmpty() }
        val customDay = systemTemplate.days.first().copy(
            dayOffset = 2,
            dayNumber = 3,
            exercises = listOf(systemTemplate.days.first().exercises.first())
        )
        val customTemplate = systemTemplate.copy(
            id = "custom-test",
            days = listOf(customDay),
            source = RoutineSource.CUSTOM
        )
        val weekStartDate = LocalDate.parse("2026-05-25")
        val plan = store.buildWeeklyPlan(customTemplate, weekStartDate)
        val plannedDate = weekStartDate.plusDays(customDay.dayOffset.toLong())
        val exerciseId = customDay.exercises.first().exerciseId.value

        assertThat(plan.days.single().exercises.single().id.value)
            .isEqualTo("${plannedDate}_${customTemplate.id}_day3_slot1_$exerciseId")
    }
}
