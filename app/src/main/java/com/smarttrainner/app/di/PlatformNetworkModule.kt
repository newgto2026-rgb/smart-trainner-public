package com.smarttrainner.app.di

import com.smarttrainner.core.network.SessionNetworkApi
import com.smarttrainner.core.network.SmartTrainnerNetwork
import com.smarttrainner.core.network.WorkoutLogNetworkApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlatformNetworkModule {
    @Provides
    @Singleton
    fun provideSessionNetworkApi(): SessionNetworkApi =
        SmartTrainnerNetwork.createSessionApi()

    @Provides
    @Singleton
    fun provideWorkoutLogNetworkApi(): WorkoutLogNetworkApi =
        SmartTrainnerNetwork.createWorkoutLogApi()
}
