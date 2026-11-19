package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomRuleDao {
    @Query("SELECT * FROM custom_rules ORDER BY createdAt DESC")
    fun observeAllRules(): Flow<List<CustomRuleEntity>>

    @Query("SELECT * FROM custom_rules WHERE isEnabled = 1 ORDER BY createdAt DESC")
    suspend fun getEnabledRulesSnapshot(): List<CustomRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: CustomRuleEntity): Long

    @Query("UPDATE custom_rules SET isEnabled = :enabled WHERE id = :id")
    suspend fun setRuleEnabled(id: Long, enabled: Boolean)

    @Query("UPDATE custom_rules SET hitCount = hitCount + 1 WHERE id = :id")
    suspend fun incrementHitCount(id: Long)

    @Query("DELETE FROM custom_rules WHERE id = :id")
    suspend fun deleteRuleById(id: Long)
}

@Dao
interface FilteredMessageDao {
    @Query("SELECT * FROM filtered_messages ORDER BY timestamp DESC LIMIT 100")
    fun observeRecentMessages(): Flow<List<FilteredMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: FilteredMessageEntity): Long

    @Query("DELETE FROM filtered_messages WHERE id = :id")
    suspend fun deleteMessageById(id: Long)

    @Query("DELETE FROM filtered_messages")
    suspend fun clearAllMessages()
}
