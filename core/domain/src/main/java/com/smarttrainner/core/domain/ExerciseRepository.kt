package com.smarttrainner.core.domain

import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.CustomExerciseInput
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun observeExercises(): Flow<List<Exercise>>
    suspend fun getExercise(id: ExerciseId): Exercise?

    suspend fun saveCustomExercise(input: CustomExerciseInput): Result<Exercise> =
        Result.failure(UnsupportedOperationException("Custom exercise saving is not supported"))

    suspend fun archiveCustomExercise(id: ExerciseId): Result<Unit> =
        Result.failure(UnsupportedOperationException("Custom exercise archiving is not supported"))
}
