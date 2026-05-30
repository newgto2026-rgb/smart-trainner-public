package com.smarttrainner.app.di

import com.smarttrainner.core.data.DefaultExerciseRepository
import com.smarttrainner.core.data.DefaultSessionRepository
import com.smarttrainner.core.data.DefaultWeeklySummaryRepository
import com.smarttrainner.core.data.DefaultWorkoutLogRepository
import com.smarttrainner.core.domain.ExerciseRepository
import com.smarttrainner.core.domain.SessionRepository
import com.smarttrainner.core.domain.WeeklySummaryCalculator
import com.smarttrainner.core.domain.WeeklyPlanRepository
import com.smarttrainner.core.domain.WeeklySummaryRepository
import com.smarttrainner.core.domain.WorkoutLogRepository
import com.smarttrainner.feature.routine.data.DefaultRoutinePlanRepository
import com.smarttrainner.feature.routine.data.DefaultRoutineProgressRepository
import com.smarttrainner.feature.routine.domain.RoutinePlanCatalogRepository
import com.smarttrainner.feature.routine.domain.RoutinePlanCommandRepository
import com.smarttrainner.feature.routine.domain.RoutineProgressCommandRepository
import com.smarttrainner.feature.routine.domain.RoutineProgressRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class CoreRepositoryBindingsModule {
    @Binds
    abstract fun bindExerciseRepository(
        repository: DefaultExerciseRepository
    ): ExerciseRepository

    @Binds
    abstract fun bindWeeklyPlanRepository(
        repository: DefaultRoutinePlanRepository
    ): WeeklyPlanRepository

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

    @Binds
    abstract fun bindWorkoutLogRepository(
        repository: DefaultWorkoutLogRepository
    ): WorkoutLogRepository

    @Binds
    abstract fun bindWeeklySummaryRepository(
        repository: DefaultWeeklySummaryRepository
    ): WeeklySummaryRepository

    @Binds
    abstract fun bindSessionRepository(
        repository: DefaultSessionRepository
    ): SessionRepository

    companion object {
        @Provides
        fun provideWeeklySummaryCalculator(): WeeklySummaryCalculator = WeeklySummaryCalculator()
    }
}
