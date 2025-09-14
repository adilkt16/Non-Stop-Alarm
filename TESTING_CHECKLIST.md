# NonStopAlarm Testing Checklist

## Building and Installation

### Build APK (Android Studio Required)
Since the gradle wrapper needs Android Studio setup, please follow these steps:

1. **Open in Android Studio:**
   ```bash
   # Clone and open project
   git clone https://github.com/adilkt16/Non-Stop-Alarm.git
   cd Non-Stop-Alarm
   ```

2. **Build Debug APK:**
   - Open project in Android Studio
   - Select "Build" > "Build Bundle(s) / APK(s)" > "Build APK(s)"
   - Or use Terminal in Android Studio: `./gradlew assembleDebug`

3. **Install APK:**
   ```bash
   # Enable USB debugging on Android device
   # Connect device via USB
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

## Testing Guidelines

### 🎯 Core Functionality Testing

#### 1. Time Configuration (Clarity Principle)
- [ ] **Start Time Selection**
  - Tap "Select Start Time" 
  - Choose a time 10+ seconds in future (debug mode)
  - Verify time displays correctly in HH:MM format
  - Verify "Duration" section appears when both times set

- [ ] **End Time Selection**
  - Tap "Select End Time"
  - Choose time after start time
  - Verify duration calculation shows correct time span
  - Test various durations: 10s, 1m, 1h, 12h

#### 2. Validation Testing (Reliability Principle)
- [ ] **Invalid Time Windows**
  - Set end time before start time → Should show "⚠️ End time must be after start time"
  - Set start time in past → Should show "⚠️ Start time must be in the future"
  - Set duration < 10s (debug) → Should show "⚠️ Alarm duration must be at least 10 seconds"

- [ ] **Valid Time Windows**
  - Set valid time window → Duration displays correctly
  - "SET ALARM" button becomes enabled
  - No validation error messages shown

#### 3. Alarm Creation (Fairness Principle)
- [ ] **Successful Creation**
  - Set valid time window
  - Tap "SET ALARM"
  - Verify success message appears
  - Verify configuration clears after creation
  - Verify "Cancel Alarm" button becomes enabled

#### 4. Time-Bounded Operation (Respect Principle)
- [ ] **Alarm Activation**
  - Wait for start time (10s in debug mode)
  - Verify alarm activates automatically
  - Verify AlarmActivity launches with math puzzle
  - Verify persistent audio playback

- [ ] **Math Puzzle Dismissal**
  - Solve displayed math problem correctly
  - Verify alarm stops after correct answer
  - Test wrong answers → alarm continues

- [ ] **Automatic Termination**
  - Set short alarm window (30s)
  - Let alarm expire without solving puzzle
  - Verify alarm stops automatically at end time

### 🔧 Debug Mode Features

#### 1. Faster Testing
- [ ] **Reduced Timing Constraints**
  - Minimum duration: 10 seconds (vs 1 minute production)
  - Minimum future time: 5 seconds (vs 1 minute production)
  - App title shows "NonStop Alarm Debug"

#### 2. Quick Test Scenarios
- [ ] **10-Second Alarm**
  - Set start time 10 seconds from now
  - Set end time 20 seconds from now
  - Create alarm and verify 10-second operation

### 🎨 User Experience Validation

#### 1. Visual Clarity
- [ ] **Time Display**
  - Times show in clear HH:MM format
  - Duration calculation is accurate
  - Validation messages are prominent and clear

- [ ] **Status Feedback**
  - Success messages appear after actions
  - Error messages are descriptive and helpful
  - Button states change appropriately

#### 2. User Flow Completeness
- [ ] **Configuration → Creation → Activation → Dismissal**
  - Complete flow works end-to-end
  - Each step provides clear feedback
  - Users understand current state at all times

### 📱 Device Compatibility

#### 1. Permissions
- [ ] **Required Permissions Granted**
  - Exact alarm scheduling
  - Background operation
  - Audio playback
  - System alert window (for alarm overlay)

#### 2. Background Operation
- [ ] **App in Background**
  - Create alarm, minimize app
  - Verify alarm activates at scheduled time
  - Verify AlarmActivity appears on top

#### 3. Device Scenarios
- [ ] **Screen Lock**
  - Lock device during alarm
  - Verify alarm still audible
  - Verify AlarmActivity appears on lock screen

### 🚨 Edge Cases

#### 1. System Interactions
- [ ] **Phone Calls During Alarm**
  - Receive call while alarm active
  - Verify alarm pauses/resumes appropriately

#### 2. App State Recovery
- [ ] **App Force Close**
  - Create alarm, force close app
  - Verify alarm still activates at scheduled time

#### 3. Multiple Alarms
- [ ] **Conflict Prevention**
  - Try creating overlapping alarms
  - Verify system prevents conflicts

### ✅ Success Criteria

**UI and Validation:**
- ✓ Time selection is intuitive and clear
- ✓ Duration display helps users understand time window
- ✓ Validation prevents invalid configurations
- ✓ Error messages are helpful and specific

**Core Functionality:**
- ✓ Alarms activate precisely at start time
- ✓ Math puzzles must be solved to dismiss
- ✓ Alarms auto-terminate at end time
- ✓ Persistent operation survives app backgrounding

**User Experience:**
- ✓ Each step provides clear feedback
- ✓ Users understand time-bounded operation concept
- ✓ Debug mode enables rapid testing (10s minimum)
- ✓ Production mode enforces reasonable limits (1m minimum)

### 🐛 Known Issues to Test
- Gradle build setup (requires Android Studio)
- Permission dialogs on first launch
- Audio focus management during calls
- Battery optimization whitelist prompts

---

**Note:** Debug build allows 10-second alarms for rapid testing. Production build requires 1-minute minimum duration for practical use.
