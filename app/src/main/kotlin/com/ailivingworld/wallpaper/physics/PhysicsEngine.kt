package com.ailivingworld.wallpaper.physics

import kotlin.math.*

/**
 * Lightweight 2D physics engine for AI Living World
 * Handles gravity, collision, friction, and physics bodies
 */
class PhysicsEngine (
    val worldWidth: Float = 1080f,
    val worldHeight: Float = 1920f,
    val gravityX: Float = 0f,
    val gravityY: Float = 9.8f
) {
    private val bodies = mutableListOf<PhysicsBody>()
    private val collisions = mutableListOf<Collision>()
    
    var deltaTime: Float = 0.016f // ~60 FPS
    var friction: Float = 0.98f
    var bounceRestitution: Float = 0.6f
    
    fun addBody(body: PhysicsBody) {
        bodies.add(body)
    }
    
    fun removeBody(body: PhysicsBody) {
        bodies.remove(body)
    }
    
    fun step() {
        // Apply forces (gravity)
        for (body in bodies) {
            if (!body.isStatic) {
                body.velocityY += gravityY * deltaTime
                body.velocityX += gravityX * deltaTime
                
                // Apply friction
                body.velocityX *= friction
                body.velocityY *= friction
            }
        }
        
        // Update positions
        for (body in bodies) {
            if (!body.isStatic) {
                body.x += body.velocityX * deltaTime
                body.y += body.velocityY * deltaTime
                body.rotation += body.angularVelocity * deltaTime
            }
        }
        
        // Detect and resolve collisions
        collisions.clear()
        detectCollisions()
        resolveCollisions()
        
        // Clamp to world bounds
        for (body in bodies) {
            constrainToWorld(body)
        }
    }
    
    private fun detectCollisions() {
        for (i in bodies.indices) {
            for (j in i + 1 until bodies.size) {
                val bodyA = bodies[i]
                val bodyB = bodies[j]
                
                if (bodyA.isStatic && bodyB.isStatic) continue
                
                val collision = checkCollision(bodyA, bodyB)
                if (collision != null) {
                    collisions.add(collision)
                }
            }
        }
    }
    
    private fun checkCollision(a: PhysicsBody, b: PhysicsBody): Collision? {
        val dx = b.x - a.x
        val dy = b.y - a.y
        val distance = sqrt(dx * dx + dy * dy)
        val minDistance = a.radius + b.radius
        
        return if (distance < minDistance) {
            val overlap = minDistance - distance
            val nx = if (distance > 0) dx / distance else 1f
            val ny = if (distance > 0) dy / distance else 0f
            Collision(a, b, nx, ny, overlap)
        } else {
            null
        }
    }
    
    private fun resolveCollisions() {
        for (collision in collisions) {
            val a = collision.bodyA
            val b = collision.bodyB
            val nx = collision.normalX
            val ny = collision.normalY
            val overlap = collision.overlap
            
            // Separate bodies
            val separationX = nx * overlap * 0.5f
            val separationY = ny * overlap * 0.5f
            
            if (!a.isStatic) {
                a.x -= separationX
                a.y -= separationY
            }
            if (!b.isStatic) {
                b.x += separationX
                b.y += separationY
            }
            
            // Resolve velocity
            val relVelX = b.velocityX - a.velocityX
            val relVelY = b.velocityY - a.velocityY
            val velAlongNormal = relVelX * nx + relVelY * ny
            
            if (velAlongNormal < 0) {
                val restitution = minOf(a.bounceRestitution, b.bounceRestitution)
                val impulse = -(1 + restitution) * velAlongNormal / (a.invMass + b.invMass)
                
                a.velocityX -= impulse * a.invMass * nx
                a.velocityY -= impulse * a.invMass * ny
                b.velocityX += impulse * b.invMass * nx
                b.velocityY += impulse * b.invMass * ny
            }
        }
    }
    
    private fun constrainToWorld(body: PhysicsBody) {
        // Bottom collision
        if (body.y + body.radius > worldHeight) {
            body.y = worldHeight - body.radius
            body.velocityY *= -bounceRestitution
            if (abs(body.velocityY) < 0.1f) body.velocityY = 0f
        }
        
        // Top collision
        if (body.y - body.radius < 0) {
            body.y = body.radius
            body.velocityY *= -bounceRestitution
        }
        
        // Left collision
        if (body.x - body.radius < 0) {
            body.x = body.radius
            body.velocityX *= -bounceRestitution
        }
        
        // Right collision
        if (body.x + body.radius > worldWidth) {
            body.x = worldWidth - body.radius
            body.velocityX *= -bounceRestitution
        }
    }
    
    fun getAllBodies(): List<PhysicsBody> = bodies.toList()
    fun getCollisions(): List<Collision> = collisions.toList()
}

data class PhysicsBody(
    var x: Float = 0f,
    var y: Float = 0f,
    var velocityX: Float = 0f,
    var velocityY: Float = 0f,
    var rotation: Float = 0f,
    var angularVelocity: Float = 0f,
    var radius: Float = 50f,
    var mass: Float = 1f,
    var bounceRestitution: Float = 0.6f,
    var isStatic: Boolean = false,
    var tag: String = ""
) {
    val invMass: Float get() = if (isStatic) 0f else 1f / mass
    
    fun applyForce(forceX: Float, forceY: Float) {
        if (!isStatic) {
            velocityX += forceX / mass
            velocityY += forceY / mass
        }
    }
    
    fun applyImpulse(impulseX: Float, impulseY: Float) {
        if (!isStatic) {
            velocityX += impulseX * invMass
            velocityY += impulseY * invMass
        }
    }
    
    fun setVelocity(vx: Float, vy: Float) {
        velocityX = vx
        velocityY = vy
    }
}

data class Collision(
    val bodyA: PhysicsBody,
    val bodyB: PhysicsBody,
    val normalX: Float,
    val normalY: Float,
    val overlap: Float
)