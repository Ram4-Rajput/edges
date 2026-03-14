# Requirements Document

## Introduction

EDGES is an AI-powered Android notification assistant that monitors incoming notifications, uses Google Gemini AI to intelligently detect and analyze meeting requests, and automatically schedules them in the user's calendar. The app operates as a native Android application (API 26+) with no backend server, using on-device JSON storage and direct API calls. The AI functions as an autonomous agent that makes scheduling decisions based on confidence levels, either auto-scheduling meetings or presenting suggestions to the user for confirmation. The app requires internet connectivity to function (Gemini API requirement).

## Glossary

- **EDGES_App**: The Android notification assistant application
- **Notification_Listener**: The Android NotificationListenerService that captures incoming notifications
- **AI_Agent**: The Google Gemini AI service that analyzes notifications and makes scheduling decisions
- **Calendar_Manager**: The component that interfaces with native Android CalendarContract API
- **Meeting_Suggestion**: A detected meeting that requires user confirmation before scheduling
- **Auto_Schedule**: The process of scheduling a meeting without user confirmation when AI confidence is high
- **Confidence_Score**: A numerical value (0.0-1.0) indicating AI certainty about meeting detection
- **Calendar_Request**: A structured JSON object containing calendar event data
- **Foreground_Service**: An Android service that runs continuously with a persistent notification
- **JSON_Storage**: On-device file-based storage using JSON format

## Requirements

### Requirement 1: Notification Capture

**User Story:** As a user, I want the app to capture all my incoming notifications in real-time, so that no meeting requests are missed.

#### Acceptance Criteria

1. THE Notification_Listener SHALL capture all incoming notification content including title, text, expanded text, and metadata
2. THE Notification_Listener SHALL extract the source application name and package identifier from each notification
3. THE Notification_Listener SHALL record the timestamp when each notification is received
4. WHEN a notification is captured, THE EDGES_App SHALL save it to the notifications.json file
5. THE Notification_Listener SHALL operate continuously in the background even when the device is locked
6. THE Notification_Listener SHALL filter out system notifications from Android OS
7. WHEN the device reboots, THE EDGES_App SHALL automatically restart the Notification_Listener service

### Requirement 2: AI Agent Analysis and Decision Making

**User Story:** As a user, I want AI to analyze my notifications and automatically decide what action to take, so that I don't have to manually review every notification.

#### Acceptance Criteria

1. WHEN a notification is captured, THE AI_Agent SHALL analyze the notification content
2. THE AI_Agent SHALL return a structured JSON response containing meetingDetected, confidence, action, meetingDetails, and reasoning fields
3. THE AI_Agent SHALL calculate a Confidence_Score between 0.0 and 1.0 for each meeting detection
4. THE AI_Agent SHALL decide one of four actions: schedule_meeting, ask_user, dismiss, or remind_later
5. WHEN the Confidence_Score is greater than 0.8, THE AI_Agent SHALL set action to schedule_meeting
6. WHEN the Confidence_Score is between 0.5 and 0.8, THE AI_Agent SHALL set action to ask_user
7. WHEN the Confidence_Score is less than 0.5, THE AI_Agent SHALL set action to dismiss
8. WHEN a meeting is detected but the datetime is more than 30 days in the future, THE AI_Agent SHALL set action to remind_later
9. THE AI_Agent SHALL provide meeting details with title, datetime, duration, location, and description
10. THE AI_Agent SHALL parse and convert all date and time expressions to ISO 8601 format with timezone
11. THE AI_Agent SHALL calculate meeting end time based on start time and duration
12. THE AI_Agent SHALL extract attendee email addresses when present in the notification
13. THE AI_Agent SHALL provide reasoning for its decision in simple, user-friendly language

### Requirement 3: Automatic Meeting Scheduling

**User Story:** As a user, I want meetings with clear details to be automatically scheduled in my calendar, so that I don't have to manually confirm obvious meetings.

#### Acceptance Criteria

1. WHEN the AI_Agent returns action schedule_meeting, THE Calendar_Manager SHALL automatically create a calendar event without user confirmation
2. THE Calendar_Manager SHALL use Android CalendarContract API to insert events into the device's primary calendar
3. WHEN a meeting is successfully scheduled, THE EDGES_App SHALL save the calendar event ID to meetings.json
4. WHEN a meeting is successfully scheduled, THE EDGES_App SHALL display a notification to the user with the meeting title
5. THE Calendar_Manager SHALL add a 10-minute alert reminder to all auto-scheduled meetings

### Requirement 4: Meeting Suggestion Display and User Actions

**User Story:** As a user, I want to see meeting suggestions with AI reasoning and easily accept or reject them, so that I can quickly decide what to schedule.

#### Acceptance Criteria

1. WHEN the AI_Agent returns action ask_user, THE EDGES_App SHALL display a Meeting_Suggestion card in the user interface
2. THE Meeting_Suggestion card SHALL display the meeting title, suggested datetime, duration, location, and attendees
3. THE Meeting_Suggestion card SHALL display the AI Confidence_Score as a colored badge (green for high, yellow for medium)
4. THE Meeting_Suggestion card SHALL display the AI reasoning in simple bullet points or brief text
5. THE Meeting_Suggestion card SHALL provide a "Schedule" button and a "Dismiss" button
6. WHEN the user taps the "Schedule" button, THE Calendar_Manager SHALL create the calendar event
7. WHEN the user taps the "Dismiss" button, THE EDGES_App SHALL mark the suggestion as dismissed and remove it from view
8. THE EDGES_App SHALL show a confirmation message after scheduling or dismissing

### Requirement 5: Background Operation

**User Story:** As a user, I want the app to work continuously in the background, so that it monitors notifications even when I'm not actively using my phone.

#### Acceptance Criteria

1. THE EDGES_App SHALL run the Notification_Listener as a Foreground_Service with a persistent notification
2. THE Foreground_Service SHALL display a notification showing the service status
3. THE EDGES_App SHALL process notifications efficiently in the background
4. THE EDGES_App SHALL automatically restart the service if terminated by the system

### Requirement 6: Data Storage and Persistence

**User Story:** As a user, I want my notification history and meeting data to be saved on my device, so that I can review past detections and scheduled meetings.

#### Acceptance Criteria

1. THE EDGES_App SHALL store all captured notifications in a notifications.json file in the app's private storage directory
2. THE EDGES_App SHALL store all meeting detections and suggestions in a meetings.json file
3. THE EDGES_App SHALL store user preferences and settings in a preferences.json file
4. WHEN saving data, THE JSON_Storage SHALL write files safely to prevent data corruption
5. WHEN loading data, THE JSON_Storage SHALL validate JSON structure and handle parsing errors gracefully

### Requirement 7: Native Calendar Integration

**User Story:** As a user, I want meetings to be scheduled in my device calendar, so that they appear in my calendar app and sync with my accounts.

#### Acceptance Criteria

1. THE Calendar_Manager SHALL use Android CalendarContract API to create calendar events
2. THE Calendar_Manager SHALL request READ_CALENDAR and WRITE_CALENDAR permissions
3. THE Calendar_Manager SHALL insert events into the device's primary calendar account
4. THE Calendar_Manager SHALL convert the AI-provided meeting data to CalendarContract ContentValues format
5. WHEN creating an event, THE Calendar_Manager SHALL extract and save the event ID from the returned URI
6. THE Calendar_Manager SHALL set the event timezone to match the device's current timezone
7. IF the calendar insertion fails, THEN THE Calendar_Manager SHALL return a descriptive error message

### Requirement 8: Calendar Reminder Configuration

**User Story:** As a user, I want reminders added to scheduled meetings, so that I don't forget about them.

#### Acceptance Criteria

1. WHEN a calendar event is created, THE Calendar_Manager SHALL add a reminder using CalendarContract.Reminders
2. THE Calendar_Manager SHALL set the reminder method to METHOD_ALERT for popup notifications
3. THE Calendar_Manager SHALL set the reminder time to 10 minutes before the event
4. IF adding the reminder fails, THE Calendar_Manager SHALL log the error but still consider the event creation successful

### Requirement 9: Permission Management

**User Story:** As a user, I want to be guided through granting necessary permissions, so that I understand why each permission is needed and can enable them easily.

#### Acceptance Criteria

1. WHEN the app is first launched, THE EDGES_App SHALL display a permission setup screen
2. THE EDGES_App SHALL request notification listener access permission
3. THE EDGES_App SHALL request calendar read and write permissions (READ_CALENDAR, WRITE_CALENDAR)
4. THE EDGES_App SHALL request POST_NOTIFICATIONS permission for Android 13 and above
5. THE permission setup screen SHALL display the status of each permission as granted or not granted
6. THE permission setup screen SHALL provide action buttons to open system settings for each permission
7. THE EDGES_App SHALL not proceed to the main screen until notification listener and calendar permissions are granted
8. WHEN a required permission is revoked, THE EDGES_App SHALL display a notification prompting the user to re-grant it

### Requirement 10: User Interface Screens and Navigation

**User Story:** As a user, I want a beautiful and intuitive interface with clear screens, so that I can easily understand and use the app.

#### Acceptance Criteria

1. THE EDGES_App SHALL display a Welcome/Splash screen with app logo and "Get Started" button
2. THE EDGES_App SHALL display a Permission Setup screen with step-by-step permission cards
3. THE Permission Setup screen SHALL show icons, descriptions, and "Grant" buttons for each permission
4. THE EDGES_App SHALL display a Home screen with statistics at the top and meeting suggestion cards below
5. THE Home screen SHALL show meeting cards with: title, date/time, AI confidence badge, AI reasoning (bullet points), and "Schedule"/"Dismiss" buttons
6. THE EDGES_App SHALL display a Meetings screen listing all meetings with filters (Pending, Scheduled, Dismissed)
7. THE EDGES_App SHALL use Jetpack Compose for all UI components
8. THE EDGES_App SHALL use bottom navigation with "Home" and "Meetings" tabs
9. THE EDGES_App SHALL use a primary color of #6c5cff (purple) for buttons and highlights
10. THE EDGES_App SHALL use card-based design with rounded corners and shadows

### Requirement 11: Error Handling

**User Story:** As a user, I want the app to handle errors gracefully, so that it never crashes and always provides helpful feedback.

#### Acceptance Criteria

1. THE EDGES_App SHALL wrap all API calls in try-catch blocks to prevent unhandled exceptions
2. WHEN an error occurs, THE EDGES_App SHALL log the error details for debugging
3. WHEN an error occurs, THE EDGES_App SHALL display a user-friendly error message
4. IF the Gemini API is unavailable, THEN THE EDGES_App SHALL display "No internet connection" message
5. IF calendar insertion fails, THEN THE EDGES_App SHALL display an error message and allow retry
6. THE EDGES_App SHALL check for required permissions before attempting operations
7. THE EDGES_App SHALL implement a global exception handler to prevent crashes

### Requirement 12: Gemini API Configuration

**User Story:** As a developer, I want the Gemini API integration to be properly configured, so that it returns consistent and structured responses.

#### Acceptance Criteria

1. THE AI_Agent SHALL use the gemini-2.5-flash model for all analysis requests
2. THE AI_Agent SHALL set responseMimeType to application/json to force JSON output
3. THE AI_Agent SHALL define a responseJsonSchema in generationConfig specifying the exact JSON structure required
4. THE AI_Agent SHALL set temperature to 0.2 for consistent results
5. THE AI_Agent SHALL set maxOutputTokens to 500
6. THE AI_Agent SHALL include the current date, time, and device timezone in the prompt
7. THE AI_Agent SHALL use Retrofit library for HTTP calls
8. THE AI_Agent SHALL handle API failures gracefully with retry logic

### Requirement 13: Meeting Reminder System

**User Story:** As a user, I want to be reminded about meetings detected far in the future, so that I can schedule them at an appropriate time.

#### Acceptance Criteria

1. WHEN the AI_Agent returns action remind_later, THE EDGES_App SHALL save the meeting details with a reminder timestamp
2. THE EDGES_App SHALL calculate the reminder timestamp as 7 days before the meeting datetime
3. THE EDGES_App SHALL use Android WorkManager to schedule the reminder notification
4. WHEN the reminder time arrives, THE EDGES_App SHALL display a notification with the meeting details
5. THE reminder notification SHALL include "Schedule Now" and "Dismiss" action buttons
6. WHEN the user taps "Schedule Now", THE EDGES_App SHALL display the meeting suggestion card

### Requirement 14: Notification Filtering and Categorization

**User Story:** As a user, I want the app to categorize notifications intelligently, so that I can focus on important meeting-related notifications.

#### Acceptance Criteria

1. THE AI_Agent SHALL categorize each notification as meeting, payment, social, work, or other
2. THE AI_Agent SHALL assign a priority level of high, medium, or low to each notification
3. THE EDGES_App SHALL display high-priority meeting notifications prominently in the UI
4. THE EDGES_App SHALL filter out notifications from system apps and package installers
5. THE EDGES_App SHALL maintain separate counts for each notification category in the dashboard statistics

### Requirement 15: Calendar Data Formatting

**User Story:** As a developer, I want the AI to provide calendar event data in a clear format, so that minimal processing is needed in the app.

#### Acceptance Criteria

1. THE AI_Agent SHALL include meeting details in the response for all meeting detections
2. THE meeting details SHALL contain a title field with the meeting title
3. THE meeting details SHALL contain a datetime field in ISO 8601 format with timezone
4. THE meeting details SHALL contain a duration field in minutes
5. THE meeting details SHALL contain an attendees array with email addresses
6. THE meeting details SHALL contain a location field when a meeting location is detected
7. THE meeting details SHALL contain a description field with context from the original notification
8. THE Calendar_Manager SHALL convert the AI-provided meeting details to ContentValues format for CalendarContract insertion

### Requirement 16: App Lifecycle and State Management

**User Story:** As a user, I want the app to maintain its state correctly across app restarts and device reboots, so that no data is lost.

#### Acceptance Criteria

1. WHEN the app is closed, THE EDGES_App SHALL save all pending meeting suggestions to meetings.json
2. WHEN the app is reopened, THE EDGES_App SHALL load all saved data from JSON files
3. WHEN the device reboots, THE EDGES_App SHALL register a BOOT_COMPLETED broadcast receiver to restart services
4. THE EDGES_App SHALL restore the Foreground_Service state after device reboot
5. THE EDGES_App SHALL preserve app settings and preferences across app restarts
6. WHEN the app is updated, THE EDGES_App SHALL migrate data from old JSON format to new format if schema changes
7. THE EDGES_App SHALL handle low memory situations by releasing non-critical cached data

### Requirement 17: Testing and Quality Assurance

**User Story:** As a developer, I want to ensure the app works correctly, so that users have a reliable experience.

#### Acceptance Criteria

1. THE EDGES_App SHALL be tested on physical Android devices running Android 8.0 (API 26) and above
2. THE EDGES_App SHALL be tested with notifications from at least 5 different messaging apps
3. THE EDGES_App SHALL be tested with various meeting formats including different date and time expressions
4. THE EDGES_App SHALL be tested with the device locked to verify background operation
5. THE EDGES_App SHALL be tested after device reboot to verify service restart functionality
6. THE EDGES_App SHALL not crash during any normal user operation or error condition
