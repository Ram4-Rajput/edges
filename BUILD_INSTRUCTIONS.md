# EDGES Notification Assistant - Build Instructions

## Project Status

✅ **Core Implementation Complete**

The EDGES Notification Assistant Android app has been successfully set up with all core functionality implemented.

## What's Been Implemented

### ✅ Core Components (100% Complete)

1. **Project Setup**
   - Android project structure with Kotlin
   - Gradle build configuration
   - All required dependencies (Retrofit, Kotlinx Serialization, Coroutines, Jetpack Compose)
   - Gradle wrapper (version 8.5)

2. **Data Models**
   - Notification.kt - Captured notification data
   - Meeting.kt - Meeting with AI analysis
   - AIResponse.kt - Gemini AI response structure
   - CalendarRequest.kt - Calendar event data
   - Preferences.kt - User settings

3. **Storage Layer**
   - JsonStorage.kt - JSON file operations for notifications, meetings, and preferences
   - Atomic file writes to prevent corruption
   - Graceful error handling

4. **AI Integration**
   - AIAnalyzer.kt - Gemini API integration with Retrofit
   - Confidence-based decision logic (schedule_meeting, ask_user, dismiss, remind_later)
   - Enhanced prompt template with detailed instructions
   - JSON schema for structured responses
   - Model: gemini-2.0-flash-exp
   - API Key: AIzaSyCCZge7wkf8xhkiBD9PeHuEoASh_SybXBY (hardcoded as specified)

5. **Calendar Integration**
   - CalendarManager.kt - Android CalendarContract API integration
   - Event creation in primary calendar
   - 10-minute reminders
   - Permission checking
   - Timezone handling

6. **Notification Monitoring**
   - EDGESNotificationListenerService.kt - Captures incoming notifications
   - Filters system notifications
   - Processes notifications through AI pipeline
   - Auto-schedules or creates suggestions based on AI decisions
   - Runs as foreground service

7. **System Integration**
   - BootReceiver.kt - Restarts service after device reboot
   - AndroidManifest.xml - All permissions and service declarations
   - Foreground service configuration

8. **User Interface**
   - MainActivity.kt - Welcome screen with permission requests
   - Material3 theme with EDGES branding (#6c5cff primary color)
   - Permission request flows

## Requirements Coverage

### ✅ Fully Implemented Requirements

- **Requirement 1**: Notification Capture (1.1-1.7) ✅
- **Requirement 2**: AI Agent Analysis (2.1-2.13) ✅
- **Requirement 3**: Automatic Meeting Scheduling (3.1-3.5) ✅
- **Requirement 5**: Background Operation (5.1-5.4) ✅
- **Requirement 6**: Data Storage (6.1-6.5) ✅
- **Requirement 7**: Calendar Integration (7.1-7.7) ✅
- **Requirement 9**: Permission Management (9.1-9.8) ✅
- **Requirement 11**: Error Handling (11.1-11.6) ✅
- **Requirement 12**: Gemini API Configuration (12.1-12.8) ✅
- **Requirement 15**: Calendar Data Formatting (15.1-15.7) ✅
- **Requirement 16**: App Lifecycle (16.1-16.7) ✅

### ⏳ Partially Implemented (UI Components)

- **Requirement 4**: Meeting Suggestion Display (4.1-4.8) - Backend complete, UI screens pending
- **Requirement 10**: User Interface Screens (10.1-10.10) - Welcome screen complete, dashboard/meetings screens pending
- **Requirement 13**: Meeting Reminder System (13.1-13.6) - Pending
- **Requirement 14**: Notification Categorization (14.1-14.5) - Pending

## How to Build and Run

### Prerequisites

1. **Android Studio** (Hedgehog or later)
2. **JDK 17** or later
3. **Android SDK** with API 26+ (Android 8.0 Oreo)
4. **Physical Android device** (API 26+) - Recommended for testing notification listener

### Build Steps

1. **Open Project in Android Studio**
   ```bash
   cd /path/to/edges
   # Open this directory in Android Studio
   ```

2. **Sync Gradle Files**
   - Android Studio will automatically prompt to sync
   - Or: File → Sync Project with Gradle Files

3. **Build the Project**
   - Build → Make Project
   - Or use terminal:
   ```bash
   ./gradlew build
   ```

4. **Run on Device**
   - Connect Android device via USB (enable USB debugging)
   - Run → Run 'app'
   - Or use terminal:
   ```bash
   ./gradlew installDebug
   ```

### Alternative: Command Line Build

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Build and install
./gradlew build installDebug
```

The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

## Setup Instructions for Users

1. **Install the App**
   - Install the APK on your Android device (API 26+)

2. **Grant Permissions**
   - Open the app
   - Tap "Grant Calendar Permissions"
   - Allow READ_CALENDAR and WRITE_CALENDAR permissions

3. **Enable Notification Access**
   - Tap "Enable Notification Access"
   - Find "EDGES" in the list
   - Toggle on notification access

4. **Start Using**
   - The app will now monitor notifications automatically
   - Meetings will be detected and scheduled based on AI confidence
   - Check your calendar for auto-scheduled meetings

## Testing the App

### Test Notification Detection

1. Send yourself a test notification with meeting content:
   - "Meeting tomorrow at 2 PM with John about project review"
   - "Calendar invite: Team standup on Friday at 10 AM"

2. Check the logs:
   ```bash
   adb logcat | grep EDGES
   ```

3. Verify:
   - Notification is captured
   - AI analyzes the notification
   - Meeting is created or scheduled based on confidence

### Test Calendar Integration

1. Grant calendar permissions
2. Send a high-confidence meeting notification
3. Check your device calendar for the scheduled event
4. Verify reminder is set (10 minutes before)

## Architecture Overview

```
EDGES/
├── app/
│   ├── src/main/
│   │   ├── java/com/edges/notificationassistant/
│   │   │   ├── data/                    # Data models
│   │   │   │   ├── Notification.kt
│   │   │   │   ├── Meeting.kt
│   │   │   │   ├── AIResponse.kt
│   │   │   │   ├── CalendarRequest.kt
│   │   │   │   └── Preferences.kt
│   │   │   ├── services/                # Background services
│   │   │   │   ├── AIAnalyzer.kt
│   │   │   │   ├── GeminiApiService.kt
│   │   │   │   ├── CalendarManager.kt
│   │   │   │   ├── EDGESNotificationListenerService.kt
│   │   │   │   └── BootReceiver.kt
│   │   │   ├── utils/                   # Utilities
│   │   │   │   └── JsonStorage.kt
│   │   │   ├── ui/theme/                # UI theme
│   │   │   │   ├── Color.kt
│   │   │   │   ├── Type.kt
│   │   │   │   └── Theme.kt
│   │   │   └── MainActivity.kt
│   │   ├── AndroidManifest.xml
│   │   └── res/                         # Resources
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## Key Technologies

- **Language**: Kotlin 1.9.22
- **UI**: Jetpack Compose (Material3)
- **Architecture**: MVVM
- **HTTP Client**: Retrofit 2.9.0
- **JSON**: Kotlinx Serialization 1.6.0
- **Async**: Coroutines 1.7.3
- **AI**: Google Gemini API (gemini-2.0-flash-exp)
- **Calendar**: Android CalendarContract API
- **Storage**: JSON files (on-device)
- **Min SDK**: 26 (Android 8.0 Oreo)
- **Target SDK**: 34 (Android 14)

## Known Limitations

1. **UI Incomplete**: Dashboard and meetings list screens are not yet implemented
2. **No Backend**: All data stored locally in JSON files
3. **Internet Required**: Gemini API requires active internet connection
4. **Single Calendar**: Uses device's primary calendar only
5. **No User Settings**: Settings UI not implemented (uses defaults)

## Next Steps for Full Implementation

To complete the remaining UI components:

1. **Home Screen Dashboard** (Task 8.4)
   - Statistics cards
   - Meeting suggestion cards with AI reasoning
   - Schedule/Dismiss buttons

2. **Meetings Screen** (Task 8.5)
   - List of all meetings
   - Filter tabs (Pending, Scheduled, Dismissed)
   - Meeting details dialog

3. **ViewModels** (Tasks 9.1, 9.2, 9.3)
   - HomeViewModel for dashboard state
   - MeetingsViewModel for meetings list
   - PermissionsViewModel for permission tracking

4. **Navigation** (Task 8.6)
   - Bottom navigation bar
   - Screen routing

5. **Additional Features**
   - Meeting reminders (Task 12)
   - Notification categorization (Task 13)
   - Loading states and animations (Task 18.2)

## Troubleshooting

### Build Errors

**Issue**: Gradle sync fails
- **Solution**: Ensure you have JDK 17+ and Android SDK installed
- Try: File → Invalidate Caches / Restart

**Issue**: Kotlin version mismatch
- **Solution**: The project uses Kotlin 1.9.22, ensure Android Studio is updated

### Runtime Issues

**Issue**: Notifications not being captured
- **Solution**: Ensure notification listener permission is granted in Settings

**Issue**: Calendar events not created
- **Solution**: Grant READ_CALENDAR and WRITE_CALENDAR permissions

**Issue**: AI analysis fails
- **Solution**: Check internet connection (Gemini API requires internet)

## Support

For issues or questions:
1. Check the logs: `adb logcat | grep EDGES`
2. Review the requirements document: `.kiro/specs/edges-notification-assistant/requirements.md`
3. Review the design document: `.kiro/specs/edges-notification-assistant/design.md`

## License

This project is for educational/demonstration purposes.

---

**Status**: Core functionality complete and ready for testing. UI components can be added incrementally.
