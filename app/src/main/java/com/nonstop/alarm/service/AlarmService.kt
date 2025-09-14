package com.nonstop.alarm.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import androidx.core.app.NotificationCompat
import com.nonstop.alarm.AlarmActivity
import com.nonstop.alarm.MainActivity
import com.nonstop.alarm.R

class AlarmService : Service() {
    
    companion object {
        const val CHANNEL_ID = "NonStopAlarmChannel"
        const val NOTIFICATION_ID = 1
        const val ACTION_START_ALARM = "START_ALARM"
        const val ACTION_STOP_ALARM = "STOP_ALARM"
        const val EXTRA_END_TIME = "END_TIME"
    }
    
    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var vibrator: Vibrator? = null
    private var endTime: Long = 0
    private var endTimeHandler: Handler? = null
    private var endTimeRunnable: Runnable? = null
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        
        // Initialize vibrator
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_ALARM -> {
                endTime = intent.getLongExtra(EXTRA_END_TIME, 0)
                startAlarm()
            }
            ACTION_STOP_ALARM -> {
                stopAlarm()
            }
        }
        
        return START_NOT_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun startAlarm() {
        // Acquire wake lock to keep device awake
        acquireWakeLock()
        
        // Start foreground notification
        startForeground(NOTIFICATION_ID, createNotification())
        
        // Start alarm sound
        startAlarmSound()
        
        // Start vibration pattern
        startVibration()
        
        // Launch alarm activity
        launchAlarmActivity()
        
        // Schedule automatic stop at end time
        scheduleAutoStop()
    }
    
    private fun stopAlarm() {
        // Stop alarm sound
        stopAlarmSound()
        
        // Stop vibration
        stopVibration()
        
        // Release wake lock
        releaseWakeLock()
        
        // Cancel auto stop
        cancelAutoStop()
        
        // Stop foreground service
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "NonStopAlarm::AlarmWakeLock"
        )
        wakeLock?.acquire(10 * 60 * 1000L) // 10 minutes max
    }
    
    private fun releaseWakeLock() {
        wakeLock?.let { wl ->
            if (wl.isHeld) {
                wl.release()
            }
        }
        wakeLock = null
    }
    
    private fun startAlarmSound() {
        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                
                // Use system alarm sound or default alarm tone
                val alarmUri = android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI
                setDataSource(this@AlarmService, alarmUri)
                
                isLooping = true
                prepareAsync()
                
                setOnPreparedListener { player ->
                    // Set maximum volume for alarm
                    val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0)
                    
                    player.start()
                }
                
                setOnErrorListener { _, _, _ ->
                    // If system alarm fails, use fallback tone
                    useFallbackAlarmTone()
                    true
                }
            }
        } catch (e: Exception) {
            useFallbackAlarmTone()
        }
    }
    
    private fun useFallbackAlarmTone() {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                
                // Use a raw resource as fallback (you'll need to add an alarm sound file)
                val afd = resources.openRawResourceFd(R.raw.alarm_sound)
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            // Last resort: system beep
            val toneGenerator = android.media.ToneGenerator(
                AudioManager.STREAM_ALARM,
                android.media.ToneGenerator.MAX_VOLUME
            )
            
            // Create repeating beep pattern
            val handler = Handler(Looper.getMainLooper())
            val beepRunnable = object : Runnable {
                override fun run() {
                    toneGenerator.startTone(android.media.ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1000)
                    handler.postDelayed(this, 2000)
                }
            }
            handler.post(beepRunnable)
        }
    }
    
    private fun stopAlarmSound() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.stop()
            }
            player.release()
        }
        mediaPlayer = null
    }
    
    private fun startVibration() {
        val pattern = longArrayOf(0, 1000, 500, 1000, 500) // Vibrate for 1s, pause 0.5s, repeat
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }
    
    private fun stopVibration() {
        vibrator?.cancel()
    }
    
    private fun launchAlarmActivity() {
        val intent = Intent(this, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                   Intent.FLAG_ACTIVITY_CLEAR_TOP or
                   Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_END_TIME, endTime)
        }
        startActivity(intent)
    }
    
    private fun scheduleAutoStop() {
        if (endTime > 0) {
            val delay = endTime - System.currentTimeMillis()
            if (delay > 0) {
                endTimeHandler = Handler(Looper.getMainLooper())
                endTimeRunnable = Runnable {
                    stopAlarm()
                }
                endTimeHandler?.postDelayed(endTimeRunnable!!, delay)
            }
        }
    }
    
    private fun cancelAutoStop() {
        endTimeRunnable?.let { runnable ->
            endTimeHandler?.removeCallbacks(runnable)
        }
        endTimeHandler = null
        endTimeRunnable = null
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "NonStop Alarm",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Persistent alarm notifications"
                setSound(null, null) // We handle sound ourselves
                enableVibration(false) // We handle vibration ourselves
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("NonStop Alarm Active")
            .setContentText("Tap to solve math puzzle and dismiss alarm")
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()
    }
}
