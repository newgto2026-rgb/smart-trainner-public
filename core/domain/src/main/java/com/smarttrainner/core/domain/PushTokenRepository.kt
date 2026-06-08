package com.smarttrainner.core.domain

interface PushTokenRepository {
    suspend fun registerToken(token: String): Result<Unit>
}
