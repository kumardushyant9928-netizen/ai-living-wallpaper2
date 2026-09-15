# 🌸 AI Living World - Interactive Anime Live Wallpaper

> An advanced Android live wallpaper featuring an AI-powered anime character companion with physics simulation, emotional intelligence, and flexible API integration.

## ✨ Features

### 🎨 Character System
- **Anime Character Decomposition**: Modular character parts (head, body, limbs, clothing)
- **Emotional Intelligence**: Dynamic emotional states (happiness, fear, anger, sadness, etc.)
- **Personality Traits**: Customizable personality profiles affecting behavior
- **Pet Companion**: Interactive pet animal that responds to world events
- **Expression System**: Real-time facial expressions reflecting emotions

### 🎮 Physics Engine
- **2D Lightweight Physics**: Gravity, collision detection, friction, and bouncing
- **Sensor Fusion**: Accelerometer + Gyroscope integration for motion awareness
- **Device Orientation**: Character responds to phone tilt and movement
- **Realistic Animation**: Physics-based character movement and interaction

### 🤖 AI Integration
- **Flexible AI Provider Support**:
  - OpenAI GPT-3.5/GPT-4
  - Hugging Face (any open-source model)
  - Custom APIs (enterprise solutions)
- **User Configurable API Keys**: No hardcoded keys - users bring their own
- **Emotion-Driven Responses**: AI generates contextual responses based on character mood
- **Memory System**: Short-term, long-term, and experience memory for coherent interactions

### 🎯 Interaction System
- **Touch Detection**: Character responds to taps and holds
- **Gesture Recognition**: Shake, fling, and tilt gestures trigger events
- **Long-press Actions**: Character jumps and reacts to extended interaction
- **Neglect Detection**: Character becomes sad if ignored for too long
- **Water Splash Effects**: Physics-based water interactions

### 🎨 Rendering
- **OpenGL ES 2.0**: Hardware-accelerated 2D rendering
- **Layered Drawing**: Proper depth ordering of character parts
- **Smooth Animations**: 60 FPS target framerate
- **Expression Animation**: Dynamic eye and mouth animations

## 🚀 Quick Start

### Prerequisites
- Android SDK 31 or higher
- Kotlin 1.8+
- Gradle 7.0+
- An API key from one of these providers:
  - [OpenAI API](https://platform.openai.com/api-keys)
  - [Hugging Face API](https://huggingface.co/settings/tokens)
  - Your custom API endpoint

### Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/kumardushyant9928-netizen/ai-living-wallpaper2.git
   cd ai-living-wallpaper2
   ```

2. **Build the project**:
   ```bash
   ./gradlew build
   ```

3. **Run on device/emulator**:
   ```bash
   ./gradlew installDebug
   ```

4. **Configure AI Provider**:
   - Open the app
   - Go to Settings
   - Select your preferred AI provider (OpenAI, Hugging Face, or Custom)
   - Enter your API key
   - Click "Save Configuration"
   - Optionally test the connection

5. **Set as Live Wallpaper**:
   - Long-press on your home screen
   - Select "Wallpapers"
   - Choose "AI Living World"
   - Tap "Set Wallpaper"

## 📁 Project Structure

```
app/src/main/
├── kotlin/com/ailivingworld/wallpaper/
│   ├── physics/
│   │   ├── PhysicsEngine.kt          # 2D physics simulation
│   │   └── SensorFusion.kt           # Sensor integration
│   ├── character/
│   │   ├── AnimeCharacter.kt         # Character data structure
│   │   └── CharacterController.kt    # Character behavior
│   ├── ai/
│   │   ├── AIProvider.kt             # Flexible AI interface
│   │   ├── CharacterBrain.kt         # Emotion & decision logic
│   │   └── Memory.kt                 # Memory systems
│   ├── renderer/
│   │   └── GLRenderer.kt             # OpenGL ES 2.0 rendering
│   ├── wallpaper/
│   │   └── LiveWallpaperService.kt   # Main service & orchestration
│   └── ui/
│       └── SettingsActivity.kt       # Configuration UI
├── res/
│   ├── values/
│   │   ├── strings.xml
│   │   ├── themes.xml
│   │   └── colors.xml
│   ├── layout/
│   │   └── activity_settings.xml
│   └── xml/
│       ├── wallpaper_metadata.xml
│       ├── data_extraction_rules.xml
│       └── backup_schemes.xml
└── AndroidManifest.xml
```

## 🤖 AI Provider Configuration

### OpenAI
```
Provider: OpenAI
API Key: sk-...
Model: gpt-3.5-turbo (default)
Endpoint: https://api.openai.com/v1/chat/completions
```

### Hugging Face
```
Provider: Hugging Face
API Key: hf_...
Model ID: gpt2, distilgpt2, or any HF model
Endpoint: https://api-inference.huggingface.co/models/{model_id}
```

### Custom API
```
Provider: Custom
API Key: your-api-key
Endpoint: https://your-api.com/v1/chat/completions
Model ID: (optional)
```

## 🎮 Interaction Guide

| Interaction | Effect |
|-------------|--------|
| **Tap Character** | She acknowledges you, happiness increases |
| **Long Press** | Character jumps up |
| **Shake Device** | Character startles, fear increases |
| **Tilt Phone** | Character tries to balance, reacts to movement |
| **No Interaction (30s+)** | Character becomes sad, sadness increases |
| **Water Splash** | If playful: laughs; If shy: embarrassed |

## 💭 Emotional States

```kotlin
data class CharacterEmotions(
    val happiness: Float,     // Joy and positive feelings
    val sadness: Float,       // Loneliness and disappointment
    val anger: Float,         // Frustration and annoyance
    val fear: Float,          // Worry and startlement
    val curiosity: Float,     // Interest and engagement
    val affection: Float,     // Attachment to user
    val trust: Float,         // Confidence in user
    val energy: Float         // Vitality and tiredness
)
```

## 🔧 Technical Details

### Physics Engine
- Custom 2D rigid body dynamics
- Circle collision detection with impulse resolution
- Configurable gravity and friction
- Sensor-based world orientation

### AI System
- **Emotion Processing**: Real-time emotional response generation
- **Context Awareness**: Considers character mood, personality, and interaction history
- **Memory Integration**: References past interactions for coherent conversations
- **Voice Generation**: Text-based responses that match emotional state

### Rendering Pipeline
1. Calculate physics (gravity, collisions, constraints)
2. Update character position and animation state
3. Render world background
4. Render character parts in layered order
5. Apply expressions and animations
6. Swap buffers (60 FPS target)

## 📊 Performance

- **Target FPS**: 60 (16ms per frame)
- **Physics Updates**: 60 Hz
- **Sensor Polling**: ~100 Hz (game speed)
- **Memory**: ~50-80 MB runtime
- **Battery**: Optimized for extended use

## 🔐 Privacy & Security

- API keys are stored locally in SharedPreferences (encrypted on Android 6.0+)
- No keys are sent to third parties except the configured AI provider
- Character data is stored locally on device
- No analytics or telemetry
- Open source - audit the code!

## 📦 Dependencies

```gradle
dependencies {
    implementation 'org.jetbrains.kotlin:kotlin-stdlib:1.8.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4'
    implementation 'com.squareup.okhttp3:okhttp:4.10.0'
    implementation 'com.google.code.gson:gson:2.10.1'
    implementation 'com.jakewharton.timber:timber:5.0.1'
}
```

## 🐛 Troubleshooting

### Character not moving
- Ensure sensors are enabled in AndroidManifest.xml
- Check that device has accelerometer
- Verify physics engine is initialized

### AI not responding
- Check API key is correct in Settings
- Verify internet connection
- Test connection from Settings Activity
- Check Logcat for error messages

### Low FPS
- Disable other live wallpapers
- Check device temperature
- Reduce animation complexity
- Close background apps

### Wallpaper not applying
- Ensure permissions are granted
- Try clearing cache: `./gradlew clean`
- Restart device
- Check Android version compatibility (API 31+)

## 📚 Architecture

### Character Brain Flow
```
World Event (touch, shake, etc.)
    ↓
Character Brain analyzes event
    ↓
Emotional Response generated
    ↓
Update character emotions
    ↓
Generate action (animation, voice)
    ↓
Execute action & update memory
    ↓
AI generates context-aware response
```

### Physics Simulation
```
Apply gravity and forces
    ↓
Update velocity & position
    ↓
Detect collisions
    ↓
Resolve collisions with impulses
    ↓
Constrain to world bounds
    ↓
Render updated positions
```

## 🤝 Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## 📄 License

MIT License - see LICENSE file for details

## 🙏 Credits

- Anime character design concepts inspired by visual novels
- Physics engine based on classic rigid body dynamics
- AI integration powered by community APIs
- OpenGL ES 2.0 rendering best practices

## 📞 Support

- **Issues**: Report bugs on GitHub Issues
- **Discussions**: Share ideas in GitHub Discussions
- **Email**: kumardushyant9928@gmail.com

## 🚦 Roadmap

- [ ] Touch-drag interactions
- [ ] Multiple character skins
- [ ] Voice synthesis (TTS)
- [ ] Persistent character personality learning
- [ ] Multi-pet system
- [ ] Weather effects (rain, snow)
- [ ] Time-based behaviors (sleepy at night)
- [ ] Screenshot sharing
- [ ] Custom background scenes
- [ ] Achievement system

---

**Made with ❤️ by Dushyant Kumar**

*Your personal AI companion, always watching over your home screen* 🌸✨