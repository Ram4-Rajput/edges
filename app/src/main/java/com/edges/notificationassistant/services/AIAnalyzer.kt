package com.edges.notificationassistant.services

import android.util.Log
import com.edges.notificationassistant.data.AIResponse
import com.edges.notificationassistant.data.MeetingDetails
import com.edges.notificationassistant.data.Notification
import com.edges.notificationassistant.utils.MeetingDetailsProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

/**
 * AIAnalyzer service that communicates with Google Gemini API.
 * Analyzes notifications and returns structured AI decisions with confidence-based action selection.
 * 
 * Features:
 * - Enhanced prompt template with detailed action instructions
 * - Confidence-based action selection (schedule_meeting, ask_user, dismiss, remind_later)
 * - Reasoning extraction for UI display
 * - Meeting details parsing and validation
 * - 30-day future meeting detection for remind_later action
 * 
 * **Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 2.13, 12.1, 12.2, 12.3, 12.7**
 */
class AIAnalyzer {
    
    companion object {
        private const val TAG = "AIAnalyzer"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/"
        private const val API_KEY = "AIzaSyCCZge7wkf8xhkiBD9PeHuEoASh_SybXBY"
        private const val MODEL = "gemini-2.0-flash-exp"
    }
    
    private val json = Json { 
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    private val apiService: GeminiApiService
    
    init {
        // Set up OkHttp client with logging
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
        
        // Set up Retrofit client
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        apiService = retrofit.create(GeminiApiService::class.java)
    }
    
    /**
     * Analyzes a notification and returns AI decision.
     * 
     * @param notification The notification to analyze
     * @return AIResponse with meeting detection and action decision
     */
    suspend fun analyzeNotification(notification: Notification): AIResponse? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Analyzing notification: ${notification.title}")
                
                // Build the prompt with current context
                val prompt = buildPrompt(notification)
                
                // Build the request with response schema
                val request = buildGeminiRequest(prompt)
                
                // Make API call
                val response = apiService.generateContent(API_KEY, request)
                
                if (response.isSuccessful && response.body() != null) {
                    val geminiResponse = response.body()!!
                    
                    // Extract text from response
                    if (geminiResponse.candidates.isNotEmpty()) {
                        val candidate = geminiResponse.candidates[0]
                        val responseText = candidate.content.parts.firstOrNull()?.text
                        
                        if (responseText != null) {
                            Log.d(TAG, "AI Response: $responseText")
                            
                            // Parse JSON response to AIResponse
                            return@withContext parseAIResponse(responseText)
                        }
                    }
                }
                
                Log.e(TAG, "API call failed: ${response.code()} - ${response.message()}")
                null
            } catch (e: Exception) {
                Log.e(TAG, "Error analyzing notification", e)
                null
            }
        }
    }
    
    /**
     * Builds the prompt for Gemini API with current context.
     * Enhanced with detailed action instructions and confidence guidance.
     * 
     * **Validates: Requirements 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 2.13**
     */
    private fun buildPrompt(notification: Notification): String {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val timezone = TimeZone.getDefault().id
        
        return """
You are EDGES, an intelligent notification assistant that helps users manage meeting requests. Analyze the notification below and make a smart decision about what action to take.

CURRENT CONTEXT:
- Date: $currentDate
- Time: $currentTime
- Timezone: $timezone

NOTIFICATION TO ANALYZE:
- App: ${notification.appName}
- Title: ${notification.title}
- Text: ${notification.text}
${if (notification.expandedText != null) "- Expanded Text: ${notification.expandedText}" else ""}

YOUR ANALYSIS TASKS:

1. MEETING DETECTION:
   - Determine if this notification contains a meeting request, invitation, or scheduling information
   - Look for: dates, times, meeting titles, locations, attendees, calendar invites
   - Set meetingDetected to true only if you find clear meeting information

2. CONFIDENCE SCORING (0.0 to 1.0):
   - 0.9-1.0: Explicit meeting invite with clear date/time (e.g., "Meeting tomorrow at 2 PM")
   - 0.7-0.9: Strong meeting indicators with most details present
   - 0.5-0.7: Possible meeting but missing some details or ambiguous
   - 0.3-0.5: Weak meeting signals, might be discussing a meeting
   - 0.0-0.3: No meeting detected or just casual mention

3. ACTION DECISION (choose exactly one):
   - "schedule_meeting": Use when confidence > 0.8 AND all required details are present (title, datetime, duration)
   - "ask_user": Use when confidence is 0.5-0.8 OR when meeting is detected but user confirmation is needed
   - "dismiss": Use when confidence < 0.5 OR no meeting detected
   - "remind_later": Use when meeting is detected BUT datetime is more than 30 days in the future

4. MEETING DETAILS EXTRACTION (if meetingDetected is true):
   - title: Extract the meeting subject or create a descriptive title
   - datetime: Convert to ISO 8601 format with timezone (e.g., "2024-01-15T14:00:00${TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT)}")
   - duration: Estimate in minutes (default to 60 if not specified)
   - location: Extract physical location or video call link if present
   - description: Summarize the meeting context from the notification
   - attendees: Extract email addresses if present

5. REASONING (provide clear, user-friendly explanation):
   - Explain WHY you made this decision
   - Mention key factors: confidence level, what details were found, what's missing
   - Keep it concise and actionable (2-3 sentences)
   - Examples:
     * "High confidence meeting detected with clear date and time. All details present for automatic scheduling."
     * "Possible meeting request but date is ambiguous. User confirmation recommended."
     * "No meeting detected - this appears to be a regular message."

IMPORTANT RULES:
- Always provide reasoning, even for dismissed notifications
- If meetingDetected is true, you MUST provide meetingDetails
- Use the current date/time context to interpret relative dates (e.g., "tomorrow", "next week")
- Be conservative with confidence scores - only use >0.8 when you're very certain
- For action "schedule_meeting", ensure datetime is within the next 30 days

Return your analysis in the specified JSON format.
        """.trimIndent()
    }
    
    /**
     * Builds the Gemini API request with response schema.
     */
    private fun buildGeminiRequest(prompt: String): GeminiRequest {
        return GeminiRequest(
            contents = listOf(
                Content(
                    parts = listOf(Part(text = prompt))
                )
            ),
            generationConfig = GenerationConfig(
                temperature = 0.2,
                maxOutputTokens = 500,
                responseMimeType = "application/json",
                responseSchema = buildResponseSchema()
            )
        )
    }
    
    /**
     * Builds the JSON schema for structured AI responses.
     */
    private fun buildResponseSchema(): ResponseSchema {
        return ResponseSchema(
            type = "object",
            properties = mapOf(
                "meetingDetected" to SchemaProperty(
                    type = "boolean",
                    description = "Whether a meeting request was detected"
                ),
                "confidence" to SchemaProperty(
                    type = "number",
                    description = "Confidence score between 0.0 and 1.0"
                ),
                "action" to SchemaProperty(
                    type = "string",
                    description = "Action to take",
                    enum = listOf("schedule_meeting", "ask_user", "dismiss", "remind_later")
                ),
                "reasoning" to SchemaProperty(
                    type = "string",
                    description = "Explanation for the decision"
                ),
                "meetingDetails" to SchemaProperty(
                    type = "object",
                    description = "Meeting details if detected",
                    properties = mapOf(
                        "title" to SchemaProperty(
                            type = "string",
                            description = "Meeting title"
                        ),
                        "datetime" to SchemaProperty(
                            type = "string",
                            description = "Meeting datetime in ISO 8601 format with timezone"
                        ),
                        "duration" to SchemaProperty(
                            type = "integer",
                            description = "Meeting duration in minutes"
                        ),
                        "location" to SchemaProperty(
                            type = "string",
                            description = "Meeting location (optional)"
                        ),
                        "description" to SchemaProperty(
                            type = "string",
                            description = "Meeting description (optional)"
                        ),
                        "attendees" to SchemaProperty(
                            type = "array",
                            description = "List of attendee email addresses",
                            items = SchemaProperty(type = "string")
                        )
                    )
                ),
                "category" to SchemaProperty(
                    type = "string",
                    description = "Notification category",
                    enum = listOf("meeting", "payment", "social", "work", "other")
                ),
                "priority" to SchemaProperty(
                    type = "string",
                    description = "Priority level",
                    enum = listOf("high", "medium", "low")
                )
            ),
            required = listOf("meetingDetected", "confidence", "action", "reasoning")
        )
    }
    
    /**
     * Parses the JSON response text to AIResponse object.
     */
    private fun parseAIResponse(responseText: String): AIResponse? {
        return try {
            val response = json.decodeFromString<AIResponse>(responseText)
            validateAndEnhanceResponse(response)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing AI response", e)
            null
        }
    }
    
    /**
     * Validates and enhances the AI response with confidence-based logic.
     * Ensures action selection follows confidence thresholds and meeting details are properly processed.
     * 
     * **Validates: Requirements 2.4, 2.5, 2.6, 2.7, 2.9, 2.10, 2.11, 2.12**
     */
    private fun validateAndEnhanceResponse(response: AIResponse): AIResponse {
        // Validate confidence is in valid range
        val validatedConfidence = response.confidence.coerceIn(0.0, 1.0)
        
        // Process and validate meeting details if present
        val processedMeetingDetails = if (response.meetingDetails != null) {
            MeetingDetailsProcessor.processAndValidate(response.meetingDetails)
        } else {
            null
        }
        
        // Apply confidence-based action selection rules
        val validatedAction = when {
            !response.meetingDetected -> "dismiss"
            validatedConfidence > 0.8 && processedMeetingDetails != null -> "schedule_meeting"
            validatedConfidence >= 0.5 && processedMeetingDetails != null -> "ask_user"
            else -> "dismiss"
        }
        
        // Check if meeting is too far in the future (>30 days)
        val finalAction = if (validatedAction != "dismiss" && processedMeetingDetails != null) {
            if (isMeetingTooFarInFuture(processedMeetingDetails.datetime)) {
                "remind_later"
            } else {
                validatedAction
            }
        } else {
            validatedAction
        }
        
        // Enhance reasoning with confidence information if not already present
        val enhancedReasoning = enhanceReasoning(response.reasoning, validatedConfidence, finalAction)
        
        Log.d(TAG, "Validated response - Confidence: $validatedConfidence, Action: $finalAction")
        
        return response.copy(
            confidence = validatedConfidence,
            action = finalAction,
            reasoning = enhancedReasoning,
            meetingDetails = processedMeetingDetails
        )
    }
    
    /**
     * Checks if a meeting datetime is more than 30 days in the future.
     * 
     * **Validates: Requirement 2.8**
     */
    private fun isMeetingTooFarInFuture(datetimeStr: String): Boolean {
        return try {
            val meetingDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                .parse(datetimeStr.substringBefore("+").substringBefore("-").substringBefore("Z"))
            
            if (meetingDate != null) {
                val thirtyDaysFromNow = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 30)
                }.time
                
                meetingDate.after(thirtyDaysFromNow)
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing meeting datetime", e)
            false
        }
    }
    
    /**
     * Enhances reasoning with confidence and action context for UI display.
     * 
     * **Validates: Requirements 2.13, 4.4**
     */
    private fun enhanceReasoning(reasoning: String, confidence: Double, action: String): String {
        // If reasoning already mentions confidence, return as-is
        if (reasoning.contains("confidence", ignoreCase = true)) {
            return reasoning
        }
        
        // Add confidence context based on action
        val confidenceContext = when (action) {
            "schedule_meeting" -> "High confidence (${String.format("%.0f", confidence * 100)}%) - "
            "ask_user" -> "Medium confidence (${String.format("%.0f", confidence * 100)}%) - "
            "dismiss" -> "Low confidence (${String.format("%.0f", confidence * 100)}%) - "
            "remind_later" -> "Meeting too far in future - "
            else -> ""
        }
        
        return confidenceContext + reasoning
    }
}
