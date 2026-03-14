# EDGES Notification Assistant

An AI-powered Android notification assistant that monitors incoming notifications, uses Google Gemini AI to detect meetings, and schedules them in the user's calendar.

## Project Structure

```
app/
├── src/main/
│   ├── java/com/edges/notificationassistant/
│   │   ├── data/              # Data models
│   │   │   ├── Notification.kt
│   │   │   ├── Meeting.kt
│   │   │   ├── AIResponse.kt
│   │   │   ├── CalendarRequest.kt
│   │   │   └── Preferences.kt
│   │   ├── services/          # Background services
│   │   ├── ui/                # Jetpack Compose UI
│   │   │   └── theme/         # App theme
│   │   ├── utils/             # Utility classes
│   │   └── MainActivity.kt
│   ├── res/                   # Resources
│   └── AndroidManifest.xml
└── build.gradle.kts
```

## Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Minimum SDK**: API 26 (Android 8.0 Oreo)
- **Target SDK**: API 34

## Key Dependencies

- Jetpack Compose (UI)
- Retrofit (HTTP client for Gemini API)
- Kotlinx Serialization (JSON parsing)
- Coroutines (Asynchronous programming)
- WorkManager (Background tasks)
- Android CalendarContract (Calendar integration)

## Data Models

### Notification
Represents a captured notification from the Android system.

### Meeting
Represents a detected meeting with AI analysis results.

### AIResponse
Structured response from Google Gemini AI containing meeting detection and decision.

### CalendarRequest
Calendar event creation request for scheduling meetings.

### Preferences
User preferences and app settings.

## MVVM Architecture

- **Model**: Data classes, storage manager, API clients
- **ViewModel**: Business logic and state management
- **View**: Jetpack Compose screens and components

## Required Permissions

- `INTERNET` - API calls to Gemini
- `READ_CALENDAR` / `WRITE_CALENDAR` - Calendar integration
- `BIND_NOTIFICATION_LISTENER_SERVICE` - Notification access
- `POST_NOTIFICATIONS` - Display notifications (Android 13+)
- `WAKE_LOCK` - Background operation
- `FOREGROUND_SERVICE` - Continuous monitoring
- `RECEIVE_BOOT_COMPLETED` - Auto-restart after reboot

## Build Instructions

1. Open project in Android Studio
2. Sync Gradle files
3. Build and run on device (API 26+)

## Next Steps

- Implement JSON storage manager (Task 2)
- Implement Gemini AI integration (Task 3)
- Implement calendar integration (Task 4)
- Build notification capture service (Task 6)
- Create UI screens (Task 8)

## Notes

- Uses hardcoded Gemini API key for simplicity
- No backend server - all API calls made directly from device
- JSON file storage in app private directory
- Requires internet connectivity for AI analysis
