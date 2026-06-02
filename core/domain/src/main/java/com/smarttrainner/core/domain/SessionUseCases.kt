package com.smarttrainner.core.domain

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

class StartDefaultSessionUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    suspend operator fun invoke() = repository.startDefaultSession()
}

class CheckNicknameAvailabilityUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    suspend operator fun invoke(nickname: String) = repository.checkNicknameAvailability(nickname)
}

class SignInWithGoogleUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    suspend operator fun invoke(idToken: String, nickname: String) =
        repository.signInWithGoogle(idToken, nickname)
}

class SetTrainingExperienceUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    suspend operator fun invoke(experience: TrainingExperience) =
        repository.setTrainingExperience(experience)
}

class LogoutUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    suspend operator fun invoke() = repository.logout()
}
