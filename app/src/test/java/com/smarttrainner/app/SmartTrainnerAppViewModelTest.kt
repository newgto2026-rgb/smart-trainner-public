package com.smarttrainner.app

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.domain.CheckNicknameAvailabilityUseCase
import com.smarttrainner.core.domain.DeviceLoginConflictException
import com.smarttrainner.core.domain.LogoutUseCase
import com.smarttrainner.core.domain.NetworkStatusRepository
import com.smarttrainner.core.domain.ObserveActiveSessionUseCase
import com.smarttrainner.core.domain.ObserveNetworkOnlineUseCase
import com.smarttrainner.core.domain.ObserveTrainingExperienceUseCase
import com.smarttrainner.core.domain.SessionRepository
import com.smarttrainner.core.domain.SetTrainingExperienceUseCase
import com.smarttrainner.core.domain.SignInWithGoogleUseCase
import com.smarttrainner.core.domain.SyncPendingTrainingDataUseCase
import com.smarttrainner.core.domain.TrainingDataSyncer
import com.smarttrainner.core.domain.UpdateBodyProfileUseCase
import com.smarttrainner.core.domain.ValidateActiveSessionDeviceUseCase
import com.smarttrainner.core.model.AuthProvider
import com.smarttrainner.core.model.BodyMeasurement
import com.smarttrainner.core.model.NicknameAvailability
import com.smarttrainner.core.model.ProfileGender
import com.smarttrainner.core.model.ProfileSetup
import com.smarttrainner.core.model.TrainingExperience
import com.smarttrainner.core.model.UserProfile
import com.smarttrainner.core.model.UserSession
import com.smarttrainner.core.model.UserSessionId
import java.time.LocalDate
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class SmartTrainnerAppViewModelTest {
    @get:Rule
    val mainDispatcherRule = AppMainDispatcherRule()

    @Test
    fun checkNickname_marksAvailableNickname() = runTest {
        val repository = FakeSessionRepository()
        val viewModel = viewModel(repository)

        viewModel.uiState.test {
            awaitItem()
            viewModel.updateLoginNickname(" Lift Kim ")
            viewModel.checkNickname()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertThat(state.nicknameInput).isEqualTo("Lift Kim")
            assertThat(state.checkedNickname).isEqualTo("Lift Kim")
            assertThat(state.nicknameCheckStatus).isEqualTo(NicknameCheckStatus.Available)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun googleSignInCancelled_clearsProgressWithoutFailure() = runTest {
        val viewModel = viewModel(FakeSessionRepository())

        viewModel.uiState.test {
            awaitItem()
            viewModel.beginGoogleCredentialRequest()
            viewModel.googleSignInCancelled()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertThat(state.googleSignInInProgress).isFalse()
            assertThat(state.googleSignInCancelled).isTrue()
            assertThat(state.loginFailed).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun signInWithGoogle_savesGoogleSession() = runTest {
        val repository = FakeSessionRepository()
        val viewModel = viewModel(repository)

        viewModel.uiState.test {
            awaitItem()
            viewModel.signInWithGoogle(idToken = "id-token")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertThat(repository.lastGoogleIdToken).isEqualTo("id-token")
            assertThat(repository.lastGoogleNickname).isNull()
            assertThat(repository.lastForceDeviceLogin).isFalse()
            assertThat(repository.lastGoogleProfileSetup).isNull()
            assertThat(state.activeSession?.provider).isEqualTo(AuthProvider.GOOGLE)
            assertThat(state.activeSession?.nickname).isEqualTo("Lift Kim")
            assertThat(state.activeSession?.profile?.gender).isNull()
            assertThat(state.loginFailed).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun completeProfileSetup_savesNicknameAndBodyAfterGoogleSignIn() = runTest {
        val repository = FakeSessionRepository()
        val viewModel = viewModel(repository)

        viewModel.uiState.test {
            awaitItem()
            viewModel.signInWithGoogle(idToken = "id-token")
            advanceUntilIdle()
            viewModel.updateLoginNickname(" Neo ")
            viewModel.updateLoginGender(ProfileGender.MALE)
            viewModel.updateLoginHeightCm("180")
            viewModel.updateLoginWeightKg("82.5")
            viewModel.completeProfileSetup()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertThat(state.activeSession?.nickname).isEqualTo("Neo")
            assertThat(state.activeSession?.profile?.gender).isEqualTo(ProfileGender.MALE)
            assertThat(state.activeSession?.profile?.latestBodyMeasurement?.heightCm).isEqualTo(180)
            assertThat(state.activeSession?.profile?.latestBodyMeasurement?.weightKg).isEqualTo(82.5)
            assertThat(state.loginFailed).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun signInWithGoogle_deviceConflictCanForceLogin() = runTest {
        val repository = FakeSessionRepository()
        repository.googleSignInFailure = DeviceLoginConflictException("Galaxy Phone")
        val viewModel = viewModel(repository)

        viewModel.uiState.test {
            awaitItem()
            viewModel.signInWithGoogle(idToken = "id-token")
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.deviceLoginConflict).isTrue()
            assertThat(viewModel.uiState.value.deviceLoginConflictDeviceName).isEqualTo("Galaxy Phone")
            assertThat(viewModel.uiState.value.loginFailed).isFalse()

            repository.googleSignInFailure = null
            viewModel.confirmDeviceLoginTakeover()
            advanceUntilIdle()

            assertThat(repository.lastForceDeviceLogin).isTrue()
            assertThat(viewModel.uiState.value.deviceLoginConflict).isFalse()
            assertThat(viewModel.uiState.value.activeSession?.nickname).isEqualTo("Lift Kim")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateTrainingExperience_savesProfileExperience() = runTest {
        val repository = FakeSessionRepository()
        val viewModel = viewModel(repository)

        viewModel.uiState.test {
            awaitItem()
            viewModel.updateTrainingExperience(TrainingExperience.ADVANCED)
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.trainingExperience).isEqualTo(TrainingExperience.ADVANCED)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateBodyProfile_savesLatestMeasurementWithoutChangingGender() = runTest {
        val repository = FakeSessionRepository()
        repository.seedSession(
            UserSession(
                id = UserSessionId("local-default"),
                displayName = "Local Athlete",
                nickname = "local-athlete",
                email = null,
                provider = AuthProvider.LOCAL,
                linkedAt = null,
                profile = UserProfile(
                    gender = ProfileGender.MALE,
                    bodyMeasurements = listOf(
                        BodyMeasurement(LocalDate.of(2026, 6, 1), heightCm = 180, weightKg = 82.0)
                    )
                )
            )
        )
        val viewModel = viewModel(repository)

        viewModel.uiState.test {
            awaitItem()
            viewModel.updateBodyProfile(ProfileGender.FEMALE, heightCm = 181, weightKg = 83.4)
            advanceUntilIdle()

            val profile = viewModel.uiState.value.activeSession?.profile
            assertThat(profile?.gender).isEqualTo(ProfileGender.MALE)
            assertThat(profile?.latestBodyMeasurement?.heightCm).isEqualTo(181)
            assertThat(profile?.latestBodyMeasurement?.weightKg).isEqualTo(83.4)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onlineGoogleSession_syncsOnceWhenNetworkBecomesOnline() = runTest {
        val repository = FakeSessionRepository()
        repository.seedSession(
            UserSession(
                id = UserSessionId("google-subject"),
                displayName = "Lift Kim",
                nickname = "Lift Kim",
                email = "lift@example.com",
                provider = AuthProvider.GOOGLE,
                linkedAt = "2026-06-02T00:00:00Z",
                profile = UserProfile()
            )
        )
        val networkStatusRepository = MutableNetworkStatusRepository(initialOnline = false)
        val syncer = CountingTrainingDataSyncer()
        viewModel(
            repository = repository,
            networkStatusRepository = networkStatusRepository,
            syncer = syncer
        )

        runCurrent()
        assertThat(syncer.callCount).isEqualTo(0)

        networkStatusRepository.setOnline(true)
        runCurrent()
        assertThat(syncer.callCount).isEqualTo(1)

        runCurrent()
        assertThat(syncer.callCount).isEqualTo(1)

        networkStatusRepository.setOnline(false)
        runCurrent()
        assertThat(syncer.callCount).isEqualTo(1)

        networkStatusRepository.setOnline(true)
        runCurrent()
        assertThat(syncer.callCount).isEqualTo(2)
    }

    @Test
    fun onlineGoogleSession_exposesSyncProgressWhilePendingSyncRuns() = runTest {
        val repository = FakeSessionRepository()
        repository.seedSession(
            UserSession(
                id = UserSessionId("google-subject"),
                displayName = "Lift Kim",
                nickname = "Lift Kim",
                email = "lift@example.com",
                provider = AuthProvider.GOOGLE,
                linkedAt = "2026-06-02T00:00:00Z",
                profile = UserProfile()
            )
        )
        val syncer = SuspendingTrainingDataSyncer()
        val networkStatusRepository = MutableNetworkStatusRepository(initialOnline = true)
        val viewModel = viewModel(
            repository = repository,
            networkStatusRepository = networkStatusRepository,
            syncer = syncer
        )

        viewModel.uiState.test {
            awaitItem()
            runCurrent()

            assertThat(syncer.started).isTrue()
            assertThat(viewModel.uiState.value.syncInProgress).isTrue()

            syncer.complete()
            runCurrent()

            assertThat(viewModel.uiState.value.syncInProgress).isFalse()
            networkStatusRepository.setOnline(false)
            runCurrent()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun viewModel(
        repository: SessionRepository,
        networkStatusRepository: NetworkStatusRepository = AlwaysOfflineNetworkStatusRepository,
        syncer: TrainingDataSyncer = NoOpTrainingDataSyncer,
        pushTokenRegistrar: PushTokenRegistrar = NoOpPushTokenRegistrar
    ) = SmartTrainnerAppViewModel(
        observeActiveSession = ObserveActiveSessionUseCase(repository),
        observeTrainingExperience = ObserveTrainingExperienceUseCase(repository),
        observeNetworkOnline = ObserveNetworkOnlineUseCase(networkStatusRepository),
        checkNicknameAvailability = CheckNicknameAvailabilityUseCase(repository),
        signInWithGoogleUseCase = SignInWithGoogleUseCase(repository),
        setTrainingExperienceUseCase = SetTrainingExperienceUseCase(repository),
        syncPendingTrainingDataUseCase = SyncPendingTrainingDataUseCase(setOf(syncer)),
        updateBodyProfileUseCase = UpdateBodyProfileUseCase(repository),
        validateActiveSessionDeviceUseCase = ValidateActiveSessionDeviceUseCase(repository),
        logoutUseCase = LogoutUseCase(repository),
        pushTokenRegistrar = pushTokenRegistrar
    )
}

private object AlwaysOfflineNetworkStatusRepository : NetworkStatusRepository {
    override fun observeOnline(): Flow<Boolean> = flowOf(false)
}

private object NoOpTrainingDataSyncer : TrainingDataSyncer {
    override suspend fun syncPendingTrainingData(): Result<Unit> = Result.success(Unit)
}

private object NoOpPushTokenRegistrar : PushTokenRegistrar {
    override suspend fun registerCurrentTokenIfConfigured(): Result<Boolean> = Result.success(false)

    override suspend fun registerToken(token: String): Result<Boolean> = Result.success(true)
}

private class CountingTrainingDataSyncer : TrainingDataSyncer {
    var callCount = 0
        private set

    override suspend fun syncPendingTrainingData(): Result<Unit> {
        callCount += 1
        return Result.success(Unit)
    }
}

private class SuspendingTrainingDataSyncer : TrainingDataSyncer {
    private val completion = CompletableDeferred<Unit>()
    var started = false
        private set

    override suspend fun syncPendingTrainingData(): Result<Unit> {
        started = true
        completion.await()
        return Result.success(Unit)
    }

    fun complete() {
        completion.complete(Unit)
    }
}

private class MutableNetworkStatusRepository(initialOnline: Boolean) : NetworkStatusRepository {
    private val online = MutableStateFlow(initialOnline)

    override fun observeOnline(): Flow<Boolean> = online

    fun setOnline(value: Boolean) {
        online.value = value
    }
}

private class FakeSessionRepository : SessionRepository {
    private val activeSession = MutableStateFlow<UserSession?>(null)
    private val trainingExperience = MutableStateFlow(TrainingExperience.BEGINNER)
    var lastGoogleIdToken: String? = null
        private set
    var lastGoogleNickname: String? = null
        private set
    var lastGoogleProfileSetup: ProfileSetup? = null
        private set
    var lastForceDeviceLogin: Boolean? = null
        private set
    var googleSignInFailure: Throwable? = null
    var lastDefaultNickname: String? = null
        private set
    var lastDefaultProfileSetup: ProfileSetup? = null
        private set

    fun seedSession(session: UserSession) {
        activeSession.value = session
    }

    override fun observeActiveSession(): Flow<UserSession?> = activeSession

    override fun observeTrainingExperience(): Flow<TrainingExperience> = trainingExperience

    override suspend fun startDefaultSession(nickname: String, profileSetup: ProfileSetup): Result<UserSession> {
        lastDefaultNickname = nickname
        lastDefaultProfileSetup = profileSetup
        val session = UserSession(
            id = UserSessionId("local-default"),
            displayName = "Local Athlete",
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
        lastGoogleIdToken = idToken
        lastGoogleNickname = nickname
        lastGoogleProfileSetup = profileSetup
        lastForceDeviceLogin = forceDeviceLogin
        googleSignInFailure?.let { return Result.failure(it) }
        val session = UserSession(
            id = UserSessionId("google-subject"),
            displayName = "Lift Kim",
            nickname = nickname?.trim()?.takeIf { it.isNotEmpty() } ?: "Lift Kim",
            email = "lift@example.com",
            provider = AuthProvider.GOOGLE,
            linkedAt = "2026-06-02T00:00:00Z",
            profile = profileSetup?.toUserProfile() ?: UserProfile()
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
        val currentProfile = current.profile
        activeSession.value = current.copy(
            nickname = nickname?.trim()?.takeIf { it.isNotEmpty() } ?: current.nickname,
            profile = currentProfile.copy(
                gender = currentProfile.gender ?: gender,
                bodyMeasurements = currentProfile.bodyMeasurements
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

@OptIn(ExperimentalCoroutinesApi::class)
class AppMainDispatcherRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
