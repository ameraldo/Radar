# Build Instructions

Guide for building the Radar Android application.

## Build Variants

| Variant | Description |
|---------|-------------|
| **debug** | Development build, debuggable |
| **release** | Production build, optimized, signed |

---

## Build Commands

### Debug Build

```bash
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

### Release Build

```bash
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

### Clean Build

```bash
./gradlew clean
```

---

## Build Tasks

### Available Tasks

```bash
./gradlew tasks
```

Common tasks:

| Task | Description |
|------|-------------|
| `assemble` | Build all variants |
| `assembleDebug` | Build debug variant |
| `assembleRelease` | Build release variant |
| `lint` | Run static analysis |
| `test` | Run unit tests |
| `connectedAndroidTest` | Run instrumented tests |

---

## Build Configuration

### Gradle Properties

Edit `gradle.properties`:

```properties
# Enable parallel builds
org.gradle.parallel=true

# Enable caching
org.gradle.caching=true

# Enable configuration cache
org.gradle.configuration-cache=true

# JVM args
org.gradle.jvmargs=-Xmx4g -XX:+HeapDumpOnOutOfMemoryError
```

### Local SDK Path

Edit `local.properties`:

```properties
sdk.dir=/path/to/android/sdk
```

---

## Troubleshooting

### Out of Memory

Increase JVM heap in `gradle.properties`:

```properties
org.gradle.jvmargs=-Xmx8g -XX:+HeapDumpOnOutOfMemoryError
```

### Slow Builds

1. Enable parallel builds: `org.gradle.parallel=true`
2. Enable caching: `org.gradle.caching=true`
3. Use configuration cache: `org.gradle.configuration-cache=true`

### Lint Errors

Fix issues or suppress in `app/build.gradle.kts`:

```kotlin
android {
    lint {
        abortOnError = false
        // or specific checks
        ignoreWarnings = true
    }
}
```

---

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Build
on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '17'
      - name: Build debug
        run: ./gradlew assembleDebug
      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: app-debug
          path: app/build/outputs/apk/debug/app-debug.apk
```

---

## Release Build

### Signing

Release builds require a signing config. For development, use the debug keystore:

```kotlin
// app/build.gradle.kts
buildTypes {
    release {
        isMinifyEnabled = false
        signingConfig = signingConfigs.getByName("debug")
    }
}
```

### ProGuard

ProGuard rules are in `app/proguard-rules.pro`. Enable minification:

```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

---

## Output Files

| Build Type | Location |
|------------|----------|
| Debug | `app/build/outputs/apk/debug/app-debug.apk` |
| Release | `app/build/outputs/apk/release/app-release.apk` |

---

## Related

- [Setup Guide](setup.md) - Development environment
- [Testing Guide](testing.md) - Running tests