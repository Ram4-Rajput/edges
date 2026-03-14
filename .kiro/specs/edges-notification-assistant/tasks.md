# Implementation Plan: EDGES Notification Assistant

## Overview

This plan implements an Android notification assistant that captures notifications, uses Google Gemini AI to detect meetings, and schedules them in the user's calendar. The implementation follows a simple, incremental approach focusing on core functionality first, then UI polish, and finally background reliability.

## Tasks

- [x] 1. Project setup and core data models
  - Create Android project with Kotlin and Jetpack Compose
  - Add dependencies: Retrofit, Kotlinx Serialization, Coroutines, Compose
  - Create data classes: Notification, Meeting, AIResponse, CalendarRequest
  - Set up MVVM architecture folders (data, ui, services, utils)
  - _Requirements: All (foundation)_

- [ ] 2. Implement JSON storage manager
  - [x] 2.1 Create JsonStorage utility class
    - Implement save/load functions for JSON files
    - Add file operations for notifications.json, meetings.json, preferences.json
    - Handle parsing errors gracefully
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_
  
  - [ ]* 2.2 Write unit tests for JSON storage
    - Test save and load operations
    - Test error handling for corrupted JSON
    - _Requirements: 6.4, 6.5_

- [ ] 3. Implement Gemini AI integration
  - [x] 3.1 Create AIAnalyzer service with Retrofit
    - Set up Retrofit client for Gemini API
    - Configure API endpoint and authentication
    - Implement request/response models with responseSchema
    - Set responseMimeType to "application/json"
    - _Requirements: 2.1, 2.2, 12.1, 12.2, 12.3, 12.7_
  
  - [x] 3.2 Implement AI agent decision logic
    - Build prompt template with action instructions
    - Parse AI response JSON (meetingDetected, confidence, action, meetingDetails, calendarRequest)
    - Implement confidence-based action selection
    - Add reasoning extraction for UI display
    - _Requirements: 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 2.13_
  
  - [x] 3.3 Add meeting details extraction
    - Parse datetime to ISO 8601 format
    - Calculate meeting end time from duration
    - Extract attendees, location, description
    - Handle timezone conversion
    - _Requirements: 2.9, 2.10, 2.11, 2.12, 15.1, 15.2, 15.3, 15.4, 15.5, 15.6, 15.7_
  
  - [x] 3.4 Implement error handling and retry logic
    - Add try-catch for API calls
    - Implement 3-attempt retry with delays
    - Add fallback to keyword matching
    - Log errors for debugging
    - _Requirements: 11.1, 11.2, 11.4, 12.8_

- [ ] 4. Implement calendar integration
  - [x] 4.1 Create CalendarManager service
    - Set up Google Calendar API client with OAuth 2.0
    - Implement native Android CalendarContract fallback
    - Request READ_CALENDAR and WRITE_CALENDAR permissions
    - _Requirements: 7.1, 7.2, 7.3_
  
  - [x] 4.2 Implement calendar event creation
    - Convert AI calendarRequest to Google Calendar API format
    - Insert event into primary calendar
    - Extract and save event ID from response
    - Handle timezone correctly
    - _Requirements: 3.2, 3.3, 7.4, 7.5, 7.6_
  
  - [x] 4.3 Add calendar reminders
    - Add 10-minute popup reminder to events
    - Use CalendarContract.Reminders for native calendar
    - Handle reminder creation failures gracefully
    - _Requirements: 3.5, 8.1, 8.2, 8.3, 8.4_
  
  - [x] 4.4 Add error handling for calendar operations
    - Check permissions before operations
    - Handle calendar insertion failures
    - Return descriptive error messages
    - Provide retry option
    - _Requirements: 7.7, 11.5, 11.6_

- [~] 5. Checkpoint - Test AI and calendar integration
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 6. Implement notification capture service
  - [x] 6.1 Create NotificationListenerService
    - Extend NotificationListenerService class
    - Capture notification content (title, text, expanded text)
    - Extract app name and package identifier
    - Record timestamp for each notification
    - Filter out system notifications
    - _Requirements: 1.1, 1.2, 1.3, 1.6_
  
  - [x] 6.2 Implement notification processing pipeline
    - Save captured notification to notifications.json
    - Send notification to AIAnalyzer
    - Process AI response and execute action
    - Handle auto-schedule vs ask-user actions
    - _Requirements: 1.4, 2.1, 3.1, 4.1_
  
  - [x] 6.3 Set up foreground service
    - Configure service as foreground with persistent notification
    - Add wake lock for background operation
    - Implement boot receiver for auto-restart
    - _Requirements: 1.5, 1.7, 5.1, 5.2, 5.4_

- [ ] 7. Implement permission management
  - [~] 7.1 Create permission utility functions
    - Check notification listener access
    - Check calendar permissions
    - Check POST_NOTIFICATIONS permission (Android 13+)
    - Provide functions to open system settings
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.6_
  
  - [~] 7.2 Add permission status tracking
    - Track granted/not granted status for each permission
    - Show status indicators in UI
    - Handle permission revocation
    - _Requirements: 9.5, 9.7, 9.8_

- [ ] 8. Build UI screens with Jetpack Compose
  - [~] 8.1 Create design system and theme
    - Define colors (#6c5cff primary, #10b981 success, etc.)
    - Set up typography (24sp title, 20sp heading, 16sp body)
    - Create spacing constants (8dp, 16dp, 24dp)
    - Build reusable card components with rounded corners
    - _Requirements: 10.9, 10.10_
  
  - [~] 8.2 Implement Welcome/Splash screen
    - Display app logo and name
    - Add tagline "AI-Powered Meeting Assistant"
    - Create "Get Started" button
    - Navigate to Permission Setup on button click
    - _Requirements: 10.1_
  
  - [~] 8.3 Implement Permission Setup screen
    - Create permission cards for each permission type
    - Show icons, descriptions, and grant buttons
    - Display status indicators (checkmark/warning)
    - Enable "Continue" button when all permissions granted
    - _Requirements: 10.2, 10.3_
  
  - [~] 8.4 Implement Home screen dashboard
    - Create stats cards (notifications, meetings, service status)
    - Build meeting suggestion card component
    - Display AI confidence badge (green/yellow/red)
    - Show AI reasoning as bullet points
    - Add "Schedule" and "Dismiss" buttons
    - _Requirements: 10.4, 10.5, 4.2, 4.3, 4.4, 4.5_
  
  - [~] 8.5 Implement Meetings screen
    - Create filter tabs (Pending, Scheduled, Dismissed)
    - Build meeting list with status icons
    - Show meeting details (title, date/time, status)
    - Add action buttons for pending meetings
    - _Requirements: 10.6_
  
  - [~] 8.6 Add bottom navigation
    - Create navigation bar with Home and Meetings tabs
    - Implement tab switching
    - Highlight active tab
    - _Requirements: 10.8_
  
  - [~] 8.7 Create meeting details dialog
    - Show full meeting information
    - Display AI analysis and reasoning
    - Show original notification source
    - Add Schedule and Dismiss buttons
    - _Requirements: 4.2, 4.3, 4.4_

- [ ] 9. Implement ViewModels and state management
  - [~] 9.1 Create HomeViewModel
    - Manage dashboard statistics state
    - Handle meeting suggestion list
    - Implement schedule and dismiss actions
    - Expose StateFlow for UI observation
    - _Requirements: 4.6, 4.7, 4.8_
  
  - [~] 9.2 Create MeetingsViewModel
    - Manage meetings list with filters
    - Handle filter state (Pending/Scheduled/Dismissed)
    - Implement meeting actions
    - Load data from meetings.json
    - _Requirements: 10.6_
  
  - [~] 9.3 Create PermissionsViewModel
    - Track permission states
    - Handle permission requests
    - Navigate to settings when needed
    - _Requirements: 9.5, 9.6_

- [~] 10. Checkpoint - Test core functionality
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 11. Implement auto-scheduling logic
  - [~] 11.1 Add auto-schedule action handler
    - Check if AI action is "schedule_meeting"
    - Extract calendarRequest from AI response
    - Call CalendarManager to create event
    - Save event ID to meetings.json
    - Display success notification to user
    - _Requirements: 3.1, 3.2, 3.3, 3.4_
  
  - [~] 11.2 Add meeting suggestion handler
    - Check if AI action is "ask_user"
    - Create meeting suggestion card
    - Display in Home screen
    - Handle user confirmation
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

- [ ] 12. Implement meeting reminder system
  - [~] 12.1 Add remind_later action handler
    - Detect when AI returns "remind_later" action
    - Calculate reminder timestamp (7 days before meeting)
    - Save reminder to meetings.json
    - _Requirements: 2.8, 13.1, 13.2_
  
  - [~] 12.2 Set up WorkManager for reminders
    - Schedule reminder notification using WorkManager
    - Create reminder notification with action buttons
    - Handle "Schedule Now" and "Dismiss" actions
    - _Requirements: 13.3, 13.4, 13.5, 13.6_

- [ ] 13. Add notification categorization
  - [~] 13.1 Implement category detection
    - Parse AI response for category field
    - Assign priority level (high/medium/low)
    - Filter system apps and package installers
    - _Requirements: 14.1, 14.2, 14.4_
  
  - [~] 13.2 Update UI to show categories
    - Display high-priority meetings prominently
    - Add category counts to dashboard statistics
    - _Requirements: 14.3, 14.5_

- [ ] 14. Implement error handling and user feedback
  - [~] 14.1 Add global error handling
    - Wrap all operations in try-catch blocks
    - Log errors with details
    - Display user-friendly error messages
    - _Requirements: 11.1, 11.2, 11.3_
  
  - [~] 14.2 Add network error handling
    - Check internet connectivity
    - Show "No internet connection" message
    - Implement offline mode indicators
    - _Requirements: 11.4_
  
  - [~] 14.3 Add success/error notifications
    - Show toast for successful scheduling
    - Show toast for dismissal confirmation
    - Display error messages with retry option
    - _Requirements: 4.8, 11.5_

- [ ] 15. Implement app lifecycle management
  - [~] 15.1 Add state persistence
    - Save pending suggestions on app close
    - Load saved data on app open
    - Preserve settings across restarts
    - _Requirements: 16.1, 16.2, 16.5_
  
  - [x] 15.2 Implement boot receiver
    - Register BOOT_COMPLETED broadcast receiver
    - Restart NotificationListenerService after reboot
    - Restore foreground service state
    - _Requirements: 16.3, 16.4_
  
  - [~] 15.3 Handle low memory situations
    - Release non-critical cached data
    - Implement data migration for schema changes
    - _Requirements: 16.6, 16.7_

- [x] 16. Add AndroidManifest permissions and declarations
  - Add all required permissions (BIND_NOTIFICATION_LISTENER_SERVICE, POST_NOTIFICATIONS, READ_CALENDAR, WRITE_CALENDAR, INTERNET, WAKE_LOCK, FOREGROUND_SERVICE, RECEIVE_BOOT_COMPLETED)
  - Declare NotificationListenerService
  - Declare foreground service type
  - Register boot receiver
  - _Requirements: 9.1, 9.2, 9.3, 9.4, 5.1_

- [ ] 17. Optimize background operations
  - [~] 17.1 Implement efficient notification processing
    - Process notifications asynchronously
    - Batch API calls when possible
    - Use coroutines for background work
    - _Requirements: 5.3_
  
  - [~] 17.2 Add battery optimization
    - Request battery optimization exemption
    - Minimize wake lock usage
    - Use WorkManager for periodic sync
    - _Requirements: 5.3_

- [ ] 18. Final integration and polish
  - [~] 18.1 Wire all components together
    - Connect NotificationListener → AIAnalyzer → CalendarManager
    - Link ViewModels to UI screens
    - Ensure navigation flows work correctly
    - Test end-to-end flows
    - _Requirements: All_
  
  - [~] 18.2 Add loading states and animations
    - Show loading indicators during API calls
    - Add card animations for schedule/dismiss
    - Implement smooth transitions between screens
    - _Requirements: 10.4, 10.5_
  
  - [~] 18.3 Polish UI details
    - Ensure consistent spacing and alignment
    - Add empty states for all screens
    - Verify color scheme matches design
    - Test on different screen sizes
    - _Requirements: 10.9, 10.10_

- [~] 19. Final checkpoint - Complete testing
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Focus on getting core functionality working first, then polish UI
- Test on real Android device (API 26+) throughout development
- Use hardcoded Gemini API key for simplicity: `AIzaSyCCZge7wkf8xhkiBD9PeHuEoASh_SybXBY`
