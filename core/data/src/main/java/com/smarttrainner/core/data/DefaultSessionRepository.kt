package com.smarttrainner.core.data

import com.smarttrainner.core.datastore.TrainingPreferencesDataSource
import com.smarttrainner.core.datastore.DEFAULT_USER_SESSION_ID
import com.smarttrainner.core.domain.SessionRepository
import com.smarttrainner.core.model.AuthProvider
import com.smarttrainner.core.model.NicknameAvailability
import com.smarttrainner.core.model.TrainingExperience
import com.smarttrainner.core.model.UserSession
import com.smarttrainner.core.model.UserSessionId
import com.smarttrainner.core.network.CreateSessionRequest
import com.smarttrainner.core.network.GoogleSignInRequest
import com.smarttrainner.core.network.NicknameAvailabilityDto
import com.smarttrainner.core.network.SessionNetworkApi
import com.smarttrainner.core.network.UserSessionDto
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@Singleton
class DefaultSessionRepository @Inject constructor(
    private val preferences: TrainingPreferencesDataSource,
    private val sessionNetworkApi: SessionNetworkApi
) : SessionRepository {
    override fun observeActiveSession(): Flow<UserSession?> = preferences.activeSession

    override fun observeTrainingExperience(): Flow<TrainingExperience> =
        preferences.activeTrainingExperience

    override suspend fun startDefaultSession(): Result<UserSession> = runCatching {
        val localSession = preferences.startDefaultSession()
        runCatching {
            val remoteSession = sessionNetworkApi.createSession(
                CreateSessionRequest(
                    id = localSession.id.value,
                    displayName = localSession.displayName,
                    nickname = localSession.nickname,
                    email = localSession.email,
                    provider = localSession.provider.name.lowercase()
                )
            ).data.toUserSession()
            preferences.setActiveSession(remoteSession)
            remoteSession
        }.getOrDefault(localSession)
    }

    override suspend fun checkNicknameAvailability(nickname: String): Result<NicknameAvailability> = runCatching {
        val currentSessionId = preferences.activeSession.first()?.id?.value
        sessionNetworkApi.checkNicknameAvailability(
            nickname = nickname.trim(),
            sessionId = currentSessionId
        ).data.toNicknameAvailability()
    }

    override suspend fun signInWithGoogle(idToken: String, nickname: String): Result<UserSession> = runCatching {
        val currentSessionId = preferences.activeSession.first()?.id?.value
        val remoteSession = sessionNetworkApi.signInWithGoogle(
            GoogleSignInRequest(
                idToken = idToken,
                nickname = nickname.trim(),
                sessionId = currentSessionId
            )
        ).data.toUserSession()
        preferences.setActiveSession(remoteSession)
        remoteSession
    }

    override suspend fun setTrainingExperience(experience: TrainingExperience): Result<Unit> = runCatching {
        val sessionId = preferences.activeSessionId.first() ?: DEFAULT_USER_SESSION_ID
        preferences.setTrainingExperience(sessionId, experience)
    }

    override suspend fun logout(): Result<Unit> = runCatching {
        preferences.clearActiveSession()
    }

    private fun NicknameAvailabilityDto.toNicknameAvailability(): NicknameAvailability =
        NicknameAvailability(
            nickname = nickname,
            available = available
        )

    private fun UserSessionDto.toUserSession(): UserSession =
        UserSession(
            id = UserSessionId(id),
            displayName = displayName,
            nickname = nickname,
            email = email,
            provider = when (provider.lowercase()) {
                "google" -> AuthProvider.GOOGLE
                else -> AuthProvider.LOCAL
            },
            linkedAt = linkedAt
        )
}
