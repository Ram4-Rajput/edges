package com.edges.notificationassistant.data

import kotlinx.serialization.Serializable

/**
 * Represents user preferences and app settings.
 * Stored in preferences.json.
 */
@Serializable
data class Preferences(
    val notificationListenerEnabled: Boolean = false,
    val calendarPermissionGranted: Boolean = false,
    val postNotificationsGranted: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val geminiApiKey: String = "AIzaSyCCZge7wkf8xhkiBD9PeHuEoASh_SybXBY",
    val autoScheduleEnabled: Boolean = true,
    val defaultReminderMinutes: Int = 10
)
