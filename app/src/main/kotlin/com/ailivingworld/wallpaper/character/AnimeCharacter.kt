package com.ailivingworld.wallpaper.character

import com.ailivingworld.wallpaper.physics.PhysicsBody
import kotlin.math.abs

/**
 * Anime character decomposition system
 * Characters are built from modular parts: head, body, limbs, clothing
 */

data class AnimeCharacter(
    var id: String = "char_001",
    var name: String = "Akira",
    var x: Float = 540f,
    var y: Float = 960f
) {
    // Character parts (anime decomposition)
    var head: Head = Head()
    var body: Body = Body()
    var leftArm: Arm = Arm(side = ArmSide.LEFT)
    var rightArm: Arm = Arm(side = ArmSide.RIGHT)
    var leftLeg: Leg = Leg(side = LegSide.LEFT)
    var rightLeg: Leg = Leg(side = LegSide.RIGHT)
    var clothing: Clothing = Clothing()
    
    // Physics body
    var physicsBody: PhysicsBody = PhysicsBody(
        x = x,
        y = y,
        radius = 30f,
        mass = 60f,
        tag = "character"
    )
    
    // Animation state
    var currentAnimation: AnimationState = AnimationState.IDLE
    var animationProgress: Float = 0f
    var animationSpeed: Float = 1f
    
    // Emotional state
    var happiness: Float = 0.5f
    var trust: Float = 0.5f
    var fear: Float = 0.1f
    var anger: Float = 0.05f
    var curiosity: Float = 0.7f
    var sadness: Float = 0.1f
    var affection: Float = 0.5f
    var energy: Float = 0.8f
    
    // Personality traits
    var kindness: Float = 0.7f
    var confidence: Float = 0.6f
    var playfulness: Float = 0.75f
    var shyness: Float = 0.3f
    var patience: Float = 0.6f
    var humor: Float = 0.7f
    
    // Physical state
    var isBalanced: Boolean = true
    var isFalling: Boolean = false
    var health: Float = 100f
    var wetness: Float = 0f
    var temperature: Float = 37f // Body temperature
    
    fun update(deltaTime: Float) {
        // Sync physics body position
        physicsBody.x = x
        physicsBody.y = y
        
        // Update animation
        animationProgress += deltaTime * animationSpeed
        if (animationProgress > 1f) {
            animationProgress = 0f
        }
        
        // Check balance state
        val tilt = abs(physicsBody.velocityX)
        isBalanced = tilt < 2f
        isFalling = physicsBody.velocityY > 1f
        
        // Update emotional decay (return to neutral)
        happiness = lerp(happiness, 0.5f, deltaTime * 0.1f)
        fear = lerp(fear, 0.1f, deltaTime * 0.05f)
        anger = lerp(anger, 0.05f, deltaTime * 0.05f)
        
        // Reduce wetness over time
        wetness = maxOf(0f, wetness - deltaTime * 0.05f)
    }
    
    fun getExpressionDescription(): String {
        return when {
            isFalling && fear > 0.5f -> "scared"
            !isBalanced && fear > 0.3f -> "startled"
            happiness > 0.7f -> "happy"
            anger > 0.6f -> "angry"
            sadness > 0.6f -> "sad"
            curiosity > 0.7f -> "curious"
            affection > 0.7f && trust > 0.6f -> "affectionate"
            else -> "neutral"
        }
    }
    
    private fun lerp(a: Float, b: Float, t: Float): Float {
        return a + (b - a) * t.coerceIn(0f, 1f)
    }
}

data class Head(
    var faceShape: FaceShape = FaceShape.ROUND,
    var skinTone: SkinTone = SkinTone.PALE,
    var eyeStyle: EyeStyle = EyeStyle.LARGE_ROUND,
    var hairColor: String = "#2a2a2a",
    var hairLength: Float = 1f, // 0-1 scale
    var expressions: MutableMap<String, Float> = mutableMapOf(
        "happy" to 0f,
        "sad" to 0f,
        "angry" to 0f,
        "surprised" to 0f,
        "scared" to 0f
    )
)

data class Body(
    var height: Float = 160f,
    var width: Float = 40f,
    var bodyType: BodyType = BodyType.SLENDER,
    var skinTone: SkinTone = SkinTone.PALE
)

data class Arm(
    var side: ArmSide = ArmSide.RIGHT,
    var length: Float = 60f,
    var rotation: Float = 0f,
    var handPosition: Pair<Float, Float> = Pair(0f, 0f)
)

data class Leg(
    var side: LegSide = LegSide.RIGHT,
    var length: Float = 80f,
    var rotation: Float = 0f,
    var footPosition: Pair<Float, Float> = Pair(0f, 0f)
)

data class Clothing(
    var top: ClothingTop = ClothingTop.SCHOOL_UNIFORM,
    var bottom: ClothingBottom = ClothingBottom.SKIRT,
    var shoes: Shoes = Shoes.SCHOOL_SHOES,
    var outerwear: Outerwear = Outerwear.NONE,
    var accessories: MutableList<String> = mutableListOf("hair_ribbon")
)

enum class FaceShape {
    ROUND, OVAL, SQUARE, HEART, LONG
}

enum class SkinTone {
    PALE, FAIR, MEDIUM, OLIVE, DARK
}

enum class EyeStyle {
    LARGE_ROUND, NARROW_SHARP, CAT_LIKE, SLEEPY, DETERMINED
}

enum class BodyType {
    SLENDER, ATHLETIC, CURVY, PETITE
}

enum class ArmSide {
    LEFT, RIGHT
}

enum class LegSide {
    LEFT, RIGHT
}

enum class ClothingTop {
    SCHOOL_UNIFORM, HOODIE, TANK_TOP, DRESS, SHIRT
}

enum class ClothingBottom {
    SKIRT, PANTS, SHORTS, LEGGINGS
}

enum class Shoes {
    SCHOOL_SHOES, SNEAKERS, BOOTS, SANDALS, BAREFOOT
}

enum class Outerwear {
    NONE, JACKET, COAT, CARDIGAN, BLAZER
}

enum class AnimationState {
    IDLE, WALK, RUN, SIT, FALL, GRAB, RECOVER, DANCE, JUMP, WAVE, CONFUSED, LAUGH, CRY
}

data class PetAnimal(
    var id: String = "pet_001",
    var name: String = "Mochi",
    var animalType: AnimalType = AnimalType.CAT,
    var x: Float = 600f,
    var y: Float = 1000f,
    var color: String = "#FF9E64"
) {
    var physicsBody: PhysicsBody = PhysicsBody(
        x = x,
        y = y,
        radius = 20f,
        mass = 3f,
        tag = "pet"
    )
    
    var happiness: Float = 0.7f
    var energy: Float = 0.8f
    var affectionToCharacter: Float = 0.6f
    var currentAnimation: AnimationState = AnimationState.IDLE
    var animationProgress: Float = 0f
    
    fun update(deltaTime: Float) {
        physicsBody.x = x
        physicsBody.y = y
        
        animationProgress += deltaTime * 0.8f
        if (animationProgress > 1f) {
            animationProgress = 0f
        }
        
        happiness = maxOf(0f, happiness - deltaTime * 0.01f)
        energy = maxOf(0f, energy - deltaTime * 0.02f)
    }
}

enum class AnimalType {
    CAT, RABBIT, BIRD, DOG, FOX
}