# 🚀 EDGES - AI Notification Assistant
## Simple Android App Build Specification

> **Goal**: Build a working Android app that reads notifications, detects meetings using AI, and schedules them in calendar. Keep it SIMPLE and FUNCTIONAL. ALL api keys and request to be send directly by device no backend a simple all functions from the device and make the things work maybe we will use the api and other things which would help in google calendar things..

---

## 📱 What This App Does

**EDGES** monitors your Android notifications, uses Google's Gemini AI to detect meeting requests, and lets you schedule them in your calendar with one tap.

**Example Flow (AI as Agent):**
1. WhatsApp notification: "Team meeting on March 15 at 2 PM"
2. AI analyzes and decides: "schedule_meeting" (high confidence)
3. AI provides complete calendar event data in JSON
4. App automatically schedules in calendar (no user action needed!)
5. User gets notification: "Meeting scheduled: Team meeting"

**Alternative Flow (AI asks user):**
1. WhatsApp notification: "Let's meet tomorrow afternoon"
2. AI analyzes and decides: "ask_user" (unclear time)
3. AI provides best guess for meeting details
4. App shows card: "Meeting detected - Schedule for 2 PM?"
5. User confirms or edits → Event created

---

## 🎯 Core Features (Keep Simple)

1. **Read Notifications** - Capture all incoming notifications in real-time
2. **AI Agent Analysis** - AI analyzes, decides action, provides complete data
3. **Auto-Schedule** - AI schedules meetings automatically when confident
4. **Show Suggestions** - AI asks user when details unclear
5. **Background Operation** - Work continuously even when phone is locked

**Key Difference from Normal Apps:**
- AI is not just a detector - it's an AGENT that makes decisions
- AI provides complete, ready-to-use calendar event data
- App executes AI's decisions (auto-schedule or ask user)
- Less user interaction needed for clear meetings

---

## 🏗️ Tech Stack (Use Latest Stable Versions)

```
Platform: Native Android (Android Studio Ladybug 2024.2.1+)
Language: Kotlin
UI: Jetpack Compose (modern declarative UI)
Architecture: MVVM (Model-View-ViewModel)
Storage: JSON files on device (NO complex database)
Background: NotificationListenerService + Foreground Service
AI: Google Gemini API (gemini-2.5-flash)
Calendar: Google Calendar API v3 + Native Calendar fallback
```

**Why These Choices:**
- Kotlin: Modern, concise, official Android language
- Jetpack Compose: Easy to build beautiful UIs
- MVVM: Clean separation of UI and logic
- JSON files: Simple storage, no database complexity
- Gemini API: Free, powerful AI for text analysis
- Dual calendar: Works even if Google Calendar fails

---

## 📂 Simple Project Structure

```
app/
├── data/
│   ├── models/          # Data classes (Notification, Meeting, etc.)
│   ├── storage/         # JSON file storage manager
│   └── api/             # Gemini API and Calendar API calls
├── ui/
│   ├── screens/         # All app screens (Home, Permissions, etc.)
│   ├── components/      # Reusable UI components
│   └── theme/           # Colors, typography, styles
├── services/
│   ├── NotificationListener.kt    # Captures notifications
│   ├── AIAnalyzer.kt              # Gemini API integration
│   └── CalendarManager.kt         # Calendar operations
└── utils/
    ├── JsonStorage.kt             # Save/load JSON files
    └── Permissions.kt             # Handle permissions
```

---

## 🤖 AI Agent Architecture (CRITICAL CONCEPT)

### What is AI Agent?

**Traditional Approach (NOT what we want):**
```
Notification → AI detects meeting → Show to user → User clicks → App schedules
```

**AI Agent Approach (WHAT WE WANT):**
```
Notification → AI detects + decides + provides data → App executes AI's decision
```

### How AI Agent Works

**Step 1: AI Analyzes**
- Reads notification content
- Understands context and intent
- Extracts all relevant information

**Step 2: AI Decides**
- Determines confidence level
- Chooses appropriate action
- Considers user preferences and context

**Step 3: AI Provides Complete Data**
- Formats calendar event data
- Converts times to proper format
- Adds all necessary fields
- Makes it ready to use

**Step 4: App Executes**
- Takes AI's decision
- Uses AI's provided data
- Executes the action
- Reports back to user

### AI Agent Actions

**1. schedule_meeting (Auto-schedule)**
- **When**: High confidence (>0.8), clear details
- **AI Provides**: Complete calendarRequest ready for Google Calendar API
- **App Does**: Sends request directly to calendar, no user confirmation
- **User Sees**: "Meeting scheduled: [title]"

**2. ask_user (Show suggestion)**
- **When**: Medium confidence (0.5-0.8), some unclear details
- **AI Provides**: Best guess calendarRequest + meetingDetails
- **App Does**: Shows card with details, user can edit/confirm
- **User Sees**: Meeting card with "Schedule" button

**3. dismiss (Ignore)**
- **When**: Low confidence (<0.5), not a meeting
- **AI Provides**: Reason for dismissal
- **App Does**: Marks as processed, doesn't show anything
- **User Sees**: Nothing (silent)

**4. remind_later (Future reminder)**
- **When**: Meeting detected but time is far in future
- **AI Provides**: Reminder time and meeting details
- **App Does**: Sets reminder, asks user closer to time
- **User Sees**: Reminder notification later

### AI Agent JSON Structure

**Complete AI Response:**
```json
{
  "meetingDetected": true,
  "confidence": 0.9,
  "action": "schedule_meeting",
  
  "meetingDetails": {
    "title": "Project Review",
    "datetime": "2024-03-15T14:00:00+05:30",
    "duration": 60,
    "attendees": ["john@company.com", "sarah@company.com"],
    "location": "Conference Room A",
    "description": "Quarterly project review meeting"
  },
  
  "calendarRequest": {
    "summary": "Project Review",
    "description": "Quarterly project review meeting (scheduled from WhatsApp)",
    "location": "Conference Room A",
    "start": {
      "dateTime": "2024-03-15T14:00:00+05:30",
      "timeZone": "Asia/Kolkata"
    },
    "end": {
      "dateTime": "2024-03-15T15:00:00+05:30",
      "timeZone": "Asia/Kolkata"
    },
    "attendees": [
      {"email": "john@company.com", "displayName": "John"},
      {"email": "sarah@company.com", "displayName": "Sarah"}
    ],
    "reminders": {
      "useDefault": false,
      "overrides": [
        {"method": "email", "minutes": 1440},
        {"method": "popup", "minutes": 10}
      ]
    },
    "colorId": "1",
    "status": "confirmed"
  },
  
  "reasoning": "Clear meeting with specific date, time, location, and attendees. High confidence for auto-scheduling.",
  "category": "meeting",
  "priority": "high"
}
```

**Key Points:**
- `calendarRequest` is EXACTLY what Google Calendar API expects
- No app processing needed - just send it directly
- AI handles all formatting, timezone, calculations
- AI adds reminders, colors, status automatically

### App Implementation Flow

**When Notification Arrives:**

1. **Capture notification**
   - Extract all content
   - Save to notifications.json

2. **Send to AI Agent**
   - Build prompt with notification data
   - Include current date/time for context
   - Request structured JSON response

3. **Receive AI Decision**
   - Parse JSON response
   - Check `action` field
   - Validate `calendarRequest` if present

4. **Execute Action**
   ```
   IF action == "schedule_meeting":
       → Take calendarRequest from AI
       → Send POST to Google Calendar API
       → Save event ID
       → Show success notification
   
   ELSE IF action == "ask_user":
       → Show meeting card with details
       → Pre-fill with AI's calendarRequest
       → Let user edit/confirm
       → Then schedule
   
   ELSE IF action == "dismiss":
       → Mark as processed
       → Don't show anything
   
   ELSE IF action == "remind_later":
       → Save reminder
       → Schedule notification for later
   ```

5. **Update Storage**
   - Save AI response to meetings.json
   - Link to notification ID
   - Track status (pending/scheduled/dismissed)

### Why AI Agent is Better

**Traditional Approach Problems:**
- App must parse dates/times (complex)
- App must format calendar data (error-prone)
- App must handle timezones (difficult)
- User must always confirm (annoying)

**AI Agent Benefits:**
- AI does all the hard work
- AI provides ready-to-use data
- AI makes intelligent decisions
- Auto-scheduling when confident
- Less code, fewer bugs
- More accurate results

### Example Scenarios

**Scenario 1: Clear Meeting**
```
Notification: "Team standup tomorrow at 10 AM in Zoom"
AI Action: "schedule_meeting"
AI Provides: Complete calendar event
App Does: Auto-schedules
Result: Meeting in calendar, user notified
```

**Scenario 2: Unclear Time**
```
Notification: "Let's meet next week to discuss project"
AI Action: "ask_user"
AI Provides: Best guess (next Monday 2 PM)
App Does: Shows suggestion card
Result: User confirms/edits, then schedules
```

**Scenario 3: Not a Meeting**
```
Notification: "Your Amazon package was delivered"
AI Action: "dismiss"
AI Provides: Reason
App Does: Nothing
Result: Silent, no user interruption
```

**Scenario 4: Future Meeting**
```
Notification: "Conference in December 2024"
AI Action: "remind_later"
AI Provides: Reminder date (November 2024)
App Does: Sets reminder
Result: User reminded closer to date
```

---

## 🔌 API Integration (Critical Details)

### Google Gemini API

**Documentation**: https://ai.google.dev/gemini-api/docs/text-generation

**What You Need:**
- API Key: `AIzaSyCCZge7wkf8xhkiBD9PeHuEoASh_SybXBY` (hardcode in app)
- Model: `gemini-2.5-flash`
- Endpoint: `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent`

**How It Works:**
1. Send notification text to Gemini
2. Ask: "Is this a meeting? Extract details"
3. Get JSON response with meeting info
4. Use `responseSchema` to guarantee JSON format

**Important Settings:**
- `responseMimeType`: "application/json" (forces JSON output)
- `responseSchema`: Define exact JSON structure you want
- `temperature`: 0.2 (low = consistent results)
- `maxOutputTokens`: 500 (enough for meeting details)

**Response Schema (Tell AI exactly what to return):**
```
{
  "meetingDetected": true/false,
  "confidence": 0.0 to 1.0,
  "title": "Meeting title",
  "time": "ISO datetime or null",
  "duration": 60 (minutes),
  "attendees": ["email1", "email2"],
  "location": "place or null",
  "category": "meeting/payment/social/work/other",
  "priority": "high/medium/low"
}
```

**Error Handling:**
- If API fails, use simple keyword matching (fallback)
- Retry 3 times with 1-second delays
- Show user-friendly error messages

### Google Calendar API

**Documentation**: https://developers.google.com/calendar/api/v3/reference/events/insert

**What You Need:**
- OAuth 2.0 setup (Google Cloud Console)
- Scopes: calendar.readonly, calendar.events
- Endpoint: `https://www.googleapis.com/calendar/v3/calendars/primary/events`

**How It Works:**
1. User signs in with Google account
2. Get access token
3. Create event with POST request
4. Store event ID for reference

**Event Format:**
```
{
  "summary": "Meeting Title",
  "start": {"dateTime": "2024-03-15T14:00:00+05:30", "timeZone": "Asia/Kolkata"},
  "end": {"dateTime": "2024-03-15T15:00:00+05:30", "timeZone": "Asia/Kolkata"},
  "attendees": [{"email": "person@example.com"}],
  "reminders": {"overrides": [{"method": "popup", "minutes": 10}]}
}
```

**Native Calendar Fallback:**
- Use Android's CalendarContract
- No internet needed
- Works offline
- Simpler but less features

---

## 💾 Data Storage (Simple JSON Files)

**NO Database - Use JSON Files Instead**

**Why JSON Files:**
- Simple to read/write
- Easy to debug
- No migration headaches
- Perfect for this app size

**Storage Structure:**
```
/data/data/com.edgesmobile/files/
├── notifications.json       # All captured notifications
├── meetings.json            # Meeting suggestions
└── preferences.json         # User settings
```

**notifications.json Format:**
```json
[
  {
    "id": "notif_123",
    "appName": "WhatsApp",
    "title": "John Doe",
    "content": "Meeting tomorrow at 2 PM?",
    "timestamp": 1710504000000,
    "meetingDetected": true,
    "aiConfidence": 0.85,
    "category": "meeting",
    "priority": "high",
    "isRead": false
  }
]
```

**meetings.json Format:**
```json
[
  {
    "id": "meeting_456",
    "notificationId": "notif_123",
    "title": "Meeting with John",
    "suggestedTime": 1710590400000,
    "duration": 60,
    "attendees": ["john@example.com"],
    "confidence": 0.85,
    "status": "pending",
    "calendarEventId": null,
    "createdAt": 1710504000000
  }
]
```

**Storage Operations:**
- Load: Read JSON file, parse to objects
- Save: Convert objects to JSON, write file
- Update: Load → Modify → Save
- Delete: Filter out item → Save

---

## 🎨 UI/UX Design (Modern & Beautiful)

### Design System

**Colors:**
- Primary: #6c5cff (Purple - for buttons, highlights)
- Secondary: #10b981 (Green - for success)
- Background: #f6f5f8 (Light gray)
- Surface: #ffffff (White cards)
- Text: #1e293b (Dark gray)
- Error: #ef4444 (Red)

**Typography:**
- Display: 32sp, Bold (screen titles)
- Headline: 24sp, Bold (section headers)
- Title: 20sp, SemiBold (card titles)
- Body: 16sp, Regular (main text)
- Caption: 14sp, Regular (small text)

**Spacing:**
- Small: 8dp
- Medium: 16dp
- Large: 24dp
- XLarge: 32dp

**Card Style (Glassmorphism):**
- White background with 80% opacity
- 16dp corner radius
- Subtle shadow
- Blur effect on background

### Screen Layouts

**1. Onboarding Screen**
- Welcome message
- 3 feature cards (Monitor, Analyze, Schedule)
- "Get Started" button
- Skip option

**2. Permission Setup Screen**
- Progress indicator (1/3, 2/3, 3/3)
- Permission cards:
  - Notification Access (Required)
  - Calendar Access (Required)
  - Battery Optimization (Optional)
- Each card shows: Icon, Title, Description, Status, Action button
- "Continue" button (enabled when required permissions granted)

**3. Home Dashboard Screen**
- Header: "Hello, User" + wellness score
- Stats cards (2x2 grid):
  - Total Notifications
  - Meetings Detected
  - Priority Alerts
  - Health Status
- "Needs Attention" section:
  - Meeting suggestion cards
  - Each card: App icon, Title, Content preview, Confidence badge, Actions (Add to Calendar, Dismiss)
- Bottom navigation: Home, Meetings, Profile

**4. Meeting Suggestions Screen**
- Header: "Meeting Suggestions" + count
- Calendar provider status
- List of meeting cards:
  - Confidence indicator (color-coded)
  - Meeting title
  - Suggested time
  - Duration
  - Attendees
  - Actions: Schedule, Reject

**5. Profile Screen**
- User info section
- Calendar connection status
- AI service status
- Settings toggles
- Sign out button

### UI Components

**GlassCard:**
- Rounded corners
- Semi-transparent white background
- Subtle shadow
- Padding inside

**NotificationCard:**
- App icon (left)
- Title and content (center)
- Timestamp (top right)
- Action buttons (bottom)
- Confidence badge (if meeting)

**MeetingCard:**
- Confidence indicator (colored bar)
- Meeting icon
- Title, time, duration
- Attendees list
- Location (if available)
- Two buttons: Schedule (primary), Reject (secondary)

**StatsCard:**
- Large number (center)
- Label (below number)
- Icon (top)
- Background color based on type

---

## 🔧 Implementation Guidelines

### 1. Notification Capture (Critical)

**Requirements:**
- Capture ALL notification content (title, text, big text, expanded content)
- Work in background 24/7
- Handle device locked state
- Filter out system notifications
- Extract complete information

**Key Points:**
- Use NotificationListenerService
- Request notification access permission
- Start as foreground service (shows persistent notification)
- Use wake lock for background operation
- Extract all extras from notification bundle

### 2. AI Analysis (Critical) - AI AS AN AGENT

**🤖 AI Agent Concept:**
The AI doesn't just analyze - it DECIDES and TRIGGERS actions automatically!

**How AI Agent Works:**
1. Notification arrives → AI analyzes it
2. AI detects meeting → AI decides what action to take
3. AI returns JSON with action instructions
4. App executes the action AI requested
5. AI can trigger: schedule_meeting, send_reminder, dismiss, ask_user

**Requirements:**
- Send notification to Gemini API
- AI returns structured JSON with ACTION to perform
- AI provides ALL data needed for that action
- App executes the action based on AI's decision
- Handle API failures gracefully
- Fallback to keyword matching

**Key Points:**
- Use responseSchema for guaranteed JSON
- Set low temperature (0.2) for consistency
- AI decides the action, not just detects
- Include clear prompt with examples
- Implement retry logic (3 attempts)
- Cache responses to avoid duplicate calls
- Track API failures and disable if too many errors

**AI Agent Prompt Template:**
```
You are an intelligent agent that analyzes notifications and decides actions.

NOTIFICATION:
Title: [title]
Content: [content]
App: [app name]
Time: [timestamp]

YOUR JOB:
1. Analyze if this is a meeting/appointment
2. Extract all meeting details
3. DECIDE what action to take
4. Provide ALL information needed for that action

RESPOND WITH JSON ONLY:
{
  "meetingDetected": boolean,
  "confidence": 0.0-1.0,
  "action": "schedule_meeting" | "ask_user" | "dismiss" | "remind_later",
  "meetingDetails": {
    "title": "extracted meeting title",
    "datetime": "2024-03-15T14:00:00+05:30" (ISO format),
    "duration": 60 (minutes),
    "attendees": ["email1@example.com", "email2@example.com"],
    "location": "place or null",
    "description": "full context from notification"
  },
  "calendarRequest": {
    "summary": "Meeting with John",
    "start": {"dateTime": "2024-03-15T14:00:00+05:30", "timeZone": "Asia/Kolkata"},
    "end": {"dateTime": "2024-03-15T15:00:00+05:30", "timeZone": "Asia/Kolkata"},
    "attendees": [{"email": "john@example.com"}],
    "location": "Conference Room A",
    "description": "Meeting scheduled from WhatsApp notification",
    "reminders": {"overrides": [{"method": "popup", "minutes": 10}]}
  },
  "reasoning": "why you chose this action",
  "category": "meeting/payment/social/work/other",
  "priority": "high/medium/low"
}

ACTION RULES:
- "schedule_meeting": High confidence (>0.8), clear time, auto-schedule
- "ask_user": Medium confidence (0.5-0.8), unclear details, show suggestion
- "dismiss": Low confidence (<0.5), not a meeting
- "remind_later": Meeting detected but time is far future

IMPORTANT:
- If action is "schedule_meeting", provide COMPLETE calendarRequest
- calendarRequest should be ready to send directly to Google Calendar API
- Extract timezone from context or use device timezone
- Calculate end time = start time + duration
- Format datetime as ISO 8601 with timezone

Examples:
1. "Meeting tomorrow at 2 PM with John"
   → action: "ask_user" (no exact date)
   → provide calendarRequest with best guess

2. "Team standup on March 15, 2024 at 10 AM"
   → action: "schedule_meeting" (clear details)
   → provide complete calendarRequest

3. "Your package was delivered"
   → action: "dismiss" (not a meeting)
```

**AI Agent Response Format:**
```json
{
  "meetingDetected": true,
  "confidence": 0.85,
  "action": "schedule_meeting",
  "meetingDetails": {
    "title": "Team Standup",
    "datetime": "2024-03-15T10:00:00+05:30",
    "duration": 30,
    "attendees": ["team@company.com"],
    "location": "Zoom",
    "description": "Daily team standup meeting"
  },
  "calendarRequest": {
    "summary": "Team Standup",
    "start": {
      "dateTime": "2024-03-15T10:00:00+05:30",
      "timeZone": "Asia/Kolkata"
    },
    "end": {
      "dateTime": "2024-03-15T10:30:00+05:30",
      "timeZone": "Asia/Kolkata"
    },
    "attendees": [
      {"email": "team@company.com"}
    ],
    "location": "Zoom",
    "description": "Daily team standup meeting scheduled from notification",
    "reminders": {
      "overrides": [
        {"method": "popup", "minutes": 10}
      ]
    }
  },
  "reasoning": "Clear meeting with specific date, time, and attendees. High confidence.",
  "category": "meeting",
  "priority": "high"
}
```

**How App Uses AI Agent Response:**

1. **If action = "schedule_meeting":**
   - Take calendarRequest from AI response
   - Send directly to Google Calendar API
   - No user confirmation needed (auto-schedule)
   - Show notification: "Meeting scheduled!"

2. **If action = "ask_user":**
   - Show meeting suggestion card
   - Display meetingDetails to user
   - User can edit and confirm
   - Then use calendarRequest to schedule

3. **If action = "dismiss":**
   - Don't show anything
   - Mark as processed
   - Continue monitoring

4. **If action = "remind_later":**
   - Save for later
   - Show reminder before meeting time
   - Then ask user to schedule

**Benefits of AI Agent Approach:**
- AI makes intelligent decisions
- AI provides ready-to-use calendar data
- Less app logic needed
- More accurate scheduling
- AI handles timezone conversions
- AI formats everything correctly

### 3. Calendar Integration (Critical)

**Requirements:**
- Try Google Calendar first
- Fallback to native calendar if Google fails
- Handle timezone correctly
- Add reminders automatically
- Store event ID for reference

**Key Points:**
- OAuth 2.0 for Google Calendar
- Request calendar permissions for native
- Format datetime as RFC3339 (ISO 8601)
- Use device timezone
- Add 10-minute popup reminder
- Show success/error messages

### 4. Background Processing

**Requirements:**
- Work continuously
- Process notifications immediately
- Sync periodically
- Minimize battery usage
- Restart after device reboot

**Key Points:**
- Foreground service with notification
- Wake lock for critical operations
- WorkManager for periodic sync (every 30 minutes)
- Battery optimization exemption request
- Boot receiver to restart service

### 5. Error Handling

**Requirements:**
- Never crash the app
- Show user-friendly messages
- Log errors for debugging
- Retry failed operations
- Provide fallback options

**Key Points:**
- Try-catch all API calls
- Validate all user inputs
- Check permissions before operations
- Handle network errors
- Provide offline functionality

---

## 🚀 Build Instructions

### Step 1: Setup Project
1. Create new Android Studio project
2. Select "Empty Compose Activity"
3. Set minSdk = 26, targetSdk = 34
4. Choose Kotlin language

### Step 2: Add Dependencies
Add to build.gradle:
- Jetpack Compose (UI)
- Retrofit (API calls)
- Kotlinx Serialization (JSON)
- Coroutines (async operations)
- Google Sign-In (Calendar auth)

### Step 3: Configure APIs
1. Get Gemini API key from Google AI Studio
2. Setup Google Cloud project for Calendar API
3. Add API keys to BuildConfig
4. Configure OAuth 2.0 credentials

### Step 4: Implement Core Features
1. NotificationListenerService (capture notifications)
2. JSON storage manager (save/load data)
3. Gemini API integration (AI analysis)
4. Calendar API integration (schedule events)
5. UI screens with Compose

### Step 5: Add Permissions
In AndroidManifest.xml:
- BIND_NOTIFICATION_LISTENER_SERVICE
- POST_NOTIFICATIONS
- READ_CALENDAR, WRITE_CALENDAR
- INTERNET, ACCESS_NETWORK_STATE
- WAKE_LOCK, FOREGROUND_SERVICE
- RECEIVE_BOOT_COMPLETED

### Step 6: Test Thoroughly
1. Test notification capture
2. Test AI analysis
3. Test calendar scheduling
4. Test background operation
5. Test error scenarios

### Step 7: Build APK
1. Generate signed APK
2. Test on real device
3. Verify all features work
4. Check battery usage

---

## ✅ Success Criteria

**The app is successful if:**
1. ✅ Captures all incoming notifications
2. ✅ AI detects meetings with >80% accuracy
3. ✅ Schedules meetings in calendar with one tap
4. ✅ Works in background continuously
5. ✅ UI is beautiful and easy to use
6. ✅ No crashes or major bugs
7. ✅ Battery usage is reasonable (<5% per day)

---

## 🎯 Key Principles

1. **Keep It Simple** - No complex database, no backend, just JSON files
2. **Make It Work** - Focus on functionality over perfection
3. **Beautiful UI** - Modern glassmorphic design with Jetpack Compose
4. **Reliable** - Handle errors gracefully, never crash
5. **Fast** - Process notifications immediately, respond quickly
6. **Battery Friendly** - Optimize background operations
7. **User Friendly** - Clear messages, easy navigation, one-tap actions

---

## 📚 Essential Resources

**Official Documentation:**
- Gemini API: https://ai.google.dev/gemini-api/docs
- Gemini Structured Output: https://firebase.google.com/docs/ai-logic/generate-structured-output
- Google Calendar API: https://developers.google.com/calendar/api/v3/reference
- NotificationListenerService: https://developer.android.com/reference/android/service/notification/NotificationListenerService
- Jetpack Compose: https://developer.android.com/jetpack/compose
- MVVM Architecture: https://developer.android.com/topic/architecture

**Key Concepts:**
- Use `responseSchema` in Gemini API for guaranteed JSON output
- Use `responseMimeType: "application/json"` to force JSON
- Calendar events need RFC3339 datetime format
- NotificationListenerService needs special permission
- Foreground service needs notification to stay alive

---

## 🎨 Final Notes

**This is a SIMPLE app:**
- No complex backend
- No heavy database
- Just JSON files on device
- API keys hardcoded (for simplicity)
- Focus on core functionality

**Build it to WORK:**
- Test on real device
- Handle all errors
- Make UI beautiful
- Keep code clean
- Document important parts

**Make it USEFUL:**
- Actually detect meetings
- Actually schedule in calendar
- Actually work in background
- Actually help users

**That's it! Build this app and make it awesome! 🚀**
