package com.smarttrainner.app.di

import com.smarttrainner.core.domain.RoutineProgressRepository
import com.smarttrainner.core.domain.TrainingDataSyncer
import com.smarttrainner.feature.routine.data.DefaultRoutinePlanRepository
import com.smarttrainner.feature.routine.data.DefaultRoutineProgressRepository
import com.smarttrainner.feature.routine.domain.RoutinePlanCatalogRepository
import com.smarttrainner.feature.routine.domain.RoutinePlanCommandRepository
import com.smarttrainner.feature.routine.domain.RoutineProgressCommandRepository
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RoutineDataRepositoryBindingsModule {
    @Binds
    abstract fun bindRoutinePlanCatalogRepository(
        repository: DefaultRoutinePlanRepository
    ): RoutinePlanCatalogRepository

    @Binds
    abstract fun bindRoutinePlanCommandRepository(
        repository: DefaultRoutinePlanRepository
    ): RoutinePlanCommandRepository

    @Binds
    @IntoSet
    abstract fun bindRoutinePlanTrainingDataSyncer(
        repository: DefaultRoutinePlanRepository
    ): TrainingDataSyncer

    @Binds
    abstract fun bindRoutineProgressRepository(
        repository: DefaultRoutineProgressRepository
    ): RoutineProgressRepository

    @Binds
    abstract fun bindRoutineProgressCommandRepository(
        repository: DefaultRoutineProgressRepository
    ): RoutineProgressCommandRepository

    @Binds
    @IntoSet
    abstract fun bindRoutineProgressTrainingDataSyncer(
        repository: DefaultRoutineProgressRepository
    ): TrainingDataSyncer
}
