package com.smarttrainner.app.di

import com.smarttrainner.app.DefaultNetworkStatusRepository
import com.smarttrainner.core.data.DefaultExerciseRepository
import com.smarttrainner.core.data.DefaultDeviceSessionStore
import com.smarttrainner.core.data.DefaultPushTokenRepository
import com.smarttrainner.core.data.DefaultSessionRepository
import com.smarttrainner.core.data.DefaultCyclePlanRepository
import com.smarttrainner.core.data.DefaultWorkoutLogRepository
import com.smarttrainner.core.datastore.TrainingPreferencesDataSource
import com.smarttrainner.core.domain.ExerciseRepository
import com.smarttrainner.core.domain.DeviceSessionStore
import com.smarttrainner.core.domain.NetworkStatusRepository
import com.smarttrainner.core.domain.PushTokenRepository
import com.smarttrainner.core.domain.SessionRepository
import com.smarttrainner.core.domain.TrainingDataSyncer
import com.smarttrainner.core.domain.CyclePlanRepository
import com.smarttrainner.core.domain.WorkoutLogRepository
import com.smarttrainner.core.designsystem.SmartTrainnerThemeTone
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Module
@InstallIn(SingletonComponent::class)
abstract class CoreRepositoryBindingsModule {
    @Binds
    abstract fun bindExerciseRepository(
        repository: DefaultExerciseRepository
    ): ExerciseRepository

    @Binds
    @IntoSet
    abstract fun bindExerciseTrainingDataSyncer(
        repository: DefaultExerciseRepository
    ): TrainingDataSyncer

    @Binds
    abstract fun bindCyclePlanRepository(
        repository: DefaultCyclePlanRepository
    ): CyclePlanRepository

    @Binds
    abstract fun bindWorkoutLogRepository(
        repository: DefaultWorkoutLogRepository
    ): WorkoutLogRepository

    @Binds
    @IntoSet
    abstract fun bindWorkoutLogTrainingDataSyncer(
        repository: DefaultWorkoutLogRepository
    ): TrainingDataSyncer

    @Binds
    abstract fun bindSessionRepository(
        repository: DefaultSessionRepository
    ): SessionRepository

    @Binds
    abstract fun bindNetworkStatusRepository(
        repository: DefaultNetworkStatusRepository
    ): NetworkStatusRepository

    @Binds
    abstract fun bindPushTokenRepository(
        repository: DefaultPushTokenRepository
    ): PushTokenRepository

    @Binds
    @IntoSet
    abstract fun bindSessionTrainingDataSyncer(
        repository: DefaultSessionRepository
    ): TrainingDataSyncer

    @Binds
    abstract fun bindDeviceSessionStore(
        store: DefaultDeviceSessionStore
    ): DeviceSessionStore
}

class ThemePreferenceStore @Inject constructor(
    private val preferences: TrainingPreferencesDataSource
) {
    val selectedThemeTone: Flow<SmartTrainnerThemeTone> =
        preferences.selectedThemeTone.map(SmartTrainnerThemeTone::fromStorageValue)

    suspend fun setSelectedThemeTone(themeTone: SmartTrainnerThemeTone) {
        preferences.setSelectedThemeTone(themeTone.storageValue)
    }
}
