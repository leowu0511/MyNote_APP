package com.MyNote.leowu.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Note::class],
    version = 2,
    exportSchema = false
)
abstract class NoteDatabase : RoomDatabase() {
    
    abstract fun noteDao(): NoteDao
    
    companion object {
        @Volatile
        private var INSTANCE: NoteDatabase? = null
        
        // 定義從版本1到版本2的遷移
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 創建新表
                database.execSQL(
                    "CREATE TABLE notes_new (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "title TEXT NOT NULL DEFAULT '', " +
                        "content TEXT NOT NULL DEFAULT '', " +
                        "timestamp INTEGER NOT NULL DEFAULT 0)"
                )
                
                // 將舊資料複製到新表，text欄位的資料移到content欄位
                database.execSQL(
                    "INSERT INTO notes_new (id, content, timestamp) " +
                        "SELECT id, text, timestamp FROM notes"
                )
                
                // 刪除舊表
                database.execSQL("DROP TABLE notes")
                
                // 重命名新表為notes
                database.execSQL("ALTER TABLE notes_new RENAME TO notes")
            }
        }
        
        fun getDatabase(context: Context): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "note_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 