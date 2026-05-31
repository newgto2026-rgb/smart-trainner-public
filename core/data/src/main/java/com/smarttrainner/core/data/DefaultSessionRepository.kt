package com.smarttrainner.core.data

import com.smarttrainner.core.datastore.TrainingPreferencesDataSource
import com.smarttrainner.core.domain.SessionRepository
import com.smarttrainner.core.model.UserSession
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class DefaultSessionRepository @Inject constructor(
    private val preferences: TrainingPreferencesDataSource
) : SessionRepository {
    override fun observeActiveSession(): Flow<UserSession?> = preferences.activeSession

    override suspend fun startDefaultSession(): Result<UserSession> = runCatching {
        preferences.startDefaultSession()
    }
}
