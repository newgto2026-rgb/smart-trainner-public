package com.smarttrainner

import com.smarttrainner.core.domain.ExerciseRepository
import com.smarttrainner.core.domain.SeedTrainingContent
import com.smarttrainner.core.domain.SessionRepository
import com.smarttrainner.core.domain.SocialSignInCredential
import com.smarttrainner.core.domain.WeeklyPlanRepository
import com.smarttrainner.core.domain.WorkoutLogRepository
import com.smarttrainner.core.model.AuthProvider
import com.smarttrainner.core.model.CustomRoutineInput
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlanId
import com.smarttrainner.core.model.PlanLevel
import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.PlanTemplateDay
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.RoutineProgress
import com.smarttrainner.core.model.RoutineSource
import com.smarttrainner.core.model.TemplateExercise
import com.smarttrainner.core.model.UserSession
import com.smarttrainner.core.model.UserSessionId
import com.smarttrainner.core.model.WeeklyPlan
import com.smarttrainner.core.model.WeeklySummary
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.WorkoutLogId
import com.smarttrainner.core.model.WorkoutLogInput
import com.smarttrainner.feature.analysis.domain.WeeklySummaryCalculator
import com.smarttrainner.feature.analysis.domain.WeeklySummaryRepository
import com.smarttrainner.feature.routine.domain.RoutinePlanCatalogRepository
import com.smarttrainner.feature.routine.domain.RoutinePlanCommandRepository
import com.smarttrainner.feature.routine.domain.RoutineProgressCommandRepository
import com.smarttrainner.feature.routine.domain.RoutineProgressRepository
import com.smarttrainner.feature.workout.domain.WorkoutRecordingRepository
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

internal class InMemorySessionRepository : SessionRepository {
    private val activeSession = MutableStateFlow<UserSession?>(null)

    fun reset() {
        activeSession.value = null
    }

    override fun observeActiveSession(): Flow<UserSession?> = activeSession

    override suspend fun startDefaultSession(): Result<UserSession> {
        val session = UserSession(
            id = UserSessionId(TEST_SESSION_ID),
            displayName = "Test Athlete",
            nickname = "test-athlete",
            email = null,
            provider = AuthProvider.LOCAL,
            providerAccountId = null,
            avatarUrl = null,
            linkedAt = null
        )
        activeSession.value = session
        return Result.success(session)
    }

    override suspend fun checkNicknameAvailability(nickname: String): Result<Boolean> =
        Result.success(nickname.trim().equals("taken", ignoreCase = true).not())

    override suspend fun startSocialSession(
        credential: SocialSignInCredential,
        nickname: String
    ): Result<UserSession> {
        val session = UserSession(
            id = UserSessionId(TEST_SESSION_ID),
            displayName = credential.displayName ?: nickname,
            nickname = nickname,
            email = credential.email,
            provider = credential.provider,
            providerAccountId = "test-provider-account",
            avatarUrl = credential.avatarUrl,
            linkedAt = "2026-05-31T00:00:00Z"
        )
        activeSession.value = session
        return Result.success(session)
    }

}

internal class InMemoryTrainingRepository :
    ExerciseRepository,
    WeeklyPlanRepository,
    RoutinePlanCatalogRepository,
    RoutineProgressRepository,
    RoutinePlanCommandRepository,
    RoutineProgressCommandRepository,
    WorkoutRecordingRepository,
    WorkoutLogRepository,
    WeeklySummaryRepository {
    private val exercises = MutableStateFlow(SeedTrainingContent.exercises)
    private val exerciseById = SeedTrainingContent.exercises.associateBy { it.id }
    private val systemTemplates = SeedTrainingContent.templates
    private val selectedTemplateId = MutableStateFlow(DEFAULT_TEMPLATE_ID)
    private val customTemplates = MutableStateFlow<List<PlanTemplate>>(emptyList())
    private val progress = MutableStateFlow(defaultProgress())
    private val logs = MutableStateFlow<List<WorkoutLog>>(emptyList())
    private val summaryCalculator = WeeklySummaryCalculator()

    fun reset() {
        selectedTemplateId.value = DEFAULT_TEMPLATE_ID
        customTemplates.value = emptyList()
        progress.value = defaultProgress()
        logs.value = emptyList()
    }

    override fun observeExercises(): Flow<List<Exercise>> = exercises

    override fun observePlanTemplates(): Flow<List<PlanTemplate>> =
        customTemplates.map { custom -> systemTemplates + custom }

    override fun observeCurrentWeeklyPlan(weekStartDate: LocalDate): Flow<WeeklyPlan> =
        combine(selectedTemplateId, customTemplates) { templateId, custom ->
            buildWeeklyPlan(templateById(templateId, custom), weekStartDate)
        }

    override fun observeRoutineProgress(): Flow<RoutineProgress> = progress

    override fun observeWorkoutLogs(weekStartDate: LocalDate): Flow<List<WorkoutLog>> = logs

    override fun observeLatestWorkoutLogs(): Flow<List<WorkoutLog>> = logs.map { currentLogs ->
        currentLogs
            .sortedByDescending { it.performedAt }
            .distinctBy { it.exerciseId }
    }

    override fun observeWeeklySummary(weekStartDate: LocalDate): Flow<WeeklySummary> =
        combine(observeCurrentWeeklyPlan(weekStartDate), logs) { plan, logs ->
            summaryCalculator.calculate(weekStartDate, plan, logs)
        }

    override suspend fun getExercise(id: ExerciseId): Exercise? = exerciseById[id]

    override suspend fun getLatestWorkoutLog(exerciseId: ExerciseId): WorkoutLog? =
        logs.value
            .filter { it.exerciseId == exerciseId }
            .maxByOrNull { it.performedAt }

    override suspend fun selectPlanTemplate(templateId: String): Result<Unit> = runCatching {
        require(templateExists(templateId)) { "Unknown plan template: $templateId" }
        selectedTemplateId.value = templateId
    }

    override suspend fun startRoutine(templateId: String): Result<Unit> = runCatching {
        require(templateExists(templateId)) { "Unknown plan template: $templateId" }
        selectedTemplateId.value = templateId
        progress.value = RoutineProgress(
            templateId = templateId,
            dayIndex = 0,
            lastCompletedDayIndex = null,
            lastCompletedAt = null,
            startedAt = Instant.EPOCH,
            cycleStartedAt = Instant.EPOCH
        )
    }

    override suspend fun saveCustomRoutine(input: CustomRoutineInput): Result<PlanTemplate> = runCatching {
        val customTemplate = input.toPlanTemplate()
        customTemplates.value = customTemplates.value.filterNot { it.id == customTemplate.id } + customTemplate
        customTemplate
    }

    override suspend fun deleteCustomRoutine(templateId: String): Result<Unit> = runCatching {
        val beforeSize = customTemplates.value.size
        customTemplates.value = customTemplates.value.filterNot { it.id == templateId }
        require(customTemplates.value.size < beforeSize) { "Unknown custom routine: $templateId" }
        if (selectedTemplateId.value == templateId || progress.value.templateId == templateId) {
            selectedTemplateId.value = DEFAULT_TEMPLATE_ID
            progress.value = defaultProgress()
        }
    }

    override suspend fun markRoutineDayCompleted(
        completedDayIndex: Int,
        nextDayIndex: Int,
        completedAt: Instant,
        newCycleStartedAt: Instant?
    ): Result<Unit> = runCatching {
        val current = progress.value
        progress.value = current.copy(
            dayIndex = nextDayIndex,
            lastCompletedDayIndex = completedDayIndex,
            lastCompletedAt = completedAt,
            cycleStartedAt = newCycleStartedAt ?: current.cycleStartedAt
        )
    }

    override suspend fun saveWorkoutLog(input: WorkoutLogInput): Result<Unit> = runCatching {
        logs.value = logs.value + WorkoutLog(
            id = WorkoutLogId(logs.value.size.toLong() + 1),
            sessionId = UserSessionId(TEST_SESSION_ID),
            plannedExerciseId = input.plannedExerciseId,
            exerciseId = input.exerciseId,
            performedAt = input.performedAt,
            sets = input.sets,
            reps = input.reps,
            weightKg = input.weightKg,
            durationMinutes = input.durationMinutes,
            memo = input.memo,
            completed = input.completed,
            setEntries = input.setEntries
        )
    }

    private fun CustomRoutineInput.toPlanTemplate(): PlanTemplate {
        val routineId = id?.takeIf { it.isNotBlank() } ?: nextCustomRoutineId()
        return PlanTemplate(
            id = routineId,
            name = name,
            level = PlanLevel.INTERMEDIATE,
            daysPerWeek = days.size,
            description = description,
            days = days.mapIndexed { dayIndex, day ->
                PlanTemplateDay(
                    dayOffset = dayIndex,
                    title = day.title,
                    focus = day.focus,
                    exercises = day.exercises.map { exercise ->
                        val repRangeStart = exercise.repRangeStart
                        val repRangeEnd = exercise.repRangeEnd
                        TemplateExercise(
                            exerciseId = exercise.exerciseId,
                            sets = exercise.sets,
                            repRange = if (repRangeStart != null && repRangeEnd != null) {
                                repRangeStart..repRangeEnd
                            } else {
                                null
                            },
                            durationMinutes = exercise.durationMinutes,
                            restSeconds = exercise.restSeconds,
                            note = exercise.note
                        )
                    },
                    dayNumber = dayIndex + 1,
                    primaryFocus = day.primaryFocus,
                    secondaryFocuses = day.secondaryFocuses,
                    minRecoveryHours = day.minRecoveryHours
                )
            },
            cycleLength = days.size,
            focusSummary = days.mapNotNull { it.primaryFocus }.distinct(),
            source = RoutineSource.CUSTOM
        )
    }

    private fun buildWeeklyPlan(template: PlanTemplate, weekStartDate: LocalDate): WeeklyPlan =
        WeeklyPlan(
            id = PlanId("${template.id}_$weekStartDate"),
            templateId = template.id,
            name = template.name,
            weekStartDate = weekStartDate,
            days = template.days.map { day ->
                val date = weekStartDate.plusDays(day.dayOffset.toLong())
                com.smarttrainner.core.model.WorkoutDayPlan(
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

    private fun templateById(templateId: String, custom: List<PlanTemplate>): PlanTemplate =
        (systemTemplates + custom).firstOrNull { it.id == templateId } ?: systemTemplates.first()

    private fun templateExists(templateId: String): Boolean =
        (systemTemplates + customTemplates.value).any { it.id == templateId }

    private fun nextCustomRoutineId(): String =
        if (customTemplates.value.none { it.id == "custom-test" }) {
            "custom-test"
        } else {
            "custom-${UUID.randomUUID()}"
        }

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

    private fun defaultProgress(): RoutineProgress =
        RoutineProgress(
            templateId = DEFAULT_TEMPLATE_ID,
            dayIndex = 0,
            lastCompletedDayIndex = null,
            lastCompletedAt = null,
            startedAt = Instant.EPOCH,
            cycleStartedAt = Instant.EPOCH
        )
}

private const val TEST_SESSION_ID = "android-ui-test"
private const val DEFAULT_TEMPLATE_ID = "beginner-full-body-3day"
