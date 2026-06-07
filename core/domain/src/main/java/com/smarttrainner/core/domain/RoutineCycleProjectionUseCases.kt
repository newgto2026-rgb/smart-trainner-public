package com.smarttrainner.core.domain

import com.smarttrainner.core.model.CurrentRoutineCycle
import com.smarttrainner.core.model.CurrentRoutineCycleCompletion
import com.smarttrainner.core.model.CyclePlan
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.RoutineProgress
import com.smarttrainner.core.model.WorkoutDayPlan
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.routineAdditionalExerciseCyclePrefix
import com.smarttrainner.core.model.routineDayInstanceId
import com.smarttrainner.core.model.routineDayInstancePrefix
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveCurrentRoutineCycleUseCase @Inject constructor(
    private val routineProgressRepository: RoutineProgressRepository,
    private val cyclePlanRepository: CyclePlanRepository,
    private val workoutLogRepository: WorkoutLogRepository,
    private val resolveCurrentRoutineCycle: ResolveCurrentRoutineCycleUseCase,
    private val clock: Clock
) {
    operator fun invoke(zone: ZoneId): Flow<CurrentRoutineCycle> =
        routineProgressRepository.observeRoutineProgress().flatMapLatest { progress ->
            val cycleStartDate = progress.cycleStartDate(
                zone = zone,
                fallback = LocalDate.now(clock.withZone(zone))
            )
            combine(
                cyclePlanRepository.observeCurrentCyclePlan(progress.templateId, cycleStartDate),
                workoutLogRepository.observeAllWorkoutLogs()
            ) { plan, logs ->
                resolveCurrentRoutineCycle(
                    progress = progress,
                    plan = plan,
                    logs = logs,
                    zone = zone
                )
            }
        }
}

class ResolveCurrentRoutineCycleUseCase @Inject constructor() {
    operator fun invoke(
        progress: RoutineProgress,
        plan: CyclePlan,
        logs: List<WorkoutLog>,
        zone: ZoneId
    ): CurrentRoutineCycle {
        val currentDayIndex = progress.dayIndex.coerceToPlanIndex(plan)
        val currentDay = plan.days.getOrNull(currentDayIndex)
        val currentRoutineDayInstanceId = currentDay?.let { day ->
            routineDayInstanceId(
                templateId = progress.templateId,
                cycleNumber = progress.cycleNumber,
                dayNumber = day.dayNumber
            )
        }
        val currentRoutineDayDate = currentRoutineDayInstanceId
            ?.let(progress.routineDayDates::get)
        val previousRoutineDayInstanceId = currentDay?.let { day ->
            previousRoutineDayInstanceId(
                progress = progress,
                plan = plan,
                currentDay = day
            )
        }
        val previousRoutineDayDate = previousRoutineDayInstanceId
            ?.let(progress.routineDayDates::get)
        val currentCyclePlannedExerciseIds = plan.days
            .flatMap { day -> day.exercises.map { it.id } }
            .toSet()
        val currentCycleLogs = logs.filterCurrentCycleLogs(
            progress = progress,
            zone = zone,
            currentCyclePlannedExerciseIds = currentCyclePlannedExerciseIds
        )
        val currentDayCompletedPlannedExerciseIds = currentCycleLogs
            .completedPlannedExerciseIdsForCurrentDay(
                routineDayInstanceId = currentRoutineDayInstanceId,
                currentDayPlannedExerciseIds = currentDay
                    ?.exercises
                    ?.map { it.id }
                    ?.toSet()
                    .orEmpty()
            )
        return CurrentRoutineCycle(
            progress = progress,
            plan = plan,
            currentDayIndex = currentDayIndex,
            currentDay = currentDay?.withRoutineDayContext(
                routineDayInstanceId = currentRoutineDayInstanceId,
                routineDayDate = currentRoutineDayDate
            ),
            currentRoutineDayInstanceId = currentRoutineDayInstanceId,
            currentRoutineDayDate = currentRoutineDayDate,
            previousRoutineDayInstanceId = previousRoutineDayInstanceId,
            previousRoutineDayDate = previousRoutineDayDate,
            currentCycleLogs = currentCycleLogs,
            allLogs = logs,
            currentCyclePlannedExerciseIds = currentCyclePlannedExerciseIds,
            currentDayCompletedPlannedExerciseIds = currentDayCompletedPlannedExerciseIds,
            latestCompletion = progress.latestCompletionFor(plan)
        )
    }
}

private fun RoutineProgress.cycleStartDate(
    zone: ZoneId,
    fallback: LocalDate
): LocalDate =
    (cycleStartedAt ?: startedAt)?.atZone(zone)?.toLocalDate() ?: fallback

private fun RoutineProgress.cycleStartedAtDateTime(zone: ZoneId): LocalDateTime? =
    (cycleStartedAt ?: startedAt)?.let { LocalDateTime.ofInstant(it, zone) }

private fun RoutineProgress.dayInstancePrefix(): String =
    routineDayInstancePrefix(
        templateId = templateId,
        cycleNumber = cycleNumber
    )

private fun RoutineProgress.additionalExerciseCyclePrefix(): String =
    routineAdditionalExerciseCyclePrefix(
        templateId = templateId,
        cycleNumber = cycleNumber
    )

private fun Int.coerceToPlanIndex(plan: CyclePlan): Int =
    if (plan.days.isEmpty()) {
        0
    } else {
        coerceIn(0, plan.days.lastIndex)
    }

private fun previousRoutineDayInstanceId(
    progress: RoutineProgress,
    plan: CyclePlan,
    currentDay: WorkoutDayPlan
): String? = when {
    currentDay.dayNumber > 1 -> routineDayInstanceId(
        templateId = progress.templateId,
        cycleNumber = progress.cycleNumber,
        dayNumber = currentDay.dayNumber - 1
    )
    progress.cycleNumber > 1 -> plan.days.maxOfOrNull { it.dayNumber }?.let { previousCycleLastDay ->
        routineDayInstanceId(
            templateId = progress.templateId,
            cycleNumber = progress.cycleNumber - 1,
            dayNumber = previousCycleLastDay
        )
    }
    else -> null
}

private fun List<WorkoutLog>.filterCurrentCycleLogs(
    progress: RoutineProgress,
    zone: ZoneId,
    currentCyclePlannedExerciseIds: Set<PlannedExerciseId>
): List<WorkoutLog> {
    val cycleStartedAt = progress.cycleStartedAtDateTime(zone)
    val routineDayInstancePrefix = progress.dayInstancePrefix()
    val additionalExerciseCyclePrefix = progress.additionalExerciseCyclePrefix()
    return filter { log ->
        val routineDayInstanceId = log.routineDayInstanceId
        when {
            routineDayInstanceId != null -> {
                routineDayInstanceId.startsWith(routineDayInstancePrefix)
            }
            log.plannedExerciseId in currentCyclePlannedExerciseIds ||
                log.plannedExerciseId.value.startsWith(additionalExerciseCyclePrefix) -> {
                cycleStartedAt == null || !log.performedAt.isBefore(cycleStartedAt)
            }
            else -> false
        }
    }
}

private fun List<WorkoutLog>.completedPlannedExerciseIdsForCurrentDay(
    routineDayInstanceId: String?,
    currentDayPlannedExerciseIds: Set<PlannedExerciseId>
): Set<PlannedExerciseId> {
    val completedLogs = filter { it.completed }
    val instanceLogs = routineDayInstanceId?.let { instanceId ->
        completedLogs.filter { it.routineDayInstanceId == instanceId }
    }.orEmpty()
    val currentDayLogs = when {
        routineDayInstanceId == null -> completedLogs
        instanceLogs.isNotEmpty() -> instanceLogs
        else -> completedLogs.filter { log ->
            log.routineDayInstanceId == null &&
                log.plannedExerciseId in currentDayPlannedExerciseIds
        }
    }
    return currentDayLogs.map { it.plannedExerciseId }.toSet()
}

private fun WorkoutDayPlan.withRoutineDayContext(
    routineDayInstanceId: String?,
    routineDayDate: LocalDate?
): WorkoutDayPlan = copy(
    exercises = exercises.map { exercise ->
        exercise.copy(
            routineDayInstanceId = routineDayInstanceId,
            routineDayDate = routineDayDate
        )
    }
)

private fun RoutineProgress.latestCompletionFor(plan: CyclePlan): CurrentRoutineCycleCompletion? {
    val completedDayIndex = lastCompletedDayIndex ?: return null
    val completedCycleNumber = lastCompletedCycleNumber ?: return null
    val completedDay = plan.days.getOrNull(completedDayIndex) ?: return null
    if (completedCycleNumber != cycleNumber) return null
    if (completedDayIndex + 1 >= plan.days.size) return null
    if (dayIndex != completedDayIndex + 1) return null
    return CurrentRoutineCycleCompletion(
        day = completedDay,
        cycleNumber = completedCycleNumber,
        dayNumber = completedDay.dayNumber,
        completedAt = lastCompletedAt
    )
}
