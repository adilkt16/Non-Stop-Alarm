package com.nonstop.alarm.ui.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nonstop.alarm.data.repository.AlarmRepository
import com.nonstop.alarm.data.repository.MathPuzzleRepository
import com.nonstop.alarm.data.model.Alarm
import com.nonstop.alarm.data.model.MathPuzzle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * ViewModel for AlarmActivity
 * 
 * Handles the alarm dismissal process following NonStopAlarm requirements:
 * 1. Math puzzle generation and display
 * 2. Answer validation for dismissal
 * 3. Alarm time tracking and auto-termination
 * 4. Puzzle regeneration on wrong answers
 * 
 * Supports critical behavioral requirements:
 * - Cognitive engagement through math puzzles
 * - No easy dismissal methods
 * - Time-bound operation with automatic termination
 */
@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val mathPuzzleRepository: MathPuzzleRepository
) : ViewModel() {
    
    // ================== UI STATE ==================
    
    private val _uiState = MutableStateFlow(AlarmUiState())
    val uiState: StateFlow<AlarmUiState> = _uiState.asStateFlow()
    
    // ================== CURRENT ALARM ==================
    
    private var currentAlarmId: Long? = null
    private var currentPuzzle: MathPuzzle? = null
    
    // ================== TIME FORMATTER ==================
    
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    // ================== INITIALIZATION ==================
    
    fun initializeAlarm(alarmId: Long?, endTime: Long?) {
        currentAlarmId = alarmId
        
        if (endTime != null) {
            _uiState.value = _uiState.value.copy(
                endTime = endTime,
                endTimeDisplay = timeFormat.format(Date(endTime))
            )
            startTimeTracking(endTime)
        }
        
        if (alarmId != null) {
            loadAlarmDetails(alarmId)
        }
        
        generateNewPuzzle()
    }
    
    // ================== PUZZLE OPERATIONS ==================
    
    /**
     * Generate new math puzzle
     */
    fun generateNewPuzzle() {
        _uiState.value = _uiState.value.copy(isGeneratingPuzzle = true)
        
        viewModelScope.launch {
            val result = mathPuzzleRepository.generatePuzzle()
            
            result.fold(
                onSuccess = { puzzle ->
                    currentPuzzle = puzzle
                    _uiState.value = _uiState.value.copy(
                        isGeneratingPuzzle = false,
                        currentPuzzle = puzzle,
                        puzzleText = puzzle.getPuzzleText(),
                        userAnswer = "",
                        attempts = puzzle.attempts,
                        error = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isGeneratingPuzzle = false,
                        error = "Failed to generate puzzle: ${error.message}"
                    )
                    // Fallback: create a simple puzzle manually
                    createFallbackPuzzle()
                }
            )
        }
    }
    
    /**
     * Submit answer for validation
     */
    fun submitAnswer() {
        val puzzle = currentPuzzle
        val answer = _uiState.value.userAnswer
        
        if (puzzle == null) {
            showError("No puzzle available")
            return
        }
        
        if (answer.isBlank()) {
            showError("Please enter an answer")
            return
        }
        
        val answerInt = try {
            answer.toInt()
        } catch (e: NumberFormatException) {
            showError("Please enter a valid number")
            return
        }
        
        _uiState.value = _uiState.value.copy(isValidatingAnswer = true)
        
        viewModelScope.launch {
            val result = mathPuzzleRepository.validateAnswer(puzzle.id, answerInt)
            
            result.fold(
                onSuccess = { isCorrect ->
                    if (isCorrect) {
                        // Correct answer - dismiss alarm
                        dismissAlarm()
                    } else {
                        // Wrong answer - generate new puzzle
                        _uiState.value = _uiState.value.copy(
                            isValidatingAnswer = false,
                            attempts = _uiState.value.attempts + 1,
                            message = "Incorrect. Try again with a new problem."
                        )
                        generateNewPuzzle()
                    }
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isValidatingAnswer = false,
                        error = "Validation failed: ${error.message}"
                    )
                }
            )
        }
    }
    
    // ================== ANSWER INPUT ==================
    
    /**
     * Update user answer input
     */
    fun updateAnswer(answer: String) {
        // Limit input length to prevent extremely long inputs
        val limitedAnswer = if (answer.length <= 10) answer else answer.substring(0, 10)
        _uiState.value = _uiState.value.copy(userAnswer = limitedAnswer)
    }
    
    /**
     * Append digit to answer
     */
    fun appendDigit(digit: String) {
        val currentAnswer = _uiState.value.userAnswer
        if (currentAnswer.length < 10) { // Prevent extremely long inputs
            updateAnswer(currentAnswer + digit)
        }
    }
    
    /**
     * Clear answer input
     */
    fun clearAnswer() {
        _uiState.value = _uiState.value.copy(userAnswer = "")
    }
    
    /**
     * Remove last digit from answer
     */
    fun backspace() {
        val currentAnswer = _uiState.value.userAnswer
        if (currentAnswer.isNotEmpty()) {
            updateAnswer(currentAnswer.dropLast(1))
        }
    }
    
    // ================== ALARM MANAGEMENT ==================
    
    /**
     * Dismiss alarm (called when puzzle is solved correctly)
     */
    private fun dismissAlarm() {
        val alarmId = currentAlarmId
        if (alarmId == null) {
            _uiState.value = _uiState.value.copy(
                isValidatingAnswer = false,
                isAlarmDismissed = true,
                message = "Alarm dismissed!"
            )
            return
        }
        
        viewModelScope.launch {
            val result = alarmRepository.dismissAlarm(alarmId)
            
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isValidatingAnswer = false,
                        isAlarmDismissed = true,
                        message = "Correct! Alarm dismissed."
                    )
                },
                onFailure = { error ->
                    // Even if database update fails, consider alarm dismissed
                    // since user solved the puzzle correctly
                    _uiState.value = _uiState.value.copy(
                        isValidatingAnswer = false,
                        isAlarmDismissed = true,
                        message = "Correct! Alarm dismissed.",
                        error = "Note: ${error.message}"
                    )
                }
            )
        }
    }
    
    // ================== TIME TRACKING ==================
    
    /**
     * Start tracking time until alarm auto-termination
     */
    private fun startTimeTracking(endTime: Long) {
        viewModelScope.launch {
            while (!_uiState.value.isAlarmDismissed && !_uiState.value.hasExpired) {
                val currentTime = System.currentTimeMillis()
                val timeRemaining = endTime - currentTime
                
                if (timeRemaining <= 0) {
                    // Alarm has expired
                    handleAlarmExpiration()
                    break
                }
                
                // Update time remaining display
                _uiState.value = _uiState.value.copy(
                    timeRemaining = timeRemaining,
                    timeRemainingDisplay = formatTimeRemaining(timeRemaining)
                )
                
                // Update every second
                kotlinx.coroutines.delay(1000)
            }
        }
    }
    
    /**
     * Handle alarm expiration at end time
     */
    private fun handleAlarmExpiration() {
        val alarmId = currentAlarmId
        
        _uiState.value = _uiState.value.copy(
            hasExpired = true,
            timeRemaining = 0,
            timeRemainingDisplay = "Time's up!",
            message = "Alarm stopped automatically at end time"
        )
        
        if (alarmId != null) {
            viewModelScope.launch {
                alarmRepository.expireAlarm(alarmId)
            }
        }
    }
    
    // ================== HELPER METHODS ==================
    
    private fun loadAlarmDetails(alarmId: Long) {
        viewModelScope.launch {
            val alarm = alarmRepository.getAlarmById(alarmId)
            if (alarm != null) {
                _uiState.value = _uiState.value.copy(
                    alarmLabel = alarm.label
                )
            }
        }
    }
    
    private fun createFallbackPuzzle() {
        // Create a simple puzzle as fallback
        val operand1 = (10..50).random()
        val operand2 = (1..20).random()
        val answer = operand1 + operand2
        
        _uiState.value = _uiState.value.copy(
            puzzleText = "$operand1 + $operand2 = ?",
            userAnswer = "",
            error = null
        )
        
        // Store the answer for validation (simplified fallback)
        // In production, this should still use the repository
    }
    
    private fun formatTimeRemaining(timeMillis: Long): String {
        if (timeMillis <= 0) return "Time's up!"
        
        val hours = timeMillis / (1000 * 60 * 60)
        val minutes = (timeMillis % (1000 * 60 * 60)) / (1000 * 60)
        val seconds = (timeMillis % (1000 * 60)) / 1000
        
        return when {
            hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
            else -> String.format("%02d:%02d", minutes, seconds)
        }
    }
    
    private fun showError(message: String) {
        _uiState.value = _uiState.value.copy(error = message)
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Clear info message
     */
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}

/**
 * UI State for AlarmActivity
 */
data class AlarmUiState(
    // Alarm information
    val alarmLabel: String = "NonStop Alarm",
    val endTime: Long? = null,
    val endTimeDisplay: String = "",
    
    // Time tracking
    val timeRemaining: Long = 0,
    val timeRemainingDisplay: String = "",
    val hasExpired: Boolean = false,
    
    // Puzzle state
    val currentPuzzle: MathPuzzle? = null,
    val puzzleText: String = "",
    val userAnswer: String = "",
    val attempts: Int = 0,
    
    // Loading states
    val isGeneratingPuzzle: Boolean = false,
    val isValidatingAnswer: Boolean = false,
    
    // Result states
    val isAlarmDismissed: Boolean = false,
    val error: String? = null,
    val message: String? = null
)
