package com.edges.notificationassistant.services

import com.edges.notificationassistant.data.Notification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Example usage of AIAnalyzer service.
 * Demonstrates how to analyze notifications with Gemini API.
 */
object AIAnalyzerExample {
    
    /**
     * Example: Analyze a notification and handle the response.
     */
    fun analyzeNotificationExample() {
        val aiAnalyzer = AIAnalyzer()
        
        // Create a sample notification
        val notification = Notification(
            id = "notif-123",
            title = "Meeting Invitation",
            text = "You have a meeting tomorrow at 2 PM with John Doe",
            expandedText = "Project Review Meeting\nTime: Tomorrow 2:00 PM\nLocation: Conference Room A",
            appName = "Gmail",
            packageName = "com.google.android.gm",
            timestamp = System.currentTimeMillis()
        )
        
        // Analyze the notification (must be called from a coroutine)
        CoroutineScope(Dispatchers.IO).launch {
            val response = aiAnalyzer.analyzeNotification(notification)
            
            response?.let { aiResponse ->
                println("Meeting Detected: ${aiResponse.meetingDetected}")
                println("Confidence: ${aiResponse.confidence}")
                println("Action: ${aiResponse.action}")
                println("Reasoning: ${aiResponse.reasoning}")
                
                // Handle meeting details if detected
                aiResponse.meetingDetails?.let { details ->
                    println("Meeting Title: ${details.title}")
                    println("DateTime: ${details.datetime}")
                    println("Duration: ${details.duration} minutes")
                    println("Location: ${details.location}")
                    println("Attendees: ${details.attendees.joinToString(", ")}")
                }
                
                // Take action based on AI decision
                when (aiResponse.action) {
                    "schedule_meeting" -> {
                        println("Auto-scheduling meeting...")
                        // Call CalendarManager to schedule
                    }
                    "ask_user" -> {
                        println("Showing meeting suggestion to user...")
                        // Display meeting card in UI
                    }
                    "dismiss" -> {
                        println("Dismissing notification...")
                        // No action needed
                    }
                    "remind_later" -> {
                        println("Setting reminder for later...")
                        // Schedule reminder with WorkManager
                    }
                }
            } ?: run {
                println("Failed to analyze notification")
            }
        }
    }
}
