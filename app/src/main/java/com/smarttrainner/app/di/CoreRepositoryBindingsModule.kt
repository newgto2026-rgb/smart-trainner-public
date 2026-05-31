package com.smarttrainner.app.di

import com.smarttrainner.core.data.DefaultExerciseRepository
import com.smarttrainner.core.data.DefaultSessionRepository
import com.smarttrainner.core.data.DefaultWorkoutLogRepository
import com.smarttrainner.core.domain.ExerciseRepository
import com.smarttrainner.core.domain.SessionRepository
import com.smarttrainner.core.domain.WorkoutLogRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class CoreRepositoryBindingsModule {
    @Binds
    abstract fun bindExerciseRepository(
        repository: DefaultExerciseRepository
    ): ExerciseRepository

    @Binds
    abstract fun bindWorkoutLogRepository(
        repository: DefaultWorkoutLogRepository
    ): WorkoutLogRepository

    @Binds
    abstract fun bindSessionRepository(
        repository: DefaultSessionRepository
    ): SessionRepository
}
