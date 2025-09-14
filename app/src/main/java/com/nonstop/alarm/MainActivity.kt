package com.nonstop.alarm

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.nonstop.alarm.databinding.ActivityMainBinding
import com.nonstop.alarm.service.AlarmService
import com.nonstop.alarm.ui.main.MainViewModel
import com.nonstop.alarm.util.AlarmManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        setupClickListeners()
        observeViewModel()
    }
    
    private fun setupUI() {
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
            viewModel.createAlarm()
        }
        
        binding.btnCancelAlarm.setOnClickListener {
            viewModel.activeAlarm.value?.let { alarm ->
                viewModel.cancelAlarm(alarm.id)
            }
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUI(state)
                }
            }
        }
        
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.activeAlarm.collect { alarm ->
                    binding.btnCancelAlarm.isEnabled = alarm != null
                    binding.tvAlarmStatus.text = if (alarm != null) {
                        "Active alarm: ${timeFormat.format(Date(alarm.startTime))}"
                    } else {
                        "No alarm set"
                    }
                }
            }
        }
    }
    
    private fun updateUI(state: com.nonstop.alarm.ui.main.MainUiState) {
        // Update time displays
        binding.tvStartTime.text = state.startTimeDisplay
        binding.tvEndTime.text = state.endTimeDisplay
        
        // Update button states
        binding.btnSetAlarm.isEnabled = state.canCreateAlarm && !state.isLoading
        
        // Show loading
        if (state.isLoading) {
            // Could add progress indicator here
        }
        
        // Show messages
        state.error?.let { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
        
        state.message?.let { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
        
        state.validationError?.let { error ->
            // Show validation error in a less intrusive way
            binding.tvAlarmStatus.text = error
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
                viewModel.setStartTime(selectedTime.timeInMillis)
            } else {
                viewModel.setEndTime(selectedTime.timeInMillis)
            }
            
        }, hour, minute, true).show()
    }
}
