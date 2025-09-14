package com.nonstop.alarm

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.nonstop.alarm.databinding.ActivityAlarmBinding
import com.nonstop.alarm.service.AlarmService
import com.nonstop.alarm.ui.alarm.AlarmViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AlarmActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAlarmBinding
    private val viewModel: AlarmViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Show on lock screen and turn screen on
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        
        binding = ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val alarmId = intent.getLongExtra("ALARM_ID", -1L).takeIf { it != -1L }
        val endTime = intent.getLongExtra(AlarmService.EXTRA_END_TIME, 0).takeIf { it != 0L }
        
        viewModel.initializeAlarm(alarmId, endTime)
        
        setupClickListeners()
        observeViewModel()
    }
    
    private fun setupClickListeners() {
        // Number buttons
        binding.btn0.setOnClickListener { viewModel.appendDigit("0") }
        binding.btn1.setOnClickListener { viewModel.appendDigit("1") }
        binding.btn2.setOnClickListener { viewModel.appendDigit("2") }
        binding.btn3.setOnClickListener { viewModel.appendDigit("3") }
        binding.btn4.setOnClickListener { viewModel.appendDigit("4") }
        binding.btn5.setOnClickListener { viewModel.appendDigit("5") }
        binding.btn6.setOnClickListener { viewModel.appendDigit("6") }
        binding.btn7.setOnClickListener { viewModel.appendDigit("7") }
        binding.btn8.setOnClickListener { viewModel.appendDigit("8") }
        binding.btn9.setOnClickListener { viewModel.appendDigit("9") }
        
        // Action buttons
        binding.btnClear.setOnClickListener { viewModel.clearAnswer() }
        binding.btnBackspace.setOnClickListener { viewModel.backspace() }
        binding.btnSubmit.setOnClickListener { viewModel.submitAnswer() }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUI(state)
                }
            }
        }
    }
    
    private fun updateUI(state: com.nonstop.alarm.ui.alarm.AlarmUiState) {
        // Update time displays
        binding.tvEndTime.text = if (state.endTimeDisplay.isNotEmpty()) {
            "Alarm will stop automatically at ${state.endTimeDisplay}"
        } else {
            "Alarm active"
        }
        
        // Update time remaining if available
        if (state.timeRemainingDisplay.isNotEmpty()) {
            // You might want to add a time remaining TextView to the layout
        }
        
        // Update puzzle display
        binding.tvMathProblem.text = state.puzzleText
        binding.tvAnswerInput.text = state.userAnswer
        
        // Handle alarm dismissed
        if (state.isAlarmDismissed) {
            stopAlarm()
            finish()
        }
        
        // Handle alarm expired
        if (state.hasExpired) {
            finish()
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
        
        // Update button states
        binding.btnSubmit.isEnabled = !state.isValidatingAnswer && state.userAnswer.isNotEmpty()
    }
    
    private fun stopAlarm() {
        val intent = Intent(this, AlarmService::class.java).apply {
            action = AlarmService.ACTION_STOP_ALARM
        }
        startService(intent)
    }
    
    override fun onBackPressed() {
        // Prevent back button from dismissing alarm
        Toast.makeText(this, "Solve the math problem to dismiss the alarm", Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Clear flags
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O_MR1) {
            window.clearFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }
}
