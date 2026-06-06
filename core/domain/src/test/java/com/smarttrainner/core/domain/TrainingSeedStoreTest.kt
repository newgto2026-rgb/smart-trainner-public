package com.smarttrainner.core.domain

import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.RoutineSource
import java.time.LocalDate
import org.junit.Test

class TrainingSeedStoreTest {
    private val store = TrainingSeedStore()

    @Test
    fun lookupsReturnExercisesTemplatesCustomTemplatesAndFallbackTemplate() {
        val exercise = store.exercises.first()
        val systemTemplate = store.templates.first { it.source == RoutineSource.SYSTEM }
        val customTemplate = systemTemplate.copy(id = "custom-template", source = RoutineSource.CUSTOM)

        assertThat(store.exercise(exercise.id)).isEqualTo(exercise)
        assertThat(store.exercise(ExerciseId("missing"))).isNull()
        assertThat(store.hasTemplate(systemTemplate.id)).isTrue()
        assertThat(store.hasTemplate("missing")).isFalse()
        assertThat(store.templateById(customTemplate.id, customTemplates = listOf(customTemplate)))
            .isEqualTo(customTemplate)
        assertThat(store.templateById("missing")).isEqualTo(store.templates.first())
    }

    @Test
    fun buildCyclePlanKeepsSystemPlannedExerciseIdsStable() {
        val template = store.templates.first { it.source == RoutineSource.SYSTEM }
        val cycleStartDate = LocalDate.parse("2026-05-25")
        val plan = store.buildCyclePlan(template, cycleStartDate)
        val firstTemplateDay = template.days.first()
        val firstTemplateExercise = firstTemplateDay.exercises.first()
        val firstPlannedExercise = plan.days.first().exercises.first()

        assertThat(plan.id.value).isEqualTo("${template.id}_$cycleStartDate")
        assertThat(firstPlannedExercise.id.value)
            .isEqualTo("${cycleStartDate}_${firstTemplateExercise.exerciseId.value}")
    }

    @Test
    fun buildCyclePlanKeepsCustomPlannedExerciseIdsStable() {
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
        val cycleStartDate = LocalDate.parse("2026-05-25")
        val plan = store.buildCyclePlan(customTemplate, cycleStartDate)
        val plannedDate = cycleStartDate.plusDays(customDay.dayOffset.toLong())
        val exerciseId = customDay.exercises.first().exerciseId.value

        assertThat(plan.days.single().exercises.single().id.value)
            .isEqualTo("${plannedDate}_${customTemplate.id}_day3_slot1_$exerciseId")
    }
}
