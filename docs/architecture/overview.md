# Architecture Overview

Radar follows the **MVVM (Model-View-ViewModel)** architecture pattern with **Clean Architecture** principles, using **Hilt** for dependency injection.

## Overall Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         MainActivity                            │
│  - Starts/Stops LocationService based on recording/following    │
│  - Handles PiP mode                                             │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                      LocationService                            │
│  - SINGLE source of truth for location & recording              │
│  - Runs as Foreground Service (survives screen lock)            │
│  - Handles: startRecording, stopRecording, startFollowing, etc. │
│  - Updates notification with distance/points/time               │
│  - Uses WakeLock for reliable background tracking               │
└─────────────────────────────────────────────────────────────────┘
           │                    │                    │
           │ StateFlow          │ StateFlow          │ StateFlow
           ▼                    ▼                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                     LocationViewModel                           │
│  - OBSERVES service state (no duplicate logic)                  │
│  - Exposes data for UI                                          │
│  - Acts as a bridge between Service and UI                      │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                            UI                                   │
│  - Compose Screens (Home, Radar, Routes, Settings)              │
│  - Observe ViewModels via StateFlow                             │
└─────────────────────────────────────────────────────────────────┘
```

## Layer Responsibilities

### Service Layer (`service/`)
- **LocationService**: Foreground service that manages GPS location updates and route recording
  - Single source of truth for location data
  - Runs as a Foreground Service to survive screen lock
  - Handles route recording and following logic
  - Updates notification with recording status

### ViewModel Layer (`viewmodel/`)
- **LocationViewModel**: Bridges LocationService and UI, manages location state
- **RouteViewModel**: Manages saved routes list
- **SensorViewModel**: Handles compass/heading via accelerometer and magnetometer
- **SettingsViewModel**: Manages app settings preferences

### Data Layer (`data/`)
- **AppDatabase**: Room database instance
- **RouteDao**: Data access object for routes and points
- **RouteEntity/RecordedPointEntity**: Database entities
- **AppSettings**: DataStore preferences for user settings

### UI Layer (`ui/`)
- **Screens**: HomeScreen, RadarScreen, RoutesScreen, SettingsScreen
- **Components**: RadarView, CurrentLocationCard, RangeSelector, etc.
- **Theme**: Material 3 theming (colors, typography)

## Dependency Injection

The app uses **Hilt** for dependency injection. Key injected components:

- `AppDatabase` - Room database singleton
- `RouteDao` - Data access for routes/points
- `AppSettings` - DataStore preferences

## Key Design Decisions

1. **Foreground Service**: Location tracking runs as a foreground service to ensure reliability even when the screen is locked or the app is in the background.

2. **StateFlow for State Management**: ViewModels expose `StateFlow` properties that the UI observes. This provides a reactive data flow.

3. **Repository Pattern via DAO**: The RouteDao acts as a repository, providing a clean API for database operations.

4. **Single Source of Truth**: LocationService is the single source of truth for location and recording state, avoiding duplication between ViewModel and Service.

5. **Picture-in-Picture Support**: The app supports PiP mode, allowing users to keep the radar visible while using other apps.