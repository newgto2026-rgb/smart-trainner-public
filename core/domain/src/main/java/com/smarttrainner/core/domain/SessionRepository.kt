package com.smarttrainner.core.domain

import com.smarttrainner.core.model.NicknameAvailability
import com.smarttrainner.core.model.TrainingExperience
import com.smarttrainner.core.model.UserSession
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun observeActiveSession(): Flow<UserSession?>
    fun observeTrainingExperience(): Flow<TrainingExperience>
    suspend fun startDefaultSession(): Result<UserSession>
    suspend fun checkNicknameAvailability(nickname: String): Result<NicknameAvailability>
    suspend fun signInWithGoogle(idToken: String, nickname: String): Result<UserSession>
    suspend fun setTrainingExperience(experience: TrainingExperience): Result<Unit>
    suspend fun logout(): Result<Unit>
}
