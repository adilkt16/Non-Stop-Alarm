package com.nonstop.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nonstop.alarm.util.AlarmManager

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            
            // Check if there was an alarm set before reboot
            val alarmTimes = AlarmManager.getAlarmTimes(context)
            if (alarmTimes != null) {
                val (startTime, endTime) = alarmTimes
                val currentTime = System.currentTimeMillis()
                
                // If start time has passed but end time hasn't, start the alarm immediately
                if (currentTime >= startTime && currentTime < endTime) {
                    val serviceIntent = Intent(context, com.nonstop.alarm.service.AlarmService::class.java).apply {
                        action = com.nonstop.alarm.service.AlarmService.ACTION_START_ALARM
                        putExtra(com.nonstop.alarm.service.AlarmService.EXTRA_END_TIME, endTime)
                    }
                    context.startForegroundService(serviceIntent)
                } else if (currentTime < startTime) {
                    // Re-schedule the alarm if it hasn't started yet
                    AlarmManager.scheduleAlarm(context, startTime, endTime)
                } else {
                    // Alarm time has passed, clear it
                    AlarmManager.cancelAlarm(context)
                }
            }
        }
    }
}
