package com.smarttrainner.core.domain

import com.smarttrainner.core.model.UserSession
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun observeActiveSession(): Flow<UserSession?>
    suspend fun startDefaultSession(): Result<UserSession>
}
