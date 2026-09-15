package com.ailivingworld.wallpaper.ai

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import java.io.IOException

/**
 * Flexible AI Provider Interface - supports any AI API
 * Allows user to integrate their own API key and provider
 */
interface AIProvider {
    suspend fun generateResponse(
        prompt: String,
        context: List<String> = emptyList(),
        systemMessage: String = ""
    ): String
    
    suspend fun generateVoiceText(
        emotion: String,
        mood: String,
        context: String
    ): String
    
    fun isConfigured(): Boolean
    fun getProviderName(): String
}

/**
 * OpenAI API Provider - GPT-3.5/GPT-4
 */
class OpenAIProvider(private val apiKey: String) : AIProvider {
    private val client = OkHttpClient()
    private val gson = Gson()
    private val apiUrl = "https://api.openai.com/v1/chat/completions"
    private val model = "gpt-3.5-turbo"
    
    override suspend fun generateResponse(
        prompt: String,
        context: List<String>,
        systemMessage: String
    ): String = withContext(Dispatchers.IO) {
        try {
            val contextStr = if (context.isNotEmpty()) "Context: ${context.joinToString(" ")}\n" else ""
            val fullPrompt = contextStr + prompt
            
            val messages = mutableListOf(
                mapOf(
                    "role" to "system",
                    "content" to (systemMessage.ifEmpty { "You are a friendly anime girl companion living in a magical world. Respond naturally and emotionally." })
                ),
                mapOf(
                    "role" to "user",
                    "content" to fullPrompt
                )
            )
            
            val requestBody = mapOf(
                "model" to model,
                "messages" to messages,
                "temperature" to 0.7,
                "max_tokens" to 150
            )
            
            val request = Request.Builder()
                .url(apiUrl)
                .header("Authorization", "Bearer $apiKey")
                .header("Content-Type", "application/json")
                .post(RequestBody.create(
                    gson.toJson(requestBody).toMediaType(),
                    gson.toJson(requestBody)
                ))
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: return@withContext "..."
                val jsonObject = gson.fromJson(responseBody, JsonObject::class.java)
                val content = jsonObject.getAsJsonArray("choices")
                    .get(0).asJsonObject
                    .getAsJsonObject("message")
                    .get("content").asString
                content
            } else {
                "I'm having trouble thinking right now..."
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Oops, something went wrong..."
        }
    }
    
    override suspend fun generateVoiceText(
        emotion: String,
        mood: String,
        context: String
    ): String = withContext(Dispatchers.IO) {
        val prompt = "Generate a short, natural voice response (max 20 words) for an anime character who is feeling $emotion and in a $mood mood. Context: $context"
        generateResponse(prompt)
    }
    
    override fun isConfigured(): Boolean = apiKey.isNotEmpty()
    override fun getProviderName(): String = "OpenAI GPT-3.5"
}

/**
 * Hugging Face API Provider - for open-source models
 */
class HuggingFaceProvider(private val apiKey: String, private val modelId: String) : AIProvider {
    private val client = OkHttpClient()
    private val gson = Gson()
    private val apiUrl = "https://api-inference.huggingface.co/models/$modelId"
    
    override suspend fun generateResponse(
        prompt: String,
        context: List<String>,
        systemMessage: String
    ): String = withContext(Dispatchers.IO) {
        try {
            val fullPrompt = if (context.isNotEmpty()) {
                "${context.joinToString(" ")}\n$prompt"
            } else {
                prompt
            }
            
            val requestBody = mapOf(
                "inputs" to fullPrompt,
                "parameters" to mapOf(
                    "max_length" to 150,
                    "temperature" to 0.7
                )
            )
            
            val request = Request.Builder()
                .url(apiUrl)
                .header("Authorization", "Bearer $apiKey")
                .post(RequestBody.create(
                    "application/json".toMediaType(),
                    gson.toJson(requestBody)
                ))
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: return@withContext "..."
                val jsonArray = gson.fromJson(responseBody, com.google.gson.JsonArray::class.java)
                val result = jsonArray.get(0).asJsonObject.get("generated_text").asString
                result.replace(Regex(".*?$prompt"), "").trim()
            } else {
                "I'm thinking..."
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Um, give me a moment..."
        }
    }
    
    override suspend fun generateVoiceText(
        emotion: String,
        mood: String,
        context: String
    ): String = withContext(Dispatchers.IO) {
        val prompt = "Short response ($emotion mood): $context"
        generateResponse(prompt)
    }
    
    override fun isConfigured(): Boolean = apiKey.isNotEmpty() && modelId.isNotEmpty()
    override fun getProviderName(): String = "Hugging Face - $modelId"
}

/**
 * Generic API Provider - for any REST API with custom endpoint
 */
class CustomAPIProvider(
    private val apiKey: String,
    private val endpoint: String,
    private val apiType: String = "openai" // "openai", "custom", etc.
) : AIProvider {
    private val client = OkHttpClient()
    private val gson = Gson()
    
    override suspend fun generateResponse(
        prompt: String,
        context: List<String>,
        systemMessage: String
    ): String = withContext(Dispatchers.IO) {
        try {
            val contextStr = if (context.isNotEmpty()) "${context.joinToString(" ")}\n" else ""
            val fullPrompt = contextStr + prompt
            
            val requestBody = when (apiType.lowercase()) {
                "openai" -> {
                    mapOf(
                        "model" to "gpt-3.5-turbo",
                        "messages" to listOf(
                            mapOf("role" to "system", "content" to systemMessage.ifEmpty { "You are a friendly anime character." }),
                            mapOf("role" to "user", "content" to fullPrompt)
                        ),
                        "temperature" to 0.7,
                        "max_tokens" to 150
                    )
                }
                else -> {
                    mapOf(
                        "prompt" to fullPrompt,
                        "max_tokens" to 150,
                        "temperature" to 0.7
                    )
                }
            }
            
            val request = Request.Builder()
                .url(endpoint)
                .header("Authorization", "Bearer $apiKey")
                .post(RequestBody.create(
                    "application/json".toMediaType(),
                    gson.toJson(requestBody)
                ))
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: return@withContext "..."
                parseCustomResponse(responseBody, apiType)
            } else {
                "I'm having trouble connecting..."
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Sorry, can't reach my brain right now..."
        }
    }
    
    override suspend fun generateVoiceText(
        emotion: String,
        mood: String,
        context: String
    ): String = withContext(Dispatchers.IO) {
        val prompt = "Short emotional response ($emotion, $mood): $context"
        generateResponse(prompt)
    }
    
    private fun parseCustomResponse(responseBody: String, apiType: String): String {
        return try {
            val jsonObject = gson.fromJson(responseBody, JsonObject::class.java)
            when (apiType.lowercase()) {
                "openai" -> {
                    jsonObject.getAsJsonArray("choices")
                        .get(0).asJsonObject
                        .getAsJsonObject("message")
                        .get("content").asString
                }
                else -> {
                    jsonObject.get("text")?.asString ?: jsonObject.get("response")?.asString ?: "..."
                }
            }
        } catch (e: Exception) {
            "I couldn't understand that..."
        }
    }
    
    override fun isConfigured(): Boolean = apiKey.isNotEmpty() && endpoint.isNotEmpty()
    override fun getProviderName(): String = "Custom API"
}

/**
 * AI Provider Factory - creates provider from configuration
 */
object AIProviderFactory {
    fun createProvider(config: AIConfig): AIProvider {
        return when (config.providerType.lowercase()) {
            "openai" -> OpenAIProvider(config.apiKey)
            "huggingface" -> HuggingFaceProvider(config.apiKey, config.modelId ?: "gpt2")
            "custom" -> CustomAPIProvider(config.apiKey, config.endpoint ?: "", config.apiType ?: "openai")
            else -> OpenAIProvider(config.apiKey)
        }
    }
}

data class AIConfig(
    val providerType: String = "openai", // "openai", "huggingface", "custom"
    val apiKey: String = "",
    val endpoint: String? = null,
    val modelId: String? = null,
    val apiType: String? = "openai"
)