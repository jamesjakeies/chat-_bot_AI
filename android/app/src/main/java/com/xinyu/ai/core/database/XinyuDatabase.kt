package com.xinyu.ai.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [LocalChatMessageEntity::class, LocalMemoryEntity::class],
    version = 3,
    exportSchema = false,
)
abstract class XinyuDatabase : RoomDatabase() {
    abstract fun localChatMessageDao(): LocalChatMessageDao

    abstract fun localMemoryDao(): LocalMemoryDao
}
