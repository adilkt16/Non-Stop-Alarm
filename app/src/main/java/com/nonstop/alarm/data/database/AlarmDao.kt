package com.nonstop.alarm.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.nonstop.alarm.data.model.Alarm
import com.nonstop.alarm.data.model.AlarmStatus
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Alarm operations
 * Supports time-bounded operations and alarm state management
 * following NonStopAlarm behavioral requirements
 */
@Dao
interface AlarmDao {
    
    // ================== QUERIES ==================
    
    /**
     * Get all alarms ordered by start time
     */
    @Query("SELECT * FROM alarms ORDER BY startTime ASC")
    fun getAllAlarms(): Flow<List<Alarm>>
    
    /**
     * Get all alarms as LiveData for UI observation
     */
    @Query("SELECT * FROM alarms ORDER BY startTime ASC")
    fun getAllAlarmsLiveData(): LiveData<List<Alarm>>
    
    /**
     * Get alarm by ID
     */
    @Query("SELECT * FROM alarms WHERE id = :alarmId")
    suspend fun getAlarmById(alarmId: Long): Alarm?
    
    /**
     * Get currently active alarm (should be only one at a time)
     * Critical for non-stop alarm mechanism
     */
    @Query("SELECT * FROM alarms WHERE status = 'ACTIVE' LIMIT 1")
    suspend fun getActiveAlarm(): Alarm?
    
    /**
     * Get active alarm as Flow for real-time updates
     */
    @Query("SELECT * FROM alarms WHERE status = 'ACTIVE' LIMIT 1")
    fun getActiveAlarmFlow(): Flow<Alarm?>
    
    /**
     * Get scheduled alarms (future alarms)
     */
    @Query("SELECT * FROM alarms WHERE status = 'SCHEDULED' AND startTime > :currentTime ORDER BY startTime ASC")
    suspend fun getScheduledAlarms(currentTime: Long = System.currentTimeMillis()): List<Alarm>
    
    /**
     * Get next scheduled alarm
     */
    @Query("SELECT * FROM alarms WHERE status = 'SCHEDULED' AND startTime > :currentTime ORDER BY startTime ASC LIMIT 1")
    suspend fun getNextScheduledAlarm(currentTime: Long = System.currentTimeMillis()): Alarm?
    
    /**
     * Get alarms that should be active right now but aren't
     * Used for recovery after system restart
     */
    @Query("SELECT * FROM alarms WHERE status = 'SCHEDULED' AND startTime <= :currentTime AND endTime > :currentTime")
    suspend fun getAlarmsThatShouldBeActive(currentTime: Long = System.currentTimeMillis()): List<Alarm>
    
    /**
     * Get expired alarms (past end time but still marked as active/scheduled)
     * Used for cleanup operations
     */
    @Query("SELECT * FROM alarms WHERE (status = 'ACTIVE' OR status = 'SCHEDULED') AND endTime <= :currentTime")
    suspend fun getExpiredAlarms(currentTime: Long = System.currentTimeMillis()): List<Alarm>
    
    /**
     * Get alarms by status
     */
    @Query("SELECT * FROM alarms WHERE status = :status ORDER BY startTime ASC")
    suspend fun getAlarmsByStatus(status: AlarmStatus): List<Alarm>
    
    /**
     * Check if there's already an active alarm
     * Prevents multiple simultaneous alarms
     */
    @Query("SELECT COUNT(*) FROM alarms WHERE status = 'ACTIVE'")
    suspend fun getActiveAlarmCount(): Int
    
    // ================== INSERTS ==================
    
    /**
     * Insert new alarm
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: Alarm): Long
    
    /**
     * Insert multiple alarms
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarms(alarms: List<Alarm>)
    
    // ================== UPDATES ==================
    
    /**
     * Update entire alarm
     */
    @Update
    suspend fun updateAlarm(alarm: Alarm)
    
    /**
     * Update alarm status
     * Critical for state transitions (scheduled -> active -> dismissed/expired)
     */
    @Query("UPDATE alarms SET status = :status, updatedAt = :updatedAt WHERE id = :alarmId")
    suspend fun updateAlarmStatus(alarmId: Long, status: AlarmStatus, updatedAt: Long = System.currentTimeMillis())
    
    /**
     * Update alarm times
     */
    @Query("UPDATE alarms SET startTime = :startTime, endTime = :endTime, updatedAt = :updatedAt WHERE id = :alarmId")
    suspend fun updateAlarmTimes(alarmId: Long, startTime: Long, endTime: Long, updatedAt: Long = System.currentTimeMillis())
    
    /**
     * Mark alarm as active (when alarm starts playing)
     */
    @Query("UPDATE alarms SET status = 'ACTIVE', updatedAt = :updatedAt WHERE id = :alarmId")
    suspend fun markAlarmAsActive(alarmId: Long, updatedAt: Long = System.currentTimeMillis())
    
    /**
     * Mark alarm as dismissed (when math puzzle is solved)
     */
    @Query("UPDATE alarms SET status = 'DISMISSED', updatedAt = :updatedAt WHERE id = :alarmId")
    suspend fun markAlarmAsDismissed(alarmId: Long, updatedAt: Long = System.currentTimeMillis())
    
    /**
     * Mark alarm as expired (when end time is reached)
     */
    @Query("UPDATE alarms SET status = 'EXPIRED', updatedAt = :updatedAt WHERE id = :alarmId")
    suspend fun markAlarmAsExpired(alarmId: Long, updatedAt: Long = System.currentTimeMillis())
    
    /**
     * Cancel alarm (before it starts)
     */
    @Query("UPDATE alarms SET status = 'CANCELLED', updatedAt = :updatedAt WHERE id = :alarmId")
    suspend fun cancelAlarm(alarmId: Long, updatedAt: Long = System.currentTimeMillis())
    
    /**
     * Bulk update expired alarms
     * Used for cleanup operations
     */
    @Query("UPDATE alarms SET status = 'EXPIRED', updatedAt = :updatedAt WHERE (status = 'ACTIVE' OR status = 'SCHEDULED') AND endTime <= :currentTime")
    suspend fun markExpiredAlarmsAsExpired(currentTime: Long = System.currentTimeMillis(), updatedAt: Long = System.currentTimeMillis()): Int
    
    // ================== DELETES ==================
    
    /**
     * Delete alarm by ID
     */
    @Query("DELETE FROM alarms WHERE id = :alarmId")
    suspend fun deleteAlarmById(alarmId: Long)
    
    /**
     * Delete alarm
     */
    @Delete
    suspend fun deleteAlarm(alarm: Alarm)
    
    /**
     * Delete all dismissed and expired alarms
     * Cleanup operation
     */
    @Query("DELETE FROM alarms WHERE status IN ('DISMISSED', 'EXPIRED', 'CANCELLED')")
    suspend fun deleteCompletedAlarms(): Int
    
    /**
     * Delete all alarms
     */
    @Query("DELETE FROM alarms")
    suspend fun deleteAllAlarms()
    
    // ================== UTILITY OPERATIONS ==================
    
    /**
     * Get alarm count
     */
    @Query("SELECT COUNT(*) FROM alarms")
    suspend fun getAlarmCount(): Int
    
    /**
     * Get alarm count by status
     */
    @Query("SELECT COUNT(*) FROM alarms WHERE status = :status")
    suspend fun getAlarmCountByStatus(status: AlarmStatus): Int
}
