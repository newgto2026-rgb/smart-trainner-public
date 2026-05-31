package com.smarttrainner.app.di

import com.smarttrainner.feature.analysis.data.DefaultWeeklySummaryRepository
import com.smarttrainner.feature.analysis.domain.WeeklySummaryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalysisDataRepositoryBindingsModule {
    @Binds
    abstract fun bindWeeklySummaryRepository(
        repository: DefaultWeeklySummaryRepository
    ): WeeklySummaryRepository
}
