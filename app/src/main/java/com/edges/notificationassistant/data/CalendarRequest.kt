package com.edges.notificationassistant.data

import kotlinx.serialization.Serializable

/**
 * Represents a calendar event creation request.
 * Used to pass meeting data to CalendarManager for scheduling.
 */
@Serializable
data class CalendarRequest(
    val title: String,
    val startTime: Long, // Unix timestamp in milliseconds
    val endTime: Long, // Unix timestamp in milliseconds
    val location: String? = null,
    val description: String? = null,
    val attendees: List<String> = emptyList(),
    val reminderMinutes: Int = 10
)
