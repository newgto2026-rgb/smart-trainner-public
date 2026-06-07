package com.smarttrainner.core.domain

import javax.inject.Inject

class RegisterPushTokenUseCase @Inject constructor(
    private val repository: PushTokenRepository
) {
    suspend operator fun invoke(token: String) = repository.registerToken(token)
}
