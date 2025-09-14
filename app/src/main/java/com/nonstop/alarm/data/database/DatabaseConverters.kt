package com.nonstop.alarm.data.database

import androidx.room.TypeConverter
import com.nonstop.alarm.data.model.AlarmStatus
import com.nonstop.alarm.data.model.MathOperation
import com.nonstop.alarm.data.model.MathPuzzle
import com.nonstop.alarm.data.model.PuzzleDifficulty
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Room type converters for complex data types
 * Supports proper serialization of custom objects in the database
 */
class DatabaseConverters {
    
    private val gson = Gson()
    
    // ================== ENUM CONVERTERS ==================
    
    @TypeConverter
    fun fromAlarmStatus(status: AlarmStatus): String = status.name
    
    @TypeConverter
    fun toAlarmStatus(status: String): AlarmStatus = AlarmStatus.valueOf(status)
    
    @TypeConverter
    fun fromMathOperation(operation: MathOperation): String = operation.name
    
    @TypeConverter
    fun toMathOperation(operation: String): MathOperation = MathOperation.valueOf(operation)
    
    @TypeConverter
    fun fromPuzzleDifficulty(difficulty: PuzzleDifficulty): String = difficulty.name
    
    @TypeConverter
    fun toPuzzleDifficulty(difficulty: String): PuzzleDifficulty = PuzzleDifficulty.valueOf(difficulty)
    
    // ================== COMPLEX OBJECT CONVERTERS ==================
    
    @TypeConverter
    fun fromMathPuzzle(puzzle: MathPuzzle?): String? {
        return if (puzzle == null) null else gson.toJson(puzzle)
    }
    
    @TypeConverter
    fun toMathPuzzle(puzzleJson: String?): MathPuzzle? {
        return if (puzzleJson == null) null else {
            try {
                gson.fromJson(puzzleJson, MathPuzzle::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    // ================== LIST CONVERTERS ==================
    
    @TypeConverter
    fun fromStringList(strings: List<String>?): String? {
        return if (strings == null) null else gson.toJson(strings)
    }
    
    @TypeConverter
    fun toStringList(stringsJson: String?): List<String>? {
        return if (stringsJson == null) null else {
            try {
                val listType = object : TypeToken<List<String>>() {}.type
                gson.fromJson(stringsJson, listType)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    @TypeConverter
    fun fromLongList(longs: List<Long>?): String? {
        return if (longs == null) null else gson.toJson(longs)
    }
    
    @TypeConverter
    fun toLongList(longsJson: String?): List<Long>? {
        return if (longsJson == null) null else {
            try {
                val listType = object : TypeToken<List<Long>>() {}.type
                gson.fromJson(longsJson, listType)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
