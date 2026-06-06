package com.smarttrainner.core.model

import java.time.LocalDate

enum class AuthProvider {
    LOCAL,
    GOOGLE
}

data class UserSession(
    val id: UserSessionId,
    val displayName: String,
    val nickname: String = displayName,
    val email: String?,
    val provider: AuthProvider,
    val linkedAt: String?,
    val profile: UserProfile = UserProfile()
) {
    val isLinked: Boolean
        get() = provider != AuthProvider.LOCAL
}

enum class ProfileGender {
    MALE,
    FEMALE
}

data class BodyMeasurement(
    val recordedDate: LocalDate,
    val heightCm: Int,
    val weightKg: Double
)

data class UserProfile(
    val gender: ProfileGender? = null,
    val bodyMeasurements: List<BodyMeasurement> = emptyList()
) {
    val latestBodyMeasurement: BodyMeasurement?
        get() = bodyMeasurements.maxByOrNull { it.recordedDate }
}

data class ProfileSetup(
    val gender: ProfileGender,
    val heightCm: Int,
    val weightKg: Double
)

fun UserProfile.toProfileSetupOrNull(): ProfileSetup? {
    val gender = gender ?: return null
    val measurement = latestBodyMeasurement ?: return null
    return ProfileSetup(
        gender = gender,
        heightCm = measurement.heightCm,
        weightKg = measurement.weightKg
    )
}

data class NicknameAvailability(
    val nickname: String,
    val available: Boolean
)
