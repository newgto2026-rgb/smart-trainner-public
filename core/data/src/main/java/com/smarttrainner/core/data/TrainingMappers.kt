package com.smarttrainner.core.data

import com.smarttrainner.core.database.CustomRoutineDayEntity
import com.smarttrainner.core.database.CustomRoutineDayWithExercises
import com.smarttrainner.core.database.CustomRoutineDayWrite
import com.smarttrainner.core.database.CustomRoutineEntity
import com.smarttrainner.core.database.CustomRoutineExerciseEntity
import com.smarttrainner.core.database.CustomRoutineWithDays
import com.smarttrainner.core.database.WorkoutLogEntity
import com.smarttrainner.core.database.WorkoutLogWithSets
import com.smarttrainner.core.database.WorkoutSetLogEntity
import com.smarttrainner.core.model.CustomRoutineDayInput
import com.smarttrainner.core.model.CustomRoutineExerciseInput
import com.smarttrainner.core.model.CustomRoutineInput
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlanLevel
import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.PlanTemplateDay
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.RoutineFocus
import com.smarttrainner.core.model.RoutineSource
import com.smarttrainner.core.model.RoutineStructure
import com.smarttrainner.core.model.TemplateExercise
import com.smarttrainner.core.model.TrainingExperience
import com.smarttrainner.core.model.UserSessionId
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.WorkoutLogId
import com.smarttrainner.core.model.WorkoutLogInput
import com.smarttrainner.core.model.WorkoutSetLog
import java.time.LocalDateTime

fun WorkoutLogInput.toEntity(sessionId: String): WorkoutLogEntity = WorkoutLogEntity(
    sessionId = sessionId,
    plannedExerciseId = plannedExerciseId.value,
    exerciseId = exerciseId.value,
    performedDate = performedAt.toLocalDate().toString(),
    performedAt = performedAt.toString(),
    sets = sets,
    reps = reps,
    weightKg = weightKg,
    durationMinutes = durationMinutes,
    memo = memo,
    completed = completed
)

fun List<WorkoutSetLog>.toEntities(workoutLogId: Long = 0): List<WorkoutSetLogEntity> = map {
    WorkoutSetLogEntity(
        workoutLogId = workoutLogId,
        setIndex = it.order,
        reps = it.reps,
        weightKg = it.weightKg,
        durationMinutes = it.durationMinutes,
        restSeconds = it.restSeconds
    )
}

fun WorkoutLogWithSets.toModel(): WorkoutLog {
    val legacySetEntries = List(log.sets.coerceAtLeast(0)) { index ->
        WorkoutSetLog(
            order = index + 1,
            reps = log.reps,
            weightKg = log.weightKg,
            durationMinutes = log.durationMinutes,
            restSeconds = null
        )
    }
    val setEntries = setLogs
        .sortedBy { it.setIndex }
        .map {
            WorkoutSetLog(
                order = it.setIndex,
                reps = it.reps,
                weightKg = it.weightKg,
                durationMinutes = it.durationMinutes,
                restSeconds = it.restSeconds
            )
        }
        .ifEmpty { legacySetEntries }

    return WorkoutLog(
        id = WorkoutLogId(log.id),
        sessionId = UserSessionId(log.sessionId),
        plannedExerciseId = PlannedExerciseId(log.plannedExerciseId),
        exerciseId = ExerciseId(log.exerciseId),
        performedAt = LocalDateTime.parse(log.performedAt),
        sets = log.sets,
        reps = log.reps,
        weightKg = log.weightKg,
        durationMinutes = log.durationMinutes,
        memo = log.memo,
        completed = log.completed,
        setEntries = setEntries
    )
}

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

fun CustomRoutineWithDays.toPlanTemplate(): PlanTemplate {
    val orderedDays = days.sortedBy { it.day.dayIndex }
    val focusSummary = orderedDays
        .flatMap { day -> listOfNotNull(day.day.primaryFocus.toCustomRoutineFocus()) + day.day.secondaryFocuses.toRoutineFocuses() }
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

private fun customRoutineDayId(routineId: String, dayIndex: Int): String =
    "$routineId-day-${dayIndex + 1}"

private fun customRoutineExerciseId(dayId: String, slotIndex: Int): String =
    "$dayId-slot-${slotIndex + 1}"
