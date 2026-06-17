package com.smarttrainner.feature.calendar.impl

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.domain.ExerciseRepository
import com.smarttrainner.core.domain.ObserveExercisesUseCase
import com.smarttrainner.core.domain.SaveWorkoutLogUseCase
import com.smarttrainner.core.domain.SessionRepository
import com.smarttrainner.core.domain.UpdateWorkoutLogUseCase
import com.smarttrainner.core.domain.WorkoutLogRepository
import com.smarttrainner.core.model.AuthProvider
import com.smarttrainner.core.model.BodyMeasurement
import com.smarttrainner.core.model.DifficultyLevel
import com.smarttrainner.core.model.EquipmentType
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.ExerciseLoadType
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.NicknameAvailability
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.ProfileGender
import com.smarttrainner.core.model.ProfileSetup
import com.smarttrainner.core.model.TrainingExperience
import com.smarttrainner.core.model.UserProfile
import com.smarttrainner.core.model.UserSession
import com.smarttrainner.core.model.UserSessionId
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.WorkoutLogId
import com.smarttrainner.core.model.WorkoutLogInput
import com.smarttrainner.feature.calendar.domain.CalendarPreferencesRepository
import com.smarttrainner.feature.calendar.domain.ObserveCalendarMonthExpandedUseCase
import com.smarttrainner.feature.calendar.domain.ObserveWorkoutCalendarMonthUseCase
import com.smarttrainner.feature.calendar.domain.UpdateCalendarMonthExpandedUseCase
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fixedClock = Clock.fixed(Instant.parse("2026-05-24T12:00:00Z"), ZoneOffset.UTC)

    @Test
    fun uiState_ignoresSavedDateOutsideToday() = runTest {
        val repository = FakeCalendarRepository()
        repository.logs.value = listOf(
            workoutLog(id = 1, exerciseId = "bench", day = 9),
            workoutLog(id = 2, exerciseId = "row", day = 24)
        )
        val viewModel = viewModel(
            repository = repository,
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "calendar_month" to "2026-05",
                    "calendar_selected_date" to "2026-05-09"
                )
            )
        )

        viewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()

            assertThat(state.currentMonth.toString()).isEqualTo("2026-05")
            assertThat(state.selectedDate.toString()).isEqualTo("2026-05-24")
            assertThat(state.selectedDateWorkouts.map { it.exerciseName }).containsExactly("Row")
            assertThat(state.days.single { it.date.toString() == "2026-05-09" }.workoutCount)
                .isEqualTo(0)
            assertThat(state.days.single { it.date.toString() == "2026-05-09" }.isAccessible)
                .isFalse()
            assertThat(state.days.single { it.date.toString() == "2026-05-24" }.isAccessible)
                .isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onDateClick_ignoresDateOutsideToday() = runTest {
        val repository = FakeCalendarRepository()
        repository.logs.value = listOf(
            workoutLog(id = 1, exerciseId = "bench", day = 9),
            workoutLog(id = 2, exerciseId = "row", day = 24)
        )
        val viewModel = viewModel(
            repository = repository,
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "calendar_month" to "2026-05",
                    "calendar_selected_date" to "2026-05-09"
                )
            )
        )

        viewModel.uiState.test {
            skipItems(1)
            assertThat(awaitItem().selectedDateWorkouts.map { it.exerciseName })
                .containsExactly("Row")

            viewModel.onAction(CalendarAction.OnDateClick(LocalDate.of(2026, 5, 9)))
            advanceUntilIdle()
            val state = viewModel.uiState.value

            assertThat(state.selectedDate.toString()).isEqualTo("2026-05-24")
            assertThat(state.selectedDateWorkouts.map { it.exerciseName }).containsExactly("Row")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun monthNavigationActionsStayOnToday() = runTest {
        val repository = FakeCalendarRepository()
        repository.logs.value = listOf(workoutLog(id = 1, exerciseId = "bench", day = 24))
        val viewModel = viewModel(
            repository = repository,
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "calendar_month" to "2026-05",
                    "calendar_selected_date" to "2026-05-24"
                )
            )
        )

        advanceUntilIdle()

        viewModel.onAction(CalendarAction.OnPreviousMonthClick)
        viewModel.onAction(CalendarAction.OnNextMonthClick)
        advanceUntilIdle()
        val state = viewModel.uiState.value

        assertThat(state.currentMonth.toString()).isEqualTo("2026-05")
        assertThat(state.selectedDate.toString()).isEqualTo("2026-05-24")
        assertThat(state.todayWorkoutCount).isEqualTo(1)
        assertThat(state.days.count { it.workoutCount > 0 }).isEqualTo(1)
    }

    @Test
    fun toggleMonthExpansionAction_collapsesAndExpandsSelectedWeek() = runTest {
        val repository = FakeCalendarRepository()
        val viewModel = viewModel(
            repository = repository,
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "calendar_month" to "2026-05",
                    "calendar_selected_date" to "2026-05-24"
                )
            )
        )

        advanceUntilIdle()
        val expandedState = viewModel.uiState.value

        assertThat(expandedState.isMonthExpanded).isTrue()
        assertThat(expandedState.days.size).isGreaterThan(7)
        assertThat(expandedState.selectedWeekDays).hasSize(7)
        assertThat(expandedState.selectedWeekDays.map { it.date }).contains(LocalDate.of(2026, 5, 24))

        viewModel.onAction(CalendarAction.OnToggleMonthExpansion)
        advanceUntilIdle()
        val collapsedState = viewModel.uiState.value

        assertThat(collapsedState.isMonthExpanded).isFalse()
        assertThat(collapsedState.selectedWeekDays).hasSize(7)
        assertThat(collapsedState.selectedWeekDays.map { it.date }).contains(LocalDate.of(2026, 5, 24))

        viewModel.onAction(CalendarAction.OnToggleMonthExpansion)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isMonthExpanded).isTrue()
    }

    @Test
    fun toggleMonthExpansionAction_persistsCollapsedState() = runTest {
        val repository = FakeCalendarRepository()
        val preferencesRepository = FakeCalendarPreferencesRepository()
        val viewModel = viewModel(
            repository = repository,
            preferencesRepository = preferencesRepository,
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "calendar_month" to "2026-05",
                    "calendar_selected_date" to "2026-05-24"
                )
            )
        )

        advanceUntilIdle()

        viewModel.onAction(CalendarAction.OnToggleMonthExpansion)
        advanceUntilIdle()

        assertThat(preferencesRepository.updates).containsExactly(false)
        assertThat(preferencesRepository.monthExpanded.value).isFalse()
    }

    @Test
    fun toggleMonthExpansionAction_restoresCollapsedSavedState() = runTest {
        val repository = FakeCalendarRepository()
        val viewModel = viewModel(
            repository = repository,
            preferencesRepository = FakeCalendarPreferencesRepository(initialMonthExpanded = false),
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "calendar_month" to "2026-05",
                    "calendar_selected_date" to "2026-05-24",
                    "calendar_is_month_expanded" to false
                )
            )
        )

        advanceUntilIdle()
        val state = viewModel.uiState.value

        assertThat(state.isMonthExpanded).isFalse()
        assertThat(state.selectedWeekDays).hasSize(7)
        assertThat(state.selectedWeekDays.map { it.date }).contains(LocalDate.of(2026, 5, 24))
    }

    @Test
    fun addWorkoutEditor_savesManualWorkoutForSelectedDate() = runTest {
        val repository = FakeCalendarRepository()
        val viewModel = viewModel(
            repository = repository,
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "calendar_month" to "2026-05",
                    "calendar_selected_date" to "2026-05-24"
                )
            )
        )
        advanceUntilIdle()

        viewModel.onAction(CalendarAction.OnAddWorkoutClick)
        viewModel.onAction(CalendarAction.OnEditorExerciseSelected(ExerciseId("row")))
        viewModel.onAction(CalendarAction.OnEditorSetRepsChanged(index = 0, value = "11"))
        viewModel.onAction(CalendarAction.OnEditorSetWeightChanged(index = 0, value = "42.5"))
        viewModel.onAction(CalendarAction.OnEditorSetRestChanged(index = 0, value = "120"))
        viewModel.onAction(CalendarAction.OnEditorMemoChanged("manual row"))
        viewModel.onAction(CalendarAction.OnEditorSaveClick)
        advanceUntilIdle()

        val log = repository.logs.value.single()
        assertThat(log.id).isEqualTo(WorkoutLogId(1))
        assertThat(log.plannedExerciseId).isEqualTo(PlannedExerciseId(""))
        assertThat(log.exerciseId).isEqualTo(ExerciseId("row"))
        assertThat(log.performedAt).isEqualTo(LocalDateTime.of(2026, 5, 24, 12, 0))
        assertThat(log.memo).isEqualTo("manual row")
        assertThat(log.setEntries.first().reps).isEqualTo(11)
        assertThat(log.setEntries.first().weightKg).isEqualTo(42.5)
        assertThat(viewModel.uiState.value.editor).isNull()
    }

    @Test
    fun editWorkoutEditor_updatesExistingWorkoutWithoutChangingSyncIdentityFields() = runTest {
        val repository = FakeCalendarRepository()
        val original = workoutLog(id = 5, exerciseId = "bench", day = 24).copy(
            plannedExerciseId = PlannedExerciseId("planned_bench_sync"),
            routineDayInstanceId = "routine-day|template|cycle1|day1",
            sets = 1,
            reps = 10,
            weightKg = 40.0,
            setEntries = listOf(
                com.smarttrainner.core.model.WorkoutSetLog(
                    order = 1,
                    reps = 10,
                    weightKg = 40.0,
                    durationMinutes = null,
                    restSeconds = 90
                )
            )
        )
        repository.logs.value = listOf(original)
        val viewModel = viewModel(
            repository = repository,
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "calendar_month" to "2026-05",
                    "calendar_selected_date" to "2026-05-24"
                )
            )
        )
        advanceUntilIdle()

        val workout = viewModel.uiState.value.selectedDateWorkouts.single()
        viewModel.onAction(CalendarAction.OnEditWorkoutClick(workout))
        viewModel.onAction(CalendarAction.OnEditorSetRepsChanged(index = 0, value = "12"))
        viewModel.onAction(CalendarAction.OnEditorSetWeightChanged(index = 0, value = "45"))
        viewModel.onAction(CalendarAction.OnEditorMemoChanged("edited"))
        viewModel.onAction(CalendarAction.OnEditorSaveClick)
        advanceUntilIdle()

        val edited = repository.logs.value.single()
        assertThat(edited.id).isEqualTo(original.id)
        assertThat(edited.plannedExerciseId).isEqualTo(original.plannedExerciseId)
        assertThat(edited.routineDayInstanceId).isEqualTo(original.routineDayInstanceId)
        assertThat(edited.performedAt).isEqualTo(original.performedAt)
        assertThat(edited.memo).isEqualTo("edited")
        assertThat(edited.setEntries.single().reps).isEqualTo(12)
        assertThat(edited.setEntries.single().weightKg).isEqualTo(45.0)
    }

    private fun viewModel(
        repository: FakeCalendarRepository,
        preferencesRepository: FakeCalendarPreferencesRepository = FakeCalendarPreferencesRepository(),
        savedStateHandle: SavedStateHandle = SavedStateHandle()
    ) = CalendarViewModel(
        savedStateHandle = savedStateHandle,
        observeWorkoutCalendarMonth = ObserveWorkoutCalendarMonthUseCase(repository, repository, repository),
        observeExercises = ObserveExercisesUseCase(repository),
        observeCalendarMonthExpanded = ObserveCalendarMonthExpandedUseCase(preferencesRepository),
        updateCalendarMonthExpanded = UpdateCalendarMonthExpandedUseCase(preferencesRepository),
        saveWorkoutLog = SaveWorkoutLogUseCase(repository),
        updateWorkoutLog = UpdateWorkoutLogUseCase(repository),
        clock = fixedClock
    )
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

private class FakeCalendarRepository : WorkoutLogRepository, ExerciseRepository, SessionRepository {
    val logs = MutableStateFlow<List<WorkoutLog>>(emptyList())
    private val exercises = MutableStateFlow(
        listOf(
            exercise("bench", "Bench press", MuscleGroup.CHEST),
            exercise("row", "Row", MuscleGroup.BACK)
        )
    )
    private val activeSession = MutableStateFlow(sessionWithBodyWeight(80.0))

    override fun observeLatestWorkoutLogs(): Flow<List<WorkoutLog>> = logs

    override fun observeAllWorkoutLogs(): Flow<List<WorkoutLog>> = logs

    override suspend fun getLatestWorkoutLog(exerciseId: ExerciseId): WorkoutLog? =
        logs.value.filter { it.exerciseId == exerciseId }.maxByOrNull { it.performedAt }

    override suspend fun getLatestWorkoutLog(plannedExerciseId: PlannedExerciseId): WorkoutLog? =
        logs.value.filter { it.plannedExerciseId == plannedExerciseId }.maxByOrNull { it.performedAt }

    override suspend fun saveWorkoutLog(input: WorkoutLogInput): Result<Unit> = runCatching {
        logs.value = logs.value + input.toWorkoutLog(id = (logs.value.maxOfOrNull { it.id.value } ?: 0L) + 1L)
    }

    override suspend fun updateWorkoutLog(id: WorkoutLogId, input: WorkoutLogInput): Result<Unit> = runCatching {
        require(logs.value.any { it.id == id })
        logs.value = logs.value.map { log -> if (log.id == id) input.toWorkoutLog(id.value) else log }
    }

    override fun observeExercises(): Flow<List<Exercise>> = exercises

    override suspend fun getExercise(id: ExerciseId): Exercise? =
        exercises.value.firstOrNull { it.id == id }

    override fun observeActiveSession(): Flow<UserSession?> = activeSession

    override fun observeTrainingExperience(): Flow<TrainingExperience> =
        MutableStateFlow(TrainingExperience.BEGINNER)

    override suspend fun startDefaultSession(
        nickname: String,
        profileSetup: ProfileSetup
    ): Result<UserSession> = unsupported()

    override suspend fun checkNicknameAvailability(nickname: String): Result<NicknameAvailability> = unsupported()

    override suspend fun signInWithGoogle(
        idToken: String,
        nickname: String?,
        profileSetup: ProfileSetup?,
        forceDeviceLogin: Boolean
    ): Result<UserSession> = unsupported()

    override suspend fun validateActiveSessionDevice(): Result<Unit> = unsupported()

    override suspend fun setTrainingExperience(experience: TrainingExperience): Result<Unit> = unsupported()

    override suspend fun updateBodyProfile(
        gender: ProfileGender?,
        heightCm: Int,
        weightKg: Double,
        nickname: String?
    ): Result<Unit> = unsupported()

    override suspend fun logout(): Result<Unit> = unsupported()

    private fun <T> unsupported(): Result<T> = Result.failure(UnsupportedOperationException())
}

private class FakeCalendarPreferencesRepository(
    initialMonthExpanded: Boolean = true
) : CalendarPreferencesRepository {
    val monthExpanded = MutableStateFlow(initialMonthExpanded)
    val updates = mutableListOf<Boolean>()

    override fun observeMonthExpanded(): Flow<Boolean> = monthExpanded

    override suspend fun setMonthExpanded(isExpanded: Boolean): Result<Unit> {
        updates += isExpanded
        monthExpanded.value = isExpanded
        return Result.success(Unit)
    }
}

private fun workoutLog(
    id: Long,
    exerciseId: String,
    day: Int
) = WorkoutLog(
    id = WorkoutLogId(id),
    sessionId = UserSessionId("session"),
    plannedExerciseId = PlannedExerciseId("planned_$id"),
    exerciseId = ExerciseId(exerciseId),
    performedAt = LocalDateTime.of(2026, 5, day, 18, 0),
    sets = 3,
    reps = 10,
    weightKg = 40.0,
    durationMinutes = null,
    memo = "",
    completed = true
)

private fun WorkoutLogInput.toWorkoutLog(id: Long) = WorkoutLog(
    id = WorkoutLogId(id),
    sessionId = UserSessionId("session"),
    plannedExerciseId = plannedExerciseId,
    exerciseId = exerciseId,
    performedAt = performedAt,
    sets = sets,
    reps = reps,
    weightKg = weightKg,
    durationMinutes = durationMinutes,
    memo = memo,
    completed = completed,
    setEntries = setEntries,
    routineDayInstanceId = routineDayInstanceId
)

private fun exercise(
    id: String,
    name: String,
    muscleGroup: MuscleGroup,
    loadType: ExerciseLoadType = ExerciseLoadType.EXTERNAL_LOAD
) = Exercise(
    id = ExerciseId(id),
    name = name,
    muscleGroup = muscleGroup,
    equipment = EquipmentType.MACHINE,
    difficulty = DifficultyLevel.INTERMEDIATE,
    imageKey = id,
    summary = "",
    instructions = emptyList(),
    safetyCues = emptyList(),
    defaultSets = 3,
    defaultRepRange = 8..12,
    defaultDurationMinutes = null,
    restSeconds = 90,
    loadType = loadType
)

private fun sessionWithBodyWeight(weightKg: Double): UserSession =
    UserSession(
        id = UserSessionId("session"),
        displayName = "Local",
        email = null,
        provider = AuthProvider.LOCAL,
        linkedAt = null,
        profile = UserProfile(
            bodyMeasurements = listOf(
                BodyMeasurement(
                    recordedDate = LocalDate.of(2026, 5, 1),
                    heightCm = 180,
                    weightKg = weightKg
                )
            )
        )
    )
