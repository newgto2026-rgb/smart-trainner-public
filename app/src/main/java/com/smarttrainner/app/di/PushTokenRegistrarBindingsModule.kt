package com.smarttrainner.app.di

import com.smarttrainner.app.FirebasePushTokenRegistrar
import com.smarttrainner.app.PushTokenRegistrar
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class PushTokenRegistrarBindingsModule {
    @Binds
    abstract fun bindPushTokenRegistrar(
        registrar: FirebasePushTokenRegistrar
    ): PushTokenRegistrar
}
