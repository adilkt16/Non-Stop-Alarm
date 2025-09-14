package com.nonstop.alarm.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Core Alarm data class following the NonStopAlarm user flow requirements:
 * 1. Alarm Start Time - when alarm should begin
 * 2. Alarm End Time - when alarm should automatically stop
 * 3. Alarm Status - current state of the alarm
 * 4. Math Puzzle Data - for dismissal verification
 * 
 * Supports the critical behavioral requirements:
 * - Time-bounded operation (start/end times)
 * - Persistent playback state tracking
 * - Math puzzle engagement requirement
 */
@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Core time configuration from user flow
    val startTime: Long, // Timestamp when alarm should begin
    val endTime: Long,   // Timestamp when alarm should automatically stop
    
    // Alarm state management
    val status: AlarmStatus = AlarmStatus.SCHEDULED,
    
    // Math puzzle data for dismissal
    val currentPuzzle: MathPuzzle? = null,
    
    // Metadata
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    
    // Configuration options
    val isEnabled: Boolean = true,
    val label: String = "NonStop Alarm"
) {
    
    /**
     * Calculate alarm duration in milliseconds
     */
    fun getDurationMillis(): Long = endTime - startTime
    
    /**
     * Check if alarm should be active right now
     */
    fun shouldBeActive(): Boolean {
        val now = System.currentTimeMillis()
        return now >= startTime && now < endTime && status == AlarmStatus.ACTIVE
    }
    
    /**
     * Check if alarm has expired (past end time)
     */
    fun hasExpired(): Boolean = System.currentTimeMillis() >= endTime
    
    /**
     * Check if alarm is scheduled for future
     */
    fun isScheduledForFuture(): Boolean = System.currentTimeMillis() < startTime
    
    /**
     * Get remaining time until alarm starts (if scheduled)
     */
    fun getTimeUntilStart(): Long = maxOf(0, startTime - System.currentTimeMillis())
    
    /**
     * Get remaining time until alarm ends (if active)
     */
    fun getTimeUntilEnd(): Long = maxOf(0, endTime - System.currentTimeMillis())
}

/**
 * Alarm status following the NonStopAlarm state machine:
 * - SCHEDULED: Alarm is set for future time
 * - ACTIVE: Alarm is currently playing (non-stop)
 * - DISMISSED: Alarm was stopped by solving math puzzle
 * - EXPIRED: Alarm stopped automatically at end time
 * - CANCELLED: Alarm was manually cancelled before start time
 */
enum class AlarmStatus {
    SCHEDULED,   // Waiting for start time
    ACTIVE,      // Currently playing (non-stop)
    DISMISSED,   // Stopped by solving puzzle
    EXPIRED,     // Stopped automatically at end time
    CANCELLED    // Manually cancelled
}

/**
 * Math puzzle data for alarm dismissal requirement
 * Supports the cognitive engagement behavioral requirement
 */
@Entity(tableName = "math_puzzles")
data class MathPuzzle(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Puzzle components
    val operand1: Int,
    val operand2: Int,
    val operation: MathOperation,
    val correctAnswer: Int,
    
    // Tracking
    val generatedAt: Long = System.currentTimeMillis(),
    val attempts: Int = 0,
    val maxAttempts: Int = 5
) {
    
    /**
     * Get the puzzle as a display string
     */
    fun getPuzzleText(): String {
        val operator = when (operation) {
            MathOperation.ADDITION -> "+"
            MathOperation.SUBTRACTION -> "-"
            MathOperation.MULTIPLICATION -> "×"
        }
        return "$operand1 $operator $operand2 = ?"
    }
    
    /**
     * Check if the provided answer is correct
     */
    fun isAnswerCorrect(answer: Int): Boolean = answer == correctAnswer
    
    /**
     * Check if maximum attempts reached
     */
    fun hasReachedMaxAttempts(): Boolean = attempts >= maxAttempts
}

/**
 * Math operations for puzzle generation
 * Simple but requiring conscious thought as per requirements
 */
enum class MathOperation {
    ADDITION,       // Simple addition
    SUBTRACTION,    // Simple subtraction  
    MULTIPLICATION  // Simple multiplication
}

/**
 * Alarm configuration data for user preferences
 */
data class AlarmConfiguration(
    val defaultDuration: Long = 30 * 60 * 1000L, // 30 minutes default
    val maxDuration: Long = 12 * 60 * 60 * 1000L, // 12 hours max per requirements
    val puzzleDifficulty: PuzzleDifficulty = PuzzleDifficulty.MEDIUM,
    val enableVibration: Boolean = true,
    val maxVolume: Boolean = true
)

/**
 * Puzzle difficulty levels
 */
enum class PuzzleDifficulty {
    EASY,    // Single digit operations
    MEDIUM,  // Double digit operations  
    HARD     // Larger numbers
}
