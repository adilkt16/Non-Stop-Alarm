package com.nonstop.alarm.util

import android.app.AlarmManager as SystemAlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.nonstop.alarm.receiver.AlarmReceiver

object AlarmManager {
    
    private const val ALARM_REQUEST_CODE = 1001
    
    fun scheduleAlarm(context: Context, startTime: Long, endTime: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as SystemAlarmManager
        
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("END_TIME", endTime)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Schedule exact alarm
        try {
            alarmManager.setExactAndAllowWhileIdle(
                SystemAlarmManager.RTC_WAKEUP,
                startTime,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // Fallback for devices that don't allow exact alarms
            alarmManager.setAndAllowWhileIdle(
                SystemAlarmManager.RTC_WAKEUP,
                startTime,
                pendingIntent
            )
        }
        
        // Save alarm info to preferences
        saveAlarmInfo(context, startTime, endTime)
    }
    
    fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as SystemAlarmManager
        
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
        
        // Clear alarm info from preferences
        clearAlarmInfo(context)
    }
    
    private fun saveAlarmInfo(context: Context, startTime: Long, endTime: Long) {
        val prefs = context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putLong("start_time", startTime)
            .putLong("end_time", endTime)
            .putBoolean("is_set", true)
            .apply()
    }
    
    private fun clearAlarmInfo(context: Context) {
        val prefs = context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .remove("start_time")
            .remove("end_time")
            .putBoolean("is_set", false)
            .apply()
    }
    
    fun isAlarmSet(context: Context): Boolean {
        val prefs = context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("is_set", false)
    }
    
    fun getAlarmTimes(context: Context): Pair<Long, Long>? {
        val prefs = context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("is_set", false)) return null
        
        val startTime = prefs.getLong("start_time", 0)
        val endTime = prefs.getLong("end_time", 0)
        
        return if (startTime > 0 && endTime > 0) Pair(startTime, endTime) else null
    }
}
