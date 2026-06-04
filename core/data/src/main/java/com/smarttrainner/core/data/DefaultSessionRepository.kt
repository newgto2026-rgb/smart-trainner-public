package com.smarttrainner.core.data

import android.os.Build
import com.smarttrainner.core.datastore.TrainingPreferencesDataSource
import com.smarttrainner.core.datastore.DEFAULT_USER_SESSION_ID
import com.smarttrainner.core.domain.DeviceLoginConflictException
import com.smarttrainner.core.domain.SessionRepository
import com.smarttrainner.core.domain.TrainingDataSyncer
import com.smarttrainner.core.model.AuthProvider
import com.smarttrainner.core.model.BodyMeasurement
import com.smarttrainner.core.model.NicknameAvailability
import com.smarttrainner.core.model.ProfileGender
import com.smarttrainner.core.model.ProfileSetup
import com.smarttrainner.core.model.TrainingExperience
import com.smarttrainner.core.model.UserProfile
import com.smarttrainner.core.model.UserSession
import com.smarttrainner.core.model.UserSessionId
import com.smarttrainner.core.model.toProfileSetupOrNull
import com.smarttrainner.core.network.BodyMeasurementDto
import com.smarttrainner.core.network.CreateSessionRequest
import com.smarttrainner.core.network.GoogleSignInRequest
import com.smarttrainner.core.network.NicknameAvailabilityDto
import com.smarttrainner.core.network.SessionNetworkApi
import com.smarttrainner.core.network.SessionProfileRequest
import com.smarttrainner.core.network.UserSessionDto
import com.smarttrainner.core.network.UserProfileDto
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException

@Singleton
class DefaultSessionRepository @Inject constructor(
    private val preferences: TrainingPreferencesDataSource,
    private val sessionNetworkApi: SessionNetworkApi
) : SessionRepository, TrainingDataSyncer {
    override fun observeActiveSession(): Flow<UserSession?> = preferences.activeSession

    override fun observeTrainingExperience(): Flow<TrainingExperience> =
        preferences.activeTrainingExperience

    override suspend fun startDefaultSession(
        nickname: String,
        profileSetup: ProfileSetup
    ): Result<UserSession> = runCatching {
        val localSession = preferences.startDefaultSession(nickname, profileSetup)
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
            val profile = preferences.updateBodyProfile(
                sessionId = remoteSession.id.value,
                gender = profileSetup.gender,
                heightCm = profileSetup.heightCm,
                weightKg = profileSetup.weightKg
            )
            remoteSession.copy(profile = profile)
        }.getOrDefault(localSession)
    }

    override suspend fun checkNicknameAvailability(nickname: String): Result<NicknameAvailability> = runCatching {
        val currentSessionId = preferences.activeSession.first()?.id?.value
        sessionNetworkApi.checkNicknameAvailability(
            nickname = nickname.trim(),
            sessionId = currentSessionId
        ).data.toNicknameAvailability()
    }

    override suspend fun signInWithGoogle(
        idToken: String,
        nickname: String?,
        profileSetup: ProfileSetup?,
        forceDeviceLogin: Boolean
    ): Result<UserSession> = try {
        val currentSession = preferences.activeSession.first()
        val currentSessionId = currentSession?.id?.value
        val normalizedNickname = nickname?.trim()?.takeIf { it.isNotEmpty() }
        val profileToSave = profileSetup ?: currentSession?.profile?.toProfileSetupOrNull()
        val deviceId = preferences.installationDeviceId()
        val remoteSession = sessionNetworkApi.signInWithGoogle(
            GoogleSignInRequest(
                idToken = idToken,
                nickname = normalizedNickname,
                sessionId = currentSessionId,
                profile = profileToSave?.toSessionProfileRequest(nickname = normalizedNickname),
                deviceId = deviceId,
                deviceName = deviceDisplayName(),
                forceDeviceLogin = forceDeviceLogin
            )
        ).data.toUserSession()
        preferences.setActiveSession(remoteSession)
        val profile = profileToSave?.let {
            preferences.updateBodyProfile(
                sessionId = remoteSession.id.value,
                gender = it.gender,
                heightCm = it.heightCm,
                weightKg = it.weightKg,
                nickname = normalizedNickname
            )
        }
        Result.success(remoteSession.copy(profile = profile ?: remoteSession.profile))
    } catch (error: Throwable) {
        val remoteError = error.toRemoteError()
        if (remoteError?.code == DEVICE_LOGIN_CONFLICT) {
            Result.failure(DeviceLoginConflictException(remoteError.activeDeviceName))
        } else {
            if (remoteError?.isInvalidDevice == true) {
                preferences.clearActiveSession()
            }
            Result.failure(error)
        }
    }

    override suspend fun validateActiveSessionDevice(): Result<Unit> {
        return try {
            val session = preferences.activeSession.first() ?: return Result.success(Unit)
            if (session.provider != AuthProvider.GOOGLE) return Result.success(Unit)
            val deviceId = preferences.installationDeviceId()
            sessionNetworkApi.validateSessionDevice(session.id.value, deviceId)
            Result.success(Unit)
        } catch (error: Throwable) {
            val remoteError = error.toRemoteError()
            if (remoteError?.isInvalidDevice == true) {
                preferences.clearActiveSession()
            }
            Result.failure(error)
        }
    }

    override suspend fun setTrainingExperience(experience: TrainingExperience): Result<Unit> = runCatching {
        val sessionId = preferences.activeSessionId.first() ?: DEFAULT_USER_SESSION_ID
        preferences.setTrainingExperience(sessionId, experience)
    }

    override suspend fun updateBodyProfile(
        gender: ProfileGender?,
        heightCm: Int,
        weightKg: Double,
        nickname: String?
    ): Result<Unit> = try {
        val sessionId = preferences.activeSessionId.first() ?: DEFAULT_USER_SESSION_ID
        val normalizedNickname = nickname?.trim()?.takeIf { it.isNotEmpty() }
        val profile = preferences.updateBodyProfile(
            sessionId = sessionId,
            gender = gender,
            heightCm = heightCm,
            weightKg = weightKg,
            nickname = normalizedNickname
        )
        val measurement = profile.latestBodyMeasurement
        runCatching {
            sessionNetworkApi.updateSessionProfile(
                sessionId,
                SessionProfileRequest(
                    nickname = normalizedNickname,
                    gender = profile.gender?.toNetworkValue(),
                    recordedDate = measurement?.recordedDate?.toString(),
                    heightCm = heightCm,
                    weightKg = weightKg
                )
            )
        }.onFailure { error ->
            val remoteError = error.toRemoteError()
            if (remoteError?.isInvalidDevice == true) {
                preferences.clearActiveSession()
            }
        }
        Result.success(Unit)
    } catch (error: Throwable) {
        Result.failure(error)
    }

    override suspend fun syncPendingTrainingData(): Result<Unit> {
        return try {
            val session = preferences.activeSession.first() ?: return Result.success(Unit)
            if (session.provider != AuthProvider.GOOGLE) return Result.success(Unit)
            val measurement = session.profile.latestBodyMeasurement ?: return Result.success(Unit)
            sessionNetworkApi.updateSessionProfile(
                session.id.value,
                SessionProfileRequest(
                    nickname = session.nickname,
                    gender = session.profile.gender?.toNetworkValue(),
                    recordedDate = measurement.recordedDate.toString(),
                    heightCm = measurement.heightCm,
                    weightKg = measurement.weightKg
                )
            )
            Result.success(Unit)
        } catch (error: Throwable) {
            val remoteError = error.toRemoteError()
            if (remoteError?.isInvalidDevice == true) {
                preferences.clearActiveSession()
            }
            Result.failure(error)
        }
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
            linkedAt = linkedAt,
            profile = profile.toUserProfile()
        )

    private fun ProfileSetup.toSessionProfileRequest(nickname: String? = null): SessionProfileRequest =
        SessionProfileRequest(
            nickname = nickname,
            gender = gender.toNetworkValue(),
            heightCm = heightCm,
            weightKg = weightKg
        )

    private fun UserProfileDto.toUserProfile(): UserProfile =
        UserProfile(
            gender = gender.toProfileGenderOrNull(),
            bodyMeasurements = bodyMeasurements.mapNotNull { it.toBodyMeasurementOrNull() }
        )

    private fun BodyMeasurementDto.toBodyMeasurementOrNull(): BodyMeasurement? =
        runCatching {
            BodyMeasurement(
                recordedDate = LocalDate.parse(recordedDate),
                heightCm = heightCm,
                weightKg = weightKg
            )
        }.getOrNull()

    private fun String?.toProfileGenderOrNull(): ProfileGender? = when (this?.lowercase()) {
        "male" -> ProfileGender.MALE
        "female" -> ProfileGender.FEMALE
        else -> null
    }

    private fun ProfileGender.toNetworkValue(): String = when (this) {
        ProfileGender.MALE -> "male"
        ProfileGender.FEMALE -> "female"
    }

    private fun deviceDisplayName(): String =
        listOf(Build.MANUFACTURER, Build.MODEL)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .joinToString(" ")
            .ifBlank { "Android device" }

    private fun Throwable.toRemoteError(): RemoteError? {
        val body = (this as? HttpException)?.response()?.errorBody()?.string() ?: return null
        val root = runCatching { Json.parseToJsonElement(body).jsonObject }.getOrNull() ?: return null
        val code = root["code"]?.jsonPrimitive?.contentOrNull
        val activeDeviceName = runCatching {
            root["details"]?.jsonObject?.get("activeDeviceName")?.jsonPrimitive?.contentOrNull
        }.getOrNull()
        return RemoteError(
            code = code,
            activeDeviceName = activeDeviceName
        )
    }

    private data class RemoteError(
        val code: String?,
        val activeDeviceName: String?
    ) {
        val isInvalidDevice: Boolean
            get() = code == DEVICE_SESSION_REPLACED || code == DEVICE_REQUIRED
    }

    private companion object {
        const val DEVICE_LOGIN_CONFLICT = "DEVICE_LOGIN_CONFLICT"
        const val DEVICE_REQUIRED = "DEVICE_REQUIRED"
        const val DEVICE_SESSION_REPLACED = "DEVICE_SESSION_REPLACED"
    }
}
