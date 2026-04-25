package com.xinyu.ai.di

import com.xinyu.ai.core.datastore.PreferencesRepositoryImpl
import com.xinyu.ai.data.repository.ApiAuthRepository
import com.xinyu.ai.data.repository.ApiChatRepository
import com.xinyu.ai.data.repository.ApiMemoryRepository
import com.xinyu.ai.data.repository.ApiMoodRepository
import com.xinyu.ai.data.repository.ApiRoleRepository
import com.xinyu.ai.data.repository.ApiSubscriptionRepository
import com.xinyu.ai.data.repository.ApiUserRepository
import com.xinyu.ai.domain.repository.AuthRepository
import com.xinyu.ai.domain.repository.ChatRepository
import com.xinyu.ai.domain.repository.MemoryRepository
import com.xinyu.ai.domain.repository.MoodRepository
import com.xinyu.ai.domain.repository.PreferencesRepository
import com.xinyu.ai.domain.repository.RoleRepository
import com.xinyu.ai.domain.repository.SubscriptionRepository
import com.xinyu.ai.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: ApiAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindRoleRepository(impl: ApiRoleRepository): RoleRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: ApiUserRepository): UserRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(impl: ApiChatRepository): ChatRepository

    @Binds
    @Singleton
    abstract fun bindMemoryRepository(impl: ApiMemoryRepository): MemoryRepository

    @Binds
    @Singleton
    abstract fun bindMoodRepository(impl: ApiMoodRepository): MoodRepository

    @Binds
    @Singleton
    abstract fun bindSubscriptionRepository(impl: ApiSubscriptionRepository): SubscriptionRepository

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository
}
