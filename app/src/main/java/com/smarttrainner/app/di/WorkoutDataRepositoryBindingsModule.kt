package com.smarttrainner.app.di

import com.smarttrainner.feature.workout.data.DefaultWorkoutRecordingRepository
import com.smarttrainner.feature.workout.domain.WorkoutRecordingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class WorkoutDataRepositoryBindingsModule {
    @Binds
    abstract fun bindWorkoutRecordingRepository(
        repository: DefaultWorkoutRecordingRepository
    ): WorkoutRecordingRepository
}
