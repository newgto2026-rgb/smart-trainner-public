package com.smarttrainner.core.domain

import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlanId
import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.RoutineSource
import com.smarttrainner.core.model.CyclePlan
import com.smarttrainner.core.model.WorkoutDayPlan
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrainingSeedStore @Inject constructor() {
    val exercises: List<Exercise> = SeedTrainingContent.exercises
    val templates: List<PlanTemplate> = SeedTrainingContent.templates
    private val exerciseById = exercises.associateBy { it.id }

    fun exercise(id: ExerciseId): Exercise? = exerciseById[id]

    fun hasTemplate(templateId: String): Boolean =
        templates.any { it.id == templateId }

    fun templateById(templateId: String, customTemplates: List<PlanTemplate> = emptyList()): PlanTemplate =
        (templates + customTemplates).firstOrNull { it.id == templateId } ?: templates.first()

    fun buildCyclePlan(
        template: PlanTemplate,
        cycleStartDate: LocalDate,
        availableExercises: List<Exercise> = exercises
    ): CyclePlan = CyclePlan(
        id = PlanId("${template.id}_${cycleStartDate}"),
        templateId = template.id,
        name = template.name,
        cycleStartDate = cycleStartDate,
        days = template.days.map { day ->
            val date = cycleStartDate.plusDays(day.dayOffset.toLong())
            val exerciseById = availableExercises.associateBy { it.id }
            WorkoutDayPlan(
                date = date,
                title = day.title,
                focus = day.focus,
                exercises = day.exercises.mapIndexed { slotIndex, item ->
                    val exercise = exerciseById.getValue(item.exerciseId)
                    PlannedExercise(
                        id = PlannedExerciseId(
                            template.plannedExerciseId(date, day.dayNumber, slotIndex, item.exerciseId)
                        ),
                        exercise = exercise,
                        sets = item.sets,
                        repRange = item.repRange,
                        durationMinutes = item.durationMinutes,
                        restSeconds = item.restSeconds,
                        note = item.note
                    )
                },
                dayNumber = day.dayNumber,
                primaryFocus = day.primaryFocus,
                secondaryFocuses = day.secondaryFocuses,
                minRecoveryHours = day.minRecoveryHours
            )
        }
    )

    private fun PlanTemplate.plannedExerciseId(
        date: LocalDate,
        dayNumber: Int,
        slotIndex: Int,
        exerciseId: ExerciseId
    ): String = if (source == RoutineSource.CUSTOM) {
        "${date}_${id}_day${dayNumber}_slot${slotIndex + 1}_${exerciseId.value}"
    } else {
        "${date}_${exerciseId.value}"
    }
}
