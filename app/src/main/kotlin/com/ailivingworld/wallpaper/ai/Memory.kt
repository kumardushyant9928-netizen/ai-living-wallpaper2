package com.ailivingworld.wallpaper.ai

import java.io.Serializable

/**
 * Short-term memory - current conversation and recent events
 */
class ShortTermMemory {
    private val memories = mutableListOf<String>()
    private val maxCapacity = 50
    
    fun addMemory(memory: String) {
        memories.add(0, "[${System.currentTimeMillis()}] $memory")
        if (memories.size > maxCapacity) {
            memories.removeAt(memories.lastIndex)
        }
    }
    
    fun getRecent(count: Int = 10): List<String> {
        return memories.take(count)
    }
    
    fun clear() {
        memories.clear()
    }
}

/**
 * Long-term memory - important facts, preferences, past conversations
 */
class LongTermMemory {
    private val facts = mutableMapOf<String, String>()
    private val preferences = mutableMapOf<String, Float>()
    private val relationships = mutableMapOf<String, Float>()
    
    fun addFact(key: String, value: String) {
        facts[key] = value
    }
    
    fun getFact(key: String): String? = facts[key]
    
    fun setPreference(key: String, value: Float) {
        preferences[key] = value.coerceIn(0f, 1f)
    }
    
    fun getPreference(key: String): Float = preferences[key] ?: 0.5f
    
    fun setRelationshipValue(characterId: String, value: Float) {
        relationships[characterId] = value.coerceIn(0f, 1f)
    }
    
    fun getRelationshipValue(characterId: String): Float = relationships[characterId] ?: 0.5f
    
    fun getAllFacts(): Map<String, String> = facts.toMap()
    fun getAllPreferences(): Map<String, Float> = preferences.toMap()
}

/**
 * Experience memory - events that occurred in the world
 */
class ExperienceMemory {
    private val events = mutableListOf<ExperienceEvent>()
    private val maxCapacity = 500
    
    fun addEvent(event: WorldEvent) {
        events.add(0, ExperienceEvent(
            type = event.eventType.name,
            description = event.description,
            timestamp = event.timestamp,
            significanceScore = 0.5f
        ))
        if (events.size > maxCapacity) {
            events.removeAt(events.lastIndex)
        }
    }
    
    fun getRecentExperiences(count: Int = 20): List<ExperienceEvent> {
        return events.take(count)
    }
    
    fun getSignificantExperiences(minScore: Float = 0.7f): List<ExperienceEvent> {
        return events.filter { it.significanceScore >= minScore }
    }
}

data class ExperienceEvent(
    val type: String,
    val description: String,
    val timestamp: Long,
    val significanceScore: Float
) : Serializable