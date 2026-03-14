# EDGES Notification Service Implementation Guide

## Overview

The EDGES notification service is a sophisticated Android system that automatically captures, analyzes, and processes notifications to detect meeting requests and schedule calendar events. The service runs as a foreground service and uses AI (Google Gemini) to intelligently analyze notification content.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                    EDGES Notification Service                   │
├─────────────────────────────────────────────────────────────────┤
│  1. Notification Capture (EDGESNotificationListenerService)    │
│  2. AI Analysis (AIAnalyzer + GeminiApiService)                │
│  3. Calendar Integration (CalendarManager)                     │
│  4. Data Persistence (JsonStorage)                             │
│  5. Auto-Start (BootReceiver)                                  │
└─────────────────────────────────────────────────────────────────┘
```

## Core Components

### 1. EDGESNotificationListenerService
**File**: `app/src/main/java/com/edges/notificationassistant/services/EDGESNotificationListenerService.kt`

**Purpose**: Main service that extends Android's NotificationListenerService to capture all incoming notifications.

**Key Features**:
- Runs as foreground service for reliability
- Filters out system notifications
- Extracts notification data (title, text, app name, timestamp)
- Processes notifications asynchronously using coroutines
- Integrates with AI analyzer and calendar manager

**Key Methods**:
- `onNotificationPosted()`: Called when new notification arrives
- `extractNotificationData()`: Converts Android notification to app data model
- `processNotification()`: Handles AI analysis and calendar scheduling
- `isSystemNotification()`: Filters out unwanted system notifications

### 2. AIAnalyzer
**File**: `app/src/main/java/com/edges/notificationassistant/services/AIAnalyzer.kt`

**Purpose**: Analyzes notifications using Google Gemini AI to detect meeting requests and determine appropriate actions.

**Key Features**:
- Uses Google Gemini 2.0 Flash model
- Structured JSON response with confidence scoring
- Context-aware prompts with current date/time
- Confidence-based action selection
- Meeting details extraction and validation

**AI Actions**:
- `schedule_meeting`: High confidence (>0.8) with complete details
- `ask_user`: Medium confidence (0.5-0.8) requiring user confirmation
- `dismiss`: Low confidence (<0.5) or no meeting detected
- `remind_later`: Meeting detected but >30 days in future

### 3. CalendarManager
**File**: `app/src/main/java/com/edges/notificationassistant/services/CalendarManager.kt`

**Purpose**: Handles calendar operations using Android's CalendarContract API.

**Key Features**:
- Creates calendar events in device's primary calendar
- Adds 10-minute reminders to events
- Handles permissions and error cases gracefully
- Parses ISO 8601 datetime formats

### 4. JsonStorage
**File**: `app/src/main/java/com/edges/notificationassistant/utils/JsonStorage.kt`

**Purpose**: Manages data persistence using JSON files in app's private directory.

**Key Features**:
- Stores notifications, meetings, and preferences
- Atomic file operations to prevent corruption
- Graceful error handling and recovery
- Thread-safe operations

**Data Files**:
- `notifications.json`: All captured notifications
- `meetings.json`: Detected meetings and their status
- `preferences.json`: User settings and preferences

### 5. BootReceiver
**File**: `app/src/main/java/com/edges/notificationassistant/services/BootReceiver.kt`

**Purpose**: Ensures service restarts automatically after device reboot.

## Data Models

### Notification
**File**: `app/src/main/java/com/edges/notificationassistant/data/Notification.kt`

```kotlin
data class Notification(
    val id: String,
    val title: String,
    val text: String,
    val expandedText: String? = null,
    val appName: String,
    val packageName: String,
    val timestamp: Long,
    val processed: Boolean = false
)
```

### Meeting
**File**: `app/src/main/java/com/edges/notificationassistant/data/Meeting.kt`

```kotlin
data class Meeting(
    val id: String,
    val notificationId: String,
    val title: String,
    val datetime: String, // ISO 8601 format
    val duration: Int, // minutes
    val location: String? = null,
    val description: String? = null,
    val attendees: List<String> = emptyList(),
    val confidence: Double,
    val action: String,
    val reasoning: String,
    val category: String? = null,
    val priority: String? = null,
    val status: MeetingStatus = MeetingStatus.PENDING,
    val calendarEventId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

### AIResponse
**File**: `app/src/main/java/com/edges/notificationassistant/data/AIResponse.kt`

```kotlin
data class AIResponse(
    val meetingDetected: Boolean,
    val confidence: Double,
    val action: String,
    val reasoning: String,
    val meetingDetails: MeetingDetails? = null,
    val category: String? = null,
    val priority: String? = null
)
```

## Service Flow

### 1. Notification Capture Flow
```
Incoming Notification → EDGESNotificationListenerService.onNotificationPosted()
                     → Filter system notifications
                     → Extract notification data
                     → Save to JsonStorage
                     → Process asynchronously
```

### 2. AI Analysis Flow
```
Notification → AIAnalyzer.analyzeNotification()
            → Build context-aware prompt
            → Call Gemini API with structured schema
            → Parse JSON response
            → Validate confidence and action
            → Return AIResponse
```

### 3. Calendar Integration Flow
```
AIResponse → Check action type
          → If "schedule_meeting": CalendarManager.createEvent()
          → Create calendar event with reminders
          → Update meeting status to SCHEDULED
          → Save to JsonStorage
```

## Required Permissions

**File**: `app/src/main/AndroidManifest.xml`

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_CALENDAR" />
<uses-permission android:name="android.permission.WRITE_CALENDAR" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

## Service Registration

**File**: `app/src/main/AndroidManifest.xml`

```xml
<!-- Notification Listener Service -->
<service
    android:name=".services.EDGESNotificationListenerService"
    android:exported="true"
    android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE">
    <intent-filter>
        <action android:name="android.service.notification.NotificationListenerService" />
    </intent-filter>
</service>

<!-- Boot Receiver -->
<receiver
    android:name=".services.BootReceiver"
    android:enabled="true"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
    </intent-filter>
</receiver>
```

## Key Implementation Details

### 1. Foreground Service
The service runs as a foreground service to ensure it continues running even when the app is in the background:

```kotlin
override fun onCreate() {
    super.onCreate()
    startForeground(FOREGROUND_NOTIFICATION_ID, createForegroundNotification())
}
```

### 2. System Notification Filtering
Filters out unwanted system notifications:

```kotlin
private val SYSTEM_PACKAGES = setOf(
    "android",
    "com.android.systemui",
    "com.android.providers",
    "com.google.android.packageinstaller"
)
```

### 3. Asynchronous Processing
Uses coroutines for non-blocking notification processing:

```kotlin
private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

serviceScope.launch {
    processNotification(notification)
}
```

### 4. AI Integration
Structured prompts with JSON schema for consistent responses:

```kotlin
private fun buildPrompt(notification: Notification): String {
    // Context-aware prompt with current date/time
    // Detailed instructions for confidence scoring
    // Action selection guidelines
}
```

### 5. Error Handling
Comprehensive error handling throughout the pipeline:

```kotlin
try {
    // Process notification
} catch (e: Exception) {
    Log.e(TAG, "Error processing notification", e)
}
```

## Files Required for Implementation

### Core Service Files
1. `EDGESNotificationListenerService.kt` - Main notification listener service
2. `AIAnalyzer.kt` - AI analysis engine
3. `CalendarManager.kt` - Calendar integration
4. `GeminiApiService.kt` - Retrofit API interface
5. `BootReceiver.kt` - Auto-start after reboot

### Data Model Files
6. `Notification.kt` - Notification data structure
7. `Meeting.kt` - Meeting data structure
8. `AIResponse.kt` - AI response structure
9. `CalendarRequest.kt` - Calendar request structure
10. `Preferences.kt` - User preferences structure

### Utility Files
11. `JsonStorage.kt` - Data persistence utility
12. `MeetingDetailsProcessor.kt` - Meeting data validation

### Configuration Files
13. `AndroidManifest.xml` - Service registration and permissions
14. `build.gradle.kts` - Dependencies and build configuration

## Dependencies Required

```kotlin
// Retrofit for API calls
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

// Kotlinx Serialization for JSON
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
```

## Setup Instructions

1. **Copy all required files** to your project maintaining the package structure
2. **Add dependencies** to your `build.gradle.kts`
3. **Update AndroidManifest.xml** with service registration and permissions
4. **Configure Gemini API key** in `AIAnalyzer.kt`
5. **Request notification access** permission from user
6. **Enable notification listener** in device settings

## Key Features

- **Automatic notification capture** from all apps
- **AI-powered meeting detection** with confidence scoring
- **Smart action selection** based on confidence levels
- **Automatic calendar scheduling** for high-confidence meetings
- **Persistent data storage** with error recovery
- **Foreground service** for reliability
- **Auto-restart** after device reboot
- **System notification filtering** to avoid noise

This implementation provides a robust, production-ready notification service that can intelligently process meeting requests and integrate with the device's calendar system.