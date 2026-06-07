package com.smarttrainner.core.data

import com.smarttrainner.core.datastore.ActiveSessionResolver
import com.smarttrainner.core.domain.PushTokenRepository
import com.smarttrainner.core.network.PushTokenNetworkApi
import com.smarttrainner.core.network.PushTokenRegistrationRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultPushTokenRepository @Inject constructor(
    private val activeSessionResolver: ActiveSessionResolver,
    private val pushTokenNetworkApi: PushTokenNetworkApi
) : PushTokenRepository {
    override suspend fun registerToken(token: String): Result<Unit> = runCatching {
        val normalizedToken = token.trim()
        require(normalizedToken.isNotEmpty()) { "Push token must not be blank." }
        pushTokenNetworkApi.registerPushToken(
            sessionId = activeSessionResolver.sessionId(),
            request = PushTokenRegistrationRequest(token = normalizedToken)
        )
    }.map { Unit }
}
