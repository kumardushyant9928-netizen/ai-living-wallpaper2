# AI Living World - Installation & Setup Guide

## Prerequisites

### System Requirements
- **Android**: 12.0 (API 31) or higher
- **RAM**: 2GB minimum (4GB recommended)
- **Storage**: 200MB free space
- **Java**: JDK 11 or higher
- **Gradle**: 7.0 or higher

### Required Accounts
1. **GitHub Account** (for cloning repo)
2. **AI Provider Account** (at least one):
   - [OpenAI](https://platform.openai.com/signup) - GPT-3.5/4 API
   - [Hugging Face](https://huggingface.co/join) - Open-source models
   - Custom API endpoint (optional)

## Step-by-Step Installation

### 1. Clone Repository

```bash
git clone https://github.com/kumardushyant9928-netizen/ai-living-wallpaper2.git
cd ai-living-wallpaper2
```

### 2. Install Dependencies

**Windows**:
```bash
# Install Android SDK if not present
choco install android-sdk

# Set up environment
set ANDROID_SDK_ROOT=%LOCALAPPDATA%\Android\Sdk
set PATH=%PATH%;%ANDROID_SDK_ROOT%\tools;%ANDROID_SDK_ROOT%\platform-tools
```

**macOS**:
```bash
# Install Android SDK via Homebrew
brew install android-sdk

# Set environment
export ANDROID_SDK_ROOT=~/Library/Android/sdk
export PATH=$PATH:$ANDROID_SDK_ROOT/tools:$ANDROID_SDK_ROOT/platform-tools
```

**Linux**:
```bash
# Install Android SDK
sudo apt-get install android-sdk

# Set environment
export ANDROID_SDK_ROOT=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_SDK_ROOT/tools:$ANDROID_SDK_ROOT/platform-tools
```

### 3. Configure Gradle

```bash
# Verify gradle wrapper
./gradlew --version

# Build the project
./gradlew clean build
```

### 4. Get Your AI API Key

#### Option A: OpenAI API
1. Go to https://platform.openai.com/api-keys
2. Log in or create account
3. Click "Create new secret key"
4. Copy the key (starts with `sk-`)
5. **⚠️ Never share this key!**

#### Option B: Hugging Face API
1. Go to https://huggingface.co/settings/tokens
2. Log in or create account
3. Create "New token"
4. Select "Read" access
5. Copy the token (starts with `hf_`)

#### Option C: Custom API
- Prepare your API endpoint URL
- Get your API key/authentication token
- Know the API format (OpenAI-compatible or custom)

### 5. Install on Device

**Via Android Studio**:
1. Open project in Android Studio
2. Connect device via USB
3. Enable Developer Mode on device:
   - Settings → About Phone → Tap "Build Number" 7 times
   - Back to Settings → Developer Options → Enable USB Debugging
4. Click "Run" button in Android Studio
5. Select your device

**Via Command Line**:
```bash
# Build APK
./gradlew assembleDebug

# Install to device
adb install app/build/outputs/apk/debug/app-debug.apk

# Or use gradle task
./gradlew installDebug
```

### 6. Configure AI Provider

1. **Launch the app** on your device
2. **Open Settings** (should open automatically)
3. **Select AI Provider**:
   - OpenAI: Easy, best quality responses
   - Hugging Face: Free models, more privacy
   - Custom: Enterprise solutions
4. **Enter API Key** in the text field
5. **Optional - For custom providers**:
   - Enter endpoint URL
   - Enter model ID
6. **Click "Save Configuration"**
7. **Click "Test Connection"** to verify
   - ✅ Green = Connected
   - ❌ Red = Check your key

### 7. Set as Live Wallpaper

1. **Go to home screen**
2. **Long-press on wallpaper** → "Wallpapers"
3. **Find "AI Living World"**
4. **Tap "Set Wallpaper"**
5. **Choose location** (Lock screen or Home screen)
6. **Tap "Apply"**

## Troubleshooting

### Build Issues

**"SDK not found"**
```bash
# Update Android SDK
./gradlew --update-gradle

# Or manually set
export ANDROID_SDK_ROOT=/path/to/sdk
```

**"Gradle sync failed"**
```bash
# Clean build
./gradlew clean

# Rebuild
./gradlew build
```

### Runtime Issues

**"Wallpaper not applying"**
- Restart device
- Clear app cache: Settings → Apps → AI Living World → Storage → Clear Cache
- Reinstall: `./gradlew uninstallDebug && ./gradlew installDebug`

**"AI not responding"**
- Check internet connection
- Verify API key in Settings
- Test connection button should show status
- Check Logcat: `adb logcat | grep AILiving`

**"Low FPS/Performance"**
- Close background apps
- Disable other wallpapers
- Check device temperature
- Try on different device if available

**"Sensors not working"**
- Enable sensor access in permissions
- Check device has accelerometer: Settings → About → Sensors
- Calibrate device: Tilt left/right slowly

## Development

### Enable Debug Logging

Edit `LiveWallpaperService.kt`:
```kotlin
override fun onCreate(surfaceHolder: SurfaceHolder) {
    super.onCreate(surfaceHolder)
    Timber.plant(Timber.DebugTree())  // Enable debug logs
    ...
}
```

### View Logs
```bash
adb logcat | grep "AILiving\|com.ailivingworld"
```

### Run Tests
```bash
# Unit tests
./gradlew test

# Instrumented tests (on device)
./gradlew connectedAndroidTest
```

## Performance Optimization

### For Slow Devices

1. **Reduce physics iterations** in `PhysicsEngine.kt`:
```kotlin
val substeps = 1  // Default: 4
```

2. **Lower render resolution** in `GLRenderer.kt`:
```kotlin
// Render at half resolution
GLES20.glViewport(0, 0, width / 2, height / 2)
```

3. **Disable animations** in `CharacterController.kt`:
```kotlin
character.animationSpeed = 0f
```

## Next Steps

1. ✅ App installed and running
2. ✅ AI configured and responding
3. ✅ Set as live wallpaper
4. 🎮 **Start interacting!**
   - Tap the character
   - Shake your phone
   - Tilt and move
   - Ignore her (see what happens 👀)

## Getting Help

- **GitHub Issues**: Report bugs
- **GitHub Discussions**: Ask questions
- **Email**: kumardushyant9928@gmail.com
- **Logs**: Share output of `adb logcat`

---

**Happy wallpaper-ing!** ✨