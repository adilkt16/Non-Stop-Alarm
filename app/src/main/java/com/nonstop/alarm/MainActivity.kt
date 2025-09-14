package com.nonstop.alarm

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nonstop.alarm.databinding.ActivityMainBinding
import com.nonstop.alarm.service.AlarmService
import com.nonstop.alarm.util.AlarmManager
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private var startTime: Calendar? = null
    private var endTime: Calendar? = null
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        setupClickListeners()
    }
    
    private fun setupUI() {
        // Initialize time displays
        updateTimeDisplays()
        
        // Set app title
        supportActionBar?.title = "NonStop Alarm"
    }
    
    private fun setupClickListeners() {
        binding.btnSelectStartTime.setOnClickListener {
            showTimePickerDialog(true)
        }
        
        binding.btnSelectEndTime.setOnClickListener {
            showTimePickerDialog(false)
        }
        
        binding.btnSetAlarm.setOnClickListener {
            setAlarm()
        }
        
        binding.btnCancelAlarm.setOnClickListener {
            cancelAlarm()
        }
    }
    
    private fun showTimePickerDialog(isStartTime: Boolean) {
        val currentTime = Calendar.getInstance()
        val hour = currentTime.get(Calendar.HOUR_OF_DAY)
        val minute = currentTime.get(Calendar.MINUTE)
        
        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val selectedTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, selectedHour)
                set(Calendar.MINUTE, selectedMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                
                // If selected time is before current time, set it for tomorrow
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }
            
            if (isStartTime) {
                startTime = selectedTime
            } else {
                endTime = selectedTime
            }
            
            updateTimeDisplays()
            validateTimes()
            
        }, hour, minute, true).show()
    }
    
    private fun updateTimeDisplays() {
        binding.tvStartTime.text = startTime?.let { timeFormat.format(it.time) } ?: "Not set"
        binding.tvEndTime.text = endTime?.let { timeFormat.format(it.time) } ?: "Not set"
    }
    
    private fun validateTimes() {
        val startTimeVal = startTime
        val endTimeVal = endTime
        
        if (startTimeVal != null && endTimeVal != null) {
            if (endTimeVal.timeInMillis <= startTimeVal.timeInMillis) {
                // If end time is before or equal to start time, set it to next day
                endTimeVal.add(Calendar.DAY_OF_MONTH, 1)
                updateTimeDisplays()
            }
        }
        
        // Enable/disable set alarm button
        binding.btnSetAlarm.isEnabled = startTimeVal != null && endTimeVal != null
    }
    
    private fun setAlarm() {
        val startTimeVal = startTime
        val endTimeVal = endTime
        
        if (startTimeVal == null || endTimeVal == null) {
            Toast.makeText(this, "Please set both start and end times", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Validate that end time is after start time
        if (endTimeVal.timeInMillis <= startTimeVal.timeInMillis) {
            Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Calculate duration
        val durationMs = endTimeVal.timeInMillis - startTimeVal.timeInMillis
        val durationMinutes = durationMs / (1000 * 60)
        
        if (durationMinutes > 12 * 60) { // More than 12 hours
            Toast.makeText(this, "Alarm duration cannot exceed 12 hours", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Schedule the alarm
        AlarmManager.scheduleAlarm(this, startTimeVal.timeInMillis, endTimeVal.timeInMillis)
        
        Toast.makeText(this, "Alarm set successfully!", Toast.LENGTH_SHORT).show()
        
        // Update UI
        binding.btnSetAlarm.isEnabled = false
        binding.btnCancelAlarm.isEnabled = true
        binding.tvAlarmStatus.text = "Alarm scheduled for ${timeFormat.format(startTimeVal.time)}"
    }
    
    private fun cancelAlarm() {
        AlarmManager.cancelAlarm(this)
        
        // Stop alarm service if running
        val intent = Intent(this, AlarmService::class.java)
        stopService(intent)
        
        Toast.makeText(this, "Alarm cancelled", Toast.LENGTH_SHORT).show()
        
        // Update UI
        binding.btnSetAlarm.isEnabled = true
        binding.btnCancelAlarm.isEnabled = false
        binding.tvAlarmStatus.text = "No alarm set"
    }
}
