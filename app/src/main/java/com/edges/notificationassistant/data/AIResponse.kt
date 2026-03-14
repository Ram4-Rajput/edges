package com.edges.notificationassistant.data

import kotlinx.serialization.Serializable

/**
 * Represents the structured response from Google Gemini AI.
 * The AI analyzes notifications and returns this decision structure.
 */
@Serializable
data class AIResponse(
    val meetingDetected: Boolean,
    val confidence: Double,
    val action: String, // schedule_meeting, ask_user, dismiss, remind_later
    val reasoning: String,
    val meetingDetails: MeetingDetails? = null,
    val category: String? = null,
    val priority: String? = null
)

@Serializable
data class MeetingDetails(
    val title: String,
    val datetime: String, // ISO 8601 format with timezone
    val duration: Int, // minutes
    val location: String? = null,
    val description: String? = null,
    val attendees: List<String> = emptyList()
)
