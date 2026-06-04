package com.smarttrainner.feature.workout.impl

import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.WorkoutSetLog

internal const val MAX_RECORD_SETS = 12

internal fun PlannedExercise.defaultSetForms(previousLog: WorkoutLog? = null): List<RecordSetFormState> {
    val previousSets = previousLog?.reusableSetEntries().orEmpty()
    val setCount = previousSets
        .takeIf { it.isNotEmpty() }
        ?.size
        ?: sets

    return List(setCount.coerceIn(1, MAX_RECORD_SETS)) { index ->
        defaultSetForm(previousSets.getOrNull(index))
    }
}

internal fun PlannedExercise.defaultSetForm(previousSet: WorkoutSetLog? = null): RecordSetFormState {
    val plannedRepRange = repRange
    val plannedDurationMinutes = durationMinutes
    return RecordSetFormState(
        reps = if (plannedRepRange != null) {
            (previousSet?.reps ?: plannedRepRange.first).toString()
        } else {
            ""
        },
        weightKg = if (plannedRepRange != null) {
            previousSet?.weightKg?.toSetRecordInput().orEmpty()
        } else {
            ""
        },
        durationMinutes = if (plannedDurationMinutes != null || plannedRepRange == null) {
            (previousSet?.durationMinutes ?: plannedDurationMinutes)?.toString().orEmpty()
        } else {
            ""
        },
        restSeconds = (previousSet?.restSeconds ?: restSeconds).toString()
    )
}

internal fun List<WorkoutLog>.latestRecordForPlannedExercise(plannedExerciseId: PlannedExerciseId): WorkoutLog? =
    filter { it.plannedExerciseId == plannedExerciseId }
        .maxByOrNull { it.performedAt }

private fun WorkoutLog.reusableSetEntries(): List<WorkoutSetLog> =
    setEntries.takeIf { it.isNotEmpty() }
        ?: List(sets.coerceIn(1, MAX_RECORD_SETS)) { index ->
            WorkoutSetLog(
                order = index + 1,
                reps = reps,
                weightKg = weightKg,
                durationMinutes = durationMinutes,
                restSeconds = null
            )
        }

private fun Double.toSetRecordInput(): String =
    if (rem(1.0) == 0.0) toLong().toString() else toString()

internal fun validateSetEntries(
    planned: PlannedExercise,
    entries: List<RecordSetFormState>
): RecordFormError? {
    if (entries.isEmpty() || entries.size > MAX_RECORD_SETS) return RecordFormError.SETS
    entries.forEach { entry ->
        val reps = entry.reps.toIntOrNull()
        val weight = entry.weightKg.toDoubleOrNull()
        val duration = entry.durationMinutes.toIntOrNull()
        val rest = entry.restSeconds.toIntOrNull()
        when {
            entry.reps.isNotBlank() && (reps == null || reps !in 1..50) -> return RecordFormError.REPS
            entry.weightKg.isNotBlank() && (weight == null || weight < 0.0) -> return RecordFormError.WEIGHT
            entry.durationMinutes.isNotBlank() && (duration == null || duration !in 1..240) -> {
                return RecordFormError.DURATION
            }
            entry.restSeconds.isNotBlank() && (rest == null || rest !in 0..600) -> return RecordFormError.REST
            planned.repRange != null && reps == null -> return RecordFormError.REPS
            planned.repRange != null && weight == null -> return RecordFormError.WEIGHT
            planned.repRange == null && duration == null -> return RecordFormError.DURATION
        }
    }
    return null
}

internal fun List<RecordSetFormState>.toWorkoutSetLogs(planned: PlannedExercise): List<WorkoutSetLog> =
    mapIndexed { index, entry ->
        WorkoutSetLog(
            order = index + 1,
            reps = if (planned.repRange != null) entry.reps.toIntOrNull() else null,
            weightKg = if (planned.repRange != null) entry.weightKg.toDoubleOrNull() else null,
            durationMinutes = if (planned.durationMinutes != null || planned.repRange == null) {
                entry.durationMinutes.toIntOrNull()
            } else {
                null
            },
            restSeconds = entry.restSeconds.toIntOrNull()
        )
    }

internal fun String.onlyNumber(): String = filter { it.isDigit() }.take(3)

internal fun String.onlyDecimal(): String {
    var dotSeen = false
    return filter { char ->
        when {
            char.isDigit() -> true
            char == '.' && !dotSeen -> {
                dotSeen = true
                true
            }
            else -> false
        }
    }.take(6)
}
