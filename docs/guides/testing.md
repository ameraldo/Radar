# Testing Guide

Guide for testing the Radar Android application.

## Test Types

| Type | Location | Framework |
|------|----------|-----------|
| Unit Tests | `app/src/test/` | JUnit 4 |
| Instrumented Tests | `app/src/androidTest/` | Espresso + Compose |

---

## Running Tests

### Unit Tests

```bash
./gradlew test
```

Output: `app/build/reports/tests/testDebugUnitTest/index.html`

### Instrumented Tests

Requires a device or emulator:

```bash
./gradlew connectedAndroidTest
```

Output: `app/build/reports/androidTests/connected/index.html`

### Single Test Class

```bash
./gradlew test --tests "com.ameraldo.radar.ExampleUnitTest"
```

### Single Test Method

```bash
./gradlew test --tests "com.ameraldo.radar.ExampleUnitTest.testMethod"
```

---

## Test Structure

### Unit Tests

```
app/src/test/java/com/ameraldo/radar/
├── ExampleUnitTest.kt      # Sample unit test
```

Example:

```kotlin
package com.ameraldo.radar

import org.junit.Test
import org.junit.Assert.*

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}
```

### Instrumented Tests

```
app/src/androidTest/java/com/ameraldo/radar/
├── ExampleInstrumentedTest.kt  # Sample instrumented test
```

Example:

```kotlin
package com.ameraldo.radar

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.ameraldo.radar", appContext.packageName)
    }
}
```

---

## Compose Testing

### Basic Compose Test

```kotlin
@Composable
fun MyScreen() {
    Text("Hello Radar")
}

@Test
fun MyScreenTest() {
    composeTestRule.setContent {
        MyScreen()
    }

    composeTestRule.onNodeWithText("Hello Radar").assertExists()
}
```

### Testing ViewModel

```kotlin
@Test
fun testViewModel() {
    val viewModel = LocationViewModel(ApplicationProvider.getApplicationContext())
    // Test state changes
}
```

---

## Code Coverage

### Generate Coverage Report

```bash
./gradlew testDebugUnitTest jacocoTestReport
```

Output: `app/build/reports/jacoco/jacocoTestReport/html/index.html`

### Coverage Configuration

In `app/build.gradle.kts`:

```kotlin
tasks.withType<Test> {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}
```

---

## Linting

### Run Lint

```bash
./gradlew lint
```

Output: `app/build/reports/lint-results.html`

### Lint Options

Suppress warnings:

```kotlin
// Suppress for class
@Suppress("UnstableApiUsage")
class MyClass { }

// Suppress for file
//noinspection UnstableApiUsage
```

---

## Best Practices

### Unit Test Guidelines

1. **Test one thing per test** - Each test should verify a single behavior
2. **Use descriptive names** - `testRecordingSavesPoints()` not `test1()`
3. **Arrange-Act-Assert** - Clear test structure
4. **Mock dependencies** - Use Fake/Mock for external dependencies

### Instrumented Test Guidelines

1. **Use idling resources** - Wait for async operations
2. **Avoid sleep()** - Use `waitFor` instead
3. **Test user flows** - Focus on user interactions
4. **Clean up state** - Reset between tests

### CI/CD Testing

```yaml
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '17'
      - name: Unit Tests
        run: ./gradlew test
      - name: Lint
        run: ./gradlew lint
```

---

## Troubleshooting

### Tests Not Found

Ensure test source sets are configured:

```kotlin
android {
    sourceSets {
        getByName("test") {
            java.srcDirs("src/test/java")
        }
        getByName("androidTest") {
            java.srcDirs("src/androidTest/java")
        }
    }
}
```

### Emulator Not Found (Instrumented Tests)

```bash
# List emulators
emulator -list-avds

# Start emulator
emulator -avd <avd-name> &
```

### Out of Memory (Tests)

```properties
# gradle.properties
org.gradle.jvmargs=-Xmx4g -XX:+HeapDumpOnOutOfMemoryError
```

---

## Related

- [Setup Guide](setup.md) - Development environment
- [Build Instructions](build.md) - Building the app