package com.smarttrainner

import com.smarttrainner.core.domain.ExerciseRepository
import com.smarttrainner.core.domain.RoutineProgressRepository
import com.smarttrainner.core.domain.SeedTrainingContent
import com.smarttrainner.core.domain.SessionRepository
import com.smarttrainner.core.domain.CyclePlanRepository
import com.smarttrainner.core.domain.WorkoutLogRepository
import com.smarttrainner.core.model.AuthProvider
import com.smarttrainner.core.model.CustomRoutineInput
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.NicknameAvailability
import com.smarttrainner.core.model.PlanId
import com.smarttrainner.core.model.PlanLevel
import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.PlanTemplateDay
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.BodyMeasurement
import com.smarttrainner.core.model.ProfileGender
import com.smarttrainner.core.model.ProfileSetup
import com.smarttrainner.core.model.RoutineProgress
import com.smarttrainner.core.model.RoutineSource
import com.smarttrainner.core.model.TemplateExercise
import com.smarttrainner.core.model.TrainingExperience
import com.smarttrainner.core.model.UserProfile
import com.smarttrainner.core.model.UserSession
import com.smarttrainner.core.model.UserSessionId
import com.smarttrainner.core.model.CyclePlan
import com.smarttrainner.core.model.CycleSummary
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.WorkoutLogId
import com.smarttrainner.core.model.WorkoutLogInput
import com.smarttrainner.feature.analysis.domain.CycleSummaryCalculator
import com.smarttrainner.feature.analysis.domain.CycleSummaryRepository
import com.smarttrainner.feature.routine.domain.RoutineCompletionSnapshot
import com.smarttrainner.feature.routine.domain.RoutinePlanCatalogRepository
import com.smarttrainner.feature.routine.domain.RoutinePlanCommandRepository
import com.smarttrainner.feature.routine.domain.RoutineProgressCommandRepository
import com.smarttrainner.feature.workout.domain.WorkoutRecordingRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

internal class InMemorySessionRepository : SessionRepository {
    private val activeSession = MutableStateFlow<UserSession?>(null)
    private val trainingExperience = MutableStateFlow(TrainingExperience.BEGINNER)

    fun reset() {
        activeSession.value = null
        trainingExperience.value = TrainingExperience.BEGINNER
    }

    fun setTrainingExperienceForTest(experience: TrainingExperience) {
        trainingExperience.value = experience
    }

    override fun observeActiveSession(): Flow<UserSession?> = activeSession

    override fun observeTrainingExperience(): Flow<TrainingExperience> = trainingExperience

    override suspend fun startDefaultSession(nickname: String, profileSetup: ProfileSetup): Result<UserSession> {
        val session = UserSession(
            id = UserSessionId(TEST_SESSION_ID),
            displayName = "Test Athlete",
            nickname = nickname,
            email = null,
            provider = AuthProvider.LOCAL,
            linkedAt = null,
            profile = profileSetup.toUserProfile()
        )
        activeSession.value = session
        return Result.success(session)
    }

    override suspend fun checkNicknameAvailability(nickname: String): Result<NicknameAvailability> =
        Result.success(NicknameAvailability(nickname = nickname.trim(), available = true))

    override suspend fun signInWithGoogle(
        idToken: String,
        nickname: String?,
        profileSetup: ProfileSetup?,
        forceDeviceLogin: Boolean
    ): Result<UserSession> {
        val resolvedNickname = nickname?.trim()?.takeIf { it.isNotEmpty() } ?: "Google Test Athlete"
        val session = UserSession(
            id = UserSessionId("google-test-athlete"),
            displayName = "Google Test Athlete",
            nickname = resolvedNickname,
            email = "test@example.com",
            provider = AuthProvider.GOOGLE,
            linkedAt = "2026-06-02T00:00:00Z",
            profile = profileSetup?.toUserProfile() ?: activeSession.value?.profile ?: UserProfile()
        )
        activeSession.value = session
        return Result.success(session)
    }

    override suspend fun validateActiveSessionDevice(): Result<Unit> =
        Result.success(Unit)

    override suspend fun setTrainingExperience(experience: TrainingExperience): Result<Unit> {
        trainingExperience.value = experience
        return Result.success(Unit)
    }

    override suspend fun updateBodyProfile(
        gender: ProfileGender?,
        heightCm: Int,
        weightKg: Double,
        nickname: String?
    ): Result<Unit> {
        val current = activeSession.value ?: return Result.success(Unit)
        val profile = current.profile
        activeSession.value = current.copy(
            nickname = nickname?.trim()?.takeIf { it.isNotEmpty() } ?: current.nickname,
            profile = profile.copy(
                gender = profile.gender ?: gender,
                bodyMeasurements = profile.bodyMeasurements
                    .filterNot { it.recordedDate == LocalDate.of(2026, 6, 3) } +
                    BodyMeasurement(LocalDate.of(2026, 6, 3), heightCm, weightKg)
            )
        )
        return Result.success(Unit)
    }

    override suspend fun logout(): Result<Unit> {
        activeSession.value = null
        return Result.success(Unit)
    }

}

private fun ProfileSetup.toUserProfile(): UserProfile =
    UserProfile(
        gender = gender,
        bodyMeasurements = listOf(
            BodyMeasurement(
                recordedDate = LocalDate.of(2026, 6, 3),
                heightCm = heightCm,
                weightKg = weightKg
            )
        )
    )

internal class InMemoryTrainingRepository :
    ExerciseRepository,
    CyclePlanRepository,
    RoutinePlanCatalogRepository,
    RoutineProgressRepository,
    RoutinePlanCommandRepository,
    RoutineProgressCommandRepository,
    WorkoutRecordingRepository,
    WorkoutLogRepository,
    CycleSummaryRepository {
    private val exercises = MutableStateFlow(SeedTrainingContent.exercises)
    private val exerciseById = SeedTrainingContent.exercises.associateBy { it.id }
    private val systemTemplates = SeedTrainingContent.templates
    private val selectedTemplateId = MutableStateFlow(DEFAULT_TEMPLATE_ID)
    private val customTemplates = MutableStateFlow<List<PlanTemplate>>(emptyList())
    private val progress = MutableStateFlow(defaultProgress())
    private val logs = MutableStateFlow<List<WorkoutLog>>(emptyList())
    private val summaryCalculator = CycleSummaryCalculator()

    fun reset() {
        selectedTemplateId.value = DEFAULT_TEMPLATE_ID
        customTemplates.value = emptyList()
        progress.value = defaultProgress()
        logs.value = emptyList()
    }

    fun routineDayDatesForTest(): Map<String, LocalDate> = progress.value.routineDayDates

    fun workoutLogsForTest(): List<WorkoutLog> = logs.value

    fun progressForTest(): RoutineProgress = progress.value

    override fun observeExercises(): Flow<List<Exercise>> = exercises

    override fun observePlanTemplates(): Flow<List<PlanTemplate>> =
        customTemplates.map { custom -> systemTemplates + custom }

    override fun observeCurrentCyclePlan(cycleStartDate: LocalDate): Flow<CyclePlan> =
        combine(selectedTemplateId, customTemplates) { templateId, custom ->
            buildCyclePlan(templateById(templateId, custom), cycleStartDate)
        }

    override fun observeRoutineProgress(): Flow<RoutineProgress> = progress

    override fun observeLatestWorkoutLogs(): Flow<List<WorkoutLog>> = logs.map { currentLogs ->
        currentLogs
            .sortedByDescending { it.performedAt }
            .distinctBy { it.exerciseId }
    }

    override fun observeAllWorkoutLogs(): Flow<List<WorkoutLog>> = logs.map { currentLogs ->
        currentLogs.sortedByDescending { it.performedAt }
    }

    override fun observeCycleSummary(
        progress: RoutineProgress,
        zone: ZoneId
    ): Flow<CycleSummary> =
        combine(observeCurrentCyclePlan(progress.cycleStartDate(zone)), logs) { plan, logs ->
            summaryCalculator.calculate(
                plan = plan,
                logs = logs,
                progress = progress,
                zone = zone
            )
        }

    override suspend fun getExercise(id: ExerciseId): Exercise? = exerciseById[id]

    override suspend fun getLatestWorkoutLog(exerciseId: ExerciseId): WorkoutLog? =
        logs.value
            .filter { it.exerciseId == exerciseId }
            .maxByOrNull { it.performedAt }

    override suspend fun getLatestWorkoutLog(plannedExerciseId: PlannedExerciseId): WorkoutLog? =
        logs.value
            .filter { it.plannedExerciseId == plannedExerciseId }
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

    override suspend fun switchRoutineTemplate(templateId: String): Result<Unit> = runCatching {
        require(templateExists(templateId)) { "Unknown plan template: $templateId" }
        selectedTemplateId.value = templateId
        progress.value = progress.value.copy(
            templateId = templateId,
            dayIndex = 0,
            lastCompletedDayIndex = null,
            lastCompletedAt = null,
            lastCompletedCycleNumber = null,
            lastCompletedPreviousCycleStartedAt = null
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
        val startsNewCycle = newCycleStartedAt != null
        progress.value = current.copy(
            dayIndex = nextDayIndex,
            cycleNumber = if (startsNewCycle) current.cycleNumber + 1 else current.cycleNumber,
            lastCompletedDayIndex = completedDayIndex,
            lastCompletedAt = completedAt,
            lastCompletedCycleNumber = current.cycleNumber,
            lastCompletedPreviousCycleStartedAt = current.cycleStartedAt,
            cycleStartedAt = newCycleStartedAt ?: current.cycleStartedAt
        )
    }

    override suspend fun setRoutineDayDate(
        routineDayInstanceId: String,
        assignedDate: LocalDate,
        cycleStartedAt: Instant?
    ): Result<Unit> = runCatching {
        val current = progress.value
        progress.value = current.copy(
            cycleStartedAt = cycleStartedAt ?: current.cycleStartedAt,
            routineDayDates = current.routineDayDates + (routineDayInstanceId to assignedDate)
        )
        logs.value = logs.value.map { log ->
            if (log.routineDayInstanceId == routineDayInstanceId) {
                log.copy(performedAt = assignedDate.atTime(12, 0))
            } else {
                log
            }
        }
    }

    override suspend fun cancelLatestRoutineDayCompletion(
        restoredDayIndex: Int,
        restoredCycleNumber: Int,
        restoredCycleStartedAt: Instant?,
        remainingLatestCompletion: RoutineCompletionSnapshot?,
        routineDayInstanceId: String,
        plannedExerciseIds: Set<PlannedExerciseId>,
        additionalExerciseIdPrefix: String
    ): Result<Unit> = runCatching {
        logs.value = logs.value.filterNot { log ->
            log.routineDayInstanceId == routineDayInstanceId ||
                (
                    log.routineDayInstanceId == null &&
                        (
                            log.plannedExerciseId in plannedExerciseIds ||
                                log.plannedExerciseId.value.startsWith(additionalExerciseIdPrefix)
                            )
                    )
        }
        progress.value = progress.value.copy(
            dayIndex = restoredDayIndex,
            cycleNumber = restoredCycleNumber,
            cycleStartedAt = restoredCycleStartedAt ?: progress.value.cycleStartedAt,
            lastCompletedDayIndex = remainingLatestCompletion?.dayIndex,
            lastCompletedAt = remainingLatestCompletion?.completedAt,
            lastCompletedCycleNumber = remainingLatestCompletion?.cycleNumber,
            lastCompletedPreviousCycleStartedAt = remainingLatestCompletion?.previousCycleStartedAt
        )
    }

    override suspend fun saveWorkoutLog(input: WorkoutLogInput): Result<Unit> = runCatching {
        val nextLog = WorkoutLog(
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
            setEntries = input.setEntries,
            routineDayInstanceId = input.routineDayInstanceId
        )
        logs.value = logs.value.filterNot { it.plannedExerciseId == input.plannedExerciseId } + nextLog
    }

    private fun CustomRoutineInput.toPlanTemplate(): PlanTemplate {
        val routineId = id?.takeIf { it.isNotBlank() } ?: nextCustomRoutineId()
        return PlanTemplate(
            id = routineId,
            name = name,
            level = PlanLevel.INTERMEDIATE,
            cycleLength = days.size,
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
            focusSummary = days.mapNotNull { it.primaryFocus }.distinct(),
            source = RoutineSource.CUSTOM
        )
    }

    private fun RoutineProgress.cycleStartDate(zone: ZoneId): LocalDate =
        (cycleStartedAt ?: startedAt)?.atZone(zone)?.toLocalDate()
            ?: LocalDate.now(zone)

    private fun buildCyclePlan(template: PlanTemplate, cycleStartDate: LocalDate): CyclePlan =
        CyclePlan(
            id = PlanId("${template.id}_$cycleStartDate"),
            templateId = template.id,
            name = template.name,
            cycleStartDate = cycleStartDate,
            days = template.days.map { day ->
                val date = cycleStartDate.plusDays(day.dayOffset.toLong())
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
