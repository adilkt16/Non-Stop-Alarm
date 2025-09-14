package com.nonstop.alarm

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for NonStopAlarm
 * 
 * Enables Hilt dependency injection throughout the app
 * Supports the persistent alarm mechanism with proper DI setup
 */
@HiltAndroidApp
class NonStopAlarmApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize application-level components
        // Any global initialization can be done here
    }
}
