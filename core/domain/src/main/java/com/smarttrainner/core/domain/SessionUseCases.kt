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

class StartSocialSessionUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    suspend operator fun invoke(
        credential: SocialSignInCredential,
        nickname: String
    ) = repository.startSocialSession(credential, nickname)
}
