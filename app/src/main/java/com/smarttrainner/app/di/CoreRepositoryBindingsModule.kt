package com.smarttrainner.app.di

import com.smarttrainner.core.data.DefaultExerciseRepository
import com.smarttrainner.core.data.DefaultRoutinePlanRepository
import com.smarttrainner.core.data.DefaultRoutineProgressRepository
import com.smarttrainner.core.data.DefaultSessionRepository
import com.smarttrainner.core.data.DefaultWeeklySummaryRepository
import com.smarttrainner.core.data.DefaultWorkoutLogRepository
import com.smarttrainner.core.domain.ExerciseRepository
import com.smarttrainner.core.domain.RoutinePlanRepository
import com.smarttrainner.core.domain.RoutineProgressRepository
import com.smarttrainner.core.domain.SessionRepository
import com.smarttrainner.core.domain.WeeklySummaryCalculator
import com.smarttrainner.core.domain.WeeklySummaryRepository
import com.smarttrainner.core.domain.WorkoutLogRepository
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
    abstract fun bindRoutinePlanRepository(
        repository: DefaultRoutinePlanRepository
    ): RoutinePlanRepository

    @Binds
    abstract fun bindRoutineProgressRepository(
        repository: DefaultRoutineProgressRepository
    ): RoutineProgressRepository

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
