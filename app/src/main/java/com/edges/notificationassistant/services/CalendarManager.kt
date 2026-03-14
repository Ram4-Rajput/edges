package com.edges.notificationassistant.services

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import android.util.Log
import androidx.core.content.ContextCompat
import com.edges.notificationassistant.data.CalendarRequest
import com.edges.notificationassistant.data.Meeting
import java.util.*

/**
 * CalendarManager handles calendar operations using Android CalendarContract API.
 * 
 * Features:
 * - Create calendar events in device's primary calendar
 * - Add reminders to events
 * - Handle permissions and errors gracefully
 * 
 * **Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 7.1-7.7, 8.1-8.4**
 */
class CalendarManager(private val context: Context) {
    
    companion object {
        private const val TAG = "CalendarManager"
        private const val DEFAULT_REMINDER_MINUTES = 10
    }
    
    /**
     * Creates a calendar event from a Meeting object.
     * Returns the event ID on success, null on failure.
     * 
     * **Validates: Requirements 3.2, 3.3, 7.4, 7.5, 7.6**
     */
    fun createEvent(meeting: Meeting): Long? {
        // Check permissions first
        if (!hasCalendarPermissions()) {
            Log.e(TAG, "Calendar permissions not granted")
            return null
        }
        
        return try {
            // Parse datetime
            val startTime = parseDateTime(meeting.datetime)
            val endTime = startTime + (meeting.duration * 60 * 1000) // Convert minutes to milliseconds
            
            // Create event
            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, startTime)
                put(CalendarContract.Events.DTEND, endTime)
                put(CalendarContract.Events.TITLE, meeting.title)
                put(CalendarContract.Events.DESCRIPTION, meeting.description ?: "")
                put(CalendarContract.Events.EVENT_LOCATION, meeting.location ?: "")
                put(CalendarContract.Events.CALENDAR_ID, getPrimaryCalendarId())
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            }
            
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            val eventId = uri?.lastPathSegment?.toLongOrNull()
            
            if (eventId != null) {
                // Add reminder
                addReminder(eventId, DEFAULT_REMINDER_MINUTES)
                Log.d(TAG, "Created calendar event: $eventId")
            }
            
            eventId
        } catch (e: Exception) {
            Log.e(TAG, "Error creating calendar event", e)
            null
        }
    }
    
    /**
     * Adds a reminder to a calendar event.
     * 
     * **Validates: Requirements 3.5, 8.1, 8.2, 8.3, 8.4**
     */
    private fun addReminder(eventId: Long, minutes: Int): Boolean {
        return try {
            val values = ContentValues().apply {
                put(CalendarContract.Reminders.EVENT_ID, eventId)
                put(CalendarContract.Reminders.MINUTES, minutes)
                put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
            }
            
            context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, values)
            Log.d(TAG, "Added reminder: $minutes minutes before event $eventId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding reminder", e)
            false
        }
    }
    
    /**
     * Checks if calendar permissions are granted.
     * 
     * **Validates: Requirements 7.2, 11.6**
     */
    fun hasCalendarPermissions(): Boolean {
        val readPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
        
        val writePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
        
        return readPermission && writePermission
    }
    
    /**
     * Gets the primary calendar ID.
     * 
     * **Validates: Requirement 7.3**
     */
    private fun getPrimaryCalendarId(): Long {
        val projection = arrayOf(CalendarContract.Calendars._ID)
        val cursor = context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            "${CalendarContract.Calendars.IS_PRIMARY}=1",
            null,
            null
        )
        
        cursor?.use {
            if (it.moveToFirst()) {
                return it.getLong(0)
            }
        }
        
        // Fallback: return first calendar
        val fallbackCursor = context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            null,
            null,
            null
        )
        
        fallbackCursor?.use {
            if (it.moveToFirst()) {
                return it.getLong(0)
            }
        }
        
        return 1 // Default calendar ID
    }
    
    /**
     * Parses ISO 8601 datetime string to Unix timestamp.
     */
    private fun parseDateTime(datetimeStr: String): Long {
        return try {
            val cleanedStr = datetimeStr.substringBefore("+").substringBefore("-", datetimeStr).substringBefore("Z")
            val calendar = Calendar.getInstance()
            
            // Parse ISO 8601 format: 2024-01-15T14:00:00
            val parts = cleanedStr.split("T")
            if (parts.size == 2) {
                val dateParts = parts[0].split("-")
                val timeParts = parts[1].split(":")
                
                calendar.set(Calendar.YEAR, dateParts[0].toInt())
                calendar.set(Calendar.MONTH, dateParts[1].toInt() - 1) // Month is 0-indexed
                calendar.set(Calendar.DAY_OF_MONTH, dateParts[2].toInt())
                calendar.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                calendar.set(Calendar.MINUTE, timeParts[1].toInt())
                calendar.set(Calendar.SECOND, if (timeParts.size > 2) timeParts[2].toInt() else 0)
            }
            
            calendar.timeInMillis
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing datetime: $datetimeStr", e)
            System.currentTimeMillis() // Fallback to current time
        }
    }
}
