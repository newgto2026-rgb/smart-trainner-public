package com.smarttrainner.app.di

import com.smarttrainner.core.data.DefaultExerciseRepository
import com.smarttrainner.core.data.DefaultSessionRepository
import com.smarttrainner.core.data.DefaultWeeklyPlanRepository
import com.smarttrainner.core.data.DefaultWorkoutLogRepository
import com.smarttrainner.core.datastore.TrainingPreferencesDataSource
import com.smarttrainner.core.domain.ExerciseRepository
import com.smarttrainner.core.domain.SessionRepository
import com.smarttrainner.core.domain.WeeklyPlanRepository
import com.smarttrainner.core.domain.WorkoutLogRepository
import com.smarttrainner.core.designsystem.SmartTrainnerThemeTone
import dagger.Binds
import dagger.Module
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
    abstract fun bindWeeklyPlanRepository(
        repository: DefaultWeeklyPlanRepository
    ): WeeklyPlanRepository

    @Binds
    abstract fun bindWorkoutLogRepository(
        repository: DefaultWorkoutLogRepository
    ): WorkoutLogRepository

    @Binds
    abstract fun bindSessionRepository(
        repository: DefaultSessionRepository
    ): SessionRepository
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
