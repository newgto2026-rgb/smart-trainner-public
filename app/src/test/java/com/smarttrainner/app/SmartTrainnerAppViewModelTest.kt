package com.smarttrainner.app

import com.google.common.truth.Truth.assertThat
import com.smarttrainner.app.training.MainDispatcherRule
import com.smarttrainner.core.domain.CheckNicknameAvailabilityUseCase
import com.smarttrainner.core.domain.DuplicateNicknameException
import com.smarttrainner.core.domain.ObserveActiveSessionUseCase
import com.smarttrainner.core.domain.SessionRepository
import com.smarttrainner.core.domain.SocialSignInCredential
import com.smarttrainner.core.domain.StartDefaultSessionUseCase
import com.smarttrainner.core.domain.StartSocialSessionUseCase
import com.smarttrainner.core.model.AuthProvider
import com.smarttrainner.core.model.UserSession
import com.smarttrainner.core.model.UserSessionId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SmartTrainnerAppViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun checkNickname_updatesAvailabilityMessage() = runTest {
        val repository = FakeSessionRepository(nicknameAvailable = false)
        val viewModel = viewModel(repository)
        collectState(viewModel)

        viewModel.checkNickname("Lift Kim")
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.nicknameAvailable).isFalse()
        assertThat(viewModel.uiState.value.loginMessage).isEqualTo(LoginMessage.NICKNAME_TAKEN)
    }

    @Test
    fun requestNicknameForSocialSession_waitsForNicknameBeforeLogin() = runTest {
        val repository = FakeSessionRepository()
        val viewModel = viewModel(repository)
        collectState(viewModel)

        viewModel.requestNicknameForSocialSession(googleCredential())
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.awaitingNickname).isTrue()
        assertThat(viewModel.uiState.value.activeSession).isNull()
    }

    @Test
    fun continueWithSocialSession_marksDuplicateNickname() = runTest {
        val repository = FakeSessionRepository(duplicateNickname = true)
        val viewModel = viewModel(repository)
        collectState(viewModel)

        viewModel.requestNicknameForSocialSession(googleCredential())
        viewModel.continueWithSocialSession(nickname = "Lift Kim")
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.loginFailed).isTrue()
        assertThat(viewModel.uiState.value.loginMessage).isEqualTo(LoginMessage.NICKNAME_TAKEN)
    }

    private fun viewModel(repository: SessionRepository) = SmartTrainnerAppViewModel(
        observeActiveSession = ObserveActiveSessionUseCase(repository),
        startDefaultSession = StartDefaultSessionUseCase(repository),
        checkNicknameAvailability = CheckNicknameAvailabilityUseCase(repository),
        startSocialSession = StartSocialSessionUseCase(repository)
    )

    private fun TestScope.collectState(viewModel: SmartTrainnerAppViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
    }

    private fun googleCredential() = SocialSignInCredential(
        provider = AuthProvider.GOOGLE,
        idToken = "token",
        displayName = "Kim",
        email = "kim@example.com",
        avatarUrl = null
    )
}

private class FakeSessionRepository(
    private val nicknameAvailable: Boolean = true,
    private val duplicateNickname: Boolean = false
) : SessionRepository {
    private val activeSession = MutableStateFlow<UserSession?>(null)

    override fun observeActiveSession(): Flow<UserSession?> = activeSession

    override suspend fun startDefaultSession(): Result<UserSession> {
        val session = userSession(provider = AuthProvider.LOCAL)
        activeSession.value = session
        return Result.success(session)
    }

    override suspend fun checkNicknameAvailability(nickname: String): Result<Boolean> =
        Result.success(nicknameAvailable)

    override suspend fun startSocialSession(
        credential: SocialSignInCredential,
        nickname: String
    ): Result<UserSession> {
        if (duplicateNickname) return Result.failure(DuplicateNicknameException())
        val session = userSession(
            displayName = credential.displayName ?: nickname,
            nickname = nickname,
            email = credential.email,
            provider = credential.provider,
            avatarUrl = credential.avatarUrl
        )
        activeSession.value = session
        return Result.success(session)
    }

    private fun userSession(
        displayName: String = "Kim",
        nickname: String = "Lift Kim",
        email: String? = null,
        provider: AuthProvider,
        avatarUrl: String? = null
    ) = UserSession(
        id = UserSessionId("session"),
        displayName = displayName,
        nickname = nickname,
        email = email,
        provider = provider,
        providerAccountId = if (provider == AuthProvider.GOOGLE) "google-subject" else null,
        avatarUrl = avatarUrl,
        linkedAt = if (provider == AuthProvider.GOOGLE) "2026-05-31T00:00:00Z" else null
    )
}
