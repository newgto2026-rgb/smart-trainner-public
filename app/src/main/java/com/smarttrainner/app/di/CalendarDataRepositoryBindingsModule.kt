package com.smarttrainner.app.di

import com.smarttrainner.feature.calendar.data.DefaultCalendarPreferencesRepository
import com.smarttrainner.feature.calendar.domain.CalendarPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class CalendarDataRepositoryBindingsModule {
    @Binds
    abstract fun bindCalendarPreferencesRepository(
        repository: DefaultCalendarPreferencesRepository
    ): CalendarPreferencesRepository
}
