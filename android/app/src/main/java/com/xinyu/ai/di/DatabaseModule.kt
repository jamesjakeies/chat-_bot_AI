package com.xinyu.ai.di

import android.content.Context
import androidx.room.Room
import com.xinyu.ai.core.database.LocalChatMessageDao
import com.xinyu.ai.core.database.LocalMemoryDao
import com.xinyu.ai.core.database.XinyuDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): XinyuDatabase {
        return Room.databaseBuilder(
            context,
            XinyuDatabase::class.java,
            "xinyu.db",
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideLocalChatMessageDao(database: XinyuDatabase): LocalChatMessageDao {
        return database.localChatMessageDao()
    }

    @Provides
    fun provideLocalMemoryDao(database: XinyuDatabase): LocalMemoryDao {
        return database.localMemoryDao()
    }
}
