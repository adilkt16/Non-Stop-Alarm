package com.nonstop.alarm.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nonstop.alarm.data.repository.AlarmRepository
import com.nonstop.alarm.data.model.Alarm
import com.nonstop.alarm.data.model.AlarmStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * ViewModel for MainActivity
 * 
 * Handles alarm configuration following the NonStopAlarm user flow:
 * 1. Alarm Start Time configuration
 * 2. Alarm End Time configuration  
 * 3. Alarm validation and creation
 * 4. Current alarm status monitoring
 * 
 * Supports critical behavioral requirements:
 * - Time-bounded operation validation
 * - No conflicting alarms
 * - Persistent alarm state tracking
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository
) : ViewModel() {
    
    // ================== UI STATE ==================
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    // ================== ALARM DATA ==================
    
    val allAlarms = alarmRepository.getAllAlarms()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val activeAlarm = alarmRepository.getActiveAlarmFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    // ================== TIME FORMATTERS ==================
    
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    
    init {
        // Monitor alarm state for UI updates
        viewModelScope.launch {
            combine(allAlarms, activeAlarm) { alarms, active ->
                _uiState.value = _uiState.value.copy(
                    hasActiveAlarm = active != null,
                    nextScheduledAlarm = alarms.firstOrNull { it.status == AlarmStatus.SCHEDULED }
                )
            }.collect()
        }
        
        // Perform state recovery on initialization
        performStateRecovery()
    }
    
    // ================== USER ACTIONS ==================
    
    /**
     * Set alarm start time
     */
    fun setStartTime(timeMillis: Long) {
        _uiState.value = _uiState.value.copy(
            startTime = timeMillis,
            startTimeDisplay = timeFormat.format(Date(timeMillis))
        )
        validateAlarmConfiguration()
    }
    
    /**
     * Set alarm end time
     */
    fun setEndTime(timeMillis: Long) {
        _uiState.value = _uiState.value.copy(
            endTime = timeMillis,
            endTimeDisplay = timeFormat.format(Date(timeMillis))
        )
        validateAlarmConfiguration()
    }
    
    /**
     * Create new alarm with current configuration
     */
    fun createAlarm() {
        val state = _uiState.value
        
        if (state.startTime == null || state.endTime == null) {
            showError("Please set both start and end times")
            return
        }
        
        _uiState.value = state.copy(isLoading = true)
        
        viewModelScope.launch {
            val result = alarmRepository.createAlarm(
                startTime = state.startTime!!,
                endTime = state.endTime!!,
                label = state.alarmLabel.ifBlank { "NonStop Alarm" }
            )
            
            result.fold(
                onSuccess = { alarmId ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAlarmSet = true,
                        message = "Alarm set successfully!"
                    )
                    clearConfiguration()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to create alarm"
                    )
                }
            )
        }
    }
    
    /**
     * Cancel active or scheduled alarm
     */
    fun cancelAlarm(alarmId: Long) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        viewModelScope.launch {
            val result = alarmRepository.cancelAlarm(alarmId)
            
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAlarmSet = false,
                        message = "Alarm cancelled"
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to cancel alarm"
                    )
                }
            )
        }
    }
    
    /**
     * Update alarm label
     */
    fun updateAlarmLabel(label: String) {
        _uiState.value = _uiState.value.copy(alarmLabel = label)
    }
    
    /**
     * Clear current alarm configuration
     */
    fun clearConfiguration() {
        _uiState.value = _uiState.value.copy(
            startTime = null,
            endTime = null,
            startTimeDisplay = "Not set",
            endTimeDisplay = "Not set",
            alarmLabel = "",
            canCreateAlarm = false
        )
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
    
    // ================== PRIVATE METHODS ==================
    
    private fun validateAlarmConfiguration() {
        val state = _uiState.value
        val startTime = state.startTime
        val endTime = state.endTime
        
        if (startTime != null && endTime != null) {
            viewModelScope.launch {
                val validation = alarmRepository.validateAlarmTimes(startTime, endTime)
                
                val validationMessage = when {
                    validation.isFailure -> {
                        val error = validation.exceptionOrNull()?.message
                        when {
                            error?.contains("before current time") == true -> 
                                "⚠️ Start time must be in the future"
                            error?.contains("after end time") == true -> 
                                "⚠️ End time must be after start time"
                            error?.contains("too short") == true -> 
                                "⚠️ Alarm duration must be at least 1 minute"
                            else -> "⚠️ Invalid time configuration"
                        }
                    }
                    else -> null
                }
                
                val canCreate = if (validation.isSuccess) {
                    val canCreateResult = alarmRepository.canCreateAlarm(startTime, endTime)
                    if (canCreateResult.isFailure) {
                        val canCreateError = canCreateResult.exceptionOrNull()?.message
                        _uiState.value = _uiState.value.copy(
                            canCreateAlarm = false,
                            validationError = when {
                                canCreateError?.contains("overlaps") == true -> 
                                    "⚠️ This time conflicts with an existing alarm"
                                canCreateError?.contains("active alarm") == true -> 
                                    "⚠️ Please cancel the current alarm first"
                                else -> "⚠️ Cannot create alarm at this time"
                            }
                        )
                        return@launch
                    }
                    canCreateResult.getOrDefault(false)
                } else {
                    false
                }
                
                _uiState.value = _uiState.value.copy(
                    canCreateAlarm = canCreate,
                    validationError = validationMessage
                )
            }
        } else {
            _uiState.value = _uiState.value.copy(
                canCreateAlarm = false,
                validationError = null
            )
        }
    }
    
    private fun showError(message: String) {
        _uiState.value = _uiState.value.copy(error = message)
    }
    
    private fun performStateRecovery() {
        viewModelScope.launch {
            alarmRepository.performStateRecovery()
        }
    }
    
    // ================== HELPER METHODS ==================
    
    /**
     * Get formatted time remaining until alarm starts
     */
    fun getTimeUntilAlarmStarts(alarm: Alarm): String {
        val timeRemaining = alarm.getTimeUntilStart()
        if (timeRemaining <= 0) return "Starting now"
        
        val hours = timeRemaining / (1000 * 60 * 60)
        val minutes = (timeRemaining % (1000 * 60 * 60)) / (1000 * 60)
        
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "Less than 1m"
        }
    }
    
    /**
     * Get formatted alarm duration
     */
    fun getAlarmDuration(alarm: Alarm): String {
        val duration = alarm.getDurationMillis()
        val hours = duration / (1000 * 60 * 60)
        val minutes = (duration % (1000 * 60 * 60)) / (1000 * 60)
        
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "Less than 1m"
        }
    }
}

/**
 * UI State for MainActivity
 */
data class MainUiState(
    // Time configuration
    val startTime: Long? = null,
    val endTime: Long? = null,
    val startTimeDisplay: String = "Not set",
    val endTimeDisplay: String = "Not set",
    
    // Alarm configuration
    val alarmLabel: String = "",
    val canCreateAlarm: Boolean = false,
    val isAlarmSet: Boolean = false,
    
    // Loading and error states
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val validationError: String? = null,
    
    // Alarm status
    val hasActiveAlarm: Boolean = false,
    val nextScheduledAlarm: Alarm? = null
)
