package com.smarttrainner.feature.analysis.domain

import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.RoutineProgress
import com.smarttrainner.core.model.CyclePlan
import com.smarttrainner.core.model.CycleSummary
import com.smarttrainner.core.model.WorkoutLog
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

class CycleSummaryCalculator @Inject constructor() {
    fun calculate(
        plan: CyclePlan,
        logs: List<WorkoutLog>,
        progress: RoutineProgress,
        zone: ZoneId
    ): CycleSummary {
        val cycleStartedAt = (progress.cycleStartedAt ?: progress.startedAt)
            ?.let { LocalDateTime.ofInstant(it, zone) }
        val currentRoutineDayInstancePrefix =
            "routine-day|${progress.templateId}|cycle${progress.cycleNumber}|"
        val plannedCount = plan.days.sumOf { it.exercises.size }
        val completedLogs = logs.filter { log ->
            val routineDayInstanceId = log.routineDayInstanceId
            log.completed &&
                if (routineDayInstanceId != null) {
                    routineDayInstanceId.startsWith(currentRoutineDayInstancePrefix)
                } else {
                    cycleStartedAt == null ||
                        !log.performedAt.isBefore(cycleStartedAt)
                }
        }
        val completedCount = completedLogs
            .distinctBy { it.plannedExerciseId }
            .size
            .coerceAtMost(plannedCount)
        val totalSets = completedLogs.sumOf { log ->
            log.setEntries.takeIf { it.isNotEmpty() }?.size ?: log.sets
        }
        val totalVolume = completedLogs.sumOf { it.volumeKg }
        val totalMinutes = completedLogs.sumOf { log ->
            log.setEntries.takeIf { it.isNotEmpty() }?.sumOf { it.durationMinutes ?: 0 }
                ?: (log.durationMinutes ?: 0)
        }
        val musclesByExerciseId = plan.days
            .flatMap { it.exercises }
            .associate { planned ->
                planned.exercise.id to planned.exercise.muscleGroups.ifEmpty {
                    listOf(planned.exercise.muscleGroup)
                }
            }
        val muscleBalance = completedLogs
            .flatMap { log -> musclesByExerciseId[log.exerciseId].orEmpty() }
            .groupingBy { it }
            .eachCount()
        val streakAnchor = completedLogs
            .map { it.performedAt.toLocalDate() }
            .maxOrNull()
            ?: cycleStartedAt?.toLocalDate()
            ?: plan.cycleStartDate
        val streakDays = completedLogs
            .map { it.performedAt.toLocalDate() }
            .distinct()
            .sortedDescending()
            .longestCurrentStreak(streakAnchor)
        val rate = if (plannedCount == 0) 0 else completedCount * 100 / plannedCount
        val weakestMuscle = MuscleGroup.entries
            .filterNot {
                it == MuscleGroup.CARDIO ||
                    it == MuscleGroup.ARMS ||
                    it == MuscleGroup.FULL_BODY
            }
            .minByOrNull { muscleBalance[it] ?: 0 }
        val insight = when {
            plannedCount == 0 -> "루틴 사이클을 시작하면 분석을 볼 수 있어요."
            completedCount == 0 -> "이번 사이클의 첫 기록을 남기면 완료율과 볼륨 변화를 바로 볼 수 있어요."
            rate >= 80 -> "${progress.cycleNumber}사이클 진행률이 좋아요. 다음 일차도 같은 자세 품질로 이어가세요."
            totalVolume > 0 && weakestMuscle != null -> "${weakestMuscle.displayName} 운동이 적어요. 이번 사이클 안에서 균형을 조금 맞춰보세요."
            else -> "이번 사이클을 꾸준히 채우고 있어요. 안정적인 반복을 우선해보세요."
        }

        return CycleSummary(
            cycleStartDate = plan.cycleStartDate,
            plannedExerciseCount = plannedCount,
            completedExerciseCount = completedCount,
            totalSets = totalSets,
            totalVolumeKg = totalVolume,
            totalMinutes = totalMinutes,
            streakDays = streakDays,
            muscleBalance = muscleBalance,
            insight = insight
        )
    }
}

private fun List<LocalDate>.longestCurrentStreak(anchor: LocalDate): Int {
    if (isEmpty()) return 0
    val dates = toSet()
    var cursor = anchor
    var streak = 0
    while (cursor in dates) {
        streak += 1
        cursor = cursor.minusDays(1)
    }
    if (streak > 0) return streak

    val latest = maxOrNull() ?: return 0
    cursor = latest
    while (cursor in dates) {
        streak += 1
        cursor = cursor.minusDays(1)
    }
    return streak
}
