package com.xinyu.ai.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalChatMessageDao {
    @Query("SELECT * FROM chat_messages_cache WHERE roleId = :roleId ORDER BY createdAt ASC")
    fun observeMessages(roleId: String): Flow<List<LocalChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(messages: List<LocalChatMessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(message: LocalChatMessageEntity)

    @Query("DELETE FROM chat_messages_cache WHERE roleId = :roleId")
    suspend fun clearRole(roleId: String)

    @Query("DELETE FROM chat_messages_cache WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("UPDATE chat_messages_cache SET isRememberCandidate = :remember WHERE id = :messageId")
    suspend fun updateRememberCandidate(messageId: String, remember: Boolean)
}
