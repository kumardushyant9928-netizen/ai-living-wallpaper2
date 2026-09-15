package com.ailivingworld.wallpaper.ai

import com.ailivingworld.wallpaper.character.AnimeCharacter
import com.ailivingworld.wallpaper.character.AnimationState
import com.ailivingworld.wallpaper.character.PetAnimal

/**
 * Character Brain - manages personality, emotions, memory, and AI decisions
 */
class CharacterBrain(
    val character: AnimeCharacter
) {
    // Memory system
    val shortTermMemory = ShortTermMemory()
    val longTermMemory = LongTermMemory()
    val experienceMemory = ExperienceMemory()
    
    // Personality configuration
    var personality = PersonalityProfile(
        kindness = character.kindness,
        confidence = character.confidence,
        playfulness = character.playfulness,
        shyness = character.shyness,
        curiosity = 0.7f,
        patience = character.patience,
        humor = character.humor,
        seriousness = 1f - character.humor
    )
    
    // Relationship tracking
    var relationshipWithUser: Float = 0.5f // 0-1 scale
    var userInteractionCount: Int = 0
    
    // Mood state
    var dominantMood: String = "neutral"
    var moodIntensity: Float = 0.5f
    
    fun processEvent(event: WorldEvent) {
        // Record event
        experienceMemory.addEvent(event)
        userInteractionCount++
        
        // Analyze situation
        val emotionalResponse = analyzeEvent(event)
        
        // Update emotional state
        updateEmotions(emotionalResponse)
        
        // Update memory
        shortTermMemory.addMemory(event.description)
        
        // Generate behavior
        val action = generateAction(event, emotionalResponse)
        executeAction(action)
    }
    
    private fun analyzeEvent(event: WorldEvent): EmotionalResponse {
        val baseResponse = when (event.eventType) {
            EventType.TOUCH -> {
                EmotionalResponse(
                    happinessChange = 0.1f,
                    affectionChange = 0.08f,
                    trustChange = 0.05f,
                    dominantMood = "pleased"
                )
            }
            EventType.SHAKE -> {
                EmotionalResponse(
                    fearChange = 0.15f,
                    angerChange = 0.1f,
                    trustChange = -0.05f,
                    dominantMood = "startled"
                )
            }
            EventType.WATER_SPLASH -> {
                when {
                    character.playfulness > 0.7f -> EmotionalResponse(
                        happinessChange = 0.2f,
                        dominantMood = "playful"
                    )
                    character.shyness > 0.6f -> EmotionalResponse(
                        angerChange = 0.15f,
                        sadnessChange = 0.1f,
                        dominantMood = "embarrassed"
                    )
                    else -> EmotionalResponse(
                        angerChange = 0.15f,
                        dominantMood = "annoyed"
                    )
                }
            }
            EventType.TILT -> {
                if (character.isFalling) {
                    EmotionalResponse(
                        fearChange = 0.2f,
                        angerChange = 0.05f,
                        dominantMood = "scared"
                    )
                } else {
                    EmotionalResponse(
                        fearChange = 0.1f,
                        dominantMood = "cautious"
                    )
                }
            }
            EventType.CONVERSATION -> {
                EmotionalResponse(
                    happinessChange = 0.15f,
                    affectionChange = 0.1f,
                    trustChange = 0.08f,
                    dominantMood = "engaged"
                )
            }
            EventType.NEGLECT -> {
                EmotionalResponse(
                    sadnessChange = 0.2f,
                    affectionChange = -0.1f,
                    trustChange = -0.05f,
                    dominantMood = "lonely"
                )
            }
        }
        
        // Apply personality modifiers
        return baseResponse.copy(
            happinessChange = baseResponse.happinessChange * personality.positivityModifier(),
            affectionChange = baseResponse.affectionChange * (1f + personality.kindness * 0.3f),
            trustChange = baseResponse.trustChange * (1f + personality.confidence * 0.2f)
        )
    }
    
    private fun updateEmotions(response: EmotionalResponse) {
        character.happiness = (character.happiness + response.happinessChange).coerceIn(0f, 1f)
        character.affection = (character.affection + response.affectionChange).coerceIn(0f, 1f)
        character.trust = (character.trust + response.trustChange).coerceIn(0f, 1f)
        character.fear = (character.fear + response.fearChange).coerceIn(0f, 1f)
        character.anger = (character.anger + response.angerChange).coerceIn(0f, 1f)
        character.sadness = (character.sadness + response.sadnessChange).coerceIn(0f, 1f)
        
        dominantMood = response.dominantMood
        moodIntensity = maxOf(
            character.happiness,
            character.anger,
            character.fear,
            character.sadness
        )
        
        relationshipWithUser = (relationshipWithUser + response.affectionChange * 0.1f).coerceIn(0f, 1f)
    }
    
    private fun generateAction(event: WorldEvent, response: EmotionalResponse): CharacterAction {
        val animation = when (response.dominantMood) {
            "pleased" -> AnimationState.WAVE
            "playful" -> AnimationState.DANCE
            "startled" -> AnimationState.GRAB
            "scared" -> AnimationState.FALL
            "annoyed" -> AnimationState.CONFUSED
            "embarrassed" -> AnimationState.CONFUSED
            "engaged" -> AnimationState.WAVE
            "lonely" -> AnimationState.CRY
            else -> AnimationState.IDLE
        }
        
        return CharacterAction(
            animation = animation,
            voiceResponse = generateVoiceResponse(response),
            gestureDescription = response.dominantMood
        )
    }
    
    private fun executeAction(action: CharacterAction) {
        // Play animation
        character.currentAnimation = action.animation
        
        // Queue voice response (handled by AI provider)
        // Queue gesture/expression update
    }
    
    private fun generateVoiceResponse(response: EmotionalResponse): String {
        return when (response.dominantMood) {
            "pleased" -> listOf(
                "Ah, thank you!",
                "That feels nice...",
                "Hehe, hi there!"
            ).random()
            "playful" -> listOf(
                "Haha, did you do that on purpose?",
                "That's fun!",
                "Again!"
            ).random()
            "startled" -> listOf(
                "Whoa!",
                "S-scary!",
                "Ah!"
            ).random()
            "scared" -> listOf(
                "Help!",
                "I'm falling!",
                "Nooo!"
            ).random()
            "annoyed" -> listOf(
                "Hey, stop that!",
                "I'm not happy...",
                "Hmph!"
            ).random()
            "embarrassed" -> listOf(
                "Ugh, how embarrassing...",
                "W-why did you do that?!",
                "I'm soaking wet..."
            ).random()
            "engaged" -> listOf(
                "Oh really?",
                "Tell me more!",
                "That's interesting!"
            ).random()
            "lonely" -> listOf(
                "...Where did you go?",
                "I miss you...",
                "Sigh..."
            ).random()
            else -> "..."
        }
    }
}

data class PersonalityProfile(
    val kindness: Float = 0.7f,
    val confidence: Float = 0.6f,
    val playfulness: Float = 0.75f,
    val shyness: Float = 0.3f,
    val curiosity: Float = 0.7f,
    val patience: Float = 0.6f,
    val humor: Float = 0.7f,
    val seriousness: Float = 0.3f
) {
    fun positivityModifier(): Float {
        return (kindness + playfulness + curiosity) / 3f
    }
}

data class EmotionalResponse(
    val happinessChange: Float = 0f,
    val affectionChange: Float = 0f,
    val trustChange: Float = 0f,
    val fearChange: Float = 0f,
    val angerChange: Float = 0f,
    val sadnessChange: Float = 0f,
    val dominantMood: String = "neutral"
)

data class CharacterAction(
    val animation: AnimationState,
    val voiceResponse: String,
    val gestureDescription: String
)

data class WorldEvent(
    val eventType: EventType,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class EventType {
    TOUCH,
    SHAKE,
    WATER_SPLASH,
    TILT,
    CONVERSATION,
    NEGLECT,
    OTHER
}