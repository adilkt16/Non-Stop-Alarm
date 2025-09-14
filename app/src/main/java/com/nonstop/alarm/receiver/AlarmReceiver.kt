package com.nonstop.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.nonstop.alarm.service.AlarmService

class AlarmReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val endTime = intent.getLongExtra("END_TIME", 0)
        
        // Start the alarm service
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            action = AlarmService.ACTION_START_ALARM
            putExtra(AlarmService.EXTRA_END_TIME, endTime)
        }
        
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}
