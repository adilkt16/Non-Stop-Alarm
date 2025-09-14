package com.nonstop.alarm.di

import android.content.Context
import androidx.room.Room
import com.nonstop.alarm.data.database.AlarmDao
import com.nonstop.alarm.data.database.MathPuzzleDao
import com.nonstop.alarm.data.database.NonStopAlarmDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for database dependencies
 * 
 * Provides database instances and DAOs for the NonStopAlarm app
 * Supports the persistent alarm mechanism with proper dependency injection
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    /**
     * Provide NonStopAlarm database instance
     */
    @Provides
    @Singleton
    fun provideNonStopAlarmDatabase(
        @ApplicationContext context: Context
    ): NonStopAlarmDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            NonStopAlarmDatabase::class.java,
            "nonstop_alarm_database"
        )
            .fallbackToDestructiveMigration() // For development - remove in production
            .build()
    }
    
    /**
     * Provide AlarmDao from database
     */
    @Provides
    fun provideAlarmDao(
        database: NonStopAlarmDatabase
    ): AlarmDao = database.alarmDao()
    
    /**
     * Provide MathPuzzleDao from database
     */
    @Provides
    fun provideMathPuzzleDao(
        database: NonStopAlarmDatabase
    ): MathPuzzleDao = database.mathPuzzleDao()
}
