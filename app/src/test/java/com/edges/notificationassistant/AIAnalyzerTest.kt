package com.edges.notificationassistant

import com.edges.notificationassistant.data.Notification
import com.edges.notificationassistant.services.AIAnalyzer
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for AIAnalyzer service.
 * Tests Retrofit setup, API integration, and confidence-based decision logic.
 * 
 * **Validates: Requirements 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 2.13**
 */
class AIAnalyzerTest {
    
    private lateinit var aiAnalyzer: AIAnalyzer
    
    @Before
    fun setup() {
        aiAnalyzer = AIAnalyzer()
    }
    
    @Test
    fun testAIAnalyzerInitialization() {
        // Verify AIAnalyzer can be instantiated
        assertNotNull(aiAnalyzer)
    }
    
    @Test
    fun testAnalyzeNotification_withMeetingRequest() = runBlocking {
        // Create a test notification with clear meeting content
        val notification = Notification(
            id = "test-1",
            title = "Meeting Invitation",
            text = "You have a meeting tomorrow at 2 PM with John Doe about project review",
            expandedText = "Meeting: Project Review\nTime: Tomorrow 2:00 PM\nLocation: Conference Room A",
            appName = "Gmail",
            packageName = "com.google.android.gm",
            timestamp = System.currentTimeMillis()
        )
        
        // Analyze the notification
        val response = aiAnalyzer.analyzeNotification(notification)
        
        // Verify response is not null
        assertNotNull("AI response should not be null", response)
        
        // Verify response structure
        response?.let {
            assertTrue("Meeting should be detected", it.meetingDetected)
            assertTrue("Confidence should be between 0 and 1", it.confidence in 0.0..1.0)
            assertTrue("Action should be valid", 
                it.action in listOf("schedule_meeting", "ask_user", "dismiss", "remind_later"))
            assertNotNull("Reasoning should be provided", it.reasoning)
            assertFalse("Reasoning should not be empty", it.reasoning.isEmpty())
        }
    }
    
    @Test
    fun testAnalyzeNotification_withNonMeetingContent() = runBlocking {
        // Create a test notification without meeting content
        val notification = Notification(
            id = "test-2",
            title = "New Message",
            text = "Hey, how are you doing?",
            expandedText = null,
            appName = "WhatsApp",
            packageName = "com.whatsapp",
            timestamp = System.currentTimeMillis()
        )
        
        // Analyze the notification
        val response = aiAnalyzer.analyzeNotification(notification)
        
        // Verify response
        assertNotNull("AI response should not be null", response)
        
        response?.let {
            // For non-meeting content, confidence should be low
            assertTrue("Confidence should be low for non-meeting", it.confidence < 0.5)
            assertEquals("Action should be dismiss", "dismiss", it.action)
        }
    }
    
    @Test
    fun testAnalyzeNotification_highConfidenceMeeting() = runBlocking {
        // Test high confidence meeting (should trigger schedule_meeting action)
        val notification = Notification(
            id = "test-3",
            title = "Calendar Invite: Team Standup",
            text = "Meeting scheduled for tomorrow at 10:00 AM. Duration: 30 minutes. Location: Zoom",
            expandedText = "Team Standup\nWhen: Tomorrow, 10:00 AM - 10:30 AM\nWhere: https://zoom.us/j/123456",
            appName = "Google Calendar",
            packageName = "com.google.android.calendar",
            timestamp = System.currentTimeMillis()
        )
        
        val response = aiAnalyzer.analyzeNotification(notification)
        
        assertNotNull("Response should not be null", response)
        response?.let {
            assertTrue("Meeting should be detected", it.meetingDetected)
            assertTrue("Confidence should be high (>0.8)", it.confidence > 0.8)
            assertTrue("Action should be schedule_meeting or ask_user", 
                it.action in listOf("schedule_meeting", "ask_user"))
            assertNotNull("Meeting details should be provided", it.meetingDetails)
            assertTrue("Reasoning should mention confidence", 
                it.reasoning.contains("confidence", ignoreCase = true) || 
                it.reasoning.contains("High") || 
                it.reasoning.contains("%"))
        }
    }
    
    @Test
    fun testAnalyzeNotification_mediumConfidenceMeeting() = runBlocking {
        // Test medium confidence meeting (should trigger ask_user action)
        val notification = Notification(
            id = "test-4",
            title = "Message from Sarah",
            text = "Can we meet sometime next week to discuss the project?",
            expandedText = null,
            appName = "Slack",
            packageName = "com.slack",
            timestamp = System.currentTimeMillis()
        )
        
        val response = aiAnalyzer.analyzeNotification(notification)
        
        assertNotNull("Response should not be null", response)
        response?.let {
            // Medium confidence should result in ask_user action
            if (it.meetingDetected && it.confidence >= 0.5 && it.confidence <= 0.8) {
                assertEquals("Action should be ask_user for medium confidence", "ask_user", it.action)
            }
            assertNotNull("Reasoning should be provided", it.reasoning)
            assertFalse("Reasoning should not be empty", it.reasoning.isEmpty())
        }
    }
    
    @Test
    fun testAnalyzeNotification_reasoningExtraction() = runBlocking {
        // Test that reasoning is properly extracted and enhanced
        val notification = Notification(
            id = "test-5",
            title = "Lunch Meeting",
            text = "Let's have lunch tomorrow at noon at the Italian restaurant",
            expandedText = null,
            appName = "Messages",
            packageName = "com.android.messaging",
            timestamp = System.currentTimeMillis()
        )
        
        val response = aiAnalyzer.analyzeNotification(notification)
        
        assertNotNull("Response should not be null", response)
        response?.let {
            assertNotNull("Reasoning should be provided", it.reasoning)
            assertFalse("Reasoning should not be empty", it.reasoning.isEmpty())
            assertTrue("Reasoning should be user-friendly (not too technical)", 
                it.reasoning.length > 10)
        }
    }
    
    @Test
    fun testAnalyzeNotification_meetingDetailsExtraction() = runBlocking {
        // Test that meeting details are properly extracted
        val notification = Notification(
            id = "test-6",
            title = "Meeting Invitation: Q1 Planning",
            text = "You're invited to Q1 Planning meeting on January 15, 2024 at 3:00 PM. Duration: 2 hours. Location: Board Room",
            expandedText = "Q1 Planning Session\nAttendees: john@example.com, sarah@example.com",
            appName = "Outlook",
            packageName = "com.microsoft.office.outlook",
            timestamp = System.currentTimeMillis()
        )
        
        val response = aiAnalyzer.analyzeNotification(notification)
        
        assertNotNull("Response should not be null", response)
        response?.let {
            if (it.meetingDetected) {
                assertNotNull("Meeting details should be provided", it.meetingDetails)
                it.meetingDetails?.let { details ->
                    assertNotNull("Title should be extracted", details.title)
                    assertFalse("Title should not be empty", details.title.isEmpty())
                    assertNotNull("Datetime should be extracted", details.datetime)
                    assertTrue("Duration should be positive", details.duration > 0)
                }
            }
        }
    }
    
    @Test
    fun testAnalyzeNotification_confidenceValidation() = runBlocking {
        // Test that confidence scores are always in valid range [0.0, 1.0]
        val notification = Notification(
            id = "test-7",
            title = "Test Notification",
            text = "This is a test notification",
            expandedText = null,
            appName = "TestApp",
            packageName = "com.test.app",
            timestamp = System.currentTimeMillis()
        )
        
        val response = aiAnalyzer.analyzeNotification(notification)
        
        assertNotNull("Response should not be null", response)
        response?.let {
            assertTrue("Confidence should be >= 0.0", it.confidence >= 0.0)
            assertTrue("Confidence should be <= 1.0", it.confidence <= 1.0)
        }
    }
    
    @Test
    fun testAnalyzeNotification_actionValidation() = runBlocking {
        // Test that action is always one of the valid values
        val notification = Notification(
            id = "test-8",
            title = "Random Notification",
            text = "Some random text content",
            expandedText = null,
            appName = "SomeApp",
            packageName = "com.some.app",
            timestamp = System.currentTimeMillis()
        )
        
        val response = aiAnalyzer.analyzeNotification(notification)
        
        assertNotNull("Response should not be null", response)
        response?.let {
            val validActions = listOf("schedule_meeting", "ask_user", "dismiss", "remind_later")
            assertTrue("Action should be one of the valid values: $validActions", 
                it.action in validActions)
        }
    }
}
