# NonStopAlarm 🚨

A specialized Android alarm application designed to ensure users wake up or respond to time-sensitive alerts through a unique non-stop alarm mechanism combined with cognitive engagement via math puzzles.

## Core Concept

Unlike traditional alarm apps that can be easily dismissed or snoozed, NonStopAlarm creates a persistent alert system that requires active problem-solving to disable, ensuring the user is fully awake and mentally engaged.

## Key Features

### 🔄 Non-Stop Alarm Mechanism
- **Persistent Playback**: Alarm continues playing regardless of app state (foreground, background, or closed)
- **No Snooze Function**: Cannot be paused or delayed
- **Time-Bounded**: Automatically stops at a user-defined end time

### 🧮 Math Puzzle Dismissal
- **Only Dismissal Method**: Solve a math puzzle to stop the alarm
- **Cognitive Engagement**: Simple but conscious-thought-requiring arithmetic
- **Randomized Problems**: Prevents memorization, ensures alertness
- **No Alternative Routes**: No bypass methods or volume control workarounds

### ⏰ Time Window Configuration
- **Start Time**: When the alarm begins playing
- **End Time**: When the alarm automatically stops (safety mechanism)
- **Duration Control**: Maximum 12-hour alarm window
- **Smart Scheduling**: Handles day transitions automatically

## How It Works

### 1. Setup
1. Set your desired **alarm start time**
2. Set your **alarm end time** (when alarm should automatically stop)
3. Tap "SET ALARM" to activate

### 2. Alarm Activation
- At start time, alarm begins playing immediately
- Continues non-stop until either:
  - ✅ You solve the math puzzle correctly
  - ⏰ End time is reached (automatic safety stop)

### 3. Dismissal Process
- Alarm launches math puzzle interface
- Solve simple arithmetic (addition, subtraction, multiplication)
- Enter correct answer to dismiss alarm
- Wrong answers generate new problems

## Technical Features

### 🔒 Persistent Operation
- **Foreground Service**: Maintains alarm even when app is closed
- **Wake Locks**: Keeps device awake during alarm
- **Boot Recovery**: Restores alarms after device restart
- **Battery Optimization**: Requests exemption from battery restrictions

### 🔊 Audio & Alerts
- **Maximum Volume**: Sets alarm volume to maximum
- **System Alarm Tones**: Uses device default alarm sounds
- **Vibration Patterns**: Additional tactile alerts
- **Fallback Audio**: Multiple audio sources for reliability

### 🛡️ Security & Safety
- **Time-Bounded Safety**: Always respects end time boundary
- **Permission Management**: Requests only necessary permissions
- **Data Privacy**: No external data transmission
- **Local Storage**: Alarm settings stored locally

## Installation Requirements

### Android Version
- **Minimum SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 14 (API 34)
- **Recommended**: Android 8.0+ for optimal performance

### Permissions Required
- `SCHEDULE_EXACT_ALARM` - Schedule precise alarm times
- `WAKE_LOCK` - Keep device awake during alarm
- `FOREGROUND_SERVICE` - Run persistent background service
- `MODIFY_AUDIO_SETTINGS` - Control alarm volume
- `RECEIVE_BOOT_COMPLETED` - Restore alarms after restart
- `SYSTEM_ALERT_WINDOW` - Display alarm over other apps
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` - Prevent system interference

## Usage Guidelines

### ⚠️ Important Considerations
- **Set Appropriate End Times**: Always set a reasonable end time as a safety mechanism
- **Battery Optimization**: Grant battery optimization exemption for reliable operation
- **Volume Settings**: Alarm will override current volume settings
- **Emergency Access**: End time provides automatic termination if you cannot respond

### 🎯 Best Practices
- **Test First**: Try the alarm when you can respond to ensure it works
- **Reasonable Durations**: Avoid extremely long alarm windows
- **Backup Plans**: Have alternative wake-up methods for critical situations
- **Clear Schedule**: Ensure you'll be available during the alarm window

## Development & Architecture

### Project Structure
```
app/
├── src/main/java/com/nonstop/alarm/
│   ├── MainActivity.kt              # Alarm configuration UI
│   ├── AlarmActivity.kt            # Math puzzle interface
│   ├── service/
│   │   └── AlarmService.kt         # Persistent alarm service
│   ├── receiver/
│   │   ├── AlarmReceiver.kt        # Alarm trigger handler
│   │   └── BootReceiver.kt         # Boot recovery handler
│   └── util/
│       └── AlarmManager.kt         # Alarm scheduling utilities
├── src/main/res/
│   ├── layout/                     # UI layouts
│   ├── values/                     # Strings, colors, themes
│   └── drawable/                   # Icons and backgrounds
└── src/main/AndroidManifest.xml    # App configuration
```

### Key Technologies
- **Kotlin**: Primary development language
- **Android Jetpack**: Modern Android development
- **AlarmManager**: System-level alarm scheduling
- **Foreground Services**: Persistent background operation
- **MediaPlayer**: Audio playback management
- **SharedPreferences**: Local data storage

## Building the Project

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 24-34
- Kotlin 1.9.10+
- Gradle 8.1.4+

### Build Steps
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle dependencies
4. Build and run on device/emulator

```bash
# Clone the repository
git clone <repository-url>
cd NonStopAlarm

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

## Contributing

This project follows the core principle of persistent, puzzle-based alarm dismissal. When contributing:

1. **Maintain Core Concept**: Non-stop alarm + math puzzle dismissal + time boundaries
2. **Avoid Feature Creep**: Focus on reliability over additional features
3. **Test Thoroughly**: Verify behavior across all app states
4. **Respect End Time**: This safety mechanism must never be bypassed

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Disclaimer

⚠️ **Important**: This app is designed for persistent alarm functionality. Always set appropriate end times and ensure you have alternative wake-up methods for critical situations. The developers are not responsible for oversleeping or missed alarms.

---

**Remember**: The goal is to ensure you're fully awake and mentally alert when dismissing the alarm. The math puzzle requirement serves this purpose and should not be bypassed or made optional.
