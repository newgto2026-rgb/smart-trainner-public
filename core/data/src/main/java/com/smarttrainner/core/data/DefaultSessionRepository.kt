package com.smarttrainner.core.data

import com.smarttrainner.core.datastore.TrainingPreferencesDataSource
import com.smarttrainner.core.domain.DuplicateNicknameException
import com.smarttrainner.core.domain.SessionRepository
import com.smarttrainner.core.domain.SocialSignInCredential
import com.smarttrainner.core.model.AuthProvider
import com.smarttrainner.core.model.UserSession
import com.smarttrainner.core.model.UserSessionId
import com.smarttrainner.core.network.GoogleSignInRequest
import com.smarttrainner.core.network.SessionNetworkApi
import com.smarttrainner.core.network.UserSessionDto
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException

@Singleton
class DefaultSessionRepository @Inject constructor(
    private val preferences: TrainingPreferencesDataSource,
    private val sessionApi: SessionNetworkApi
) : SessionRepository {
    override fun observeActiveSession(): Flow<UserSession?> = preferences.activeSession

    override suspend fun startDefaultSession(): Result<UserSession> = runCatching {
        preferences.startDefaultSession()
    }

    override suspend fun clearActiveSession(): Result<Unit> = runCatching {
        preferences.clearActiveSession()
    }

    override suspend fun checkNicknameAvailability(nickname: String): Result<Boolean> =
        try {
            Result.success(sessionApi.checkNicknameAvailability(nickname).data.available)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Result.failure(error)
        }

    override suspend fun startSocialSession(
        credential: SocialSignInCredential,
        nickname: String
    ): Result<UserSession> =
        try {
            val session = when (credential.provider) {
                AuthProvider.GOOGLE -> {
                    val signedInSession = try {
                        sessionApi.signInWithGoogle(
                            GoogleSignInRequest(
                                idToken = credential.idToken,
                                nickname = nickname
                            )
                        ).data.toModel()
                    } catch (error: HttpException) {
                        if (error.code() == 409) throw DuplicateNicknameException()
                        throw error
                    }
                    preferences.setActiveSession(signedInSession)
                    signedInSession
                }

                AuthProvider.LOCAL -> error("Local sessions do not use social sign-in")
            }
            Result.success(session)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Result.failure(error)
        }
}

internal fun UserSessionDto.toModel(): UserSession =
    UserSession(
        id = UserSessionId(id),
        displayName = displayName,
        nickname = nickname,
        email = email,
        provider = runCatching { AuthProvider.valueOf(provider.uppercase()) }
            .getOrDefault(AuthProvider.LOCAL),
        providerAccountId = providerAccountId,
        avatarUrl = avatarUrl,
        linkedAt = linkedAt
    )
