package com.smarttrainner.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.smarttrainner.core.model.AuthProvider
import com.smarttrainner.core.model.RoutineProgressPreference
import com.smarttrainner.core.model.UserSession
import com.smarttrainner.core.model.UserSessionId
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.trainingDataStore by preferencesDataStore(name = "training_preferences")

const val DEFAULT_USER_SESSION_ID = "local-default"

@Singleton
class TrainingPreferencesDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val clock: Clock
) {
    val activeSessionId: Flow<String?> = context.trainingDataStore.data.map { preferences ->
        preferences[ACTIVE_SESSION_ID]
    }

    val activeSession: Flow<UserSession?> = context.trainingDataStore.data.map { preferences ->
        val sessionId = preferences[ACTIVE_SESSION_ID] ?: return@map null
        UserSession(
            id = UserSessionId(sessionId),
            displayName = preferences[sessionDisplayNameKey(sessionId)] ?: DEFAULT_DISPLAY_NAME,
            email = preferences[sessionEmailKey(sessionId)],
            provider = preferences[sessionProviderKey(sessionId)]
                ?.let { runCatching { AuthProvider.valueOf(it) }.getOrNull() }
                ?: AuthProvider.LOCAL,
            linkedAt = preferences[sessionLinkedAtKey(sessionId)]
        )
    }

    fun selectedTemplateId(sessionId: String): Flow<String> = context.trainingDataStore.data.map { preferences ->
        preferences[selectedTemplateIdKey(sessionId)] ?: DEFAULT_TEMPLATE_ID
    }

    fun activeRoutineProgress(sessionId: String): Flow<RoutineProgressPreference> =
        context.trainingDataStore.data.map { preferences ->
            RoutineProgressPreference(
                templateId = preferences[activeRoutineTemplateIdKey(sessionId)]
                    ?: preferences[selectedTemplateIdKey(sessionId)]
                    ?: DEFAULT_TEMPLATE_ID,
                dayIndex = preferences[activeRoutineDayIndexKey(sessionId)] ?: 0,
                startedAt = preferences[activeRoutineStartedAtKey(sessionId)],
                cycleStartedAt = preferences[activeRoutineCycleStartedAtKey(sessionId)],
                lastCompletedDayIndex = preferences[lastCompletedRoutineDayKey(sessionId)],
                lastCompletedAt = preferences[lastCompletedAtKey(sessionId)]
            )
        }

    suspend fun setSelectedTemplateId(sessionId: String, templateId: String) {
        context.trainingDataStore.edit { preferences ->
            preferences[selectedTemplateIdKey(sessionId)] = templateId
        }
    }

    suspend fun setActiveRoutineTemplate(sessionId: String, templateId: String) {
        context.trainingDataStore.edit { preferences ->
            preferences[activeRoutineTemplateIdKey(sessionId)] = templateId
            preferences[activeRoutineDayIndexKey(sessionId)] = 0
            val now = clock.instant().toString()
            preferences[activeRoutineStartedAtKey(sessionId)] = now
            preferences[activeRoutineCycleStartedAtKey(sessionId)] = now
            preferences.remove(lastCompletedRoutineDayKey(sessionId))
            preferences.remove(lastCompletedAtKey(sessionId))
        }
    }

    suspend fun setActiveRoutineDayIndex(sessionId: String, dayIndex: Int) {
        context.trainingDataStore.edit { preferences ->
            preferences[activeRoutineDayIndexKey(sessionId)] = dayIndex
        }
    }

    suspend fun markRoutineDayCompleted(
        sessionId: String,
        completedDayIndex: Int,
        nextDayIndex: Int,
        completedAt: String,
        newCycleStartedAt: String?
    ) {
        context.trainingDataStore.edit { preferences ->
            preferences[lastCompletedRoutineDayKey(sessionId)] = completedDayIndex
            preferences[lastCompletedAtKey(sessionId)] = completedAt
            preferences[activeRoutineDayIndexKey(sessionId)] = nextDayIndex
            if (newCycleStartedAt != null) {
                preferences[activeRoutineCycleStartedAtKey(sessionId)] = newCycleStartedAt
            }
        }
    }

    suspend fun startDefaultSession(): UserSession {
        val now = System.currentTimeMillis().toString()
        context.trainingDataStore.edit { preferences ->
            val createdAtKey = sessionCreatedAtKey(DEFAULT_USER_SESSION_ID)
            if (!preferences.contains(createdAtKey)) {
                preferences[createdAtKey] = now
            }
            preferences[ACTIVE_SESSION_ID] = DEFAULT_USER_SESSION_ID
            preferences[sessionDisplayNameKey(DEFAULT_USER_SESSION_ID)] =
                preferences[sessionDisplayNameKey(DEFAULT_USER_SESSION_ID)] ?: DEFAULT_DISPLAY_NAME
            preferences[sessionProviderKey(DEFAULT_USER_SESSION_ID)] = AuthProvider.LOCAL.name
        }
        return UserSession(
            id = UserSessionId(DEFAULT_USER_SESSION_ID),
            displayName = DEFAULT_DISPLAY_NAME,
            email = null,
            provider = AuthProvider.LOCAL,
            linkedAt = null
        )
    }

    private companion object {
        const val DEFAULT_TEMPLATE_ID = "beginner-full-body-3day"
        const val DEFAULT_DISPLAY_NAME = "Local Athlete"
        val ACTIVE_SESSION_ID = stringPreferencesKey("active_session_id")

        fun selectedTemplateIdKey(sessionId: String) =
            stringPreferencesKey("selected_template_id_$sessionId")

        fun activeRoutineTemplateIdKey(sessionId: String) =
            stringPreferencesKey("active_routine_template_id_$sessionId")

        fun activeRoutineDayIndexKey(sessionId: String) =
            intPreferencesKey("active_routine_day_index_$sessionId")

        fun activeRoutineStartedAtKey(sessionId: String) =
            stringPreferencesKey("active_routine_started_at_$sessionId")

        fun activeRoutineCycleStartedAtKey(sessionId: String) =
            stringPreferencesKey("active_routine_cycle_started_at_$sessionId")

        fun lastCompletedRoutineDayKey(sessionId: String) =
            intPreferencesKey("last_completed_routine_day_$sessionId")

        fun lastCompletedAtKey(sessionId: String) =
            stringPreferencesKey("last_completed_at_$sessionId")

        fun sessionDisplayNameKey(sessionId: String) =
            stringPreferencesKey("session_display_name_$sessionId")

        fun sessionEmailKey(sessionId: String) =
            stringPreferencesKey("session_email_$sessionId")

        fun sessionProviderKey(sessionId: String) =
            stringPreferencesKey("session_provider_$sessionId")

        fun sessionCreatedAtKey(sessionId: String) =
            stringPreferencesKey("session_created_at_$sessionId")

        fun sessionLinkedAtKey(sessionId: String) =
            stringPreferencesKey("session_linked_at_$sessionId")
    }
}
