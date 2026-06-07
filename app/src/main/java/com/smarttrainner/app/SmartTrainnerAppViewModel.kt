package com.smarttrainner.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttrainner.core.domain.CheckNicknameAvailabilityUseCase
import com.smarttrainner.core.domain.DeviceLoginConflictException
import com.smarttrainner.core.domain.LogoutUseCase
import com.smarttrainner.core.domain.ObserveNetworkOnlineUseCase
import com.smarttrainner.core.domain.ObserveActiveSessionUseCase
import com.smarttrainner.core.domain.ObserveTrainingExperienceUseCase
import com.smarttrainner.core.domain.SetTrainingExperienceUseCase
import com.smarttrainner.core.domain.SignInWithGoogleUseCase
import com.smarttrainner.core.domain.SyncPendingTrainingDataUseCase
import com.smarttrainner.core.domain.UpdateBodyProfileUseCase
import com.smarttrainner.core.domain.ValidateActiveSessionDeviceUseCase
import com.smarttrainner.core.model.AuthProvider
import com.smarttrainner.core.model.ProfileGender
import com.smarttrainner.core.model.ProfileSetup
import com.smarttrainner.core.model.TrainingExperience
import com.smarttrainner.core.model.UserSession
import com.smarttrainner.core.model.toProfileSetupOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
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
    val trainingExperience: TrainingExperience = TrainingExperience.BEGINNER,
    val isLoading: Boolean = true,
    val loginFailed: Boolean = false,
    val googleSignInCancelled: Boolean = false,
    val googleSignInInProgress: Boolean = false,
    val nicknameInput: String = DEFAULT_LOGIN_NICKNAME,
    val checkedNickname: String? = null,
    val nicknameCheckStatus: NicknameCheckStatus = NicknameCheckStatus.Idle,
    val genderInput: ProfileGender? = null,
    val heightCmInput: String = "",
    val weightKgInput: String = "",
    val profileInputInvalid: Boolean = false,
    val deviceLoginConflict: Boolean = false,
    val deviceLoginConflictDeviceName: String? = null,
    val syncInProgress: Boolean = false,
    val pendingGoogleIdToken: String? = null,
    val pendingGoogleNickname: String? = null
)

@HiltViewModel
class SmartTrainnerAppViewModel @Inject constructor(
    observeActiveSession: ObserveActiveSessionUseCase,
    observeTrainingExperience: ObserveTrainingExperienceUseCase,
    observeNetworkOnline: ObserveNetworkOnlineUseCase,
    private val checkNicknameAvailability: CheckNicknameAvailabilityUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val setTrainingExperienceUseCase: SetTrainingExperienceUseCase,
    private val syncPendingTrainingDataUseCase: SyncPendingTrainingDataUseCase,
    private val updateBodyProfileUseCase: UpdateBodyProfileUseCase,
    private val validateActiveSessionDeviceUseCase: ValidateActiveSessionDeviceUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val pushTokenRegistrar: PushTokenRegistrar
) : ViewModel() {
    private val loginState = MutableStateFlow(SmartTrainnerAppUiState(isLoading = false))

    val uiState: StateFlow<SmartTrainnerAppUiState> = combine(
        observeActiveSession(),
        observeTrainingExperience(),
        loginState
    ) { session, trainingExperience, login ->
        val profileSetupRequired = session != null && session.profile.toProfileSetupOrNull() == null
        login.copy(
            activeSession = session,
            trainingExperience = trainingExperience,
            isLoading = false,
            nicknameInput = if (profileSetupRequired && login.nicknameInput.isBlank()) {
                session.nickname
            } else {
                login.nicknameInput
            },
            genderInput = login.genderInput ?: session?.profile?.gender
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SmartTrainnerAppUiState()
        )

    init {
        viewModelScope.launch {
            combine(
                observeActiveSession(),
                observeNetworkOnline()
            ) { session, online ->
                if (online && session?.provider == AuthProvider.GOOGLE) session.id.value else null
            }
                .distinctUntilChanged()
                .collectLatest { sessionId ->
                    if (sessionId != null) {
                        loginState.update { it.copy(syncInProgress = true) }
                        try {
                            validateActiveSessionDeviceUseCase()
                                .onSuccess {
                                    syncPendingTrainingDataUseCase()
                                    pushTokenRegistrar.registerCurrentTokenIfConfigured()
                                }
                        } finally {
                            loginState.update { it.copy(syncInProgress = false) }
                        }
                    } else {
                        loginState.update { it.copy(syncInProgress = false) }
                    }
                }
        }
    }

    fun updateLoginNickname(nickname: String) {
        loginState.update {
            it.copy(
                nicknameInput = nickname,
                checkedNickname = null,
                nicknameCheckStatus = NicknameCheckStatus.Idle,
                loginFailed = false,
                googleSignInCancelled = false,
                deviceLoginConflict = false,
                pendingGoogleIdToken = null,
                pendingGoogleNickname = null
            )
        }
    }

    fun updateLoginGender(gender: ProfileGender) {
        loginState.update {
            it.copy(
                genderInput = gender,
                profileInputInvalid = false,
                loginFailed = false,
                googleSignInCancelled = false,
                deviceLoginConflict = false
            )
        }
    }

    fun updateLoginHeightCm(heightCm: String) {
        loginState.update {
            it.copy(
                heightCmInput = heightCm.filter { character -> character.isDigit() }
                    .take(MAX_HEIGHT_INPUT_LENGTH),
                profileInputInvalid = false,
                loginFailed = false,
                googleSignInCancelled = false,
                deviceLoginConflict = false
            )
        }
    }

    fun updateLoginWeightKg(weightKg: String) {
        loginState.update {
            it.copy(
                weightKgInput = weightKg.filter { character -> character.isDigit() || character == '.' }
                    .take(MAX_WEIGHT_INPUT_LENGTH),
                profileInputInvalid = false,
                loginFailed = false,
                googleSignInCancelled = false,
                deviceLoginConflict = false
            )
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
                googleSignInCancelled = false,
                deviceLoginConflict = false
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
                googleSignInCancelled = false,
                deviceLoginConflict = false
            )
        }
    }

    fun googleSignInCancelled() {
        loginState.update {
            it.copy(
                googleSignInInProgress = false,
                googleSignInCancelled = true,
                loginFailed = false,
                deviceLoginConflict = false
            )
        }
    }

    fun googleCredentialFailed() {
        loginState.update {
            it.copy(
                googleSignInInProgress = false,
                googleSignInCancelled = false,
                loginFailed = true,
                deviceLoginConflict = false
            )
        }
    }

    fun signInWithGoogle(
        idToken: String,
        nickname: String? = null,
        forceDeviceLogin: Boolean = false
    ) {
        val normalizedNickname = nickname?.trim()?.takeIf { it.isNotEmpty() }
        val activeSession = uiState.value.activeSession
        val profileSetup = activeSession?.profile?.toProfileSetupOrNull() ?: loginState.value.profileSetupOrNull()
        if (normalizedNickname != null && normalizedNickname.length < MIN_NICKNAME_LENGTH) {
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
                googleSignInCancelled = false,
                deviceLoginConflict = false
            )
        }
        viewModelScope.launch {
            val result = signInWithGoogleUseCase(
                idToken = idToken,
                nickname = normalizedNickname,
                profileSetup = profileSetup,
                forceDeviceLogin = forceDeviceLogin
            )
            loginState.update { current ->
                result.fold(
                    onSuccess = { SmartTrainnerAppUiState(isLoading = false) },
                    onFailure = { error ->
                        if (error is DeviceLoginConflictException && !forceDeviceLogin) {
                            current.copy(
                                googleSignInInProgress = false,
                                loginFailed = false,
                                deviceLoginConflict = true,
                                deviceLoginConflictDeviceName = error.activeDeviceName,
                                pendingGoogleIdToken = idToken,
                                pendingGoogleNickname = normalizedNickname
                            )
                        } else {
                            current.copy(
                                googleSignInInProgress = false,
                                loginFailed = true,
                                deviceLoginConflict = false,
                                pendingGoogleIdToken = null,
                                pendingGoogleNickname = null,
                                nicknameCheckStatus = NicknameCheckStatus.Error
                            )
                        }
                    }
                )
            }
        }
    }

    fun confirmDeviceLoginTakeover() {
        val state = loginState.value
        val idToken = state.pendingGoogleIdToken ?: return
        signInWithGoogle(
            idToken = idToken,
            nickname = state.pendingGoogleNickname,
            forceDeviceLogin = true
        )
    }

    fun dismissDeviceLoginConflict() {
        loginState.update {
            it.copy(
                googleSignInInProgress = false,
                deviceLoginConflict = false,
                deviceLoginConflictDeviceName = null,
                pendingGoogleIdToken = null,
                pendingGoogleNickname = null
            )
        }
    }

    fun updateTrainingExperience(experience: TrainingExperience) {
        viewModelScope.launch {
            setTrainingExperienceUseCase(experience)
        }
    }

    fun updateBodyProfile(gender: ProfileGender?, heightCm: Int, weightKg: Double) {
        viewModelScope.launch {
            updateBodyProfileUseCase(gender, heightCm, weightKg)
        }
    }

    fun completeProfileSetup() {
        val state = loginState.value
        val nickname = state.nicknameInput.trim()
        val profileSetup = state.profileSetupOrNull()
        if (nickname.length < MIN_NICKNAME_LENGTH || profileSetup == null) {
            loginState.update {
                it.copy(
                    nicknameCheckStatus = if (nickname.length < MIN_NICKNAME_LENGTH) {
                        NicknameCheckStatus.Invalid
                    } else {
                        it.nicknameCheckStatus
                    },
                    profileInputInvalid = profileSetup == null
                )
            }
            return
        }
        loginState.update {
            it.copy(
                loginFailed = false,
                profileInputInvalid = false
            )
        }
        viewModelScope.launch {
            val result = updateBodyProfileUseCase(
                gender = profileSetup.gender,
                heightCm = profileSetup.heightCm,
                weightKg = profileSetup.weightKg,
                nickname = nickname
            )
            loginState.update { current ->
                result.fold(
                    onSuccess = {
                        current.copy(
                            nicknameInput = nickname,
                            checkedNickname = nickname,
                            nicknameCheckStatus = NicknameCheckStatus.Available,
                            loginFailed = false,
                            profileInputInvalid = false
                        )
                    },
                    onFailure = {
                        current.copy(
                            loginFailed = true,
                            nicknameCheckStatus = NicknameCheckStatus.Error
                        )
                    }
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
        }
    }

    private fun SmartTrainnerAppUiState.profileSetupOrNull(): ProfileSetup? {
        val gender = genderInput ?: return null
        val heightCm = heightCmInput.toIntOrNull()?.takeIf { it > 0 } ?: return null
        val weightKg = weightKgInput.toDoubleOrNull()?.takeIf { it > 0.0 } ?: return null
        return ProfileSetup(
            gender = gender,
            heightCm = heightCm,
            weightKg = weightKg
        )
    }
}

private const val DEFAULT_LOGIN_NICKNAME = ""
private const val MIN_NICKNAME_LENGTH = 2
private const val MAX_HEIGHT_INPUT_LENGTH = 3
private const val MAX_WEIGHT_INPUT_LENGTH = 6
