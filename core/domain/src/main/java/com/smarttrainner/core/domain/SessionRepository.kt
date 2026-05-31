package com.smarttrainner.core.domain

import com.smarttrainner.core.model.AuthProvider
import com.smarttrainner.core.model.UserSession
import kotlinx.coroutines.flow.Flow

data class SocialSignInCredential(
    val provider: AuthProvider,
    val idToken: String,
    val displayName: String?,
    val email: String?,
    val avatarUrl: String?
)

class DuplicateNicknameException : IllegalStateException("Nickname is already taken")

interface SessionRepository {
    fun observeActiveSession(): Flow<UserSession?>
    suspend fun startDefaultSession(): Result<UserSession>
    suspend fun clearActiveSession(): Result<Unit>
    suspend fun checkNicknameAvailability(nickname: String): Result<Boolean>
    suspend fun startSocialSession(
        credential: SocialSignInCredential,
        nickname: String
    ): Result<UserSession>
}
