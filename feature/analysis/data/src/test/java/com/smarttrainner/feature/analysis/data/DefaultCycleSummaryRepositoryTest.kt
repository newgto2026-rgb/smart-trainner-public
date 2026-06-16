package com.smarttrainner.feature.analysis.data

import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.domain.SessionRepository
import com.smarttrainner.core.model.AuthProvider
import com.smarttrainner.core.model.CurrentRoutineCycle
import com.smarttrainner.core.model.CyclePlan
import com.smarttrainner.core.model.NicknameAvailability
import com.smarttrainner.core.model.PlanId
import com.smarttrainner.core.model.ProfileGender
import com.smarttrainner.core.model.ProfileSetup
import com.smarttrainner.core.model.RoutineProgress
import com.smarttrainner.core.model.TrainingExperience
import com.smarttrainner.core.model.UserSession
import com.smarttrainner.core.model.UserSessionId
import com.smarttrainner.feature.analysis.domain.CycleSummaryCalculator
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultCycleSummaryRepositoryTest {
    private val sessionRepository = FakeSessionRepository()
    private val repository = DefaultCycleSummaryRepository(
        summaryCalculator = CycleSummaryCalculator(),
        sessionRepository = sessionRepository
    )

    @Test
    fun observeCycleSummary_usesProvidedCurrentCycleSnapshot() = runTest {
        val currentCycle = currentCycle(
            cycleStartDate = LocalDate.of(2026, 5, 25),
            cycleNumber = 4
        )

        val summary = repository.observeCycleSummary(
            currentCycle = currentCycle,
            zone = ZoneOffset.UTC
        ).first()

        assertThat(summary.cycleStartDate).isEqualTo(LocalDate.of(2026, 5, 25))
        assertThat(summary.plannedExerciseCount).isEqualTo(0)
    }

    private fun currentCycle(
        cycleStartDate: LocalDate,
        cycleNumber: Int
    ): CurrentRoutineCycle {
        val progress = RoutineProgress(
            templateId = "balanced",
            dayIndex = 0,
            lastCompletedDayIndex = null,
            lastCompletedAt = null,
            cycleNumber = cycleNumber,
            startedAt = Instant.parse("2026-05-01T00:00:00Z"),
            cycleStartedAt = cycleStartDate.atStartOfDay(ZoneOffset.UTC).toInstant()
        )
        return CurrentRoutineCycle(
            progress = progress,
            plan = CyclePlan(
                id = PlanId("balanced-$cycleStartDate"),
                templateId = "balanced",
                name = "균형 루틴",
                cycleStartDate = cycleStartDate,
                days = emptyList()
            ),
            currentDayIndex = 0,
            currentDay = null,
            currentRoutineDayInstanceId = null,
            currentRoutineDayDate = null,
            previousRoutineDayInstanceId = null,
            previousRoutineDayDate = null,
            currentCycleLogs = emptyList(),
            allLogs = emptyList(),
            currentCyclePlannedExerciseIds = emptySet(),
            currentDayCompletedPlannedExerciseIds = emptySet(),
            latestCompletion = null
        )
    }
}

private class FakeSessionRepository : SessionRepository {
    private val activeSession = MutableStateFlow<UserSession?>(
        UserSession(
            id = UserSessionId("local-default"),
            displayName = "Local",
            email = null,
            provider = AuthProvider.LOCAL,
            linkedAt = null
        )
    )

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
