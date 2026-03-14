package com.edges.notificationassistant.utils

import android.content.Context
import com.edges.notificationassistant.data.Meeting
import com.edges.notificationassistant.data.MeetingStatus
import com.edges.notificationassistant.data.Notification
import com.edges.notificationassistant.data.Preferences

/**
 * Example usage of JsonStorage utility class
 * This file demonstrates how to use JsonStorage in the app
 */
class JsonStorageExample(context: Context) {
    
    private val storage = JsonStorage(context)
    
    fun exampleNotificationOperations() {
        // Create a notification
        val notification = Notification(
            id = "notif_123",
            title = "Meeting Invitation",
            text = "Team sync tomorrow at 10 AM",
            expandedText = "Join us for the weekly team sync meeting tomorrow at 10 AM in Conference Room A",
            appName = "Gmail",
            packageName = "com.google.android.gm",
            timestamp = System.currentTimeMillis(),
            processed = false
        )
        
        // Add notification to storage
        storage.addNotification(notification)
            .onSuccess { println("Notification saved successfully") }
            .onFailure { error -> println("Failed to save notification: ${error.message}") }
        
        // Load all notifications
        storage.loadNotifications()
            .onSuccess { notifications ->
                println("Loaded ${notifications.size} notifications")
                notifications.forEach { notif ->
                    println("- ${notif.title} from ${notif.appName}")
                }
            }
            .onFailure { error -> println("Failed to load notifications: ${error.message}") }
    }
    
    fun exampleMeetingOperations() {
        // Create a meeting
        val meeting = Meeting(
            id = "meeting_456",
            notificationId = "notif_123",
            title = "Team Sync Meeting",
            datetime = "2024-01-15T10:00:00Z",
            duration = 60,
            location = "Conference Room A",
            description = "Weekly team sync",
            attendees = listOf("alice@example.com", "bob@example.com"),
            confidence = 0.95,
            action = "schedule_meeting",
            reasoning = "Clear meeting invitation with date, time, and location",
            category = "work",
            priority = "high",
            status = MeetingStatus.PENDING
        )
        
        // Add meeting to storage
        storage.addMeeting(meeting)
            .onSuccess { println("Meeting saved successfully") }
            .onFailure { error -> println("Failed to save meeting: ${error.message}") }
        
        // Update meeting status after scheduling
        storage.updateMeeting("meeting_456") { existingMeeting ->
            existingMeeting.copy(
                status = MeetingStatus.SCHEDULED,
                calendarEventId = 12345L,
                updatedAt = System.currentTimeMillis()
            )
        }
            .onSuccess { println("Meeting updated successfully") }
            .onFailure { error -> println("Failed to update meeting: ${error.message}") }
        
        // Load all meetings
        storage.loadMeetings()
            .onSuccess { meetings ->
                println("Loaded ${meetings.size} meetings")
                meetings.forEach { mtg ->
                    println("- ${mtg.title} (${mtg.status})")
                }
            }
            .onFailure { error -> println("Failed to load meetings: ${error.message}") }
    }
    
    fun examplePreferencesOperations() {
        // Load preferences (returns default if file doesn't exist)
        val preferences = storage.loadPreferences()
            .getOrElse { Preferences() }
        
        println("Current preferences:")
        println("- Notification listener enabled: ${preferences.notificationListenerEnabled}")
        println("- Calendar permission granted: ${preferences.calendarPermissionGranted}")
        println("- Auto-schedule enabled: ${preferences.autoScheduleEnabled}")
        
        // Update preferences
        val updatedPreferences = preferences.copy(
            notificationListenerEnabled = true,
            calendarPermissionGranted = true,
            onboardingCompleted = true
        )
        
        storage.savePreferences(updatedPreferences)
            .onSuccess { println("Preferences saved successfully") }
            .onFailure { error -> println("Failed to save preferences: ${error.message}") }
    }
    
    fun exampleErrorHandling() {
        // JsonStorage handles errors gracefully
        // If a JSON file is corrupted, it returns the default value instead of crashing
        
        storage.loadNotifications()
            .onSuccess { notifications ->
                // Success case - file loaded correctly
                println("Loaded ${notifications.size} notifications")
            }
            .onFailure { error ->
                // Failure case - IO error or other exception
                println("Error loading notifications: ${error.message}")
                // App can continue with empty list
            }
    }
}
