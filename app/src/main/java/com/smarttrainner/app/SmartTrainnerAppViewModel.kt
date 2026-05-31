package com.smarttrainner.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttrainner.core.domain.CheckNicknameAvailabilityUseCase
import com.smarttrainner.core.domain.DuplicateNicknameException
import com.smarttrainner.core.domain.ObserveActiveSessionUseCase
import com.smarttrainner.core.domain.SocialSignInCredential
import com.smarttrainner.core.domain.StartDefaultSessionUseCase
import com.smarttrainner.core.domain.StartSocialSessionUseCase
import com.smarttrainner.core.model.UserSession
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SmartTrainnerAppUiState(
    val activeSession: UserSession? = null,
    val isLoading: Boolean = true,
    val loginFailed: Boolean = false,
    val loginMessage: LoginMessage? = null,
    val isCheckingNickname: Boolean = false,
    val nicknameAvailable: Boolean? = null,
    val isSigningIn: Boolean = false,
    val awaitingNickname: Boolean = false
)

enum class LoginMessage {
    NICKNAME_AVAILABLE,
    NICKNAME_TAKEN,
    NICKNAME_REQUIRED,
    GOOGLE_CANCELLED,
    LOGIN_FAILED
}

@HiltViewModel
class SmartTrainnerAppViewModel @Inject constructor(
    observeActiveSession: ObserveActiveSessionUseCase,
    private val startDefaultSession: StartDefaultSessionUseCase,
    private val checkNicknameAvailability: CheckNicknameAvailabilityUseCase,
    private val startSocialSession: StartSocialSessionUseCase
) : ViewModel() {
    private val loginState = MutableStateFlow(SmartTrainnerAppUiState(isLoading = false))
    private var nicknameCheckJob: Job? = null
    private var pendingSocialCredential: SocialSignInCredential? = null

    val uiState: StateFlow<SmartTrainnerAppUiState> = combine(
        observeActiveSession(),
        loginState
    ) { session, current ->
        current.copy(activeSession = session, isLoading = false)
    }
        .map { state ->
            if (state.activeSession != null) {
                state.copy(
                    loginFailed = false,
                    loginMessage = null,
                    isCheckingNickname = false,
                    nicknameAvailable = null,
                    isSigningIn = false,
                    awaitingNickname = false
                )
            } else {
                state
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SmartTrainnerAppUiState()
        )

    fun continueWithDefaultSession() {
        pendingSocialCredential = null
        viewModelScope.launch {
            startDefaultSession()
        }
    }

    fun onGoogleSignInStarted() {
        loginState.update {
            it.copy(
                isSigningIn = true,
                loginFailed = false,
                loginMessage = null,
                nicknameAvailable = null
            )
        }
    }

    fun requestNicknameForSocialSession(credential: SocialSignInCredential) {
        pendingSocialCredential = credential
        loginState.update {
            it.copy(
                awaitingNickname = true,
                isSigningIn = false,
                loginFailed = false,
                loginMessage = null,
                nicknameAvailable = null
            )
        }
    }

    fun returnToLogin() {
        pendingSocialCredential = null
        nicknameCheckJob?.cancel()
        loginState.update {
            it.copy(
                awaitingNickname = false,
                isSigningIn = false,
                isCheckingNickname = false,
                loginFailed = false,
                loginMessage = null,
                nicknameAvailable = null
            )
        }
    }

    fun onNicknameChanged() {
        loginState.update {
            it.copy(loginMessage = null, nicknameAvailable = null, loginFailed = false)
        }
    }

    fun checkNickname(nickname: String) {
        val trimmed = nickname.trim()
        nicknameCheckJob?.cancel()
        if (trimmed.length < MIN_NICKNAME_LENGTH) {
            loginState.update {
                it.copy(
                    loginMessage = LoginMessage.NICKNAME_REQUIRED,
                    nicknameAvailable = null,
                    isCheckingNickname = false
                )
            }
            return
        }

        nicknameCheckJob = viewModelScope.launch {
            loginState.update {
                it.copy(isCheckingNickname = true, loginMessage = null, nicknameAvailable = null)
            }
            val available = checkNicknameAvailability(trimmed).getOrNull()
            loginState.update {
                it.copy(
                    isCheckingNickname = false,
                    nicknameAvailable = available,
                    loginMessage = when (available) {
                        true -> LoginMessage.NICKNAME_AVAILABLE
                        false -> LoginMessage.NICKNAME_TAKEN
                        null -> LoginMessage.LOGIN_FAILED
                    },
                    loginFailed = available == null
                )
            }
        }
    }

    fun continueWithSocialSession(nickname: String) {
        val credential = pendingSocialCredential
        val trimmed = nickname.trim()
        if (credential == null) {
            loginState.update {
                it.copy(loginMessage = LoginMessage.LOGIN_FAILED, loginFailed = true)
            }
            return
        }
        if (trimmed.length < MIN_NICKNAME_LENGTH) {
            loginState.update {
                it.copy(loginMessage = LoginMessage.NICKNAME_REQUIRED, loginFailed = true)
            }
            return
        }

        viewModelScope.launch {
            loginState.update {
                it.copy(isSigningIn = true, loginMessage = null, loginFailed = false)
            }
            val result = startSocialSession(credential, trimmed)
            if (result.isSuccess) {
                pendingSocialCredential = null
            }
            loginState.update {
                if (result.isSuccess) {
                    it.copy(
                        isSigningIn = false,
                        loginFailed = false,
                        loginMessage = null,
                        awaitingNickname = false
                    )
                } else {
                    val error = result.exceptionOrNull()
                    it.copy(
                        isSigningIn = false,
                        loginFailed = true,
                        nicknameAvailable = if (error is DuplicateNicknameException) false else it.nicknameAvailable,
                        loginMessage = if (error is DuplicateNicknameException) {
                            LoginMessage.NICKNAME_TAKEN
                        } else {
                            LoginMessage.LOGIN_FAILED
                        }
                    )
                }
            }
        }
    }

    fun onGoogleCredentialFailed(cancelled: Boolean) {
        pendingSocialCredential = null
        loginState.update {
            it.copy(
                awaitingNickname = false,
                loginFailed = !cancelled,
                loginMessage = if (cancelled) LoginMessage.GOOGLE_CANCELLED else LoginMessage.LOGIN_FAILED,
                isSigningIn = false
            )
        }
    }

    companion object {
        private const val MIN_NICKNAME_LENGTH = 2
    }
}
