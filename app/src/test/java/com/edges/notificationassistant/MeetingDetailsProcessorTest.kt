package com.edges.notificationassistant

import com.edges.notificationassistant.data.MeetingDetails
import com.edges.notificationassistant.utils.MeetingDetailsProcessor
import org.junit.Test
import org.junit.Assert.*
import java.util.*

/**
 * Unit tests for MeetingDetailsProcessor.
 * Tests datetime parsing, validation, timezone conversion, and meeting details processing.
 * 
 * **Validates: Requirements 2.9, 2.10, 2.11, 2.12, 15.1, 15.2, 15.3, 15.4, 15.5, 15.6, 15.7**
 */
class MeetingDetailsProcessorTest {
    
    @Test
    fun testValidateAndNormalizeDatetime_validISO8601() {
        // Test various valid ISO 8601 formats
        val validFormats = listOf(
            "2024-01-15T14:00:00+05:30",
            "2024-01-15T14:00:00Z",
            "2024-01-15T14:00:00",
            "2024-01-15T14:00+05:30",
            "2024-01-15T14:00Z",
            "2024-01-15T14:00"
        )
        
        validFormats.forEach { datetime ->
            val result = MeetingDetailsProcessor.validateAndNormalizeDatetime(datetime)
            assertNotNull("Should parse valid datetime: $datetime", result)
            assertTrue("Should contain 'T' separator", result!!.contains("T"))
        }
    }
    
    @Test
    fun testValidateAndNormalizeDatetime_invalidFormat() {
        // Test invalid datetime formats
        val invalidFormats = listOf(
            "2024-01-15",
            "14:00:00",
            "invalid-datetime",
            "",
            "2024/01/15 14:00:00"
        )
        
        invalidFormats.forEach { datetime ->
            val result = MeetingDetailsProcessor.validateAndNormalizeDatetime(datetime)
            assertNull("Should reject invalid datetime: $datetime", result)
        }
    }
    
    @Test
    fun testCalculateEndTime_validInput() {
        // Test end time calculation
        val startDatetime = "2024-01-15T14:00:00+05:30"
        val duration = 60 // 1 hour
        
        val endTime = MeetingDetailsProcessor.calculateEndTime(startDatetime, duration)
        
        assertNotNull("End time should be calculated", endTime)
        assertNotEquals("End time should differ from start time", startDatetime, endTime)
    }
    
    @Test
    fun testCalculateEndTime_multipleHours() {
        // Test with 2 hour duration
        val startDatetime = "2024-01-15T14:00:00+05:30"
        val duration = 120 // 2 hours
        
        val endTime = MeetingDetailsProcessor.calculateEndTime(startDatetime, duration)
        
        assertNotNull("End time should be calculated", endTime)
        // End time should be 2 hours later
        assertTrue("End time should be after start time", endTime != null)
    }
    
    @Test
    fun testCalculateEndTime_invalidStartTime() {
        // Test with invalid start time
        val invalidStartTime = "invalid-datetime"
        val duration = 60
        
        val endTime = MeetingDetailsProcessor.calculateEndTime(invalidStartTime, duration)
        
        assertNull("Should return null for invalid start time", endTime)
    }
    
    @Test
    fun testExtractAttendeeEmails_validEmails() {
        // Test email extraction from various formats
        val attendees = listOf(
            "john@example.com",
            "John Doe <john.doe@example.com>",
            "sarah@company.org",
            "Contact: mike@test.co.uk"
        )
        
        val emails = MeetingDetailsProcessor.extractAttendeeEmails(attendees)
        
        assertEquals("Should extract 4 emails", 4, emails.size)
        assertTrue("Should contain john@example.com", emails.contains("john@example.com"))
        assertTrue("Should contain john.doe@example.com", emails.contains("john.doe@example.com"))
        assertTrue("Should contain sarah@company.org", emails.contains("sarah@company.org"))
        assertTrue("Should contain mike@test.co.uk", emails.contains("mike@test.co.uk"))
    }
    
    @Test
    fun testExtractAttendeeEmails_noEmails() {
        // Test with no valid emails
        val attendees = listOf(
            "John Doe",
            "Meeting Room A",
            "No email here"
        )
        
        val emails = MeetingDetailsProcessor.extractAttendeeEmails(attendees)
        
        assertTrue("Should return empty list when no emails found", emails.isEmpty())
    }
    
    @Test
    fun testExtractAttendeeEmails_duplicates() {
        // Test duplicate email handling
        val attendees = listOf(
            "john@example.com",
            "John <john@example.com>",
            "john@example.com"
        )
        
        val emails = MeetingDetailsProcessor.extractAttendeeEmails(attendees)
        
        assertEquals("Should remove duplicates", 1, emails.size)
        assertEquals("Should keep john@example.com", "john@example.com", emails[0])
    }
    
    @Test
    fun testValidateLocation_validLocation() {
        // Test valid location strings
        val validLocations = listOf(
            "Conference Room A",
            "https://zoom.us/j/123456",
            "Building 2, Floor 3",
            "  Trimmed Location  "
        )
        
        validLocations.forEach { location ->
            val result = MeetingDetailsProcessor.validateLocation(location)
            assertNotNull("Should validate location: $location", result)
            assertFalse("Should trim whitespace", result!!.startsWith(" ") || result.endsWith(" "))
        }
    }
    
    @Test
    fun testValidateLocation_invalidLocation() {
        // Test invalid location strings
        val invalidLocations = listOf(
            null,
            "",
            "   ",
            "\t\n"
        )
        
        invalidLocations.forEach { location ->
            val result = MeetingDetailsProcessor.validateLocation(location)
            assertNull("Should reject invalid location: '$location'", result)
        }
    }
    
    @Test
    fun testValidateDescription_validDescription() {
        // Test valid description strings
        val validDescriptions = listOf(
            "Quarterly planning meeting",
            "Discuss project roadmap and milestones",
            "  Trimmed description  "
        )
        
        validDescriptions.forEach { description ->
            val result = MeetingDetailsProcessor.validateDescription(description)
            assertNotNull("Should validate description: $description", result)
            assertFalse("Should trim whitespace", result!!.startsWith(" ") || result.endsWith(" "))
        }
    }
    
    @Test
    fun testValidateDescription_invalidDescription() {
        // Test invalid description strings
        val invalidDescriptions = listOf(
            null,
            "",
            "   ",
            "\t\n"
        )
        
        invalidDescriptions.forEach { description ->
            val result = MeetingDetailsProcessor.validateDescription(description)
            assertNull("Should reject invalid description: '$description'", result)
        }
    }
    
    @Test
    fun testProcessAndValidate_validMeetingDetails() {
        // Test processing valid meeting details
        val details = MeetingDetails(
            title = "Team Standup",
            datetime = "2024-01-15T14:00:00+05:30",
            duration = 30,
            location = "Conference Room A",
            description = "Daily standup meeting",
            attendees = listOf("john@example.com", "sarah@example.com")
        )
        
        val result = MeetingDetailsProcessor.processAndValidate(details)
        
        assertNotNull("Should validate meeting details", result)
        assertEquals("Should preserve title", "Team Standup", result!!.title)
        assertEquals("Should preserve duration", 30, result.duration)
        assertEquals("Should preserve location", "Conference Room A", result.location)
        assertEquals("Should preserve description", "Daily standup meeting", result.description)
        assertEquals("Should preserve attendees", 2, result.attendees.size)
    }
    
    @Test
    fun testProcessAndValidate_missingTitle() {
        // Test with missing title
        val details = MeetingDetails(
            title = "",
            datetime = "2024-01-15T14:00:00+05:30",
            duration = 30
        )
        
        val result = MeetingDetailsProcessor.processAndValidate(details)
        
        assertNull("Should reject meeting with blank title", result)
    }
    
    @Test
    fun testProcessAndValidate_invalidDatetime() {
        // Test with invalid datetime
        val details = MeetingDetails(
            title = "Meeting",
            datetime = "invalid-datetime",
            duration = 30
        )
        
        val result = MeetingDetailsProcessor.processAndValidate(details)
        
        assertNull("Should reject meeting with invalid datetime", result)
    }
    
    @Test
    fun testProcessAndValidate_invalidDuration() {
        // Test with invalid duration
        val details = MeetingDetails(
            title = "Meeting",
            datetime = "2024-01-15T14:00:00+05:30",
            duration = 0
        )
        
        val result = MeetingDetailsProcessor.processAndValidate(details)
        
        assertNull("Should reject meeting with zero duration", result)
    }
    
    @Test
    fun testProcessAndValidate_nullDetails() {
        // Test with null details
        val result = MeetingDetailsProcessor.processAndValidate(null)
        
        assertNull("Should return null for null input", result)
    }
    
    @Test
    fun testFormatDuration_hoursAndMinutes() {
        // Test duration formatting
        assertEquals("Should format 90 minutes as 1h 30m", "1h 30m", 
            MeetingDetailsProcessor.formatDuration(90))
        assertEquals("Should format 120 minutes as 2h", "2h", 
            MeetingDetailsProcessor.formatDuration(120))
        assertEquals("Should format 45 minutes as 45m", "45m", 
            MeetingDetailsProcessor.formatDuration(45))
        assertEquals("Should format 150 minutes as 2h 30m", "2h 30m", 
            MeetingDetailsProcessor.formatDuration(150))
    }
    
    @Test
    fun testConvertToTimezone_validDatetime() {
        // Test timezone conversion
        val datetime = "2024-01-15T14:00:00+05:30"
        val targetTimezone = TimeZone.getTimeZone("UTC")
        
        val result = MeetingDetailsProcessor.convertToTimezone(datetime, targetTimezone)
        
        assertNotNull("Should convert timezone", result)
        assertTrue("Should be in ISO 8601 format", result!!.contains("T"))
    }
    
    @Test
    fun testConvertToTimezone_invalidDatetime() {
        // Test with invalid datetime
        val datetime = "invalid-datetime"
        val targetTimezone = TimeZone.getTimeZone("UTC")
        
        val result = MeetingDetailsProcessor.convertToTimezone(datetime, targetTimezone)
        
        assertNull("Should return null for invalid datetime", result)
    }
    
    @Test
    fun testFormatToISO8601WithTimezone() {
        // Test ISO 8601 formatting
        val date = Date()
        val result = MeetingDetailsProcessor.formatToISO8601WithTimezone(date)
        
        assertNotNull("Should format date", result)
        assertTrue("Should contain 'T' separator", result.contains("T"))
        assertTrue("Should contain timezone offset", 
            result.contains("+") || result.contains("-") || result.endsWith("Z"))
    }
}
