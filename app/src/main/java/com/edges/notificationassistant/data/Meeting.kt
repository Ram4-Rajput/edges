package com.edges.notificationassistant.data

import kotlinx.serialization.Serializable

/**
 * Represents a detected meeting with AI analysis results.
 * Stored in meetings.json for tracking suggestions and scheduled meetings.
 */
@Serializable
data class Meeting(
    val id: String,
    val notificationId: String,
    val title: String,
    val datetime: String, // ISO 8601 format
    val duration: Int, // minutes
    val location: String? = null,
    val description: String? = null,
    val attendees: List<String> = emptyList(),
    val confidence: Double,
    val action: String, // schedule_meeting, ask_user, dismiss, remind_later
    val reasoning: String,
    val category: String? = null,
    val priority: String? = null,
    val status: MeetingStatus = MeetingStatus.PENDING,
    val calendarEventId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
enum class MeetingStatus {
    PENDING,
    SCHEDULED,
    DISMISSED,
    REMIND_LATER
}
