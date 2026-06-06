package com.smarttrainner.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.smarttrainner.core.model.AuthProvider
import com.smarttrainner.core.model.BodyMeasurement
import com.smarttrainner.core.model.ProfileGender
import com.smarttrainner.core.model.ProfileSetup
import com.smarttrainner.core.model.RoutineProgressPreference
import com.smarttrainner.core.model.TrainingExperience
import com.smarttrainner.core.model.UserProfile
import com.smarttrainner.core.model.UserSession
import com.smarttrainner.core.model.UserSessionId
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Clock
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
            linkedAt = preferences[sessionLinkedAtKey(sessionId)],
            profile = preferences.toUserProfile(sessionId)
        )
    }

    val selectedThemeTone: Flow<String> = context.trainingDataStore.data.map { preferences ->
        preferences[SELECTED_THEME_TONE] ?: DEFAULT_THEME_TONE
    }

    val activeTrainingExperience: Flow<TrainingExperience> = context.trainingDataStore.data.map { preferences ->
        val sessionId = preferences[ACTIVE_SESSION_ID] ?: DEFAULT_USER_SESSION_ID
        preferences[trainingExperienceKey(sessionId)].toTrainingExperience()
    }

    fun selectedTemplateId(sessionId: String): Flow<String> = context.trainingDataStore.data.map { preferences ->
        preferences[selectedTemplateIdKey(sessionId)] ?: DEFAULT_TEMPLATE_ID
    }

    fun trainingExperience(sessionId: String): Flow<TrainingExperience> =
        context.trainingDataStore.data.map { preferences ->
            preferences[trainingExperienceKey(sessionId)].toTrainingExperience()
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
                lastCompletedPreviousCycleStartedAt = preferences[lastCompletedPreviousCycleStartedAtKey(sessionId)],
                routineDayDates = decodeRoutineDayDates(preferences[routineDayDatesKey(sessionId)])
            )
        }

    suspend fun installationDeviceId(): String {
        val existing = context.trainingDataStore.data.first()[INSTALLATION_DEVICE_ID]
        if (!existing.isNullOrBlank()) return existing

        val generated = UUID.randomUUID().toString()
        var stored = generated
        context.trainingDataStore.edit { preferences ->
            val current = preferences[INSTALLATION_DEVICE_ID]
            if (current.isNullOrBlank()) {
                preferences[INSTALLATION_DEVICE_ID] = generated
            } else {
                stored = current
            }
        }
        return stored
    }

    suspend fun setSelectedTemplateId(sessionId: String, templateId: String) {
        context.trainingDataStore.edit { preferences ->
            preferences[selectedTemplateIdKey(sessionId)] = templateId
        }
    }

    suspend fun setTrainingExperience(sessionId: String, experience: TrainingExperience) {
        context.trainingDataStore.edit { preferences ->
            preferences[trainingExperienceKey(sessionId)] = experience.name
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
            preferences.remove(routineDayDatesKey(sessionId))
        }
    }

    suspend fun setActiveRoutineDayIndex(sessionId: String, dayIndex: Int) {
        context.trainingDataStore.edit { preferences ->
            preferences[activeRoutineDayIndexKey(sessionId)] = dayIndex
        }
    }

    suspend fun setRoutineDayDate(
        sessionId: String,
        routineDayInstanceId: String,
        assignedDate: String,
        cycleStartedAt: String? = null
    ) {
        context.trainingDataStore.edit { preferences ->
            val dateKey = routineDayDatesKey(sessionId)
            val dates = decodeRoutineDayDates(preferences[dateKey]) + (routineDayInstanceId to assignedDate)
            preferences[dateKey] = encodeRoutineDayDates(dates)
            if (cycleStartedAt != null) {
                preferences[activeRoutineCycleStartedAtKey(sessionId)] = cycleStartedAt
            }
        }
    }

    suspend fun clearRoutineDayDates(sessionId: String) {
        context.trainingDataStore.edit { preferences ->
            preferences.remove(routineDayDatesKey(sessionId))
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
            preferences[activeRoutineDayIndexKey(sessionId)] = nextDayIndex
            if (newCycleStartedAt != null) {
                preferences[activeRoutineCycleNumberKey(sessionId)] = currentCycleNumber + 1
                preferences[activeRoutineCycleStartedAtKey(sessionId)] = newCycleStartedAt
                preferences.remove(lastCompletedRoutineDayKey(sessionId))
                preferences.remove(lastCompletedAtKey(sessionId))
                preferences.remove(lastCompletedRoutineCycleKey(sessionId))
                preferences.remove(lastCompletedPreviousCycleStartedAtKey(sessionId))
            } else {
                preferences[lastCompletedRoutineDayKey(sessionId)] = completedDayIndex
                preferences[lastCompletedAtKey(sessionId)] = completedAt
                preferences[lastCompletedRoutineCycleKey(sessionId)] = currentCycleNumber
                if (currentCycleStartedAt != null) {
                    preferences[lastCompletedPreviousCycleStartedAtKey(sessionId)] = currentCycleStartedAt
                } else {
                    preferences.remove(lastCompletedPreviousCycleStartedAtKey(sessionId))
                }
            }
        }
    }

    suspend fun cancelLatestRoutineDayCompletion(
        sessionId: String,
        restoredDayIndex: Int,
        restoredCycleNumber: Int,
        restoredCycleStartedAt: String?,
        remainingLastCompletedDayIndex: Int? = null,
        remainingLastCompletedAt: String? = null,
        remainingLastCompletedCycleNumber: Int? = null,
        remainingLastCompletedPreviousCycleStartedAt: String? = null
    ) {
        context.trainingDataStore.edit { preferences ->
            preferences[activeRoutineDayIndexKey(sessionId)] = restoredDayIndex
            preferences[activeRoutineCycleNumberKey(sessionId)] = restoredCycleNumber.coerceAtLeast(1)
            if (restoredCycleStartedAt != null) {
                preferences[activeRoutineCycleStartedAtKey(sessionId)] = restoredCycleStartedAt
            } else {
                preferences.remove(activeRoutineCycleStartedAtKey(sessionId))
            }
            if (remainingLastCompletedDayIndex != null) {
                preferences[lastCompletedRoutineDayKey(sessionId)] = remainingLastCompletedDayIndex
            } else {
                preferences.remove(lastCompletedRoutineDayKey(sessionId))
            }
            if (remainingLastCompletedAt != null) {
                preferences[lastCompletedAtKey(sessionId)] = remainingLastCompletedAt
            } else {
                preferences.remove(lastCompletedAtKey(sessionId))
            }
            if (remainingLastCompletedCycleNumber != null) {
                preferences[lastCompletedRoutineCycleKey(sessionId)] = remainingLastCompletedCycleNumber
            } else {
                preferences.remove(lastCompletedRoutineCycleKey(sessionId))
            }
            if (remainingLastCompletedPreviousCycleStartedAt != null) {
                preferences[lastCompletedPreviousCycleStartedAtKey(sessionId)] =
                    remainingLastCompletedPreviousCycleStartedAt
            } else {
                preferences.remove(lastCompletedPreviousCycleStartedAtKey(sessionId))
            }
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
            if (progress.routineDayDates.isNotEmpty()) {
                preferences[routineDayDatesKey(sessionId)] = encodeRoutineDayDates(progress.routineDayDates)
            } else {
                preferences.remove(routineDayDatesKey(sessionId))
            }
        }
    }

    suspend fun startDefaultSession(nickname: String, profileSetup: ProfileSetup): UserSession {
        val now = System.currentTimeMillis().toString()
        val normalizedNickname = nickname.trim().ifBlank { DEFAULT_NICKNAME }
        var profile = UserProfile()
        context.trainingDataStore.edit { preferences ->
            val createdAtKey = sessionCreatedAtKey(DEFAULT_USER_SESSION_ID)
            if (!preferences.contains(createdAtKey)) {
                preferences[createdAtKey] = now
            }
            preferences[ACTIVE_SESSION_ID] = DEFAULT_USER_SESSION_ID
            preferences[sessionDisplayNameKey(DEFAULT_USER_SESSION_ID)] =
                preferences[sessionDisplayNameKey(DEFAULT_USER_SESSION_ID)] ?: DEFAULT_DISPLAY_NAME
            preferences[sessionNicknameKey(DEFAULT_USER_SESSION_ID)] = normalizedNickname
            preferences[sessionProviderKey(DEFAULT_USER_SESSION_ID)] = AuthProvider.LOCAL.name
            profile = preferences.upsertBodyProfile(
                sessionId = DEFAULT_USER_SESSION_ID,
                gender = profileSetup.gender,
                heightCm = profileSetup.heightCm,
                weightKg = profileSetup.weightKg
            )
        }
        return UserSession(
            id = UserSessionId(DEFAULT_USER_SESSION_ID),
            displayName = DEFAULT_DISPLAY_NAME,
            nickname = normalizedNickname,
            email = null,
            provider = AuthProvider.LOCAL,
            linkedAt = null,
            profile = profile
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
            preferences.mergeUserProfile(sessionId, session.profile)
            preferences[profileSyncPendingKey(sessionId)] = false
        }
    }

    suspend fun updateBodyProfile(
        sessionId: String,
        gender: ProfileGender?,
        heightCm: Int,
        weightKg: Double,
        nickname: String? = null,
        syncPending: Boolean = true
    ): UserProfile {
        var profile = UserProfile()
        context.trainingDataStore.edit { preferences ->
            nickname?.trim()?.takeIf { it.isNotEmpty() }?.let {
                preferences[sessionNicknameKey(sessionId)] = it
            }
            profile = preferences.upsertBodyProfile(
                sessionId = sessionId,
                gender = gender,
                heightCm = heightCm,
                weightKg = weightKg
            )
            preferences[profileSyncPendingKey(sessionId)] = syncPending
        }
        return profile
    }

    suspend fun profileSyncPending(sessionId: String): Boolean =
        context.trainingDataStore.data.first()[profileSyncPendingKey(sessionId)] ?: false

    suspend fun markProfileSynced(sessionId: String) {
        context.trainingDataStore.edit { preferences ->
            preferences[profileSyncPendingKey(sessionId)] = false
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
        val INSTALLATION_DEVICE_ID = stringPreferencesKey("installation_device_id")
        val SELECTED_THEME_TONE = stringPreferencesKey("selected_theme_tone")

        fun selectedTemplateIdKey(sessionId: String) =
            stringPreferencesKey("selected_template_id_$sessionId")

        fun trainingExperienceKey(sessionId: String) =
            stringPreferencesKey("training_experience_$sessionId")

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

        fun routineDayDatesKey(sessionId: String) =
            stringPreferencesKey("routine_day_dates_$sessionId")

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

        fun sessionGenderKey(sessionId: String) =
            stringPreferencesKey("session_gender_$sessionId")

        fun sessionBodyMeasurementsKey(sessionId: String) =
            stringPreferencesKey("session_body_measurements_$sessionId")

        fun profileSyncPendingKey(sessionId: String) =
            booleanPreferencesKey("profile_sync_pending_$sessionId")
    }

    private fun MutablePreferences.upsertBodyProfile(
        sessionId: String,
        gender: ProfileGender?,
        heightCm: Int,
        weightKg: Double
    ): UserProfile {
        val genderKey = sessionGenderKey(sessionId)
        if (gender != null && !contains(genderKey)) {
            this[genderKey] = gender.name
        }
        val recordedDate = LocalDate.now(clock)
        val measurement = BodyMeasurement(
            recordedDate = recordedDate,
            heightCm = heightCm,
            weightKg = weightKg
        )
        val measurements = (
            decodeBodyMeasurements(this[sessionBodyMeasurementsKey(sessionId)])
                .filterNot { it.recordedDate == recordedDate } + measurement
            )
            .sortedBy { it.recordedDate }
        this[sessionBodyMeasurementsKey(sessionId)] = encodeBodyMeasurements(measurements)
        return UserProfile(
            gender = this[genderKey].toProfileGenderOrNull(),
            bodyMeasurements = measurements
        )
    }

    private fun MutablePreferences.mergeUserProfile(
        sessionId: String,
        profile: UserProfile
    ) {
        val genderKey = sessionGenderKey(sessionId)
        if (!contains(genderKey)) {
            profile.gender?.let { this[genderKey] = it.name }
        }
        val measurementKey = sessionBodyMeasurementsKey(sessionId)
        val localMeasurements = decodeBodyMeasurements(this[measurementKey])
        val mergedMeasurements = (profile.bodyMeasurements + localMeasurements)
            .associateBy { it.recordedDate }
            .values
            .sortedBy { it.recordedDate }
        if (mergedMeasurements.isNotEmpty()) {
            this[measurementKey] = encodeBodyMeasurements(mergedMeasurements)
        }
    }

    private fun Preferences.toUserProfile(sessionId: String): UserProfile =
        UserProfile(
            gender = this[sessionGenderKey(sessionId)].toProfileGenderOrNull(),
            bodyMeasurements = decodeBodyMeasurements(this[sessionBodyMeasurementsKey(sessionId)])
                .sortedBy { it.recordedDate }
        )

    private fun String?.toProfileGenderOrNull(): ProfileGender? =
        this?.let { runCatching { ProfileGender.valueOf(it) }.getOrNull() }

    private fun decodeBodyMeasurements(raw: String?): List<BodyMeasurement> =
        raw
            ?.let {
                runCatching {
                    Json.decodeFromString<List<BodyMeasurementPreferenceDto>>(it)
                        .mapNotNull { dto ->
                            runCatching {
                                BodyMeasurement(
                                    recordedDate = LocalDate.parse(dto.recordedDate),
                                    heightCm = dto.heightCm,
                                    weightKg = dto.weightKg
                                )
                            }.getOrNull()
                        }
                }.getOrNull()
            }
            ?: emptyList()

    private fun encodeBodyMeasurements(measurements: List<BodyMeasurement>): String =
        Json.encodeToString(
            measurements.map { measurement ->
                BodyMeasurementPreferenceDto(
                    recordedDate = measurement.recordedDate.toString(),
                    heightCm = measurement.heightCm,
                    weightKg = measurement.weightKg
                )
            }
        )

    private fun decodeRoutineDayDates(raw: String?): Map<String, String> =
        raw
            ?.let {
                runCatching {
                    Json.decodeFromString<List<RoutineDayDatePreferenceDto>>(it)
                        .associate { dto -> dto.routineDayInstanceId to dto.assignedDate }
                }.getOrNull()
            }
            ?: emptyMap()

    private fun encodeRoutineDayDates(dates: Map<String, String>): String =
        Json.encodeToString(
            dates.entries
                .sortedBy { it.key }
                .map { (routineDayInstanceId, assignedDate) ->
                    RoutineDayDatePreferenceDto(
                        routineDayInstanceId = routineDayInstanceId,
                        assignedDate = assignedDate
                    )
                }
        )
}

internal fun String?.toTrainingExperience(): TrainingExperience =
    this?.let { runCatching { TrainingExperience.valueOf(it) }.getOrNull() }
        ?: TrainingExperience.BEGINNER

@Serializable
private data class BodyMeasurementPreferenceDto(
    val recordedDate: String,
    val heightCm: Int,
    val weightKg: Double
)

@Serializable
private data class RoutineDayDatePreferenceDto(
    val routineDayInstanceId: String,
    val assignedDate: String
)
