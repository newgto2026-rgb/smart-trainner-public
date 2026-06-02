package com.smarttrainner.core.domain

import com.smarttrainner.core.model.NicknameAvailability
import com.smarttrainner.core.model.UserSession
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun observeActiveSession(): Flow<UserSession?>
    suspend fun startDefaultSession(): Result<UserSession>
    suspend fun checkNicknameAvailability(nickname: String): Result<NicknameAvailability>
    suspend fun signInWithGoogle(idToken: String, nickname: String): Result<UserSession>
    suspend fun logout(): Result<Unit>
}
