package com.smarttrainner.feature.analysis.entry.di

import com.smarttrainner.feature.analysis.api.AnalysisFeatureEntry
import com.smarttrainner.feature.analysis.impl.AnalysisFeatureEntryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalysisFeatureEntryModule {
    @Binds
    abstract fun bindAnalysisFeatureEntry(impl: AnalysisFeatureEntryImpl): AnalysisFeatureEntry
}
