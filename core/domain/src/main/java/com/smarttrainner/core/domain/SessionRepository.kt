package com.smarttrainner.core.domain

import com.smarttrainner.core.model.NicknameAvailability
import com.smarttrainner.core.model.ProfileGender
import com.smarttrainner.core.model.ProfileSetup
import com.smarttrainner.core.model.TrainingExperience
import com.smarttrainner.core.model.UserSession
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun observeActiveSession(): Flow<UserSession?>
    fun observeTrainingExperience(): Flow<TrainingExperience>
    suspend fun startDefaultSession(nickname: String, profileSetup: ProfileSetup): Result<UserSession>
    suspend fun checkNicknameAvailability(nickname: String): Result<NicknameAvailability>
    suspend fun signInWithGoogle(
        idToken: String,
        nickname: String? = null,
        profileSetup: ProfileSetup?,
        forceDeviceLogin: Boolean = false
    ): Result<UserSession>
    suspend fun validateActiveSessionDevice(): Result<Unit>
    suspend fun setTrainingExperience(experience: TrainingExperience): Result<Unit>
    suspend fun updateBodyProfile(
        gender: ProfileGender?,
        heightCm: Int,
        weightKg: Double,
        nickname: String? = null
    ): Result<Unit>
    suspend fun logout(): Result<Unit>
}

interface NetworkStatusRepository {
    fun observeOnline(): Flow<Boolean>
}

interface TrainingDataSyncer {
    suspend fun syncPendingTrainingData(): Result<Unit>
}
