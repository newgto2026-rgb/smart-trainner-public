package com.smarttrainner.core.data

import com.smarttrainner.core.database.CustomRoutineDayEntity
import com.smarttrainner.core.database.CustomRoutineDayWithExercises
import com.smarttrainner.core.database.CustomRoutineExerciseEntity
import com.smarttrainner.core.database.CustomRoutineWithDays
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlanLevel
import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.PlanTemplateDay
import com.smarttrainner.core.model.RoutineFocus
import com.smarttrainner.core.model.RoutineSource
import com.smarttrainner.core.model.RoutineStructure
import com.smarttrainner.core.model.TemplateExercise
import com.smarttrainner.core.model.TrainingExperience

internal fun CustomRoutineWithDays.toPlanTemplate(): PlanTemplate {
    val orderedDays = days.sortedBy { it.day.dayIndex }
    val focusSummary = orderedDays
        .flatMap { day ->
            listOfNotNull(day.day.primaryFocus.toCustomRoutineFocus()) +
                day.day.secondaryFocuses.toRoutineFocuses()
        }
        .distinct()
    return PlanTemplate(
        id = routine.id,
        name = routine.name,
        level = PlanLevel.INTERMEDIATE,
        daysPerWeek = orderedDays.size,
        description = routine.description,
        days = orderedDays.map { it.toPlanTemplateDay() },
        structure = RoutineStructure.BALANCED_SPLIT,
        recommendedExperience = TrainingExperience.INTERMEDIATE,
        cycleLength = orderedDays.size,
        sessionMinutes = orderedDays.maxOfOrNull { day -> day.exercises.estimateSessionMinutes() } ?: 45,
        focusSummary = focusSummary,
        source = RoutineSource.CUSTOM
    )
}

private fun CustomRoutineDayWithExercises.toPlanTemplateDay(): PlanTemplateDay = PlanTemplateDay(
    dayOffset = day.dayIndex,
    title = day.title,
    focus = day.customRoutineFocusText(),
    exercises = exercises
        .sortedBy { it.slotIndex }
        .map {
            val repRangeStart = it.repRangeStart
            val repRangeEnd = it.repRangeEnd
            TemplateExercise(
                exerciseId = ExerciseId(it.exerciseId),
                sets = it.sets,
                repRange = if (repRangeStart != null && repRangeEnd != null) {
                    repRangeStart..repRangeEnd
                } else {
                    null
                },
                durationMinutes = it.durationMinutes,
                restSeconds = it.restSeconds,
                note = it.note
            )
        },
    dayNumber = day.dayIndex + 1,
    primaryFocus = day.primaryFocus.toCustomRoutineFocus(),
    secondaryFocuses = day.secondaryFocuses.toRoutineFocuses(),
    minRecoveryHours = day.minRecoveryHours
)

private fun List<CustomRoutineExerciseEntity>.estimateSessionMinutes(): Int {
    val workingMinutes = sumOf { exercise ->
        exercise.durationMinutes ?: (exercise.sets * 3)
    }
    val restMinutes = sumOf { it.restSeconds * it.sets } / 60
    return (workingMinutes + restMinutes).coerceAtLeast(15)
}

private fun CustomRoutineDayEntity.customRoutineFocusText(): String =
    if (primaryFocus.toCustomRoutineFocus() == null && focus == primaryFocus) "" else focus

private fun String.toCustomRoutineFocus(): RoutineFocus? =
    runCatching { RoutineFocus.valueOf(this) }
        .getOrNull()
        ?.takeUnless { it == RoutineFocus.FULL_BODY }

private fun String.toRoutineFocuses(): List<RoutineFocus> =
    split(",")
        .mapNotNull { raw ->
            raw.takeIf { it.isNotBlank() }?.let {
                runCatching { RoutineFocus.valueOf(it) }.getOrNull()
            }
        }
