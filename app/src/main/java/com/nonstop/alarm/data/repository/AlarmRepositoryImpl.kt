package com.nonstop.alarm.data.repository

import androidx.lifecycle.LiveData
import com.nonstop.alarm.data.database.AlarmDao
import com.nonstop.alarm.data.database.MathPuzzleDao
import com.nonstop.alarm.data.model.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Implementation of AlarmRepository
 * 
 * Handles all alarm operations following NonStopAlarm behavioral requirements:
 * - Time-bounded operations with validation
 * - Persistent alarm state management
 * - Recovery operations for system restarts
 * - Cleanup operations for old data
 */
@Singleton
class AlarmRepositoryImpl @Inject constructor(
    private val alarmDao: AlarmDao,
    private val mathPuzzleDao: MathPuzzleDao
) : AlarmRepository {
    
    // ================== ALARM OPERATIONS ==================
    
    override fun getAllAlarms(): Flow<List<Alarm>> = alarmDao.getAllAlarms()
    
    override fun getAllAlarmsLiveData(): LiveData<List<Alarm>> = alarmDao.getAllAlarmsLiveData()
    
    override suspend fun getAlarmById(alarmId: Long): Alarm? = alarmDao.getAlarmById(alarmId)
    
    override suspend fun getActiveAlarm(): Alarm? = alarmDao.getActiveAlarm()
    
    override fun getActiveAlarmFlow(): Flow<Alarm?> = alarmDao.getActiveAlarmFlow()
    
    override suspend fun getNextScheduledAlarm(): Alarm? = alarmDao.getNextScheduledAlarm()
    
    override suspend fun createAlarm(startTime: Long, endTime: Long, label: String): Result<Long> {
        return try {
            // Validate alarm times
            val validation = validateAlarmTimes(startTime, endTime)
            if (validation.isFailure) {
                return Result.failure(validation.exceptionOrNull()!!)
            }
            
            // Check for conflicts
            val canCreate = canCreateAlarm(startTime, endTime)
            if (canCreate.isFailure || canCreate.getOrNull() == false) {
                return Result.failure(Exception("Cannot create alarm - conflict detected"))
            }
            
            // Create alarm
            val alarm = Alarm(
                startTime = startTime,
                endTime = endTime,
                status = AlarmStatus.SCHEDULED,
                label = label,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            val alarmId = alarmDao.insertAlarm(alarm)
            Result.success(alarmId)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateAlarm(alarm: Alarm): Result<Unit> {
        return try {
            val updatedAlarm = alarm.copy(updatedAt = System.currentTimeMillis())
            alarmDao.updateAlarm(updatedAlarm)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun cancelAlarm(alarmId: Long): Result<Unit> {
        return try {
            alarmDao.cancelAlarm(alarmId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteAlarm(alarmId: Long): Result<Unit> {
        return try {
            alarmDao.deleteAlarmById(alarmId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ================== ALARM STATE MANAGEMENT ==================
    
    override suspend fun activateAlarm(alarmId: Long): Result<Unit> {
        return try {
            // Ensure no other alarm is currently active
            val activeAlarm = alarmDao.getActiveAlarm()
            if (activeAlarm != null && activeAlarm.id != alarmId) {
                return Result.failure(Exception("Another alarm is already active"))
            }
            
            alarmDao.markAlarmAsActive(alarmId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun dismissAlarm(alarmId: Long): Result<Unit> {
        return try {
            alarmDao.markAlarmAsDismissed(alarmId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun expireAlarm(alarmId: Long): Result<Unit> {
        return try {
            alarmDao.markAlarmAsExpired(alarmId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun performStateRecovery(): Result<Unit> {
        return try {
            val currentTime = System.currentTimeMillis()
            
            // Find alarms that should be active but aren't
            val shouldBeActive = alarmDao.getAlarmsThatShouldBeActive(currentTime)
            for (alarm in shouldBeActive) {
                alarmDao.markAlarmAsActive(alarm.id)
            }
            
            // Mark expired alarms as expired
            val expiredCount = alarmDao.markExpiredAlarmsAsExpired(currentTime)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun cleanupOldAlarms(): Result<Int> {
        return try {
            val deletedCount = alarmDao.deleteCompletedAlarms()
            Result.success(deletedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ================== VALIDATION OPERATIONS ==================
    
    override suspend fun validateAlarmTimes(startTime: Long, endTime: Long): Result<Unit> {
        return try {
            val currentTime = System.currentTimeMillis()
            
            // Start time cannot be in the past
            if (startTime <= currentTime) {
                return Result.failure(Exception("Start time cannot be in the past"))
            }
            
            // End time must be after start time
            if (endTime <= startTime) {
                return Result.failure(Exception("End time must be after start time"))
            }
            
            // Duration cannot exceed 12 hours (as per requirements)
            val duration = endTime - startTime
            val maxDuration = 12 * 60 * 60 * 1000L // 12 hours
            if (duration > maxDuration) {
                return Result.failure(Exception("Alarm duration cannot exceed 12 hours"))
            }
            
            // Minimum duration should be at least 1 minute
            val minDuration = 60 * 1000L // 1 minute
            if (duration < minDuration) {
                return Result.failure(Exception("Alarm duration must be at least 1 minute"))
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun canCreateAlarm(startTime: Long, endTime: Long): Result<Boolean> {
        return try {
            // Check if there's already an active alarm
            val activeAlarm = alarmDao.getActiveAlarm()
            if (activeAlarm != null) {
                return Result.success(false)
            }
            
            // Check for scheduled alarms that would conflict
            val scheduledAlarms = alarmDao.getScheduledAlarms()
            for (alarm in scheduledAlarms) {
                // Check for time overlap
                if (!(endTime <= alarm.startTime || startTime >= alarm.endTime)) {
                    return Result.success(false)
                }
            }
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun hasActiveAlarm(): Boolean {
        return try {
            alarmDao.getActiveAlarmCount() > 0
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Implementation of MathPuzzleRepository
 * 
 * Handles math puzzle operations for the cognitive engagement requirement
 */
@Singleton
class MathPuzzleRepositoryImpl @Inject constructor(
    private val mathPuzzleDao: MathPuzzleDao
) : MathPuzzleRepository {
    
    override suspend fun generatePuzzle(): Result<MathPuzzle> {
        return try {
            val operation = MathOperation.values().random()
            val puzzle = when (operation) {
                MathOperation.ADDITION -> generateAdditionPuzzle()
                MathOperation.SUBTRACTION -> generateSubtractionPuzzle()
                MathOperation.MULTIPLICATION -> generateMultiplicationPuzzle()
            }
            
            val puzzleId = mathPuzzleDao.insertPuzzle(puzzle)
            val savedPuzzle = puzzle.copy(id = puzzleId)
            
            Result.success(savedPuzzle)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getPuzzleById(puzzleId: Long): MathPuzzle? {
        return mathPuzzleDao.getPuzzleById(puzzleId)
    }
    
    override suspend fun validateAnswer(puzzleId: Long, answer: Int): Result<Boolean> {
        return try {
            val puzzle = mathPuzzleDao.getPuzzleById(puzzleId)
            if (puzzle == null) {
                return Result.failure(Exception("Puzzle not found"))
            }
            
            val isCorrect = puzzle.isAnswerCorrect(answer)
            
            // Increment attempts
            mathPuzzleDao.incrementAttempts(puzzleId)
            
            Result.success(isCorrect)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun incrementAttempts(puzzleId: Long): Result<Unit> {
        return try {
            mathPuzzleDao.incrementAttempts(puzzleId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun cleanupOldPuzzles(): Result<Int> {
        return try {
            val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000L)
            val deletedCount = mathPuzzleDao.deleteOldPuzzles(oneDayAgo)
            Result.success(deletedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ================== PUZZLE GENERATION HELPERS ==================
    
    private fun generateAdditionPuzzle(): MathPuzzle {
        val operand1 = Random.nextInt(10, 100)
        val operand2 = Random.nextInt(10, 100)
        val answer = operand1 + operand2
        
        return MathPuzzle(
            operand1 = operand1,
            operand2 = operand2,
            operation = MathOperation.ADDITION,
            correctAnswer = answer
        )
    }
    
    private fun generateSubtractionPuzzle(): MathPuzzle {
        val operand1 = Random.nextInt(50, 200)
        val operand2 = Random.nextInt(10, operand1)
        val answer = operand1 - operand2
        
        return MathPuzzle(
            operand1 = operand1,
            operand2 = operand2,
            operation = MathOperation.SUBTRACTION,
            correctAnswer = answer
        )
    }
    
    private fun generateMultiplicationPuzzle(): MathPuzzle {
        val operand1 = Random.nextInt(2, 20)
        val operand2 = Random.nextInt(2, 20)
        val answer = operand1 * operand2
        
        return MathPuzzle(
            operand1 = operand1,
            operand2 = operand2,
            operation = MathOperation.MULTIPLICATION,
            correctAnswer = answer
        )
    }
}
