package com.ailivingworld.wallpaper.physics

import kotlin.math.cos
import kotlin.math.sin

/**
 * Sensor fusion and world orientation
 */
class SensorFusion {
    private var roll: Float = 0f  // X-axis rotation (tilt left/right)
    private var pitch: Float = 0f // Y-axis rotation (tilt forward/back)
    private var yaw: Float = 0f   // Z-axis rotation (compass)
    
    private val alpha = 0.1f // Low-pass filter alpha
    
    fun updateAccelerometer(ax: Float, ay: Float, az: Float) {
        // Calculate roll and pitch from accelerometer
        val newRoll = Math.atan2(ay.toDouble(), az.toDouble()).toFloat()
        val newPitch = Math.atan2(-ax.toDouble(), (ay * ay + az * az).toDouble()).toFloat()
        
        roll = roll * (1 - alpha) + newRoll * alpha
        pitch = pitch * (1 - alpha) + newPitch * alpha
    }
    
    fun updateGyroscope(gx: Float, gy: Float, gz: Float, dt: Float) {
        roll += gx * dt
        pitch += gy * dt
        yaw += gz * dt
    }
    
    fun updateMagnetometer(mx: Float, my: Float, mz: Float) {
        // Simplified magnetometer fusion
        yaw = Math.atan2(my.toDouble(), mx.toDouble()).toFloat()
    }
    
    fun getGravityVector(): FloatArray {
        // Convert world gravity to device frame based on orientation
        val gravityX = sin(roll.toDouble()).toFloat() * 9.8f
        val gravityY = -sin(pitch.toDouble()).toFloat() * 9.8f
        val gravityZ = cos(roll.toDouble()).toFloat() * cos(pitch.toDouble()).toFloat() * 9.8f
        return floatArrayOf(gravityX, gravityY, gravityZ)
    }
    
    fun getRoll(): Float = roll
    fun getPitch(): Float = pitch
    fun getYaw(): Float = yaw
}

/**
 * Platform-agnostic sensor manager
 */
class SensorManager {
    private val fusion = SensorFusion()
    
    var accelerometerX = 0f
    var accelerometerY = 0f
    var accelerometerZ = 9.8f
    
    var gyroscopeX = 0f
    var gyroscopeY = 0f
    var gyroscopeZ = 0f
    
    var magnetometerX = 0f
    var magnetometerY = 0f
    var magnetometerZ = 0f
    
    fun updateSensors(deltaTime: Float) {
        fusion.updateAccelerometer(accelerometerX, accelerometerY, accelerometerZ)
        fusion.updateGyroscope(gyroscopeX, gyroscopeY, gyroscopeZ, deltaTime)
        fusion.updateMagnetometer(magnetometerX, magnetometerY, magnetometerZ)
    }
    
    fun getWorldGravity(): FloatArray = fusion.getGravityVector()
    
    fun getOrientation(): FloatArray = floatArrayOf(
        fusion.getRoll(),
        fusion.getPitch(),
        fusion.getYaw()
    )
}