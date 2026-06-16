package com.smarttrainner.feature.calendar.domain

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.domain.ExerciseRepository
import com.smarttrainner.core.domain.SessionRepository
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
import com.smarttrainner.core.model.ProfileGender
import com.smarttrainner.core.model.ProfileSetup
import com.smarttrainner.core.model.TrainingExperience
import com.smarttrainner.core.model.UserProfile
import com.smarttrainner.core.model.UserSession
import com.smarttrainner.core.model.UserSessionId
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.WorkoutLogId
import com.smarttrainner.core.model.WorkoutSetLog
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ObserveWorkoutCalendarMonthUseCaseTest {
    @Test
    fun invoke_groupsLogsByDateInsideRequestedMonth() = runTest {
        val repository = FakeWorkoutCalendarRepository()
        repository.logs.value = listOf(
            workoutLog(
                id = 1,
                exerciseId = "bench",
                performedAt = LocalDateTime.of(2026, 5, 9, 18, 0),
                sets = 3,
                reps = 10,
                weightKg = 40.0
            ),
            workoutLog(
                id = 2,
                exerciseId = "row",
                performedAt = LocalDateTime.of(2026, 5, 9, 17, 0),
                sets = 4,
                reps = 8,
                weightKg = 35.0,
                setEntries = listOf(
                    WorkoutSetLog(order = 1, reps = 8, weightKg = 35.0, durationMinutes = null),
                    WorkoutSetLog(order = 2, reps = 8, weightKg = 40.0, durationMinutes = null)
                )
            ),
            workoutLog(
                id = 3,
                exerciseId = "bench",
                performedAt = LocalDateTime.of(2026, 6, 1, 10, 0)
            )
        )
        val useCase = ObserveWorkoutCalendarMonthUseCase(repository, repository, repository)

        useCase(
            month = YearMonth.of(2026, 5),
            today = LocalDate.of(2026, 5, 9)
        ).test {
            val month = awaitItem()

            assertThat(month.summariesByDate.keys).containsExactly(LocalDate.of(2026, 5, 9))
            assertThat(month.todayWorkoutCount).isEqualTo(2)
            assertThat(month.summariesByDate.getValue(LocalDate.of(2026, 5, 9)).totalSetCount)
                .isEqualTo(7)
            assertThat(month.logsByDate.getValue(LocalDate.of(2026, 5, 9)).map { it.exerciseName })
                .containsExactly("Row", "Bench press")
                .inOrder()
            assertThat(month.logsByDate.getValue(LocalDate.of(2026, 5, 9)).first().setEntries.map { it.weightKg })
                .containsExactly(35.0, 40.0)
                .inOrder()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun invoke_calculatesEffectiveVolumeForAssistedLoadExercises() = runTest {
        val repository = FakeWorkoutCalendarRepository()
        repository.setBodyWeight(80.0)
        repository.logs.value = listOf(
            workoutLog(
                id = 1,
                exerciseId = "assisted_pullup",
                performedAt = LocalDateTime.of(2026, 5, 9, 18, 0),
                sets = 1,
                reps = 5,
                weightKg = 60.0
            )
        )
        val useCase = ObserveWorkoutCalendarMonthUseCase(repository, repository, repository)

        useCase(
            month = YearMonth.of(2026, 5),
            today = LocalDate.of(2026, 5, 9)
        ).test {
            val month = awaitItem()
            val log = month.logsByDate.getValue(LocalDate.of(2026, 5, 9)).single()

            assertThat(log.loadType).isEqualTo(ExerciseLoadType.ASSISTANCE_LOAD)
            assertThat(log.effectiveSetLoadsKg).containsExactly(20.0)
            assertThat(log.effectiveVolumeKg).isEqualTo(100.0)
            assertThat(log.volumeKg).isEqualTo(100.0)
            assertThat(month.summariesByDate.getValue(LocalDate.of(2026, 5, 9)).totalVolumeKg)
                .isEqualTo(100.0)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

private class FakeWorkoutCalendarRepository : WorkoutLogRepository, ExerciseRepository, SessionRepository {
    val logs = MutableStateFlow<List<WorkoutLog>>(emptyList())
    private val exercises = MutableStateFlow(
        listOf(
            exercise("bench", "Bench press", MuscleGroup.CHEST),
            exercise("row", "Row", MuscleGroup.BACK),
            exercise(
                id = "assisted_pullup",
                name = "Assisted Pull-up",
                muscleGroup = MuscleGroup.BACK,
                loadType = ExerciseLoadType.ASSISTANCE_LOAD
            )
        )
    )
    private val activeSession = MutableStateFlow(sessionWithBodyWeight(80.0))

    fun setBodyWeight(weightKg: Double) {
        activeSession.value = sessionWithBodyWeight(weightKg)
    }

    override fun observeLatestWorkoutLogs(): Flow<List<WorkoutLog>> = logs

    override fun observeAllWorkoutLogs(): Flow<List<WorkoutLog>> = logs

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

private fun workoutLog(
    id: Long,
    exerciseId: String,
    performedAt: LocalDateTime,
    sets: Int = 3,
    reps: Int? = 10,
    weightKg: Double? = 20.0,
    durationMinutes: Int? = null,
    setEntries: List<WorkoutSetLog> = emptyList()
) = WorkoutLog(
    id = WorkoutLogId(id),
    sessionId = UserSessionId("session"),
    plannedExerciseId = com.smarttrainner.core.model.PlannedExerciseId("planned_$id"),
    exerciseId = ExerciseId(exerciseId),
    performedAt = performedAt,
    sets = sets,
    reps = reps,
    weightKg = weightKg,
    durationMinutes = durationMinutes,
    memo = "",
    completed = true,
    setEntries = setEntries
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
