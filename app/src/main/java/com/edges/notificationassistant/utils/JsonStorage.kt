package com.edges.notificationassistant.utils

import android.content.Context
import android.util.Log
import com.edges.notificationassistant.data.Meeting
import com.edges.notificationassistant.data.Notification
import com.edges.notificationassistant.data.Preferences
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

/**
 * Utility class for managing JSON file storage in the app's private directory.
 * Handles saving and loading notifications, meetings, and preferences data.
 * 
 * Requirements: 6.1, 6.2, 6.3, 6.4, 6.5
 */
class JsonStorage(private val context: Context) {
    
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    companion object {
        private const val TAG = "JsonStorage"
        private const val NOTIFICATIONS_FILE = "notifications.json"
        private const val MEETINGS_FILE = "meetings.json"
        private const val PREFERENCES_FILE = "preferences.json"
    }
    
    // Notifications operations
    
    /**
     * Save a list of notifications to notifications.json
     * Requirement 6.1: Store all captured notifications
     */
    fun saveNotifications(notifications: List<Notification>): Result<Unit> {
        return saveToFile(NOTIFICATIONS_FILE, notifications)
    }
    
    /**
     * Load all notifications from notifications.json
     * Requirement 6.5: Handle parsing errors gracefully
     */
    fun loadNotifications(): Result<List<Notification>> {
        return loadFromFile(NOTIFICATIONS_FILE, emptyList())
    }
    
    /**
     * Add a single notification to the existing list
     * Requirement 6.1: Store all captured notifications
     */
    fun addNotification(notification: Notification): Result<Unit> {
        val currentNotifications = loadNotifications().getOrElse { emptyList() }
        val updatedNotifications = currentNotifications + notification
        return saveNotifications(updatedNotifications)
    }
    
    // Meetings operations
    
    /**
     * Save a list of meetings to meetings.json
     * Requirement 6.2: Store all meeting detections and suggestions
     */
    fun saveMeetings(meetings: List<Meeting>): Result<Unit> {
        return saveToFile(MEETINGS_FILE, meetings)
    }
    
    /**
     * Load all meetings from meetings.json
     * Requirement 6.5: Handle parsing errors gracefully
     */
    fun loadMeetings(): Result<List<Meeting>> {
        return loadFromFile(MEETINGS_FILE, emptyList())
    }
    
    /**
     * Add a single meeting to the existing list
     * Requirement 6.2: Store all meeting detections and suggestions
     */
    fun addMeeting(meeting: Meeting): Result<Unit> {
        val currentMeetings = loadMeetings().getOrElse { emptyList() }
        val updatedMeetings = currentMeetings + meeting
        return saveMeetings(updatedMeetings)
    }
    
    /**
     * Update an existing meeting by ID
     * Requirement 6.2: Store all meeting detections and suggestions
     */
    fun updateMeeting(meetingId: String, updater: (Meeting) -> Meeting): Result<Unit> {
        val currentMeetings = loadMeetings().getOrElse { emptyList() }
        val updatedMeetings = currentMeetings.map { meeting ->
            if (meeting.id == meetingId) updater(meeting) else meeting
        }
        return saveMeetings(updatedMeetings)
    }
    
    // Preferences operations
    
    /**
     * Save user preferences to preferences.json
     * Requirement 6.3: Store user preferences and settings
     */
    fun savePreferences(preferences: Preferences): Result<Unit> {
        return saveToFile(PREFERENCES_FILE, preferences)
    }
    
    /**
     * Load user preferences from preferences.json
     * Requirement 6.5: Handle parsing errors gracefully
     */
    fun loadPreferences(): Result<Preferences> {
        return loadFromFile(PREFERENCES_FILE, Preferences())
    }
    
    // Generic file operations
    
    /**
     * Generic function to save any serializable data to a JSON file
     * Requirement 6.4: Write files safely to prevent data corruption
     */
    private inline fun <reified T> saveToFile(fileName: String, data: T): Result<Unit> {
        return try {
            val jsonString = json.encodeToString(data)
            val file = File(context.filesDir, fileName)
            
            // Write to a temporary file first, then rename to prevent corruption
            val tempFile = File(context.filesDir, "$fileName.tmp")
            tempFile.writeText(jsonString)
            
            // Atomic rename operation
            if (tempFile.renameTo(file)) {
                Log.d(TAG, "Successfully saved $fileName")
                Result.success(Unit)
            } else {
                // If rename fails, try direct write as fallback
                file.writeText(jsonString)
                tempFile.delete()
                Log.d(TAG, "Saved $fileName using fallback method")
                Result.success(Unit)
            }
        } catch (e: IOException) {
            Log.e(TAG, "IO error saving $fileName", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving $fileName", e)
            Result.failure(e)
        }
    }
    
    /**
     * Generic function to load any serializable data from a JSON file
     * Requirement 6.5: Validate JSON structure and handle parsing errors gracefully
     */
    private inline fun <reified T> loadFromFile(fileName: String, defaultValue: T): Result<T> {
        return try {
            val file = File(context.filesDir, fileName)
            
            if (!file.exists()) {
                Log.d(TAG, "$fileName does not exist, returning default value")
                return Result.success(defaultValue)
            }
            
            val jsonString = file.readText()
            
            if (jsonString.isBlank()) {
                Log.d(TAG, "$fileName is empty, returning default value")
                return Result.success(defaultValue)
            }
            
            val data = json.decodeFromString<T>(jsonString)
            Log.d(TAG, "Successfully loaded $fileName")
            Result.success(data)
        } catch (e: kotlinx.serialization.SerializationException) {
            Log.e(TAG, "JSON parsing error in $fileName, returning default value", e)
            // Return default value instead of failing when JSON is corrupted
            Result.success(defaultValue)
        } catch (e: IOException) {
            Log.e(TAG, "IO error loading $fileName", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading $fileName", e)
            Result.failure(e)
        }
    }
    
    /**
     * Delete a JSON file
     */
    fun deleteFile(fileName: String): Result<Unit> {
        return try {
            val file = File(context.filesDir, fileName)
            if (file.exists()) {
                file.delete()
                Log.d(TAG, "Deleted $fileName")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting $fileName", e)
            Result.failure(e)
        }
    }
    
    /**
     * Clear all stored data
     */
    fun clearAllData(): Result<Unit> {
        return try {
            deleteFile(NOTIFICATIONS_FILE)
            deleteFile(MEETINGS_FILE)
            deleteFile(PREFERENCES_FILE)
            Log.d(TAG, "Cleared all data")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing all data", e)
            Result.failure(e)
        }
    }
}
