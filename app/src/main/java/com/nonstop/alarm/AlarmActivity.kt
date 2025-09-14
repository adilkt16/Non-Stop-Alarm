package com.nonstop.alarm

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nonstop.alarm.databinding.ActivityAlarmBinding
import com.nonstop.alarm.service.AlarmService
import kotlin.random.Random

class AlarmActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAlarmBinding
    private var currentAnswer: Int = 0
    private var endTime: Long = 0
    
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
        
        endTime = intent.getLongExtra(AlarmService.EXTRA_END_TIME, 0)
        
        setupUI()
        generateMathProblem()
        setupClickListeners()
    }
    
    private fun setupUI() {
        // Display end time
        if (endTime > 0) {
            val endTimeFormatted = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                .format(java.util.Date(endTime))
            binding.tvEndTime.text = "Alarm will stop automatically at $endTimeFormatted"
        }
    }
    
    private fun setupClickListeners() {
        // Number buttons
        binding.btn0.setOnClickListener { appendDigit("0") }
        binding.btn1.setOnClickListener { appendDigit("1") }
        binding.btn2.setOnClickListener { appendDigit("2") }
        binding.btn3.setOnClickListener { appendDigit("3") }
        binding.btn4.setOnClickListener { appendDigit("4") }
        binding.btn5.setOnClickListener { appendDigit("5") }
        binding.btn6.setOnClickListener { appendDigit("6") }
        binding.btn7.setOnClickListener { appendDigit("7") }
        binding.btn8.setOnClickListener { appendDigit("8") }
        binding.btn9.setOnClickListener { appendDigit("9") }
        
        // Action buttons
        binding.btnClear.setOnClickListener { clearAnswer() }
        binding.btnBackspace.setOnClickListener { backspace() }
        binding.btnSubmit.setOnClickListener { checkAnswer() }
    }
    
    private fun generateMathProblem() {
        // Generate simple math problems that require conscious thought
        val operations = listOf("+", "-", "×")
        val operation = operations.random()
        
        val (num1, num2, answer) = when (operation) {
            "+" -> {
                val a = Random.nextInt(10, 100)
                val b = Random.nextInt(10, 100)
                Triple(a, b, a + b)
            }
            "-" -> {
                val a = Random.nextInt(50, 200)
                val b = Random.nextInt(10, a)
                Triple(a, b, a - b)
            }
            "×" -> {
                val a = Random.nextInt(2, 20)
                val b = Random.nextInt(2, 20)
                Triple(a, b, a * b)
            }
            else -> Triple(0, 0, 0)
        }
        
        currentAnswer = answer
        binding.tvMathProblem.text = "$num1 $operation $num2 = ?"
        binding.tvAnswerInput.text = ""
    }
    
    private fun appendDigit(digit: String) {
        val currentText = binding.tvAnswerInput.text.toString()
        if (currentText.length < 6) { // Prevent extremely long inputs
            binding.tvAnswerInput.text = currentText + digit
        }
    }
    
    private fun clearAnswer() {
        binding.tvAnswerInput.text = ""
    }
    
    private fun backspace() {
        val currentText = binding.tvAnswerInput.text.toString()
        if (currentText.isNotEmpty()) {
            binding.tvAnswerInput.text = currentText.dropLast(1)
        }
    }
    
    private fun checkAnswer() {
        val userInput = binding.tvAnswerInput.text.toString()
        
        if (userInput.isEmpty()) {
            Toast.makeText(this, "Please enter an answer", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            val userAnswer = userInput.toInt()
            
            if (userAnswer == currentAnswer) {
                // Correct answer - stop the alarm
                stopAlarm()
                Toast.makeText(this, "Correct! Alarm dismissed.", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                // Wrong answer - generate new problem
                Toast.makeText(this, "Incorrect. Try again with a new problem.", Toast.LENGTH_SHORT).show()
                generateMathProblem()
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show()
        }
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
