package com.nonstop.alarm.data.repository

import androidx.lifecycle.LiveData
import com.nonstop.alarm.data.model.Alarm
import com.nonstop.alarm.data.model.AlarmStatus
import com.nonstop.alarm.data.model.MathPuzzle
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for alarm operations
 * Defines the contract for alarm data access following
 * the NonStopAlarm behavioral requirements
 */
interface AlarmRepository {
    
    // ================== ALARM OPERATIONS ==================
    
    /**
     * Get all alarms as Flow for reactive UI updates
     */
    fun getAllAlarms(): Flow<List<Alarm>>
    
    /**
     * Get all alarms as LiveData for UI observation
     */
    fun getAllAlarmsLiveData(): LiveData<List<Alarm>>
    
    /**
     * Get alarm by ID
     */
    suspend fun getAlarmById(alarmId: Long): Alarm?
    
    /**
     * Get currently active alarm (critical for non-stop mechanism)
     */
    suspend fun getActiveAlarm(): Alarm?
    
    /**
     * Get active alarm as Flow for real-time updates
     */
    fun getActiveAlarmFlow(): Flow<Alarm?>
    
    /**
     * Get next scheduled alarm
     */
    suspend fun getNextScheduledAlarm(): Alarm?
    
    /**
     * Create new alarm with validation
     */
    suspend fun createAlarm(startTime: Long, endTime: Long, label: String = "NonStop Alarm"): Result<Long>
    
    /**
     * Update existing alarm
     */
    suspend fun updateAlarm(alarm: Alarm): Result<Unit>
    
    /**
     * Cancel alarm (before it starts)
     */
    suspend fun cancelAlarm(alarmId: Long): Result<Unit>
    
    /**
     * Delete alarm
     */
    suspend fun deleteAlarm(alarmId: Long): Result<Unit>
    
    // ================== ALARM STATE MANAGEMENT ==================
    
    /**
     * Activate alarm (when start time is reached)
     */
    suspend fun activateAlarm(alarmId: Long): Result<Unit>
    
    /**
     * Dismiss alarm (when math puzzle is solved)
     */
    suspend fun dismissAlarm(alarmId: Long): Result<Unit>
    
    /**
     * Expire alarm (when end time is reached)
     */
    suspend fun expireAlarm(alarmId: Long): Result<Unit>
    
    /**
     * Check for alarms that should be active or expired
     * Used for recovery operations after system restart
     */
    suspend fun performStateRecovery(): Result<Unit>
    
    /**
     * Cleanup old completed alarms
     */
    suspend fun cleanupOldAlarms(): Result<Int>
    
    // ================== VALIDATION OPERATIONS ==================
    
    /**
     * Validate alarm times according to requirements
     */
    suspend fun validateAlarmTimes(startTime: Long, endTime: Long): Result<Unit>
    
    /**
     * Check if alarm can be created (no conflicts)
     */
    suspend fun canCreateAlarm(startTime: Long, endTime: Long): Result<Boolean>
    
    /**
     * Check if there's already an active alarm
     */
    suspend fun hasActiveAlarm(): Boolean
}

/**
 * Repository interface for math puzzle operations
 * Supports the cognitive engagement behavioral requirement
 */
interface MathPuzzleRepository {
    
    /**
     * Generate new math puzzle
     */
    suspend fun generatePuzzle(): Result<MathPuzzle>
    
    /**
     * Get puzzle by ID
     */
    suspend fun getPuzzleById(puzzleId: Long): MathPuzzle?
    
    /**
     * Validate puzzle answer
     */
    suspend fun validateAnswer(puzzleId: Long, answer: Int): Result<Boolean>
    
    /**
     * Increment puzzle attempt count
     */
    suspend fun incrementAttempts(puzzleId: Long): Result<Unit>
    
    /**
     * Clean up old puzzles
     */
    suspend fun cleanupOldPuzzles(): Result<Int>
}
