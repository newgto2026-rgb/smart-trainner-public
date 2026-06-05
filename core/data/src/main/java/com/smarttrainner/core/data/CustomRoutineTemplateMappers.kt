package com.smarttrainner.core.data

import com.smarttrainner.core.database.CustomRoutineDayEntity
import com.smarttrainner.core.database.CustomRoutineDayWithExercises
import com.smarttrainner.core.database.CustomRoutineExerciseEntity
import com.smarttrainner.core.database.CustomRoutineWithDays
import com.smarttrainner.core.model.DEFAULT_REP_DURATION_SECONDS
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlanLevel
import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.PlanTemplateDay
import com.smarttrainner.core.model.RoutineFocus
import com.smarttrainner.core.model.RoutineSource
import com.smarttrainner.core.model.RoutineStructure
import com.smarttrainner.core.model.TemplateExercise
import com.smarttrainner.core.model.TrainingExperience
import com.smarttrainner.core.model.estimateExerciseSeconds

internal fun CustomRoutineWithDays.toPlanTemplate(exercises: List<Exercise> = emptyList()): PlanTemplate {
    val orderedDays = days.sortedBy { it.day.dayIndex }
    val repDurationSecondsByExerciseId = exercises.associate { it.id.value to it.defaultRepDurationSeconds }
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
        cycleLength = orderedDays.size,
        description = routine.description,
        days = orderedDays.map { it.toPlanTemplateDay(repDurationSecondsByExerciseId) },
        structure = RoutineStructure.BALANCED_SPLIT,
        recommendedExperience = TrainingExperience.INTERMEDIATE,
        sessionMinutes = orderedDays.maxOfOrNull { day ->
            day.exercises.estimateSessionMinutes(repDurationSecondsByExerciseId)
        } ?: 45,
        focusSummary = focusSummary,
        source = RoutineSource.CUSTOM
    )
}

private fun CustomRoutineDayWithExercises.toPlanTemplateDay(
    repDurationSecondsByExerciseId: Map<String, Int>
): PlanTemplateDay = PlanTemplateDay(
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
                note = it.note,
                repDurationSeconds = repDurationSecondsByExerciseId[it.exerciseId] ?: DEFAULT_REP_DURATION_SECONDS
            )
        },
    dayNumber = day.dayIndex + 1,
    primaryFocus = day.primaryFocus.toCustomRoutineFocus(),
    secondaryFocuses = day.secondaryFocuses.toRoutineFocuses(),
    minRecoveryHours = day.minRecoveryHours
)

private fun List<CustomRoutineExerciseEntity>.estimateSessionMinutes(
    repDurationSecondsByExerciseId: Map<String, Int>
): Int = sumOf { exercise ->
    estimateExerciseSeconds(
        sets = exercise.sets,
        repRange = exercise.repRange(),
        durationMinutes = exercise.durationMinutes,
        restSeconds = exercise.restSeconds,
        repDurationSeconds = repDurationSecondsByExerciseId[exercise.exerciseId] ?: DEFAULT_REP_DURATION_SECONDS
    )
}.roundUpToMinutes().coerceAtLeast(15)

private fun CustomRoutineExerciseEntity.repRange(): IntRange? {
    val start = repRangeStart
    val end = repRangeEnd
    return if (start != null && end != null) start..end else null
}

private fun Int.roundUpToMinutes(): Int =
    if (this <= 0) 0 else (this + 59) / 60

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
