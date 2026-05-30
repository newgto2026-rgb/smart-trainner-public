package com.smarttrainner.core.data

import com.smarttrainner.core.datastore.DEFAULT_USER_SESSION_ID
import com.smarttrainner.core.datastore.TrainingPreferencesDataSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class ActiveSessionResolver @Inject constructor(
    private val preferences: TrainingPreferencesDataSource
) {
    fun observeSessionId(): Flow<String> =
        preferences.activeSessionId.map { it ?: DEFAULT_USER_SESSION_ID }

    suspend fun sessionId(): String =
        preferences.activeSessionId.first() ?: DEFAULT_USER_SESSION_ID
}
