package com.smarttrainner.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttrainner.core.domain.CheckNicknameAvailabilityUseCase
import com.smarttrainner.core.domain.LogoutUseCase
import com.smarttrainner.core.domain.ObserveActiveSessionUseCase
import com.smarttrainner.core.domain.SignInWithGoogleUseCase
import com.smarttrainner.core.domain.StartDefaultSessionUseCase
import com.smarttrainner.core.model.UserSession
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class NicknameCheckStatus {
    Idle,
    Checking,
    Available,
    Taken,
    Invalid,
    Error
}

data class SmartTrainnerAppUiState(
    val activeSession: UserSession? = null,
    val isLoading: Boolean = true,
    val loginFailed: Boolean = false,
    val googleSignInCancelled: Boolean = false,
    val googleSignInInProgress: Boolean = false,
    val nicknameInput: String = DEFAULT_LOGIN_NICKNAME,
    val checkedNickname: String? = null,
    val nicknameCheckStatus: NicknameCheckStatus = NicknameCheckStatus.Idle
)

@HiltViewModel
class SmartTrainnerAppViewModel @Inject constructor(
    observeActiveSession: ObserveActiveSessionUseCase,
    private val startDefaultSession: StartDefaultSessionUseCase,
    private val checkNicknameAvailability: CheckNicknameAvailabilityUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {
    private val loginState = MutableStateFlow(SmartTrainnerAppUiState(isLoading = false))

    val uiState: StateFlow<SmartTrainnerAppUiState> = combine(
        observeActiveSession(),
        loginState
    ) { session, login ->
        login.copy(activeSession = session, isLoading = false)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SmartTrainnerAppUiState()
        )

    fun updateLoginNickname(nickname: String) {
        loginState.update {
            it.copy(
                nicknameInput = nickname,
                checkedNickname = null,
                nicknameCheckStatus = NicknameCheckStatus.Idle,
                loginFailed = false,
                googleSignInCancelled = false
            )
        }
    }

    fun continueWithDefaultSession() {
        viewModelScope.launch {
            startDefaultSession()
        }
    }

    fun checkNickname() {
        val nickname = loginState.value.nicknameInput.trim()
        if (nickname.length < MIN_NICKNAME_LENGTH) {
            loginState.update { it.copy(nicknameCheckStatus = NicknameCheckStatus.Invalid) }
            return
        }
        loginState.update {
            it.copy(
                nicknameCheckStatus = NicknameCheckStatus.Checking,
                checkedNickname = null,
                loginFailed = false,
                googleSignInCancelled = false
            )
        }
        viewModelScope.launch {
            val result = checkNicknameAvailability(nickname)
            loginState.update { current ->
                result.fold(
                    onSuccess = { availability ->
                        current.copy(
                            nicknameInput = availability.nickname,
                            checkedNickname = availability.nickname,
                            nicknameCheckStatus = if (availability.available) {
                                NicknameCheckStatus.Available
                            } else {
                                NicknameCheckStatus.Taken
                            }
                        )
                    },
                    onFailure = {
                        current.copy(nicknameCheckStatus = NicknameCheckStatus.Error)
                    }
                )
            }
        }
    }

    fun beginGoogleCredentialRequest() {
        loginState.update {
            it.copy(
                googleSignInInProgress = true,
                loginFailed = false,
                googleSignInCancelled = false
            )
        }
    }

    fun googleSignInCancelled() {
        loginState.update {
            it.copy(
                googleSignInInProgress = false,
                googleSignInCancelled = true,
                loginFailed = false
            )
        }
    }

    fun googleCredentialFailed() {
        loginState.update {
            it.copy(
                googleSignInInProgress = false,
                googleSignInCancelled = false,
                loginFailed = true
            )
        }
    }

    fun signInWithGoogle(idToken: String, nickname: String = loginState.value.nicknameInput) {
        val normalizedNickname = nickname.trim()
        if (normalizedNickname.length < MIN_NICKNAME_LENGTH) {
            loginState.update {
                it.copy(
                    googleSignInInProgress = false,
                    nicknameCheckStatus = NicknameCheckStatus.Invalid
                )
            }
            return
        }
        loginState.update {
            it.copy(
                googleSignInInProgress = true,
                loginFailed = false,
                googleSignInCancelled = false
            )
        }
        viewModelScope.launch {
            val result = signInWithGoogleUseCase(idToken, normalizedNickname)
            loginState.update {
                if (result.isSuccess) {
                    SmartTrainnerAppUiState(isLoading = false)
                } else {
                    it.copy(
                        googleSignInInProgress = false,
                        loginFailed = true,
                        nicknameCheckStatus = NicknameCheckStatus.Error
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
        }
    }
}

private const val DEFAULT_LOGIN_NICKNAME = "Local Athlete"
private const val MIN_NICKNAME_LENGTH = 2
