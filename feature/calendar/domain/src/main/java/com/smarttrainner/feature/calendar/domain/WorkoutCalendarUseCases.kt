package com.smarttrainner.feature.calendar.domain

import com.smarttrainner.core.domain.ExerciseRepository
import com.smarttrainner.core.domain.SessionRepository
import com.smarttrainner.core.domain.WorkoutLogRepository
import com.smarttrainner.core.model.ExerciseLoadType
import com.smarttrainner.core.model.effectiveSetLoadsKg
import com.smarttrainner.core.model.effectiveVolumeKg
import com.smarttrainner.core.model.summaryVolumeKg
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.flow.combine

class ObserveWorkoutCalendarMonthUseCase @Inject constructor(
    private val workoutLogRepository: WorkoutLogRepository,
    private val exerciseRepository: ExerciseRepository,
    private val sessionRepository: SessionRepository
) {
    operator fun invoke(
        month: YearMonth,
        today: LocalDate
    ) = combine(
        workoutLogRepository.observeAllWorkoutLogs(),
        exerciseRepository.observeExercises(),
        sessionRepository.observeActiveSession()
    ) { logs, exercises, session ->
        val exercisesById = exercises.associateBy { it.id }
        val bodyWeightKg = session?.profile?.latestBodyMeasurement?.weightKg
        val monthLogs = logs
            .asSequence()
            .filter { it.performedAt.year == month.year && it.performedAt.monthValue == month.monthValue }
            .map { log ->
                val exercise = exercisesById[log.exerciseId]
                WorkoutCalendarLog(
                    id = log.id,
                    exerciseId = log.exerciseId,
                    plannedExerciseId = log.plannedExerciseId,
                    exerciseName = exercise?.name ?: log.exerciseId.value,
                    muscleGroup = exercise?.muscleGroup,
                    performedAt = log.performedAt,
                    sets = log.sets,
                    reps = log.reps,
                    weightKg = log.weightKg,
                    durationMinutes = log.durationMinutes,
                    memo = log.memo,
                    completed = log.completed,
                    volumeKg = log.summaryVolumeKg(exercise, bodyWeightKg),
                    loadType = exercise?.loadType ?: ExerciseLoadType.EXTERNAL_LOAD,
                    effectiveVolumeKg = exercise?.let { log.effectiveVolumeKg(it, bodyWeightKg) },
                    effectiveSetLoadsKg = exercise?.let { log.effectiveSetLoadsKg(it, bodyWeightKg) }.orEmpty(),
                    setEntries = log.setEntries,
                    routineDayInstanceId = log.routineDayInstanceId
                )
            }
            .toList()
        val logsByDate = monthLogs.groupBy { it.performedAt.toLocalDate() }
        val summariesByDate = logsByDate.mapValues { (date, dateLogs) ->
            WorkoutDateSummary(
                date = date,
                workoutCount = dateLogs.size,
                completedCount = dateLogs.count { it.completed },
                totalSetCount = dateLogs.sumOf { it.sets },
                totalVolumeKg = dateLogs.sumOf { it.volumeKg }
            )
        }
        WorkoutCalendarMonth(
            month = month,
            summariesByDate = summariesByDate,
            logsByDate = logsByDate.mapValues { (_, dateLogs) ->
                dateLogs.sortedByDescending { it.id.value }
            },
            todayWorkoutCount = summariesByDate[today]?.workoutCount ?: 0
        )
    }
}
