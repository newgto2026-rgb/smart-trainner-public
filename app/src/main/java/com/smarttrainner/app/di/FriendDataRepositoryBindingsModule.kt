package com.smarttrainner.app.di

import com.smarttrainner.feature.friend.data.DefaultFriendRepository
import com.smarttrainner.feature.friend.domain.FriendRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class FriendDataRepositoryBindingsModule {
    @Binds
    abstract fun bindFriendRepository(
        repository: DefaultFriendRepository
    ): FriendRepository
}
