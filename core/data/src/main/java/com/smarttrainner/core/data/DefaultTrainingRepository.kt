package com.smarttrainner.core.data

import com.smarttrainner.core.database.CustomRoutineDao
import com.smarttrainner.core.database.WorkoutLogDao
import com.smarttrainner.core.datastore.DEFAULT_USER_SESSION_ID
import com.smarttrainner.core.datastore.TrainingPreferencesDataSource
import com.smarttrainner.core.domain.TrainingRepository
import com.smarttrainner.core.domain.WeeklySummaryCalculator
import com.smarttrainner.core.model.CustomRoutineInput
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlanId
import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.RoutineProgress
import com.smarttrainner.core.model.RoutineSource
import com.smarttrainner.core.model.WeeklyPlan
import com.smarttrainner.core.model.WeeklySummary
import com.smarttrainner.core.model.WorkoutDayPlan
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.WorkoutLogInput
import com.smarttrainner.core.model.WorkoutSetLog
import java.time.Instant
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultTrainingRepository @Inject constructor(
    private val workoutLogDao: WorkoutLogDao,
    private val customRoutineDao: CustomRoutineDao,
    private val preferences: TrainingPreferencesDataSource,
    private val summaryCalculator: WeeklySummaryCalculator,
    private val clock: Clock
) : TrainingRepository {
    private val exerciseById = SeedTrainingContent.exercises.associateBy { it.id }
    private val templates = SeedTrainingContent.templates

    override fun observeExercises(): Flow<List<Exercise>> = flowOf(SeedTrainingContent.exercises)

    override fun observePlanTemplates(): Flow<List<PlanTemplate>> =
        observeCustomRoutines().map { customTemplates -> templates + customTemplates }

    override fun observeCustomRoutines(): Flow<List<PlanTemplate>> =
        preferences.activeSessionId.flatMapLatest { sessionId ->
            customRoutineDao.observeForSession(sessionId ?: DEFAULT_USER_SESSION_ID)
                .map { routines -> routines.map { it.toPlanTemplate() } }
        }

    override fun observeCurrentWeeklyPlan(weekStartDate: LocalDate): Flow<WeeklyPlan> =
        preferences.activeSessionId.flatMapLatest { sessionId ->
            val resolvedSessionId = sessionId ?: DEFAULT_USER_SESSION_ID
            combine(
                preferences.selectedTemplateId(resolvedSessionId),
                customRoutineDao.observeForSession(resolvedSessionId)
            ) { templateId, customRoutines ->
                buildWeeklyPlan(
                    template = templateById(templateId, customRoutines.map { it.toPlanTemplate() }),
                    weekStartDate = weekStartDate
                )
            }
        }

    override fun observeRoutineProgress(): Flow<RoutineProgress> =
        preferences.activeSessionId.flatMapLatest { sessionId ->
            val resolvedSessionId = sessionId ?: DEFAULT_USER_SESSION_ID
            combine(
                preferences.activeRoutineProgress(resolvedSessionId),
                customRoutineDao.observeForSession(resolvedSessionId)
            ) { preference, customRoutines ->
                val template = templateById(preference.templateId, customRoutines.map { it.toPlanTemplate() })
                val startedAt = preference.startedAt.toInstantOrNull()
                RoutineProgress(
                    templateId = template.id,
                    dayIndex = preference.dayIndex.coerceIn(0, (template.cycleLength - 1).coerceAtLeast(0)),
                    lastCompletedDayIndex = preference.lastCompletedDayIndex,
                    lastCompletedAt = preference.lastCompletedAt.toInstantOrNull(),
                    startedAt = startedAt,
                    cycleStartedAt = preference.cycleStartedAt.toInstantOrNull() ?: startedAt
                )
            }
        }

    override fun observeWorkoutLogs(weekStartDate: LocalDate): Flow<List<WorkoutLog>> =
        preferences.activeSessionId.flatMapLatest { sessionId ->
            workoutLogDao
                .observeBetween(
                    sessionId = sessionId ?: DEFAULT_USER_SESSION_ID,
                    startDate = weekStartDate.toString(),
                    endDate = weekStartDate.plusDays(6).toString()
                )
                .map { entities -> entities.map { it.toModel() } }
        }

    override fun observeLatestWorkoutLogs(): Flow<List<WorkoutLog>> =
        preferences.activeSessionId.flatMapLatest { sessionId ->
            workoutLogDao
                .observeAll(sessionId = sessionId ?: DEFAULT_USER_SESSION_ID)
                .map { entities ->
                    entities
                        .map { it.toModel() }
                        .distinctBy { it.exerciseId }
                }
        }

    override fun observeWeeklySummary(weekStartDate: LocalDate): Flow<WeeklySummary> =
        combine(
            observeCurrentWeeklyPlan(weekStartDate),
            observeWorkoutLogs(weekStartDate)
        ) { plan, logs ->
            summaryCalculator.calculate(weekStartDate, plan, logs)
        }

    override suspend fun getExercise(id: ExerciseId): Exercise? = exerciseById[id]

    override suspend fun getLatestWorkoutLog(exerciseId: ExerciseId): WorkoutLog? =
        workoutLogDao.latestByExercise(
            sessionId = activeSessionId(),
            exerciseId = exerciseId.value
        )?.toModel()

    override suspend fun selectPlanTemplate(templateId: String): Result<Unit> = runCatching {
        val sessionId = activeSessionId()
        require(templateExists(sessionId, templateId)) { "Unknown plan template: $templateId" }
        preferences.setSelectedTemplateId(sessionId, templateId)
    }

    override suspend fun startRoutine(templateId: String): Result<Unit> = runCatching {
        val sessionId = activeSessionId()
        require(templateExists(sessionId, templateId)) { "Unknown plan template: $templateId" }
        preferences.setSelectedTemplateId(sessionId, templateId)
        preferences.setActiveRoutineTemplate(sessionId, templateId)
    }

    override suspend fun saveCustomRoutine(input: CustomRoutineInput): Result<PlanTemplate> = runCatching {
        val sessionId = activeSessionId()
        val existing = input.id?.let { customRoutineDao.getById(sessionId, it) }
        val routineId = input.id?.takeIf { it.isNotBlank() } ?: "custom-${UUID.randomUUID()}"
        val now = clock.instant().toString()
        customRoutineDao.upsertFull(
            routine = input.toEntity(
                routineId = routineId,
                sessionId = sessionId,
                createdAt = existing?.routine?.createdAt ?: now,
                updatedAt = now
            ),
            days = input.toDayWrites(routineId)
        )
        requireNotNull(customRoutineDao.getById(sessionId, routineId)).toPlanTemplate()
    }

    override suspend fun deleteCustomRoutine(templateId: String): Result<Unit> = runCatching {
        val sessionId = activeSessionId()
        val deleted = customRoutineDao.deleteRoutine(sessionId, templateId)
        require(deleted > 0) { "Unknown custom routine: $templateId" }
        val selectedTemplateId = preferences.selectedTemplateId(sessionId).first()
        val activeTemplateId = preferences.activeRoutineProgress(sessionId).first().templateId
        if (selectedTemplateId == templateId || activeTemplateId == templateId) {
            val fallbackTemplateId = templates.first().id
            preferences.setSelectedTemplateId(sessionId, fallbackTemplateId)
            preferences.setActiveRoutineTemplate(sessionId, fallbackTemplateId)
        }
    }

    override suspend fun markRoutineDayCompleted(
        completedDayIndex: Int,
        nextDayIndex: Int,
        completedAt: Instant,
        newCycleStartedAt: Instant?
    ): Result<Unit> = runCatching {
        preferences.markRoutineDayCompleted(
            sessionId = activeSessionId(),
            completedDayIndex = completedDayIndex,
            nextDayIndex = nextDayIndex,
            completedAt = completedAt.toString(),
            newCycleStartedAt = newCycleStartedAt?.toString()
        )
    }

    override suspend fun saveWorkoutLog(input: WorkoutLogInput): Result<Unit> = runCatching {
        val setEntries = input.setEntries.ifEmpty {
            List(input.sets) { index ->
                WorkoutSetLog(
                    order = index + 1,
                    reps = input.reps,
                    weightKg = input.weightKg,
                    durationMinutes = input.durationMinutes
                )
            }
        }
        require(setEntries.size in 1..12) { "Sets must be between 1 and 12." }
        require(setEntries.map { it.order }.distinct().size == setEntries.size) {
            "Set order values must be unique."
        }
        setEntries.forEach { entry ->
            require(entry.order in 1..12) { "Set order must be between 1 and 12." }
            require(entry.reps != null || entry.durationMinutes != null) {
                "Each set needs reps or duration."
            }
            require(entry.reps?.let { it in 1..50 } ?: true) { "Reps must be between 1 and 50." }
            require(entry.weightKg?.let { it >= 0.0 } ?: true) { "Weight cannot be negative." }
            require(entry.durationMinutes?.let { it in 1..240 } ?: true) {
                "Duration must be between 1 and 240 minutes."
            }
            require(entry.restSeconds?.let { it in 0..600 } ?: true) {
                "Rest must be between 0 and 600 seconds."
            }
        }
        workoutLogDao.upsertWithSets(
            input.copy(sets = setEntries.size, setEntries = setEntries).toEntity(activeSessionId()),
            setEntries.toEntities()
        )
    }

    private suspend fun activeSessionId(): String =
        preferences.activeSessionId.first() ?: DEFAULT_USER_SESSION_ID

    private suspend fun templateExists(sessionId: String, templateId: String): Boolean =
        templates.any { it.id == templateId } || customRoutineDao.getById(sessionId, templateId) != null

    private fun templateById(templateId: String, customTemplates: List<PlanTemplate>): PlanTemplate =
        (templates + customTemplates).firstOrNull { it.id == templateId } ?: templates.first()

    private fun String?.toInstantOrNull(): Instant? =
        this?.let { raw -> runCatching { Instant.parse(raw) }.getOrNull() }

    private fun buildWeeklyPlan(
        template: PlanTemplate,
        weekStartDate: LocalDate
    ): WeeklyPlan = WeeklyPlan(
        id = PlanId("${template.id}_${weekStartDate}"),
        templateId = template.id,
        name = template.name,
        weekStartDate = weekStartDate,
        days = template.days.map { day ->
            val date = weekStartDate.plusDays(day.dayOffset.toLong())
            WorkoutDayPlan(
                date = date,
                title = day.title,
                focus = day.focus,
                exercises = day.exercises.mapIndexed { slotIndex, item ->
                    val exercise = exerciseById.getValue(item.exerciseId)
                    PlannedExercise(
                        id = PlannedExerciseId(template.plannedExerciseId(date, day.dayNumber, slotIndex, item.exerciseId)),
                        exercise = exercise,
                        sets = item.sets,
                        repRange = item.repRange,
                        durationMinutes = item.durationMinutes,
                        restSeconds = item.restSeconds,
                        note = item.note
                    )
                },
                dayNumber = day.dayNumber,
                primaryFocus = day.primaryFocus,
                secondaryFocuses = day.secondaryFocuses,
                minRecoveryHours = day.minRecoveryHours
            )
        }
    )

    private fun PlanTemplate.plannedExerciseId(
        date: LocalDate,
        dayNumber: Int,
        slotIndex: Int,
        exerciseId: ExerciseId
    ): String = if (source == RoutineSource.CUSTOM) {
        "${date}_${id}_day${dayNumber}_slot${slotIndex + 1}_${exerciseId.value}"
    } else {
        "${date}_${exerciseId.value}"
    }
}
