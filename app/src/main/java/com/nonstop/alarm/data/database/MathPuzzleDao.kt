package com.nonstop.alarm.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.nonstop.alarm.data.model.MathPuzzle
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for MathPuzzle operations
 * Supports the cognitive engagement behavioral requirement
 */
@Dao
interface MathPuzzleDao {
    
    // ================== QUERIES ==================
    
    /**
     * Get all math puzzles
     */
    @Query("SELECT * FROM math_puzzles ORDER BY generatedAt DESC")
    fun getAllPuzzles(): Flow<List<MathPuzzle>>
    
    /**
     * Get puzzle by ID
     */
    @Query("SELECT * FROM math_puzzles WHERE id = :puzzleId")
    suspend fun getPuzzleById(puzzleId: Long): MathPuzzle?
    
    /**
     * Get recent puzzles (for avoiding repetition)
     */
    @Query("SELECT * FROM math_puzzles ORDER BY generatedAt DESC LIMIT :limit")
    suspend fun getRecentPuzzles(limit: Int = 10): List<MathPuzzle>
    
    /**
     * Get puzzles with maximum attempts reached
     */
    @Query("SELECT * FROM math_puzzles WHERE attempts >= maxAttempts")
    suspend fun getMaxAttemptPuzzles(): List<MathPuzzle>
    
    // ================== INSERTS ==================
    
    /**
     * Insert new puzzle
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPuzzle(puzzle: MathPuzzle): Long
    
    /**
     * Insert multiple puzzles
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPuzzles(puzzles: List<MathPuzzle>)
    
    // ================== UPDATES ==================
    
    /**
     * Update puzzle
     */
    @Update
    suspend fun updatePuzzle(puzzle: MathPuzzle)
    
    /**
     * Increment attempt count
     */
    @Query("UPDATE math_puzzles SET attempts = attempts + 1 WHERE id = :puzzleId")
    suspend fun incrementAttempts(puzzleId: Long)
    
    // ================== DELETES ==================
    
    /**
     * Delete puzzle by ID
     */
    @Query("DELETE FROM math_puzzles WHERE id = :puzzleId")
    suspend fun deletePuzzleById(puzzleId: Long)
    
    /**
     * Delete puzzle
     */
    @Delete
    suspend fun deletePuzzle(puzzle: MathPuzzle)
    
    /**
     * Delete old puzzles (cleanup)
     */
    @Query("DELETE FROM math_puzzles WHERE generatedAt < :cutoffTime")
    suspend fun deleteOldPuzzles(cutoffTime: Long): Int
    
    /**
     * Delete all puzzles
     */
    @Query("DELETE FROM math_puzzles")
    suspend fun deleteAllPuzzles()
    
    // ================== UTILITY OPERATIONS ==================
    
    /**
     * Get puzzle count
     */
    @Query("SELECT COUNT(*) FROM math_puzzles")
    suspend fun getPuzzleCount(): Int
}
