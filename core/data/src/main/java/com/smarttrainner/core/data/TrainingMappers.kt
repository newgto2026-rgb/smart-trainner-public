package com.smarttrainner.core.data

import com.smarttrainner.core.database.WorkoutLogWithSets
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.UserSessionId
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.WorkoutLogId
import com.smarttrainner.core.model.WorkoutSetLog
import java.time.LocalDateTime

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
        setEntries = setEntries,
        routineDayInstanceId = log.routineDayInstanceId
    )
}
