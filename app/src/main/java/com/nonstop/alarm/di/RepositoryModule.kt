package com.nonstop.alarm.di

import com.nonstop.alarm.data.repository.AlarmRepository
import com.nonstop.alarm.data.repository.AlarmRepositoryImpl
import com.nonstop.alarm.data.repository.MathPuzzleRepository
import com.nonstop.alarm.data.repository.MathPuzzleRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for repository dependencies
 * 
 * Binds repository interfaces to their implementations
 * Supports the data layer architecture for persistent alarm operations
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    /**
     * Bind AlarmRepository interface to implementation
     */
    @Binds
    @Singleton
    abstract fun bindAlarmRepository(
        alarmRepositoryImpl: AlarmRepositoryImpl
    ): AlarmRepository
    
    /**
     * Bind MathPuzzleRepository interface to implementation
     */
    @Binds
    @Singleton
    abstract fun bindMathPuzzleRepository(
        mathPuzzleRepositoryImpl: MathPuzzleRepositoryImpl
    ): MathPuzzleRepository
}
