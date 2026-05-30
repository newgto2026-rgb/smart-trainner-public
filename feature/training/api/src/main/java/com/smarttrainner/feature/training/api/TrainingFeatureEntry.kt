package com.smarttrainner.feature.training.api

import androidx.compose.runtime.Composable

interface TrainingFeatureEntry {
    @Composable
    fun Home()

    @Composable
    fun Routine()

    @Composable
    fun Exercises()

    @Composable
    fun Analysis()
}
