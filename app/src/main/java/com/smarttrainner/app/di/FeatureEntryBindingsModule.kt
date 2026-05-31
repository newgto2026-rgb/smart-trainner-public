package com.smarttrainner.app.di

import com.smarttrainner.core.exercisemedia.DefaultExerciseMediaRenderer
import com.smarttrainner.core.exercisemedia.ExerciseMediaRenderer
import com.smarttrainner.feature.analysis.api.AnalysisFeatureEntry
import com.smarttrainner.feature.analysis.impl.AnalysisFeatureEntryImpl
import com.smarttrainner.feature.exercise.api.ExerciseCatalogFeatureEntry
import com.smarttrainner.feature.exercise.api.ExerciseDetailFeatureEntry
import com.smarttrainner.feature.exercise.impl.ExerciseFeatureEntryImpl
import com.smarttrainner.feature.routine.api.RoutineFeatureEntry
import com.smarttrainner.feature.routine.impl.RoutineFeatureEntryImpl
import com.smarttrainner.feature.workout.api.WorkoutRecordingFeatureEntry
import com.smarttrainner.feature.workout.impl.WorkoutFeatureEntryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class FeatureEntryBindingsModule {
    @Binds
    abstract fun bindAnalysisFeatureEntry(impl: AnalysisFeatureEntryImpl): AnalysisFeatureEntry

    @Binds
    abstract fun bindExerciseCatalogFeatureEntry(impl: ExerciseFeatureEntryImpl): ExerciseCatalogFeatureEntry

    @Binds
    abstract fun bindExerciseDetailFeatureEntry(impl: ExerciseFeatureEntryImpl): ExerciseDetailFeatureEntry

    @Binds
    abstract fun bindExerciseMediaRenderer(impl: DefaultExerciseMediaRenderer): ExerciseMediaRenderer

    @Binds
    abstract fun bindRoutineFeatureEntry(impl: RoutineFeatureEntryImpl): RoutineFeatureEntry

    @Binds
    abstract fun bindWorkoutRecordingFeatureEntry(impl: WorkoutFeatureEntryImpl): WorkoutRecordingFeatureEntry
}
