plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.nonstop.alarm"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.nonstop.alarm"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            manifestPlaceholders["appName"] = "NonStop Alarm Debug"
            
            // Enable debug logging
            buildConfigField("boolean", "DEBUG_MODE", "true")
            buildConfigField("String", "BUILD_TYPE", "\"debug\"")
            
            // Faster alarm testing - reduce minimum durations
            buildConfigField("long", "MIN_ALARM_DURATION_MS", "10000L") // 10 seconds for testing
            buildConfigField("long", "MIN_TIME_UNTIL_START_MS", "5000L") // 5 seconds for testing
        }
        
        release {
            isMinifyEnabled = false
            manifestPlaceholders["appName"] = "NonStop Alarm"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            buildConfigField("boolean", "DEBUG_MODE", "false")
            buildConfigField("String", "BUILD_TYPE", "\"release\"")
            
            // Production timing constraints
            buildConfigField("long", "MIN_ALARM_DURATION_MS", "60000L") // 1 minute minimum
            buildConfigField("long", "MIN_TIME_UNTIL_START_MS", "60000L") // 1 minute minimum
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    
    kotlinOptions {
        jvmTarget = "1.8"
    }
    
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // Background work and alarms
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    
    // Audio and media
    implementation("androidx.media:media:1.6.0")
    
    // Lifecycle components
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-service:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    
    // Activity and Fragment
    implementation("androidx.activity:activity-ktx:1.8.1")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    
    // Room database
    implementation("androidx.room:room-runtime:2.6.0")
    implementation("androidx.room:room-ktx:2.6.0")
    kapt("androidx.room:room-compiler:2.6.0")
    
    // Dependency Injection - Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // JSON serialization for Room converters
    implementation("com.google.code.gson:gson:2.10.1")
    
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
