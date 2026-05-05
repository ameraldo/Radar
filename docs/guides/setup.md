# Development Setup Guide

This guide covers setting up the development environment for Radar.

## Prerequisites

### Required Tools

| Tool | Version | Purpose |
|------|---------|---------|
| Android Studio | Chipmunk (2022.3) or later | IDE |
| JDK | 17+ | Java Development Kit |
| Android SDK | API 35+ | Android SDK |
| Gradle | 9.x (included) | Build system |

### System Requirements

- **Operating System**: Windows 10+, macOS 12+, or Linux
- **RAM**: 8GB minimum (16GB recommended)
- **Disk Space**: 2GB for Android Studio + SDKs

---

## Installation Steps

### 1. Install Android Studio

1. Download Android Studio from [developer.android.com](https://developer.android.com/studio)
2. Run the installer
3. Follow the setup wizard

### 2. Install SDK Components

1. Open Android Studio
2. Go to **Tools → SDK Manager**
3. Install:
   - **Android SDK Platform** (API 35, API 36)
   - **Android SDK Build-Tools**
   - **Android SDK Command-line Tools**

### 3. Clone the Repository

```bash
git clone <repository-url>
cd Radar
```

### 4. Open in Android Studio

1. File → Open
2. Select `settings.gradle.kts` or the project root
3. Wait for Gradle sync to complete

### 5. Gradle Wrapper

The project includes Gradle Wrapper. To verify:

```bash
./gradlew -v
```

---

## Project Structure

```
Radar/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com.ameraldo.radar/
│   │   │   │   ├── service/      # LocationService
│   │   │   │   ├── viewmodel/    # ViewModels
│   │   │   │   ├── data/         # Database, DAOs
│   │   │   │   ├── ui/           # Compose screens
│   │   │   │   └── utils/        # Utilities
│   │   │   ├── res/              # Resources
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                 # Unit tests
│   │   └── androidTest/          # Instrumented tests
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml        # Version catalog
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## Running the App

### Physical Device (Recommended)

1. Enable Developer Options on device:
   - Settings → About Phone → Tap "Build Number" 7 times
2. Enable USB Debugging:
   - Settings → Developer Options → USB Debugging
3. Connect device via USB
4. Run from Android Studio (Shift + F10)

### Emulator

1. Create virtual device:
   - Tools → AVD Manager → Create Virtual Device
2. Select device (Pixel recommended)
3. Select system image (API 35+)
4. Run emulator

---

## Common Issues

### Gradle Sync Fails

**Solution:**
1. File → Invalidate Caches → Invalidate and Restart
2. Check `local.properties` has correct SDK path

### SDK Not Found

**Check `local.properties`:**
```properties
sdk.dir=/path/to/android/sdk
```

### JDK Version Issues

**Solution:**
1. File → Project Structure → JDK Location
2. Use embedded JDK or set JAVA_HOME

---

## Next Steps

- [Build Instructions](build.md) - Building the app
- [Testing Guide](testing.md) - Running tests