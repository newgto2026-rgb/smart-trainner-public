package com.smarttrainner.feature.training.entry.di

import com.smarttrainner.feature.training.api.TrainingFeatureEntry
import com.smarttrainner.feature.training.impl.TrainingFeatureEntryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TrainingFeatureEntryModule {
    @Binds
    abstract fun bindTrainingFeatureEntry(impl: TrainingFeatureEntryImpl): TrainingFeatureEntry
}
