package com.smarttrainner.feature.exercise.entry.di

import com.smarttrainner.feature.exercise.api.ExerciseDetailFeatureEntry
import com.smarttrainner.feature.exercise.api.ExerciseMediaFeatureEntry
import com.smarttrainner.feature.exercise.impl.ExerciseFeatureEntryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ExerciseFeatureEntryModule {
    @Binds
    abstract fun bindExerciseDetailFeatureEntry(impl: ExerciseFeatureEntryImpl): ExerciseDetailFeatureEntry

    @Binds
    abstract fun bindExerciseMediaFeatureEntry(impl: ExerciseFeatureEntryImpl): ExerciseMediaFeatureEntry
}
