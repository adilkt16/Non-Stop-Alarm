package com.nonstop.alarm.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.nonstop.alarm.data.model.Alarm
import com.nonstop.alarm.data.model.MathPuzzle

/**
 * NonStopAlarm Room Database
 * 
 * Core database supporting the persistent alarm mechanism
 * with time-bounded operations and math puzzle requirements.
 * 
 * Entities:
 * - Alarm: Core alarm configuration and state
 * - MathPuzzle: Puzzle data for dismissal requirement
 * 
 * Features:
 * - Version management with migrations
 * - Type converters for complex objects
 * - Singleton pattern for app-wide access
 * - Support for concurrent access
 */
@Database(
    entities = [
        Alarm::class,
        MathPuzzle::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(DatabaseConverters::class)
abstract class NonStopAlarmDatabase : RoomDatabase() {
    
    // ================== DAOs ==================
    
    abstract fun alarmDao(): AlarmDao
    abstract fun mathPuzzleDao(): MathPuzzleDao
    
    companion object {
        
        private const val DATABASE_NAME = "nonstop_alarm_database"
        
        @Volatile
        private var INSTANCE: NonStopAlarmDatabase? = null
        
        /**
         * Get database instance (Singleton pattern)
         */
        fun getDatabase(context: Context): NonStopAlarmDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NonStopAlarmDatabase::class.java,
                    DATABASE_NAME
                )
                    .addTypeConverters(DatabaseConverters())
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration() // For development - remove in production
                    .build()
                
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * Close database instance (for testing)
         */
        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}

/**
 * Database callback for initialization
 */
private class DatabaseCallback : RoomDatabase.Callback() {
    
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        // Database created - can add initial data here if needed
    }
    
    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        // Database opened - can perform cleanup operations here
        
        // Enable foreign key constraints
        db.execSQL("PRAGMA foreign_keys=ON")
        
        // Clean up old completed alarms (older than 7 days)
        val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        db.execSQL(
            "DELETE FROM alarms WHERE (status = 'DISMISSED' OR status = 'EXPIRED' OR status = 'CANCELLED') AND updatedAt < $sevenDaysAgo"
        )
        
        // Clean up old math puzzles (older than 1 day)
        val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000L)
        db.execSQL("DELETE FROM math_puzzles WHERE generatedAt < $oneDayAgo")
    }
}

/**
 * Migration from version 1 to 2 (example for future use)
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Example migration - add new column
        // database.execSQL("ALTER TABLE alarms ADD COLUMN new_column TEXT")
    }
}
