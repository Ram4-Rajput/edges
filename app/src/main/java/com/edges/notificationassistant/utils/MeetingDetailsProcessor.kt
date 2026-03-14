package com.edges.notificationassistant.utils

import android.util.Log
import com.edges.notificationassistant.data.MeetingDetails
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility class for processing and validating meeting details from AI responses.
 * Handles datetime parsing, timezone conversion, and end time calculation.
 * 
 * **Validates: Requirements 2.9, 2.10, 2.11, 2.12, 15.1, 15.2, 15.3, 15.4, 15.5, 15.6, 15.7**
 */
object MeetingDetailsProcessor {
    
    private const val TAG = "MeetingDetailsProcessor"
    
    /**
     * Validates and processes meeting details from AI response.
     * Ensures datetime is in ISO 8601 format and all required fields are present.
     * Extracts attendee emails, validates location and description.
     * 
     * **Validates: Requirements 2.9, 2.10, 2.11, 2.12, 15.1, 15.2, 15.3, 15.4, 15.5, 15.6, 15.7**
     * 
     * @param details The meeting details from AI
     * @return Processed and validated meeting details, or null if validation fails
     */
    fun processAndValidate(details: MeetingDetails?): MeetingDetails? {
        if (details == null) {
            Log.w(TAG, "Meeting details are null")
            return null
        }
        
        // Validate required fields
        if (details.title.isBlank()) {
            Log.w(TAG, "Meeting title is blank")
            return null
        }
        
        if (details.datetime.isBlank()) {
            Log.w(TAG, "Meeting datetime is blank")
            return null
        }
        
        if (details.duration <= 0) {
            Log.w(TAG, "Meeting duration is invalid: ${details.duration}")
            return null
        }
        
        // Validate and normalize datetime format (Requirement 2.10, 15.3)
        val normalizedDatetime = validateAndNormalizeDatetime(details.datetime)
        if (normalizedDatetime == null) {
            Log.w(TAG, "Failed to validate datetime: ${details.datetime}")
            return null
        }
        
        // Extract and validate attendee emails (Requirement 2.12, 15.5)
        val extractedEmails = extractAttendeeEmails(details.attendees)
        
        // Validate location (Requirement 2.12, 15.6)
        val validatedLocation = validateLocation(details.location)
        
        // Validate description (Requirement 2.12, 15.7)
        val validatedDescription = validateDescription(details.description)
        
        // Return validated details with all processed fields
        return details.copy(
            datetime = normalizedDatetime,
            attendees = extractedEmails,
            location = validatedLocation,
            description = validatedDescription
        )
    }
    
    /**
     * Validates datetime string is in ISO 8601 format and includes timezone.
     * Normalizes the format if needed.
     * 
     * **Validates: Requirements 2.10, 15.3**
     * 
     * @param datetime The datetime string to validate
     * @return Normalized ISO 8601 datetime string with timezone, or null if invalid
     */
    fun validateAndNormalizeDatetime(datetime: String): String? {
        return try {
            // Try parsing various ISO 8601 formats
            val parsedDate = parseISO8601(datetime)
            
            if (parsedDate == null) {
                Log.w(TAG, "Failed to parse datetime: $datetime")
                return null
            }
            
            // Format to standard ISO 8601 with timezone
            formatToISO8601WithTimezone(parsedDate)
        } catch (e: Exception) {
            Log.e(TAG, "Error validating datetime: $datetime", e)
            null
        }
    }
    
    /**
     * Parses ISO 8601 datetime string to Date object.
     * Supports multiple ISO 8601 formats.
     * 
     * @param datetime ISO 8601 datetime string
     * @return Parsed Date object, or null if parsing fails
     */
    private fun parseISO8601(datetime: String): Date? {
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ssXXX",      // 2024-01-15T14:00:00+05:30
            "yyyy-MM-dd'T'HH:mm:ss'Z'",      // 2024-01-15T14:00:00Z
            "yyyy-MM-dd'T'HH:mm:ss",         // 2024-01-15T14:00:00
            "yyyy-MM-dd'T'HH:mmXXX",         // 2024-01-15T14:00+05:30
            "yyyy-MM-dd'T'HH:mm'Z'",         // 2024-01-15T14:00Z
            "yyyy-MM-dd'T'HH:mm"             // 2024-01-15T14:00
        )
        
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.getDefault())
                sdf.timeZone = TimeZone.getDefault()
                return sdf.parse(datetime)
            } catch (e: Exception) {
                // Try next format
                continue
            }
        }
        
        return null
    }
    
    /**
     * Formats Date object to ISO 8601 string with timezone.
     * 
     * **Validates: Requirements 2.10, 15.3**
     * 
     * @param date Date object to format
     * @return ISO 8601 formatted string with timezone
     */
    fun formatToISO8601WithTimezone(date: Date): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(date)
    }
    
    /**
     * Calculates meeting end time from start time and duration.
     * 
     * **Validates: Requirements 2.11, 15.4**
     * 
     * @param startDatetime ISO 8601 start datetime string
     * @param durationMinutes Duration in minutes
     * @return ISO 8601 end datetime string, or null if calculation fails
     */
    fun calculateEndTime(startDatetime: String, durationMinutes: Int): String? {
        return try {
            val startDate = parseISO8601(startDatetime)
            if (startDate == null) {
                Log.w(TAG, "Failed to parse start datetime: $startDatetime")
                return null
            }
            
            // Add duration to start time
            val calendar = Calendar.getInstance()
            calendar.time = startDate
            calendar.add(Calendar.MINUTE, durationMinutes)
            
            // Format end time
            formatToISO8601WithTimezone(calendar.time)
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating end time", e)
            null
        }
    }
    
    /**
     * Extracts and validates attendee email addresses.
     * 
     * **Validates: Requirements 2.12, 15.5**
     * 
     * @param attendees List of attendee strings (may include names and emails)
     * @return List of validated email addresses
     */
    fun extractAttendeeEmails(attendees: List<String>): List<String> {
        val emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}".toRegex()
        
        return attendees.mapNotNull { attendee ->
            // Try to find email in the string
            emailPattern.find(attendee)?.value
        }.distinct()
    }
    
    /**
     * Validates location string.
     * 
     * **Validates: Requirements 2.12, 15.6**
     * 
     * @param location Location string
     * @return Validated location string, or null if invalid
     */
    fun validateLocation(location: String?): String? {
        if (location.isNullOrBlank()) {
            return null
        }
        
        // Trim and return if not empty
        val trimmed = location.trim()
        return if (trimmed.isNotEmpty()) trimmed else null
    }
    
    /**
     * Validates description string.
     * 
     * **Validates: Requirements 2.12, 15.7**
     * 
     * @param description Description string
     * @return Validated description string, or null if invalid
     */
    fun validateDescription(description: String?): String? {
        if (description.isNullOrBlank()) {
            return null
        }
        
        // Trim and return if not empty
        val trimmed = description.trim()
        return if (trimmed.isNotEmpty()) trimmed else null
    }
    
    /**
     * Converts datetime to device timezone if needed.
     * 
     * **Validates: Requirement 2.10**
     * 
     * @param datetime ISO 8601 datetime string
     * @param targetTimezone Target timezone (defaults to device timezone)
     * @return Datetime string converted to target timezone
     */
    fun convertToTimezone(datetime: String, targetTimezone: TimeZone = TimeZone.getDefault()): String? {
        return try {
            val date = parseISO8601(datetime)
            if (date == null) {
                Log.w(TAG, "Failed to parse datetime for timezone conversion: $datetime")
                return null
            }
            
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
            sdf.timeZone = targetTimezone
            sdf.format(date)
        } catch (e: Exception) {
            Log.e(TAG, "Error converting timezone", e)
            null
        }
    }
    
    /**
     * Gets meeting duration in hours and minutes format.
     * 
     * @param durationMinutes Duration in minutes
     * @return Formatted duration string (e.g., "1h 30m", "45m")
     */
    fun formatDuration(durationMinutes: Int): String {
        val hours = durationMinutes / 60
        val minutes = durationMinutes % 60
        
        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            else -> "${minutes}m"
        }
    }
}
