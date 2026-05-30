package com.smarttrainner.feature.workout.entry.di

import com.smarttrainner.feature.workout.api.WorkoutRecordingFeatureEntry
import com.smarttrainner.feature.workout.impl.WorkoutFeatureEntryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class WorkoutFeatureEntryModule {
    @Binds
    abstract fun bindWorkoutRecordingFeatureEntry(impl: WorkoutFeatureEntryImpl): WorkoutRecordingFeatureEntry
}
