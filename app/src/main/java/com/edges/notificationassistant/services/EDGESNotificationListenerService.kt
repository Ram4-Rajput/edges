package com.edges.notificationassistant.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.edges.notificationassistant.R
import com.edges.notificationassistant.data.MeetingStatus
import com.edges.notificationassistant.utils.JsonStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * NotificationListenerService that captures incoming notifications.
 * 
 * Features:
 * - Captures all incoming notifications
 * - Filters out system notifications
 * - Sends notifications to AI for analysis
 * - Runs as foreground service
 * 
 * **Validates: Requirements 1.1-1.7, 5.1-5.4**
 */
class EDGESNotificationListenerService : NotificationListenerService() {
    
    companion object {
        private const val TAG = "EDGESNotificationListener"
        private const val FOREGROUND_NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "edges_service_channel"
        private val SYSTEM_PACKAGES = setOf(
            "android",
            "com.android.systemui",
            "com.android.providers",
            "com.google.android.packageinstaller"
        )
    }
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var jsonStorage: JsonStorage
    private lateinit var aiAnalyzer: AIAnalyzer
    private lateinit var calendarManager: CalendarManager
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        
        jsonStorage = JsonStorage(this)
        aiAnalyzer = AIAnalyzer()
        calendarManager = CalendarManager(this)
        
        // Start as foreground service
        startForeground(FOREGROUND_NOTIFICATION_ID, createForegroundNotification())
    }
    
    /**
     * Called when a new notification is posted.
     * 
     * **Validates: Requirements 1.1, 1.2, 1.3, 1.4, 1.6**
     */
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        try {
            // Filter out system notifications
            if (isSystemNotification(sbn.packageName)) {
                return
            }
            
            // Extract notification details
            val notification = extractNotificationData(sbn)
            
            // Save notification
            jsonStorage.addNotification(notification)
            
            // Process notification asynchronously
            serviceScope.launch {
                processNotification(notification)
            }
            
            Log.d(TAG, "Notification captured: ${notification.title}")
        } catch (e: Exception) {
            Log.e(TAG, "Error processing notification", e)
        }
    }
    
    /**
     * Extracts notification data from StatusBarNotification.
     * 
     * **Validates: Requirements 1.1, 1.2, 1.3**
     */
    private fun extractNotificationData(sbn: StatusBarNotification): com.edges.notificationassistant.data.Notification {
        val notification = sbn.notification
        val extras = notification.extras
        
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
        
        return com.edges.notificationassistant.data.Notification(
            id = UUID.randomUUID().toString(),
            title = title,
            text = text,
            expandedText = bigText,
            appName = getAppName(sbn.packageName),
            packageName = sbn.packageName,
            timestamp = sbn.postTime,
            processed = false
        )
    }
    
    /**
     * Processes notification through AI analysis pipeline.
     * 
     * **Validates: Requirements 2.1, 3.1, 4.1**
     */
    private suspend fun processNotification(notification: com.edges.notificationassistant.data.Notification) {
        try {
            // Analyze with AI
            val aiResponse = aiAnalyzer.analyzeNotification(notification)
            
            if (aiResponse == null) {
                Log.e(TAG, "AI analysis failed for notification: ${notification.id}")
                return
            }
            
            Log.d(TAG, "AI Response - Action: ${aiResponse.action}, Confidence: ${aiResponse.confidence}")
            
            // Handle AI decision
            when (aiResponse.action) {
                "schedule_meeting" -> {
                    // Auto-schedule meeting
                    aiResponse.meetingDetails?.let { details ->
                        val meeting = createMeetingFromAI(notification.id, aiResponse)
                        val eventId = calendarManager.createEvent(meeting)
                        
                        if (eventId != null) {
                            val scheduledMeeting = meeting.copy(
                                status = MeetingStatus.SCHEDULED,
                                calendarEventId = eventId
                            )
                            jsonStorage.addMeeting(scheduledMeeting)
                            Log.d(TAG, "Auto-scheduled meeting: ${meeting.title}")
                        }
                    }
                }
                "ask_user" -> {
                    // Create meeting suggestion
                    aiResponse.meetingDetails?.let { details ->
                        val meeting = createMeetingFromAI(notification.id, aiResponse)
                        jsonStorage.addMeeting(meeting)
                        Log.d(TAG, "Created meeting suggestion: ${meeting.title}")
                    }
                }
                "remind_later" -> {
                    // Save for later reminder
                    aiResponse.meetingDetails?.let { details ->
                        val meeting = createMeetingFromAI(notification.id, aiResponse).copy(
                            status = MeetingStatus.REMIND_LATER
                        )
                        jsonStorage.addMeeting(meeting)
                        Log.d(TAG, "Saved meeting for later: ${meeting.title}")
                    }
                }
                "dismiss" -> {
                    Log.d(TAG, "Dismissed notification: ${notification.title}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing notification", e)
        }
    }
    
    /**
     * Creates a Meeting object from AI response.
     */
    private fun createMeetingFromAI(
        notificationId: String,
        aiResponse: com.edges.notificationassistant.data.AIResponse
    ): com.edges.notificationassistant.data.Meeting {
        val details = aiResponse.meetingDetails!!
        
        return com.edges.notificationassistant.data.Meeting(
            id = UUID.randomUUID().toString(),
            notificationId = notificationId,
            title = details.title,
            datetime = details.datetime,
            duration = details.duration,
            location = details.location,
            description = details.description,
            attendees = details.attendees,
            confidence = aiResponse.confidence,
            action = aiResponse.action,
            reasoning = aiResponse.reasoning,
            category = aiResponse.category,
            priority = aiResponse.priority,
            status = MeetingStatus.PENDING
        )
    }
    
    /**
     * Checks if notification is from a system app.
     * 
     * **Validates: Requirement 1.6**
     */
    private fun isSystemNotification(packageName: String): Boolean {
        return SYSTEM_PACKAGES.any { packageName.startsWith(it) }
    }
    
    /**
     * Gets app name from package name.
     */
    private fun getAppName(packageName: String): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }
    
    /**
     * Creates foreground notification for the service.
     * 
     * **Validates: Requirements 5.1, 5.2**
     */
    private fun createForegroundNotification(): Notification {
        createNotificationChannel()
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("EDGES Notification Assistant")
            .setContentText("Monitoring notifications for meetings")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
    
    /**
     * Creates notification channel for Android O+.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "EDGES Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "EDGES notification monitoring service"
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
    }
}
