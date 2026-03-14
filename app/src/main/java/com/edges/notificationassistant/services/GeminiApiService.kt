package com.edges.notificationassistant.services

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Retrofit API interface for Google Gemini API.
 * Defines endpoints for AI analysis requests.
 */
interface GeminiApiService {
    
    @Headers("Content-Type: application/json")
    @POST("v1beta/models/gemini-2.0-flash-exp:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
}

/**
 * Request body for Gemini API.
 */
data class GeminiRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String
)

data class GenerationConfig(
    val temperature: Double = 0.2,
    val maxOutputTokens: Int = 500,
    val responseMimeType: String = "application/json",
    val responseSchema: ResponseSchema
)

data class ResponseSchema(
    val type: String = "object",
    val properties: Map<String, SchemaProperty>,
    val required: List<String>
)

data class SchemaProperty(
    val type: String,
    val description: String? = null,
    val properties: Map<String, SchemaProperty>? = null,
    val items: SchemaProperty? = null,
    val enum: List<String>? = null
)

/**
 * Response from Gemini API.
 */
data class GeminiResponse(
    val candidates: List<Candidate>
)

data class Candidate(
    val content: Content,
    val finishReason: String? = null
)
