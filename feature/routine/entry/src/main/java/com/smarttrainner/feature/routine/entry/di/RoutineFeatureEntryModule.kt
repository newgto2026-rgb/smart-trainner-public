package com.smarttrainner.feature.routine.entry.di

import com.smarttrainner.feature.routine.api.RoutineFeatureEntry
import com.smarttrainner.feature.routine.impl.RoutineFeatureEntryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RoutineFeatureEntryModule {
    @Binds
    abstract fun bindRoutineFeatureEntry(impl: RoutineFeatureEntryImpl): RoutineFeatureEntry
}
