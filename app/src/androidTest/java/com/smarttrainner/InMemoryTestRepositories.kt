package com.smarttrainner

import com.smarttrainner.core.domain.ExerciseRepository
import com.smarttrainner.core.domain.RoutineProgressRepository
import com.smarttrainner.core.domain.SeedTrainingContent
import com.smarttrainner.core.domain.SessionRepository
import com.smarttrainner.core.domain.CyclePlanRepository
import com.smarttrainner.core.domain.WorkoutLogRepository
import com.smarttrainner.core.model.AuthProvider
import com.smarttrainner.core.model.CustomExerciseInput
import com.smarttrainner.core.model.CustomRoutineInput
import com.smarttrainner.core.model.ExerciseSource
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
import com.smarttrainner.core.model.CurrentRoutineCycle
import com.smarttrainner.core.model.CyclePlan
import com.smarttrainner.core.model.CycleSummary
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.WorkoutLogId
import com.smarttrainner.core.model.WorkoutLogInput
import com.smarttrainner.core.model.WorkoutSetLog
import com.smarttrainner.core.model.routineAdditionalExerciseCyclePrefix
import com.smarttrainner.core.model.routineDayInstanceId
import com.smarttrainner.core.model.routineDayInstancePrefix
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
import kotlinx.coroutines.flow.flowOf
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
    private val seedExercises = SeedTrainingContent.exercises
    private val customExercisesBySession = mutableMapOf<String, List<Exercise>>()
    private val exercises = MutableStateFlow(seedExercises)
    private val systemTemplates = SeedTrainingContent.templates
    private val selectedTemplateId = MutableStateFlow(DEFAULT_TEMPLATE_ID)
    private val customTemplates = MutableStateFlow<List<PlanTemplate>>(emptyList())
    private val progress = MutableStateFlow(defaultProgress())
    private val logs = MutableStateFlow<List<WorkoutLog>>(emptyList())
    private val summaryCalculator = CycleSummaryCalculator()
    private var activeSessionId = TEST_SESSION_ID
    private var nextCustomExerciseCounter = 1

    fun reset() {
        activeSessionId = TEST_SESSION_ID
        customExercisesBySession.clear()
        publishExercises()
        nextCustomExerciseCounter = 1
        selectedTemplateId.value = DEFAULT_TEMPLATE_ID
        customTemplates.value = emptyList()
        progress.value = defaultProgress()
        logs.value = emptyList()
    }

    fun setActiveSessionForTest(sessionId: String) {
        activeSessionId = sessionId
        publishExercises()
    }

    fun routineDayDatesForTest(): Map<String, LocalDate> = progress.value.routineDayDates

    fun workoutLogsForTest(): List<WorkoutLog> = logs.value

    fun customTemplatesForTest(): List<PlanTemplate> = customTemplates.value

    fun seedCustomRoutineAndLogForExerciseForTest(exerciseId: ExerciseId) {
        val exercise = currentExerciseById().getValue(exerciseId)
        val template = PlanTemplate(
            id = "custom-delete-test",
            name = "Delete test routine",
            level = PlanLevel.INTERMEDIATE,
            cycleLength = 1,
            description = "",
            days = listOf(
                PlanTemplateDay(
                    dayOffset = 0,
                    title = "Custom day",
                    focus = "BACK",
                    exercises = listOf(
                        TemplateExercise(
                            exerciseId = exerciseId,
                            sets = exercise.defaultSets,
                            repRange = exercise.defaultRepRange,
                            durationMinutes = exercise.defaultDurationMinutes,
                            restSeconds = exercise.restSeconds,
                            note = ""
                        )
                    ),
                    dayNumber = 1,
                    primaryFocus = com.smarttrainner.core.model.RoutineFocus.BACK
                )
            ),
            source = RoutineSource.CUSTOM
        )
        customTemplates.value = customTemplates.value.filterNot { it.id == template.id } + template
        logs.value = logs.value + WorkoutLog(
            id = WorkoutLogId(logs.value.size.toLong() + 1),
            sessionId = UserSessionId(TEST_SESSION_ID),
            plannedExerciseId = PlannedExerciseId("planned-${exerciseId.value}"),
            exerciseId = exerciseId,
            performedAt = LocalDate.of(2026, 6, 17).atTime(9, 0),
            sets = exercise.defaultSets,
            reps = exercise.defaultRepRange?.first,
            weightKg = null,
            durationMinutes = exercise.defaultDurationMinutes,
            memo = "",
            completed = true,
            setEntries = listOf(
                WorkoutSetLog(
                    order = 1,
                    reps = exercise.defaultRepRange?.first,
                    weightKg = null,
                    durationMinutes = exercise.defaultDurationMinutes
                )
            )
        )
    }

    fun progressForTest(): RoutineProgress = progress.value

    fun seedAssistedPullupLogForTest() {
        val exercise = currentExerciseById().getValue(ExerciseId("assisted_pullup"))
        logs.value = listOf(
            WorkoutLog(
                id = WorkoutLogId(1),
                sessionId = UserSessionId(TEST_SESSION_ID),
                plannedExerciseId = PlannedExerciseId("seed_assisted_pullup"),
                exerciseId = exercise.id,
                performedAt = LocalDate.of(2026, 5, 24).atTime(18, 0),
                sets = 1,
                reps = 5,
                weightKg = 62.5,
                durationMinutes = null,
                memo = "",
                completed = true,
                setEntries = listOf(
                    WorkoutSetLog(
                        order = 1,
                        reps = 5,
                        weightKg = 62.5,
                        durationMinutes = null
                    )
                )
            )
        )
    }

    fun assignCurrentRoutineDayDateForTest(date: LocalDate) {
        val current = progress.value
        val currentRoutineDayInstanceId = routineDayInstanceId(
            templateId = current.templateId,
            cycleNumber = current.cycleNumber,
            dayNumber = current.dayIndex + 1
        )
        progress.value = current.copy(
            routineDayDates = current.routineDayDates + (currentRoutineDayInstanceId to date)
        )
    }

    fun seedCompletedPastCycleAndCurrentCycleLogForSwitchTest() {
        val templateId = DEFAULT_TEMPLATE_ID
        val cycleThreeStart = Instant.parse("2026-05-18T00:00:00Z")
        val cycleFourStart = Instant.parse("2026-05-24T12:00:00Z")
        progress.value = RoutineProgress(
            templateId = templateId,
            dayIndex = 1,
            cycleNumber = 4,
            lastCompletedDayIndex = 0,
            lastCompletedAt = Instant.parse("2026-05-24T13:00:00Z"),
            lastCompletedCycleNumber = 4,
            lastCompletedPreviousCycleStartedAt = cycleFourStart,
            startedAt = Instant.EPOCH,
            cycleStartedAt = cycleFourStart,
            routineDayDates = mapOf(
                routineDayInstanceId(templateId, 4, 1) to LocalDate.of(2026, 5, 24),
                routineDayInstanceId(templateId, 4, 2) to LocalDate.of(2026, 5, 25)
            )
        )
        val template = templateById(templateId, customTemplates.value)
        val cycleThreePlan = buildCyclePlan(template, cycleThreeStart.atZone(ZoneId.of("UTC")).toLocalDate())
        val cycleFourPlan = buildCyclePlan(template, cycleFourStart.atZone(ZoneId.of("UTC")).toLocalDate())
        val pastCycleExercise = cycleThreePlan.days.first().exercises.first()
        val currentCycleExercise = cycleFourPlan.days.first().exercises.first()
        val currentCycleLegacyExercise = cycleFourPlan.days[1].exercises.first()
        logs.value = listOf(
            pastCycleExercise.toTestLog(
                id = 1,
                performedAt = LocalDate.of(2026, 5, 18).atTime(10, 0),
                routineDayInstanceId = routineDayInstanceId(templateId, 3, 1)
            ),
            currentCycleExercise.toTestLog(
                id = 2,
                performedAt = LocalDate.of(2026, 5, 24).atTime(10, 0),
                routineDayInstanceId = routineDayInstanceId(templateId, 4, 1)
            ),
            currentCycleLegacyExercise.toTestLog(
                id = 3,
                performedAt = LocalDate.of(2026, 5, 25).atTime(10, 0),
                routineDayInstanceId = null
            )
        )
    }

    override fun observeExercises(): Flow<List<Exercise>> = exercises

    override fun observePlanTemplates(): Flow<List<PlanTemplate>> =
        customTemplates.map { custom -> systemTemplates + custom }

    override fun observeCurrentCyclePlan(templateId: String, cycleStartDate: LocalDate): Flow<CyclePlan> =
        customTemplates.map { custom ->
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
        currentCycle: CurrentRoutineCycle,
        zone: ZoneId
    ): Flow<CycleSummary> = flowOf(
        summaryCalculator.calculate(currentCycle, zone)
    )

    override suspend fun getExercise(id: ExerciseId): Exercise? = currentExerciseById()[id]

    override suspend fun saveCustomExercise(input: CustomExerciseInput): Result<Exercise> = runCatching {
        val id = input.id ?: ExerciseId("custom_exercise_ui_${nextCustomExerciseCounter++}")
        val exercise = Exercise(
            id = id,
            name = input.name.trim(),
            muscleGroup = input.muscleGroup,
            equipment = input.equipment,
            difficulty = input.difficulty,
            imageKey = id.value,
            summary = input.summary.trim(),
            instructions = input.instructions.map { it.trim() }.filter { it.isNotEmpty() },
            safetyCues = input.safetyCues.map { it.trim() }.filter { it.isNotEmpty() },
            defaultSets = input.defaultSets,
            defaultRepRange = input.repRangeStart?.let { start ->
                input.repRangeEnd?.let { end -> start..end }
            },
            defaultDurationMinutes = input.defaultDurationMinutes,
            restSeconds = input.restSeconds,
            source = ExerciseSource.USER_CREATED,
            ownerSessionId = UserSessionId(activeSessionId),
            imageUri = input.imageUri?.trim()?.takeIf { it.isNotEmpty() }
        )
        customExercisesBySession[activeSessionId] =
            customExercisesBySession.getValueOrEmpty(activeSessionId).filterNot { it.id == exercise.id } + exercise
        publishExercises()
        exercise
    }

    override suspend fun archiveCustomExercise(id: ExerciseId): Result<Unit> = runCatching {
        val currentCustomExercises = customExercisesBySession.getValueOrEmpty(activeSessionId)
        require(currentCustomExercises.any { it.id == id }) { "Unknown custom exercise: ${id.value}" }
        customExercisesBySession[activeSessionId] = currentCustomExercises.filterNot { it.id == id }
        customTemplates.value = customTemplates.value.mapNotNull { template ->
            if (template.source != RoutineSource.CUSTOM) {
                template
            } else {
                val days = template.days
                    .map { day -> day.copy(exercises = day.exercises.filterNot { it.exerciseId == id }) }
                    .filter { it.exercises.isNotEmpty() }
                    .mapIndexed { index, day ->
                        day.copy(dayOffset = index, dayNumber = index + 1)
                    }
                if (days.isEmpty()) {
                    null
                } else {
                    template.copy(cycleLength = days.size, days = days)
                }
            }
        }
        if (customTemplates.value.none { it.id == selectedTemplateId.value }) {
            selectedTemplateId.value = DEFAULT_TEMPLATE_ID
        }
        if (customTemplates.value.none { it.id == progress.value.templateId } && progress.value.templateId != DEFAULT_TEMPLATE_ID) {
            progress.value = defaultProgress()
        }
        logs.value = logs.value.filterNot { it.exerciseId == id }
        publishExercises()
    }

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
        val current = progress.value
        val currentTemplate = templateById(current.templateId, customTemplates.value)
        val currentCyclePlan = buildCyclePlan(
            currentTemplate,
            current.cycleStartDate(ZoneId.of("UTC"))
        )
        val currentCyclePlannedExerciseIds = currentCyclePlan.days
            .flatMap { day -> day.exercises.map { it.id } }
            .toSet()
        val currentCyclePrefix = routineDayInstancePrefix(
            templateId = current.templateId,
            cycleNumber = current.cycleNumber
        )
        val currentAdditionalPrefix = routineAdditionalExerciseCyclePrefix(
            templateId = current.templateId,
            cycleNumber = current.cycleNumber
        )
        logs.value = logs.value.filterNot { log ->
            log.routineDayInstanceId?.startsWith(currentCyclePrefix) == true ||
                (
                    log.routineDayInstanceId == null &&
                        (
                            log.plannedExerciseId in currentCyclePlannedExerciseIds ||
                                log.plannedExerciseId.value.startsWith(currentAdditionalPrefix)
                            )
                    )
        }
        progress.value = progress.value.copy(
            templateId = templateId,
            dayIndex = 0,
            startedAt = Instant.parse("2026-05-24T12:00:00Z"),
            cycleStartedAt = Instant.parse("2026-05-24T12:00:00Z"),
            lastCompletedDayIndex = null,
            lastCompletedAt = null,
            lastCompletedCycleNumber = null,
            lastCompletedPreviousCycleStartedAt = null,
            routineDayDates = emptyMap()
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
            lastCompletedDayIndex = if (startsNewCycle) null else completedDayIndex,
            lastCompletedAt = if (startsNewCycle) null else completedAt,
            lastCompletedCycleNumber = if (startsNewCycle) null else current.cycleNumber,
            lastCompletedPreviousCycleStartedAt = if (startsNewCycle) null else current.cycleStartedAt,
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
        val existingLog = logs.value.firstOrNull { log ->
            log.plannedExerciseId == input.plannedExerciseId &&
                log.routineDayInstanceId == input.routineDayInstanceId &&
                log.exerciseId == input.exerciseId &&
                log.performedAt == input.performedAt
        }
        val nextLog = WorkoutLog(
            id = existingLog?.id ?: WorkoutLogId(logs.value.maxOfOrNull { it.id.value }?.plus(1) ?: 1L),
            sessionId = UserSessionId(activeSessionId),
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
        logs.value = logs.value.filterNot { it.id == nextLog.id } + nextLog
    }

    override suspend fun updateWorkoutLog(id: WorkoutLogId, input: WorkoutLogInput): Result<Unit> = runCatching {
        require(logs.value.any { it.id == id }) { "Unknown workout log: ${id.value}" }
        val nextLog = WorkoutLog(
            id = id,
            sessionId = UserSessionId(activeSessionId),
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
        logs.value = logs.value.map { log -> if (log.id == id) nextLog else log }
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
                        val exercise = currentExerciseById().getValue(item.exerciseId)
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

    private fun PlannedExercise.toTestLog(
        id: Long,
        performedAt: java.time.LocalDateTime,
        routineDayInstanceId: String?
    ): WorkoutLog = WorkoutLog(
        id = WorkoutLogId(id),
        sessionId = UserSessionId(TEST_SESSION_ID),
        plannedExerciseId = this.id,
        exerciseId = exercise.id,
        performedAt = performedAt,
        sets = sets,
        reps = repRange?.first,
        weightKg = 1.0,
        durationMinutes = durationMinutes,
        memo = "",
        completed = true,
        routineDayInstanceId = routineDayInstanceId
    )

    private fun defaultProgress(): RoutineProgress =
        RoutineProgress(
            templateId = DEFAULT_TEMPLATE_ID,
            dayIndex = 0,
            lastCompletedDayIndex = null,
            lastCompletedAt = null,
            startedAt = Instant.EPOCH,
            cycleStartedAt = Instant.EPOCH
        )

    private fun publishExercises() {
        exercises.value = seedExercises + customExercisesBySession.getValueOrEmpty(activeSessionId)
    }

    private fun currentExerciseById(): Map<ExerciseId, Exercise> =
        exercises.value.associateBy { it.id }
}

private fun Map<String, List<Exercise>>.getValueOrEmpty(sessionId: String): List<Exercise> =
    this[sessionId].orEmpty()

private const val TEST_SESSION_ID = "android-ui-test"
private const val DEFAULT_TEMPLATE_ID = "beginner-full-body-3day"
