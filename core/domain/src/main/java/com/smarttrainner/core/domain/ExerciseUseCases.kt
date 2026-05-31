package com.smarttrainner.core.domain

import javax.inject.Inject

class ObserveExercisesUseCase @Inject constructor(
    private val repository: ExerciseRepository
) {
    operator fun invoke() = repository.observeExercises()
}
