# Architecture Overview

Radar follows the **MVVM (Model-View-ViewModel)** architecture pattern with **Clean Architecture** principles, using **manual dependency injection** via ViewModel factories.

## Overall Architecture

```mermaid
graph TB
    subgraph MainActivity
        Main[MainActivity<br/>- Binds LocationService<br/>- Manages PiP mode<br/>- Handles notifications]
    end

    subgraph Service["Service Layer"]
        LocService[LocationService<br/>Foreground Service<br/>- Single source of truth<br/>- GPS tracking<br/>- Recording/Following<br/>- Notification updates]
    end

    subgraph ViewModels["ViewModel Layer"]
        LocVM[LocationViewModel<br/>Bridges Service ↔ UI]
        RouteVM[RouteViewModel<br/>Manages saved routes]
        SensorVM[SensorViewModel<br/>Compass/heading]
        SettingsVM[SettingsViewModel<br/>App preferences]
        UIStateVM[UIStateViewModel<br/>UI state management]
    end

    subgraph UI["UI Layer (Jetpack Compose)"]
        Home[HomeScreen]
        Radar[RadarScreen]
        Routes[RoutesScreen]
        Settings[SettingsScreen]
    end

    subgraph Data["Data Layer"]
        Room[Room Database<br/>Routes + Points]
        DS1[AppSettings<br/>DataStore]
        DS2[ServiceState<br/>DataStore]
    end

    Main -->|binds to| LocService
    LocService -->|persists state| DS2
    LocService -->|writes| Room

    LocService -->|StateFlow| LocVM
    LocVM -->|StateFlow| Home
    LocVM -->|StateFlow| Radar
    SensorVM --> Home
    SensorVM --> Radar

    RouteVM -->|StateFlow| Routes
    RouteVM -->|reads/writes| Room

    SettingsVM -->|StateFlow| Settings
    SettingsVM -->|reads/writes| DS1

    Main --> UIStateVM
```

## Layer Responsibilities

### Service Layer (`service/`)
- **LocationService**: Foreground service that manages GPS location updates and route recording
  - Single source of truth for location data
  - Runs as a Foreground Service to survive screen lock
  - Handles route recording and following logic
  - Updates notification with recording/following status
  - Persists state via ServiceState (DataStore) for restarts

### ViewModel Layer (`viewmodel/`)
- **LocationViewModel**: Bridges LocationService and UI, manages location state
- **RouteViewModel**: Manages saved routes list
- **SensorViewModel**: Handles compass/heading via accelerometer and magnetometer
- **SettingsViewModel**: Manages app settings preferences
- **UIStateViewModel**: Manages UI state (navigation destination, PiP mode, pending stop actions)

### Data Layer (`data/`)
- **AppDatabase**: Room database instance (singleton via `getInstance()`)
- **RouteDao**: Data access object for routes and points
- **RouteEntity/RecordedPointEntity**: Database entities
- **AppSettings**: DataStore preferences for user settings
- **ServiceState**: DataStore for persisting service state across restarts

### UI Layer (`ui/`)
- **Screens**: HomeScreen, RadarScreen, RoutesScreen, SettingsScreen
- **Components**: RadarView, CurrentLocationCard, RangeSelector, etc.
- **Theme**: Material 3 theming (colors, typography)

## Dependency Injection

The app uses **manual dependency injection** via ViewModel factories (`viewModels()`). Key components:

- `AppDatabase.getInstance(context)` - Room database singleton
- `RouteDao` - Injected via database instance
- `AppSettings(context)` - DataStore preferences
- `LocationService` - Bound via ServiceConnection in MainActivity

## Key Design Decisions

1. **Foreground Service**: Location tracking runs as a foreground service to ensure reliability even when the screen is locked or the app is in the background.

2. **StateFlow for State Management**: ViewModels expose `StateFlow` properties that the UI observes. This provides a reactive data flow.

3. **Repository Pattern via DAO**: The RouteDao acts as a repository, providing a clean API for database operations.

4. **Single Source of Truth**: LocationService is the single source of truth for location and recording state, avoiding duplication between ViewModel and Service.

5. **Picture-in-Picture Support**: The app supports PiP mode, allowing users to keep the radar visible while using other apps.

6. **State Persistence**: ServiceState (DataStore) persists recording/following state across service restarts (e.g., system killing the service).