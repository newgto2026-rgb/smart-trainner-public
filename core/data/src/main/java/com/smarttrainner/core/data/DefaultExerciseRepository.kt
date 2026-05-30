package com.smarttrainner.core.data

import com.smarttrainner.core.domain.ExerciseRepository
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Singleton
class DefaultExerciseRepository @Inject constructor(
    private val seedStore: TrainingSeedStore
) : ExerciseRepository {
    override fun observeExercises(): Flow<List<Exercise>> = flowOf(seedStore.exercises)

    override suspend fun getExercise(id: ExerciseId): Exercise? = seedStore.exercise(id)
}
