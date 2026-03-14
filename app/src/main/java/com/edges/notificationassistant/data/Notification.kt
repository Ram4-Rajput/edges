package com.edges.notificationassistant.data

import kotlinx.serialization.Serializable

/**
 * Represents a captured notification from the Android system.
 * Stored in notifications.json for history tracking.
 */
@Serializable
data class Notification(
    val id: String,
    val title: String,
    val text: String,
    val expandedText: String? = null,
    val appName: String,
    val packageName: String,
    val timestamp: Long,
    val processed: Boolean = false
)
