package com.smarttrainner.app

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.domain.CheckNicknameAvailabilityUseCase
import com.smarttrainner.core.domain.LogoutUseCase
import com.smarttrainner.core.domain.ObserveActiveSessionUseCase
import com.smarttrainner.core.domain.ObserveTrainingExperienceUseCase
import com.smarttrainner.core.domain.SessionRepository
import com.smarttrainner.core.domain.SetTrainingExperienceUseCase
import com.smarttrainner.core.domain.SignInWithGoogleUseCase
import com.smarttrainner.core.domain.StartDefaultSessionUseCase
import com.smarttrainner.core.model.AuthProvider
import com.smarttrainner.core.model.NicknameAvailability
import com.smarttrainner.core.model.TrainingExperience
import com.smarttrainner.core.model.UserSession
import com.smarttrainner.core.model.UserSessionId
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
            viewModel.signInWithGoogle(idToken = "id-token", nickname = " Lift Kim ")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertThat(repository.lastGoogleIdToken).isEqualTo("id-token")
            assertThat(repository.lastGoogleNickname).isEqualTo("Lift Kim")
            assertThat(state.activeSession?.provider).isEqualTo(AuthProvider.GOOGLE)
            assertThat(state.activeSession?.nickname).isEqualTo("Lift Kim")
            assertThat(state.loginFailed).isFalse()
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

    private fun viewModel(repository: SessionRepository) = SmartTrainnerAppViewModel(
        observeActiveSession = ObserveActiveSessionUseCase(repository),
        observeTrainingExperience = ObserveTrainingExperienceUseCase(repository),
        startDefaultSession = StartDefaultSessionUseCase(repository),
        checkNicknameAvailability = CheckNicknameAvailabilityUseCase(repository),
        signInWithGoogleUseCase = SignInWithGoogleUseCase(repository),
        setTrainingExperienceUseCase = SetTrainingExperienceUseCase(repository),
        logoutUseCase = LogoutUseCase(repository)
    )
}

private class FakeSessionRepository : SessionRepository {
    private val activeSession = MutableStateFlow<UserSession?>(null)
    private val trainingExperience = MutableStateFlow(TrainingExperience.BEGINNER)
    var lastGoogleIdToken: String? = null
        private set
    var lastGoogleNickname: String? = null
        private set

    override fun observeActiveSession(): Flow<UserSession?> = activeSession

    override fun observeTrainingExperience(): Flow<TrainingExperience> = trainingExperience

    override suspend fun startDefaultSession(): Result<UserSession> {
        val session = UserSession(
            id = UserSessionId("local-default"),
            displayName = "Local Athlete",
            nickname = "local-athlete",
            email = null,
            provider = AuthProvider.LOCAL,
            linkedAt = null
        )
        activeSession.value = session
        return Result.success(session)
    }

    override suspend fun checkNicknameAvailability(nickname: String): Result<NicknameAvailability> =
        Result.success(NicknameAvailability(nickname = nickname.trim(), available = true))

    override suspend fun signInWithGoogle(idToken: String, nickname: String): Result<UserSession> {
        lastGoogleIdToken = idToken
        lastGoogleNickname = nickname
        val session = UserSession(
            id = UserSessionId("google-subject"),
            displayName = "Lift Kim",
            nickname = nickname,
            email = "lift@example.com",
            provider = AuthProvider.GOOGLE,
            linkedAt = "2026-06-02T00:00:00Z"
        )
        activeSession.value = session
        return Result.success(session)
    }

    override suspend fun setTrainingExperience(experience: TrainingExperience): Result<Unit> {
        trainingExperience.value = experience
        return Result.success(Unit)
    }

    override suspend fun logout(): Result<Unit> {
        activeSession.value = null
        return Result.success(Unit)
    }
}

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
