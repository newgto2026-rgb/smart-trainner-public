package com.smarttrainner.app.di

import com.smarttrainner.feature.analysis.data.DefaultCycleSummaryRepository
import com.smarttrainner.feature.analysis.domain.CycleSummaryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalysisDataRepositoryBindingsModule {
    @Binds
    abstract fun bindCycleSummaryRepository(
        repository: DefaultCycleSummaryRepository
    ): CycleSummaryRepository
}
