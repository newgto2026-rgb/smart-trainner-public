package com.smarttrainner.core.domain

import com.smarttrainner.core.model.ProfileGender
import com.smarttrainner.core.model.ProfileSetup
import com.smarttrainner.core.model.TrainingExperience
import javax.inject.Inject

class ObserveActiveSessionUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    operator fun invoke() = repository.observeActiveSession()
}

class ObserveTrainingExperienceUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    operator fun invoke() = repository.observeTrainingExperience()
}

class ObserveNetworkOnlineUseCase @Inject constructor(
    private val repository: NetworkStatusRepository
) {
    operator fun invoke() = repository.observeOnline()
}

class SyncPendingTrainingDataUseCase @Inject constructor(
    private val syncers: Set<@JvmSuppressWildcards TrainingDataSyncer>
) {
    suspend operator fun invoke(): Result<Unit> {
        var firstFailure: Throwable? = null
        syncers.forEach { syncer ->
            syncer.syncPendingTrainingData().onFailure { error ->
                if (firstFailure == null) {
                    firstFailure = error
                }
            }
        }
        return firstFailure?.let { Result.failure(it) } ?: Result.success(Unit)
    }
}

class StartDefaultSessionUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    suspend operator fun invoke(nickname: String, profileSetup: ProfileSetup) =
        repository.startDefaultSession(nickname, profileSetup)
}

class CheckNicknameAvailabilityUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    suspend operator fun invoke(nickname: String) = repository.checkNicknameAvailability(nickname)
}

class SignInWithGoogleUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    suspend operator fun invoke(
        idToken: String,
        nickname: String? = null,
        profileSetup: ProfileSetup?,
        forceDeviceLogin: Boolean = false
    ) = repository.signInWithGoogle(idToken, nickname, profileSetup, forceDeviceLogin)
}

class ValidateActiveSessionDeviceUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    suspend operator fun invoke() = repository.validateActiveSessionDevice()
}

class SetTrainingExperienceUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    suspend operator fun invoke(experience: TrainingExperience) =
        repository.setTrainingExperience(experience)
}

class UpdateBodyProfileUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    suspend operator fun invoke(
        gender: ProfileGender?,
        heightCm: Int,
        weightKg: Double,
        nickname: String? = null
    ) = repository.updateBodyProfile(gender, heightCm, weightKg, nickname)
}

class LogoutUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    suspend operator fun invoke() = repository.logout()
}
