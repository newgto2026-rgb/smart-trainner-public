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
            nickname = preferences[sessionNicknameKey(sessionId)]
                ?: preferences[sessionDisplayNameKey(sessionId)]
                ?: DEFAULT_DISPLAY_NAME,
            email = preferences[sessionEmailKey(sessionId)],
            provider = preferences[sessionProviderKey(sessionId)]
                ?.let { runCatching { AuthProvider.valueOf(it) }.getOrNull() }
                ?: AuthProvider.LOCAL,
            linkedAt = preferences[sessionLinkedAtKey(sessionId)]
        )
    }

    val selectedThemeTone: Flow<String> = context.trainingDataStore.data.map { preferences ->
        preferences[SELECTED_THEME_TONE] ?: DEFAULT_THEME_TONE
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
                cycleNumber = preferences[activeRoutineCycleNumberKey(sessionId)] ?: 1,
                startedAt = preferences[activeRoutineStartedAtKey(sessionId)],
                cycleStartedAt = preferences[activeRoutineCycleStartedAtKey(sessionId)],
                lastCompletedDayIndex = preferences[lastCompletedRoutineDayKey(sessionId)],
                lastCompletedAt = preferences[lastCompletedAtKey(sessionId)],
                lastCompletedCycleNumber = preferences[lastCompletedRoutineCycleKey(sessionId)],
                lastCompletedPreviousCycleStartedAt = preferences[lastCompletedPreviousCycleStartedAtKey(sessionId)]
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
            preferences[activeRoutineCycleNumberKey(sessionId)] = 1
            val now = clock.instant().toString()
            preferences[activeRoutineStartedAtKey(sessionId)] = now
            preferences[activeRoutineCycleStartedAtKey(sessionId)] = now
            preferences.remove(lastCompletedRoutineDayKey(sessionId))
            preferences.remove(lastCompletedAtKey(sessionId))
            preferences.remove(lastCompletedRoutineCycleKey(sessionId))
            preferences.remove(lastCompletedPreviousCycleStartedAtKey(sessionId))
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
            val currentCycleNumber = preferences[activeRoutineCycleNumberKey(sessionId)] ?: 1
            val currentCycleStartedAt = preferences[activeRoutineCycleStartedAtKey(sessionId)]
            preferences[lastCompletedRoutineDayKey(sessionId)] = completedDayIndex
            preferences[lastCompletedAtKey(sessionId)] = completedAt
            preferences[lastCompletedRoutineCycleKey(sessionId)] = currentCycleNumber
            if (currentCycleStartedAt != null) {
                preferences[lastCompletedPreviousCycleStartedAtKey(sessionId)] = currentCycleStartedAt
            } else {
                preferences.remove(lastCompletedPreviousCycleStartedAtKey(sessionId))
            }
            preferences[activeRoutineDayIndexKey(sessionId)] = nextDayIndex
            if (newCycleStartedAt != null) {
                preferences[activeRoutineCycleNumberKey(sessionId)] = currentCycleNumber + 1
                preferences[activeRoutineCycleStartedAtKey(sessionId)] = newCycleStartedAt
            }
        }
    }

    suspend fun cancelLatestRoutineDayCompletion(
        sessionId: String,
        restoredDayIndex: Int,
        restoredCycleNumber: Int,
        restoredCycleStartedAt: String?
    ) {
        context.trainingDataStore.edit { preferences ->
            preferences[activeRoutineDayIndexKey(sessionId)] = restoredDayIndex
            preferences[activeRoutineCycleNumberKey(sessionId)] = restoredCycleNumber.coerceAtLeast(1)
            if (restoredCycleStartedAt != null) {
                preferences[activeRoutineCycleStartedAtKey(sessionId)] = restoredCycleStartedAt
            } else {
                preferences.remove(activeRoutineCycleStartedAtKey(sessionId))
            }
            preferences.remove(lastCompletedRoutineDayKey(sessionId))
            preferences.remove(lastCompletedAtKey(sessionId))
            preferences.remove(lastCompletedRoutineCycleKey(sessionId))
            preferences.remove(lastCompletedPreviousCycleStartedAtKey(sessionId))
        }
    }

    suspend fun setRoutineProgress(
        sessionId: String,
        progress: RoutineProgressPreference
    ) {
        context.trainingDataStore.edit { preferences ->
            preferences[selectedTemplateIdKey(sessionId)] = progress.templateId
            preferences[activeRoutineTemplateIdKey(sessionId)] = progress.templateId
            preferences[activeRoutineDayIndexKey(sessionId)] = progress.dayIndex
            preferences[activeRoutineCycleNumberKey(sessionId)] = progress.cycleNumber.coerceAtLeast(1)
            progress.startedAt?.let { preferences[activeRoutineStartedAtKey(sessionId)] = it }
                ?: preferences.remove(activeRoutineStartedAtKey(sessionId))
            progress.cycleStartedAt?.let { preferences[activeRoutineCycleStartedAtKey(sessionId)] = it }
                ?: preferences.remove(activeRoutineCycleStartedAtKey(sessionId))
            progress.lastCompletedDayIndex?.let { preferences[lastCompletedRoutineDayKey(sessionId)] = it }
                ?: preferences.remove(lastCompletedRoutineDayKey(sessionId))
            progress.lastCompletedAt?.let { preferences[lastCompletedAtKey(sessionId)] = it }
                ?: preferences.remove(lastCompletedAtKey(sessionId))
            progress.lastCompletedCycleNumber?.let { preferences[lastCompletedRoutineCycleKey(sessionId)] = it }
                ?: preferences.remove(lastCompletedRoutineCycleKey(sessionId))
            progress.lastCompletedPreviousCycleStartedAt?.let {
                preferences[lastCompletedPreviousCycleStartedAtKey(sessionId)] = it
            } ?: preferences.remove(lastCompletedPreviousCycleStartedAtKey(sessionId))
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
            preferences[sessionNicknameKey(DEFAULT_USER_SESSION_ID)] =
                preferences[sessionNicknameKey(DEFAULT_USER_SESSION_ID)] ?: DEFAULT_NICKNAME
            preferences[sessionProviderKey(DEFAULT_USER_SESSION_ID)] = AuthProvider.LOCAL.name
        }
        return UserSession(
            id = UserSessionId(DEFAULT_USER_SESSION_ID),
            displayName = DEFAULT_DISPLAY_NAME,
            nickname = DEFAULT_NICKNAME,
            email = null,
            provider = AuthProvider.LOCAL,
            linkedAt = null
        )
    }

    suspend fun setActiveSession(session: UserSession) {
        val sessionId = session.id.value
        context.trainingDataStore.edit { preferences ->
            preferences[ACTIVE_SESSION_ID] = sessionId
            preferences[sessionDisplayNameKey(sessionId)] = session.displayName
            preferences[sessionNicknameKey(sessionId)] = session.nickname
            preferences[sessionProviderKey(sessionId)] = session.provider.name
            session.email?.let { preferences[sessionEmailKey(sessionId)] = it }
                ?: preferences.remove(sessionEmailKey(sessionId))
            session.linkedAt?.let { preferences[sessionLinkedAtKey(sessionId)] = it }
                ?: preferences.remove(sessionLinkedAtKey(sessionId))
        }
    }

    suspend fun clearActiveSession() {
        context.trainingDataStore.edit { preferences ->
            preferences.remove(ACTIVE_SESSION_ID)
        }
    }

    suspend fun setSelectedThemeTone(themeTone: String) {
        context.trainingDataStore.edit { preferences ->
            preferences[SELECTED_THEME_TONE] = themeTone
        }
    }

    private companion object {
        const val DEFAULT_TEMPLATE_ID = "beginner-full-body-3day"
        const val DEFAULT_THEME_TONE = "blue"
        const val DEFAULT_DISPLAY_NAME = "Local Athlete"
        const val DEFAULT_NICKNAME = "local-athlete"
        val ACTIVE_SESSION_ID = stringPreferencesKey("active_session_id")
        val SELECTED_THEME_TONE = stringPreferencesKey("selected_theme_tone")

        fun selectedTemplateIdKey(sessionId: String) =
            stringPreferencesKey("selected_template_id_$sessionId")

        fun activeRoutineTemplateIdKey(sessionId: String) =
            stringPreferencesKey("active_routine_template_id_$sessionId")

        fun activeRoutineDayIndexKey(sessionId: String) =
            intPreferencesKey("active_routine_day_index_$sessionId")

        fun activeRoutineCycleNumberKey(sessionId: String) =
            intPreferencesKey("active_routine_cycle_number_$sessionId")

        fun activeRoutineStartedAtKey(sessionId: String) =
            stringPreferencesKey("active_routine_started_at_$sessionId")

        fun activeRoutineCycleStartedAtKey(sessionId: String) =
            stringPreferencesKey("active_routine_cycle_started_at_$sessionId")

        fun lastCompletedRoutineDayKey(sessionId: String) =
            intPreferencesKey("last_completed_routine_day_$sessionId")

        fun lastCompletedAtKey(sessionId: String) =
            stringPreferencesKey("last_completed_at_$sessionId")

        fun lastCompletedRoutineCycleKey(sessionId: String) =
            intPreferencesKey("last_completed_routine_cycle_$sessionId")

        fun lastCompletedPreviousCycleStartedAtKey(sessionId: String) =
            stringPreferencesKey("last_completed_previous_cycle_started_at_$sessionId")

        fun sessionDisplayNameKey(sessionId: String) =
            stringPreferencesKey("session_display_name_$sessionId")

        fun sessionNicknameKey(sessionId: String) =
            stringPreferencesKey("session_nickname_$sessionId")

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
