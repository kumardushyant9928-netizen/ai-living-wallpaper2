package com.ailivingworld.wallpaper.character

/**
 * Character controller - manages character state and behavior
 */
class CharacterController(val character: AnimeCharacter) {
    
    fun playAnimation(state: AnimationState, speed: Float = 1f) {
        character.currentAnimation = state
        character.animationSpeed = speed
        character.animationProgress = 0f
    }
    
    fun onTouched() {
        character.happiness += 0.1f
        character.affection += 0.05f
        character.trust += 0.02f
        playAnimation(AnimationState.WAVE, 1.5f)
    }
    
    fun onPhoneShaken() {
        character.fear += 0.15f
        character.anger += 0.05f
        playAnimation(AnimationState.GRAB, 2f)
    }
    
    fun onWaterSplash() {
        character.wetness = 0.8f
        when {
            character.playfulness > 0.7f -> {
                character.happiness += 0.2f
                playAnimation(AnimationState.LAUGH, 1.2f)
            }
            character.shyness > 0.6f -> {
                character.anger += 0.1f
                character.sadness += 0.05f
                playAnimation(AnimationState.CONFUSED, 1.5f)
            }
            else -> {
                character.anger += 0.15f
                playAnimation(AnimationState.CONFUSED, 1.3f)
            }
        }
    }
    
    fun onLongTimeWithoutInteraction(deltaTime: Float) {
        character.sadness += deltaTime * 0.02f
        character.affection -= deltaTime * 0.005f
        character.energy -= deltaTime * 0.01f
    }
    
    fun updateCharacter(deltaTime: Float) {
        character.update(deltaTime)
        
        // Auto-animation behavior
        if (character.currentAnimation == AnimationState.IDLE) {
            // Random idle animations
            val randomVal = Math.random().toFloat()
            when {
                randomVal > 0.98f && character.energy > 0.6f -> {
                    playAnimation(AnimationState.DANCE, 0.8f)
                }
                randomVal > 0.96f -> {
                    playAnimation(AnimationState.WAVE, 0.6f)
                }
            }
        }
    }
}