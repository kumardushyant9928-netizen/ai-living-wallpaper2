package com.ailivingworld.wallpaper.wallpaper

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.opengl.GLSurfaceView
import android.service.wallpaper.WallpaperService
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.SurfaceHolder
import com.ailivingworld.wallpaper.ai.*
import com.ailivingworld.wallpaper.character.AnimeCharacter
import com.ailivingworld.wallpaper.character.CharacterController
import com.ailivingworld.wallpaper.character.PetAnimal
import com.ailivingworld.wallpaper.physics.PhysicsEngine
import com.ailivingworld.wallpaper.physics.SensorManager as WorldSensorManager
import com.ailivingworld.wallpaper.renderer.CharacterRenderData
import com.ailivingworld.wallpaper.renderer.GLRenderer
import com.ailivingworld.wallpaper.renderer.WorldRenderData
import kotlinx.coroutines.*
import timber.log.Timber

/**
 * Main Live Wallpaper Service - orchestrates everything
 */
class LiveWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        Timber.plant(Timber.DebugTree())
        return WallpaperEngine()
    }

    inner class WallpaperEngine : Engine(), SensorEventListener {
        private lateinit var glSurfaceView: GLSurfaceView
        private var glRenderer: GLRenderer? = null
        private var isVisible = false
        private var isPreview = false

        // Game world
        private lateinit var physicsEngine: PhysicsEngine
        private lateinit var character: AnimeCharacter
        private lateinit var characterController: CharacterController
        private lateinit var characterBrain: CharacterBrain
        private lateinit var pet: PetAnimal
        private lateinit var worldSensorManager: WorldSensorManager
        private lateinit var sensorManager: SensorManager

        // AI System
        private var aiProvider: AIProvider? = null
        private val gameScope = CoroutineScope(Dispatchers.Main + Job())
        private var lastInteractionTime = System.currentTimeMillis()
        private var isProcessingAI = false

        // Input handling
        private lateinit var gestureDetector: GestureDetector
        private var lastTouchX = 0f
        private var lastTouchY = 0f

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)

            Timber.d("WallpaperEngine onCreate")

            // Initialize GL Surface
            glSurfaceView = GLSurfaceView(this@LiveWallpaperService).apply {
                setEGLContextClientVersion(2)
                glRenderer = GLRenderer()
                setRenderer(glRenderer)
                holder.addCallback(this@WallpaperEngine)
            }

            // Initialize game systems
            initializeGameWorld()
            initializeSensors()
            initializeAI()
            initializeGestureDetection()

            // Start game loop
            startGameLoop()
        }

        private fun initializeGameWorld() {
            physicsEngine = PhysicsEngine(
                worldWidth = 1080f,
                worldHeight = 1920f
            )

            character = AnimeCharacter(
                name = "Akira",
                x = 540f,
                y = 960f
            )

            characterController = CharacterController(character)
            characterBrain = CharacterBrain(character)

            pet = PetAnimal(
                name = "Mochi",
                x = 600f,
                y = 1000f
            )

            // Add to physics
            physicsEngine.addBody(character.physicsBody)
            physicsEngine.addBody(pet.physicsBody)

            Timber.d("Game world initialized - Character: ${character.name}, Pet: ${pet.name}")
        }

        private fun initializeSensors() {
            worldSensorManager = WorldSensorManager()
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

            val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

            if (accelerometer != null) {
                sensorManager.registerListener(
                    this@WallpaperEngine,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_GAME
                )
            }

            if (gyroscope != null) {
                sensorManager.registerListener(
                    this@WallpaperEngine,
                    gyroscope,
                    SensorManager.SENSOR_DELAY_GAME
                )
            }

            Timber.d("Sensors initialized")
        }

        private fun initializeAI() {
            // Load AI configuration from SharedPreferences
            val prefs = getSharedPreferences("ai_config", Context.MODE_PRIVATE)
            val providerType = prefs.getString("provider_type", "openai") ?: "openai"
            val apiKey = prefs.getString("api_key", "") ?: ""
            val endpoint = prefs.getString("endpoint", "")
            val modelId = prefs.getString("model_id", "")

            if (apiKey.isEmpty()) {
                Timber.w("No AI API key configured - AI features disabled")
                return
            }

            try {
                val config = AIConfig(
                    providerType = providerType,
                    apiKey = apiKey,
                    endpoint = endpoint,
                    modelId = modelId
                )
                aiProvider = AIProviderFactory.createProvider(config)
                Timber.d("AI Provider initialized: ${aiProvider?.getProviderName()}")
            } catch (e: Exception) {
                Timber.e(e, "Failed to initialize AI provider")
            }
        }

        private fun initializeGestureDetection() {
            gestureDetector = GestureDetector(
                this@LiveWallpaperService,
                object : GestureDetector.SimpleOnGestureListener() {
                    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                        onCharacterTouched(e.x, e.y)
                        return true
                    }

                    override fun onLongPress(e: MotionEvent) {
                        onCharacterHeld(e.x, e.y)
                    }

                    override fun onFling(
                        e1: MotionEvent,
                        e2: MotionEvent,
                        velocityX: Float,
                        velocityY: Float
                    ): Boolean {
                        characterBrain.processEvent(
                            WorldEvent(
                                EventType.SHAKE,
                                "Device was shaken"
                            )
                        )
                        return true
                    }
                }
            )
        }

        private fun startGameLoop() {
            gameScope.launch {
                var lastTime = System.currentTimeMillis()
                while (isActive) {
                    val currentTime = System.currentTimeMillis()
                    val deltaTime = (currentTime - lastTime) / 1000f
                    lastTime = currentTime

                    // Update sensors
                    worldSensorManager.updateSensors(deltaTime)
                    val gravity = worldSensorManager.getWorldGravity()
                    physicsEngine.gravityY = gravity[1]
                    physicsEngine.gravityX = gravity[0]

                    // Update physics
                    physicsEngine.step()

                    // Update character
                    character.x = character.physicsBody.x
                    character.y = character.physicsBody.y
                    characterController.updateCharacter(deltaTime)

                    // Update pet
                    pet.x = pet.physicsBody.x
                    pet.y = pet.physicsBody.y
                    pet.update(deltaTime)

                    // Check for long inactivity
                    val timeSinceInteraction = currentTime - lastInteractionTime
                    if (timeSinceInteraction > 30000 && timeSinceInteraction % 10000 < 16) {
                        characterBrain.processEvent(
                            WorldEvent(
                                EventType.NEGLECT,
                                "You haven't interacted for ${timeSinceInteraction / 1000}s"
                            )
                        )
                    }

                    // Update rendering data
                    updateRenderData()

                    // Frame rate: ~60 FPS
                    delay(16)
                }
            }
        }

        private fun updateRenderData() {
            val characterRenderData = CharacterRenderData(
                x = character.x / 540f - 1f,
                y = character.y / 960f - 1f,
                rotation = character.physicsBody.rotation,
                expression = character.getExpressionDescription(),
                animationProgress = character.animationProgress
            )

            glRenderer?.setCharacterData(characterRenderData)
        }

        private fun onCharacterTouched(x: Float, y: Float) {
            lastTouchX = x
            lastTouchY = y
            lastInteractionTime = System.currentTimeMillis()

            characterController.onTouched()
            characterBrain.processEvent(
                WorldEvent(
                    EventType.TOUCH,
                    "Character was touched at ($x, $y)"
                )
            )

            generateAIResponse("User just touched me!")
            Timber.d("Character touched at $x, $y")
        }

        private fun onCharacterHeld(x: Float, y: Float) {
            lastInteractionTime = System.currentTimeMillis()
            character.physicsBody.applyImpulse(0f, -500f) // Jump up
            generateAIResponse("Oh! You picked me up!")
        }

        private fun generateAIResponse(context: String) {
            if (isProcessingAI || aiProvider == null || !aiProvider!!.isConfigured()) {
                return
            }

            isProcessingAI = true
            gameScope.launch {
                try {
                    val systemMessage = "You are ${character.name}, a ${character.clothing.top} wearing anime girl companion. " +
                        "Current emotion: ${characterBrain.dominantMood}. Respond naturally and briefly."

                    val response = aiProvider!!.generateResponse(
                        prompt = context,
                        context = characterBrain.characterBrain.shortTermMemory.getRecent(3),
                        systemMessage = systemMessage
                    )

                    Timber.d("AI Response: $response")
                    characterBrain.characterBrain.shortTermMemory.addMemory("AI said: $response")
                } catch (e: Exception) {
                    Timber.e(e, "Error generating AI response")
                } finally {
                    isProcessingAI = false
                }
            }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            return gestureDetector.onTouchEvent(event)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            isVisible = visible
            if (visible) {
                glSurfaceView.onResume()
            } else {
                glSurfaceView.onPause()
            }
        }

        override fun onDestroy() {
            isVisible = false
            glSurfaceView.onPause()
            sensorManager.unregisterListener(this)
            gameScope.cancel()
            super.onDestroy()
            Timber.d("WallpaperEngine destroyed")
        }

        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    worldSensorManager.accelerometerX = event.values[0]
                    worldSensorManager.accelerometerY = event.values[1]
                    worldSensorManager.accelerometerZ = event.values[2]
                }
                Sensor.TYPE_GYROSCOPE -> {
                    worldSensorManager.gyroscopeX = event.values[0]
                    worldSensorManager.gyroscopeY = event.values[1]
                    worldSensorManager.gyroscopeZ = event.values[2]
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

        override fun onSurfaceCreated(holder: SurfaceHolder) {}

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {}
    }
}

// Extension property for accessing brain from character
private val CharacterBrain.characterBrain: CharacterBrain get() = this