package com.smarttrainner.app.di

import android.content.Context
import androidx.room.Room
import com.smarttrainner.core.database.CustomRoutineDao
import com.smarttrainner.core.database.FriendDao
import com.smarttrainner.core.database.SmartTrainnerMigrations
import com.smarttrainner.core.database.SmartTrainnerDatabase
import com.smarttrainner.core.database.WorkoutLogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlatformDatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): SmartTrainnerDatabase = Room.databaseBuilder(
        context,
        SmartTrainnerDatabase::class.java,
        "smart_trainner.db"
    ).addMigrations(*SmartTrainnerMigrations.ALL).build()

    @Provides
    fun provideWorkoutLogDao(database: SmartTrainnerDatabase): WorkoutLogDao =
        database.workoutLogDao()

    @Provides
    fun provideCustomRoutineDao(database: SmartTrainnerDatabase): CustomRoutineDao =
        database.customRoutineDao()

    @Provides
    fun provideFriendDao(database: SmartTrainnerDatabase): FriendDao =
        database.friendDao()
}
