package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CustomRuleEntity::class, FilteredMessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TextSentryDatabase : RoomDatabase() {
    abstract fun customRuleDao(): CustomRuleDao
    abstract fun filteredMessageDao(): FilteredMessageDao

    companion object {
        @Volatile
        private var INSTANCE: TextSentryDatabase? = null

        fun getInstance(context: Context): TextSentryDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TextSentryDatabase::class.java,
                    "textsentry_local.db"
                ).fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
