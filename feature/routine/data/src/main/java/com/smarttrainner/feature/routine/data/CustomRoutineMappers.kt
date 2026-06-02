package com.smarttrainner.feature.routine.data

import com.smarttrainner.core.database.CustomRoutineDayEntity
import com.smarttrainner.core.database.CustomRoutineDayWithExercises
import com.smarttrainner.core.database.CustomRoutineDayWrite
import com.smarttrainner.core.database.CustomRoutineEntity
import com.smarttrainner.core.database.CustomRoutineExerciseEntity
import com.smarttrainner.core.database.CustomRoutineWithDays
import com.smarttrainner.core.model.CustomRoutineDayInput
import com.smarttrainner.core.model.CustomRoutineExerciseInput
import com.smarttrainner.core.model.CustomRoutineInput
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

fun CustomRoutineInput.toEntity(
    routineId: String,
    sessionId: String,
    createdAt: String,
    updatedAt: String
): CustomRoutineEntity = CustomRoutineEntity(
    id = routineId,
    sessionId = sessionId,
    name = name.trim(),
    description = description.trim(),
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun CustomRoutineInput.toDayWrites(routineId: String): List<CustomRoutineDayWrite> =
    days.mapIndexed { dayIndex, day ->
        val dayId = customRoutineDayId(routineId, dayIndex)
        CustomRoutineDayWrite(
            day = day.toEntity(
                id = dayId,
                routineId = routineId,
                dayIndex = dayIndex
            ),
            exercises = day.exercises.mapIndexed { slotIndex, exercise ->
                exercise.toEntity(
                    id = customRoutineExerciseId(dayId, slotIndex),
                    dayId = dayId,
                    slotIndex = slotIndex
                )
            }
        )
    }

fun CustomRoutineDayInput.toEntity(
    id: String,
    routineId: String,
    dayIndex: Int
): CustomRoutineDayEntity = CustomRoutineDayEntity(
    id = id,
    routineId = routineId,
    dayIndex = dayIndex,
    title = title.trim(),
    focus = focus.trim().ifEmpty { primaryFocus?.name.orEmpty() },
    primaryFocus = primaryFocus?.name.orEmpty(),
    secondaryFocuses = secondaryFocuses.joinToString(",") { it.name },
    minRecoveryHours = minRecoveryHours
)

fun CustomRoutineExerciseInput.toEntity(
    id: String,
    dayId: String,
    slotIndex: Int
): CustomRoutineExerciseEntity = CustomRoutineExerciseEntity(
    id = id,
    dayId = dayId,
    slotIndex = slotIndex,
    exerciseId = exerciseId.value,
    sets = sets,
    repRangeStart = repRangeStart,
    repRangeEnd = repRangeEnd,
    durationMinutes = durationMinutes,
    restSeconds = restSeconds,
    note = note
)

fun CustomRoutineWithDays.toPlanTemplate(exercises: List<Exercise> = emptyList()): PlanTemplate {
    val orderedDays = days.sortedBy { it.day.dayIndex }
    val repDurationSecondsByExerciseId = exercises.associate { it.id.value to it.defaultRepDurationSeconds }
    val focusSummary = orderedDays
        .flatMap { day -> listOfNotNull(day.day.primaryFocus.toCustomRoutineFocus()) + day.day.secondaryFocuses.toRoutineFocuses() }
        .distinct()
    return PlanTemplate(
        id = routine.id,
        name = routine.name,
        level = PlanLevel.INTERMEDIATE,
        daysPerWeek = orderedDays.size,
        description = routine.description,
        days = orderedDays.map { it.toPlanTemplateDay(repDurationSecondsByExerciseId) },
        structure = RoutineStructure.BALANCED_SPLIT,
        recommendedExperience = TrainingExperience.INTERMEDIATE,
        cycleLength = orderedDays.size,
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

private fun customRoutineDayId(routineId: String, dayIndex: Int): String =
    "$routineId-day-${dayIndex + 1}"

private fun customRoutineExerciseId(dayId: String, slotIndex: Int): String =
    "$dayId-slot-${slotIndex + 1}"
