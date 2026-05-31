package com.smarttrainner.feature.exercise.domain

import com.smarttrainner.core.domain.ExerciseRepository
import com.smarttrainner.core.model.ExerciseId
import javax.inject.Inject

class GetExerciseUseCase @Inject constructor(
    private val repository: ExerciseRepository
) {
    suspend operator fun invoke(id: ExerciseId) = repository.getExercise(id)
}
