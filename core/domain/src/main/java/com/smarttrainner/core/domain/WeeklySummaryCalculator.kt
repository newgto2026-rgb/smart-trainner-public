package com.smarttrainner.core.domain

import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.WeeklyPlan
import com.smarttrainner.core.model.WeeklySummary
import com.smarttrainner.core.model.WorkoutLog
import java.time.LocalDate

class WeeklySummaryCalculator {
    fun calculate(
        weekStartDate: LocalDate,
        plan: WeeklyPlan,
        logs: List<WorkoutLog>
    ): WeeklySummary {
        val plannedCount = plan.days.sumOf { it.exercises.size }
        val completedLogs = logs.filter { it.completed }
        val completedIds = completedLogs.map { it.plannedExerciseId }.toSet()
        val completedCount = plan.days.sumOf { day ->
            day.exercises.count { it.id in completedIds }
        }
        val totalSets = completedLogs.sumOf { log ->
            log.setEntries.takeIf { it.isNotEmpty() }?.size ?: log.sets
        }
        val totalVolume = completedLogs.sumOf { it.volumeKg }
        val totalMinutes = completedLogs.sumOf { log ->
            log.setEntries.takeIf { it.isNotEmpty() }?.sumOf { it.durationMinutes ?: 0 }
                ?: (log.durationMinutes ?: 0)
        }
        val exerciseByPlanId = plan.days
            .flatMap { it.exercises }
            .associateBy { it.id }
        val muscleBalance = completedLogs
            .mapNotNull { log -> exerciseByPlanId[log.plannedExerciseId]?.exercise?.muscleGroup }
            .groupingBy { it }
            .eachCount()
        val streakDays = completedLogs
            .map { it.performedAt.toLocalDate() }
            .distinct()
            .sortedDescending()
            .longestCurrentStreak(weekStartDate.plusDays(6))
        val rate = if (plannedCount == 0) 0 else completedCount * 100 / plannedCount
        val weakestMuscle = MuscleGroup.entries
            .filterNot {
                it == MuscleGroup.CARDIO ||
                    it == MuscleGroup.ARMS ||
                    it == MuscleGroup.FULL_BODY
            }
            .minByOrNull { muscleBalance[it] ?: 0 }
        val insight = when {
            plannedCount == 0 -> "이번 주 플랜을 만들면 분석을 시작할 수 있어요."
            completedCount == 0 -> "첫 기록을 남기면 완료율과 볼륨 변화를 바로 볼 수 있어요."
            rate >= 80 -> "이번 주 플랜 달성률이 좋아요. 다음 주에는 같은 자세 품질로 소폭만 올려보세요."
            totalVolume > 0 && weakestMuscle != null -> "${weakestMuscle.displayName} 운동이 적어요. 다음 세션에 균형을 조금 맞춰보세요."
            else -> "꾸준히 기록 중이에요. 실패 지점보다 안정적인 반복을 우선해보세요."
        }

        return WeeklySummary(
            weekStartDate = weekStartDate,
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
