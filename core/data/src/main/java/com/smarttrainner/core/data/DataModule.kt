package com.smarttrainner.core.data

import com.smarttrainner.core.domain.TrainingRepository
import com.smarttrainner.core.domain.SessionRepository
import com.smarttrainner.core.domain.WeeklySummaryCalculator
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindTrainingRepository(
        repository: DefaultTrainingRepository
    ): TrainingRepository

    @Binds
    abstract fun bindSessionRepository(
        repository: DefaultSessionRepository
    ): SessionRepository

    companion object {
        @Provides
        fun provideWeeklySummaryCalculator(): WeeklySummaryCalculator = WeeklySummaryCalculator()
    }
}
