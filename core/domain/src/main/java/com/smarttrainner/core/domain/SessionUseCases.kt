package com.smarttrainner.core.domain

import javax.inject.Inject

class ObserveActiveSessionUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    operator fun invoke() = repository.observeActiveSession()
}

class StartDefaultSessionUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    suspend operator fun invoke() = repository.startDefaultSession()
}

class CheckNicknameAvailabilityUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    suspend operator fun invoke(nickname: String) = repository.checkNicknameAvailability(nickname)
}

class SignInWithGoogleUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    suspend operator fun invoke(idToken: String, nickname: String) =
        repository.signInWithGoogle(idToken, nickname)
}

class LogoutUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    suspend operator fun invoke() = repository.logout()
}
