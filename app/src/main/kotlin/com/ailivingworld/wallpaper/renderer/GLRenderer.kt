package com.ailivingworld.wallpaper.renderer

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

/**
 * OpenGL ES 2.0 Renderer for 2D world rendering
 */
class GLRenderer : GLSurfaceView.Renderer {
    
    private var screenWidth = 1080
    private var screenHeight = 1920
    
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    
    private var characterRenderer: CharacterRenderer? = null
    private var worldRenderer: WorldRenderer? = null
    
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.95f, 0.95f, 1f, 1f) // Light blue background
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        
        // Initialize renderers
        characterRenderer = CharacterRenderer()
        worldRenderer = WorldRenderer()
    }
    
    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        
        // Set up camera
        Matrix.setLookAtM(
            viewMatrix, 0,
            0f, 0f, 1f, // Camera position
            0f, 0f, 0f, // Look at
            0f, 1f, 0f  // Up vector
        )
        
        // Render world
        worldRenderer?.render(projectionMatrix, viewMatrix)
        
        // Render character
        characterRenderer?.render(projectionMatrix, viewMatrix)
    }
    
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
        GLES20.glViewport(0, 0, width, height)
        
        val aspectRatio = width.toFloat() / height.toFloat()
        
        // Set up orthographic projection for 2D
        val left = -aspectRatio
        val right = aspectRatio
        val bottom = -1f
        val top = 1f
        
        Matrix.orthoM(projectionMatrix, 0, left, right, bottom, top, 0.1f, 100f)
    }
    
    fun setCharacterData(characterData: CharacterRenderData) {
        characterRenderer?.updateCharacter(characterData)
    }
    
    fun setWorldData(worldData: WorldRenderData) {
        worldRenderer?.updateWorld(worldData)
    }
}

/**
 * Simple shader program manager
 */
class ShaderProgram(vertexShaderCode: String, fragmentShaderCode: String) {
    
    private val programId: Int
    
    init {
        val vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        
        programId = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
            
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(it, GLES20.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] == 0) {
                GLES20.glDeleteProgram(it)
                throw RuntimeException("Failed to link program: ${GLES20.glGetProgramInfoLog(it)}")
            }
        }
        
        GLES20.glDeleteShader(vertexShader)
        GLES20.glDeleteShader(fragmentShader)
    }
    
    fun use() {
        GLES20.glUseProgram(programId)
    }
    
    fun getAttributeLocation(name: String): Int = GLES20.glGetAttribLocation(programId, name)
    fun getUniformLocation(name: String): Int = GLES20.glGetUniformLocation(programId, name)
    
    private fun compileShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        
        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            GLES20.glDeleteShader(shader)
            throw RuntimeException("Failed to compile shader: ${GLES20.glGetShaderInfoLog(shader)}")
        }
        
        return shader
    }
}

/**
 * Character renderer - draws anime character with layering
 */
class CharacterRenderer {
    
    private val vertexShaderCode = """
        uniform mat4 uMVPMatrix;
        attribute vec4 vPosition;
        attribute vec2 vTexCoord;
        varying vec2 texCoord;
        
        void main() {
            gl_Position = uMVPMatrix * vPosition;
            texCoord = vTexCoord;
        }
    """.trimIndent()
    
    private val fragmentShaderCode = """
        precision mediump float;
        uniform vec4 uColor;
        varying vec2 texCoord;
        
        void main() {
            gl_FragColor = uColor;
        }
    """.trimIndent()
    
    private var shaderProgram: ShaderProgram? = null
    private var characterData: CharacterRenderData? = null
    
    init {
        try {
            shaderProgram = ShaderProgram(vertexShaderCode, fragmentShaderCode)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun updateCharacter(data: CharacterRenderData) {
        characterData = data
    }
    
    fun render(projectionMatrix: FloatArray, viewMatrix: FloatArray) {
        shaderProgram?.use()
        characterData?.let {
            // Draw character body parts in order (layering)
            // 1. Back legs
            drawCircle(it.x, it.y + 40f, 15f, 0.8f, 0.7f, 1f) // Skin tone
            
            // 2. Body
            drawRectangle(it.x - 20f, it.y - 30f, it.x + 20f, it.y + 30f, 0.9f, 0.8f, 1f)
            
            // 3. Arms
            drawCircle(it.x - 25f, it.y, 12f, 0.8f, 0.7f, 1f) // Left arm
            drawCircle(it.x + 25f, it.y, 12f, 0.8f, 0.7f, 1f) // Right arm
            
            // 4. Head
            drawCircle(it.x, it.y - 50f, 25f, 0.9f, 0.8f, 1f)
            
            // 5. Eyes based on expression
            drawEyes(it.x, it.y - 50f, it.expression)
        }
    }
    
    private fun drawCircle(x: Float, y: Float, radius: Float, r: Float, g: Float, b: Float) {
        val vertices = FloatArray(3 * 30)
        for (i in 0 until 30) {
            val angle = 2 * Math.PI * i / 30
            vertices[i * 3] = (x + radius * cos(angle)).toFloat()
            vertices[i * 3 + 1] = (y + radius * sin(angle)).toFloat()
            vertices[i * 3 + 2] = 0f
        }
        
        drawVertices(vertices, r, g, b)
    }
    
    private fun drawRectangle(x1: Float, y1: Float, x2: Float, y2: Float, r: Float, g: Float, b: Float) {
        val vertices = floatArrayOf(
            x1, y1, 0f,
            x2, y1, 0f,
            x2, y2, 0f,
            x1, y2, 0f
        )
        drawVertices(vertices, r, g, b)
    }
    
    private fun drawVertices(vertices: FloatArray, r: Float, g: Float, b: Float) {
        shaderProgram?.let { program ->
            val colorLoc = program.getUniformLocation("uColor")
            GLES20.glUniform4f(colorLoc, r, g, b, 1f)
        }
    }
    
    private fun drawEyes(x: Float, y: Float, expression: String) {
        val eyeSize = when (expression) {
            "scared" -> 8f
            "happy" -> 5f
            "sad" -> 3f
            else -> 6f
        }
        
        drawCircle(x - 8f, y - 8f, eyeSize, 0.2f, 0.2f, 0.2f) // Left eye
        drawCircle(x + 8f, y - 8f, eyeSize, 0.2f, 0.2f, 0.2f) // Right eye
    }
}

/**
 * World renderer - draws environment and objects
 */
class WorldRenderer {
    
    private var worldData: WorldRenderData? = null
    
    fun updateWorld(data: WorldRenderData) {
        worldData = data
    }
    
    fun render(projectionMatrix: FloatArray, viewMatrix: FloatArray) {
        worldData?.let {
            // Draw floor
            drawFloor(it.floorY)
            
            // Draw objects
            for (obj in it.objects) {
                drawObject(obj)
            }
        }
    }
    
    private fun drawFloor(y: Float) {
        // Brown floor
        val vertices = floatArrayOf(
            -2f, y, 0f,
            2f, y, 0f,
            2f, y + 0.2f, 0f,
            -2f, y + 0.2f, 0f
        )
        
        val vbo = IntArray(1)
        GLES20.glGenBuffers(1, vbo, 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0])
    }
    
    private fun drawObject(obj: ObjectRenderData) {
        // Draw simple colored circle for object
        val vertices = FloatArray(3 * 30)
        for (i in 0 until 30) {
            val angle = 2 * Math.PI * i / 30
            vertices[i * 3] = (obj.x + obj.radius * cos(angle)).toFloat()
            vertices[i * 3 + 1] = (obj.y + obj.radius * sin(angle)).toFloat()
            vertices[i * 3 + 2] = 0f
        }
    }
}

data class CharacterRenderData(
    val x: Float,
    val y: Float,
    val rotation: Float = 0f,
    val expression: String = "neutral",
    val animationProgress: Float = 0f
)

data class WorldRenderData(
    val floorY: Float,
    val objects: List<ObjectRenderData> = emptyList()
)

data class ObjectRenderData(
    val x: Float,
    val y: Float,
    val radius: Float,
    val r: Float = 0.8f,
    val g: Float = 0.6f,
    val b: Float = 0.4f
)