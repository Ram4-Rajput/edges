package com.edges.notificationassistant

import android.content.Context
import com.edges.notificationassistant.data.Meeting
import com.edges.notificationassistant.data.MeetingStatus
import com.edges.notificationassistant.data.Notification
import com.edges.notificationassistant.data.Preferences
import com.edges.notificationassistant.utils.JsonStorage
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.io.File

/**
 * Unit tests for JsonStorage utility class
 * Tests save/load operations and error handling
 */
@RunWith(MockitoJUnitRunner::class)
class JsonStorageTest {
    
    @Mock
    private lateinit var mockContext: Context
    
    private lateinit var jsonStorage: JsonStorage
    private lateinit var testDir: File
    
    @Before
    fun setup() {
        // Create a temporary directory for testing
        testDir = createTempDir("json_storage_test")
        `when`(mockContext.filesDir).thenReturn(testDir)
        
        jsonStorage = JsonStorage(mockContext)
    }
    
    @Test
    fun `saveNotifications and loadNotifications should work correctly`() {
        // Given
        val notifications = listOf(
            Notification(
                id = "1",
                title = "Test Notification",
                text = "Test content",
                expandedText = null,
                appName = "Test App",
                packageName = "com.test.app",
                timestamp = System.currentTimeMillis(),
                processed = false
            )
        )
        
        // When
        val saveResult = jsonStorage.saveNotifications(notifications)
        val loadResult = jsonStorage.loadNotifications()
        
        // Then
        assertTrue(saveResult.isSuccess)
        assertTrue(loadResult.isSuccess)
        assertEquals(1, loadResult.getOrNull()?.size)
        assertEquals("Test Notification", loadResult.getOrNull()?.first()?.title)
    }
    
    @Test
    fun `addNotification should append to existing list`() {
        // Given
        val notification1 = Notification(
            id = "1",
            title = "First",
            text = "First notification",
            expandedText = null,
            appName = "App",
            packageName = "com.app",
            timestamp = System.currentTimeMillis(),
            processed = false
        )
        val notification2 = Notification(
            id = "2",
            title = "Second",
            text = "Second notification",
            expandedText = null,
            appName = "App",
            packageName = "com.app",
            timestamp = System.currentTimeMillis(),
            processed = false
        )
        
        // When
        jsonStorage.addNotification(notification1)
        jsonStorage.addNotification(notification2)
        val loadResult = jsonStorage.loadNotifications()
        
        // Then
        assertTrue(loadResult.isSuccess)
        assertEquals(2, loadResult.getOrNull()?.size)
    }
    
    @Test
    fun `saveMeetings and loadMeetings should work correctly`() {
        // Given
        val meetings = listOf(
            Meeting(
                id = "1",
                notificationId = "n1",
                title = "Team Meeting",
                datetime = "2024-01-15T10:00:00Z",
                duration = 60,
                location = "Conference Room",
                description = "Weekly sync",
                attendees = listOf("user@example.com"),
                confidence = 0.9,
                action = "schedule_meeting",
                reasoning = "Clear meeting request",
                category = "work",
                priority = "high",
                status = MeetingStatus.PENDING
            )
        )
        
        // When
        val saveResult = jsonStorage.saveMeetings(meetings)
        val loadResult = jsonStorage.loadMeetings()
        
        // Then
        assertTrue(saveResult.isSuccess)
        assertTrue(loadResult.isSuccess)
        assertEquals(1, loadResult.getOrNull()?.size)
        assertEquals("Team Meeting", loadResult.getOrNull()?.first()?.title)
    }
    
    @Test
    fun `updateMeeting should modify existing meeting`() {
        // Given
        val meeting = Meeting(
            id = "1",
            notificationId = "n1",
            title = "Meeting",
            datetime = "2024-01-15T10:00:00Z",
            duration = 60,
            confidence = 0.9,
            action = "ask_user",
            reasoning = "Test",
            status = MeetingStatus.PENDING
        )
        jsonStorage.addMeeting(meeting)
        
        // When
        jsonStorage.updateMeeting("1") { it.copy(status = MeetingStatus.SCHEDULED) }
        val loadResult = jsonStorage.loadMeetings()
        
        // Then
        assertTrue(loadResult.isSuccess)
        assertEquals(MeetingStatus.SCHEDULED, loadResult.getOrNull()?.first()?.status)
    }
    
    @Test
    fun `savePreferences and loadPreferences should work correctly`() {
        // Given
        val preferences = Preferences(
            notificationListenerEnabled = true,
            calendarPermissionGranted = true,
            postNotificationsGranted = true,
            onboardingCompleted = true,
            autoScheduleEnabled = false,
            defaultReminderMinutes = 15
        )
        
        // When
        val saveResult = jsonStorage.savePreferences(preferences)
        val loadResult = jsonStorage.loadPreferences()
        
        // Then
        assertTrue(saveResult.isSuccess)
        assertTrue(loadResult.isSuccess)
        assertEquals(true, loadResult.getOrNull()?.onboardingCompleted)
        assertEquals(15, loadResult.getOrNull()?.defaultReminderMinutes)
    }
    
    @Test
    fun `loadNotifications should return empty list when file does not exist`() {
        // When
        val loadResult = jsonStorage.loadNotifications()
        
        // Then
        assertTrue(loadResult.isSuccess)
        assertEquals(0, loadResult.getOrNull()?.size)
    }
    
    @Test
    fun `loadPreferences should return default when file does not exist`() {
        // When
        val loadResult = jsonStorage.loadPreferences()
        
        // Then
        assertTrue(loadResult.isSuccess)
        assertNotNull(loadResult.getOrNull())
        assertEquals(false, loadResult.getOrNull()?.onboardingCompleted)
    }
    
    @Test
    fun `clearAllData should delete all files`() {
        // Given
        jsonStorage.addNotification(
            Notification(
                id = "1",
                title = "Test",
                text = "Test",
                appName = "App",
                packageName = "com.app",
                timestamp = System.currentTimeMillis()
            )
        )
        
        // When
        val clearResult = jsonStorage.clearAllData()
        val loadResult = jsonStorage.loadNotifications()
        
        // Then
        assertTrue(clearResult.isSuccess)
        assertTrue(loadResult.isSuccess)
        assertEquals(0, loadResult.getOrNull()?.size)
    }
}
