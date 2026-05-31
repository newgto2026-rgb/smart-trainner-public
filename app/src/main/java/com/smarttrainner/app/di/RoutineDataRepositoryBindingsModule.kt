package com.smarttrainner.app.di

import com.smarttrainner.feature.routine.data.DefaultRoutinePlanRepository
import com.smarttrainner.feature.routine.data.DefaultRoutineProgressRepository
import com.smarttrainner.feature.routine.domain.RoutinePlanCatalogRepository
import com.smarttrainner.feature.routine.domain.RoutinePlanCommandRepository
import com.smarttrainner.feature.routine.domain.RoutineProgressCommandRepository
import com.smarttrainner.feature.routine.domain.RoutineProgressRepository
import dagger.Binds
import dagger.Module
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
    abstract fun bindRoutineProgressRepository(
        repository: DefaultRoutineProgressRepository
    ): RoutineProgressRepository

    @Binds
    abstract fun bindRoutineProgressCommandRepository(
        repository: DefaultRoutineProgressRepository
    ): RoutineProgressCommandRepository
}
