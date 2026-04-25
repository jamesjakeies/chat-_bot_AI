package com.xinyu.ai.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalMemoryDao {
    @Query("SELECT * FROM memories_cache ORDER BY createdAt DESC")
    fun observeMemories(): Flow<List<LocalMemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(memories: List<LocalMemoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(memory: LocalMemoryEntity)

    @Query("DELETE FROM memories_cache")
    suspend fun clearAll()

    @Query("DELETE FROM memories_cache WHERE id = :memoryId")
    suspend fun delete(memoryId: String)
}
