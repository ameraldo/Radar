# Data Flow

This document describes how data flows through the Radar application, from GPS signals to UI updates.

## Architecture Diagram

```mermaid
graph TB
    subgraph UI["UI Layer (Compose)"]
        HomeScreen[HomeScreen]
        RadarScreen[RadarScreen]
        RoutesScreen[RoutesScreen]
        SettingsScreen[SettingsScreen]
    end

    subgraph ViewModel["ViewModel Layer"]
        LocationVM[LocationViewModel]
        RouteVM[RouteViewModel]
        SensorVM[SensorViewModel]
        SettingsVM[SettingsViewModel]
        UIStateVM[UIStateViewModel]
    end

    subgraph Service["Service Layer"]
        LocService[LocationService<br/>Foreground Service]
    end

    subgraph Data["Data Layer"]
        Room[Room Database<br/>AppDatabase]
        DataStore[DataStore<br/>AppSettings + ServiceState]
    end

    MainActivity[MainActivity] -->|binds to| LocService
    MainActivity -->|manages| UIStateVM
    HomeScreen --> LocationVM
    HomeScreen --> SensorVM
    RadarScreen --> LocationVM
    RadarScreen --> SensorVM
    RoutesScreen --> RouteVM
    SettingsScreen --> SettingsVM

    LocationVM -->|observes| LocService
    LocService -->|writes| Room
    RouteVM -->|reads/writes| Room
    SettingsVM -->|reads/writes| DataStore
    LocService -->|persists state| DataStore
```

## Location Data Flow

```mermaid
sequenceDiagram
    participant GPS as GPS Satellites
    participant FLP as FusedLocationProvider
    participant Service as LocationService
    participant VM as LocationViewModel
    participant UI as UI Screens

    GPS->>FLP: Satellite signals
    FLP->>Service: onLocationResult(LocationResult)
    Service->>Service: _locationState.update()
    Service->>Service: handleLocationUpdate()

    alt Recording Active
        Service->>Room: RouteDao.insertPoints()
    end

    alt Following Active
        Service->>Service: Remove reached points from _followingRemainingPoints
    end

    Service-->>VM: StateFlow emits new locationState
    VM-->>UI: StateFlow emits updated state
    UI->>UI: Recompose with new data
```

## Route Recording Flow

```mermaid
sequenceDiagram
    participant User
    participant UI as HomeScreen
    participant VM as LocationViewModel
    participant Main as MainActivity
    participant Service as LocationService
    participant Room as Room Database
    participant Notif as Notification

    User->>UI: Tap "Start Recording"
    UI->>VM: startRecording()
    VM->>Main: startLocationService(ACTION_START_RECORDING)
    Main->>Service: startForegroundService(intent)
    Service->>Service: Generate route name
    Service->>Room: Create RouteEntity
    Service->>Room: Insert first point
    Service->>Service: _isRecording = true
    Service->>Service: startLocationUpdates()
    Service->>Notif: Show "Recording route" notification
```

## Route Following Flow

```mermaid
sequenceDiagram
    participant User
    participant UI as RoutesScreen
    participant VM as LocationViewModel
    participant Main as MainActivity
    participant Service as LocationService
    participant Room as Room Database
    participant Radar as RadarScreen

    User->>UI: Select route to follow
    UI->>VM: startFollowing(routeId)
    VM->>Room: Load route points
    VM->>Main: startLocationService(ACTION_START_FOLLOWING)
    Main->>Service: startForegroundService(intent)
    Service->>Room: Load route points
    Service->>Service: _followingRemainingPoints = points
    Service->>Service: _isFollowing = true
    Service->>Service: startLocationUpdates()
    UI->>Radar: Navigate to RadarScreen
    Radar->>VM: Observe currentRoutePoints

    loop Location Updates
        Service->>Service: Check proximity to next point (5m)
        alt Point Reached
            Service->>Service: Remove from _followingRemainingPoints
        end
        alt All Points Reached
            Service->>Service: stopFollowing()
        end
    end
```

## StateFlow Architecture

The app uses Kotlin **StateFlow** for reactive state management:

| StateFlow | Type | Description |
|-----------|------|-------------|
| `locationState` | `StateFlow<LocationState>` | Current GPS location, accuracy, satellites |
| `isRecording` | `StateFlow<Boolean>` | Whether route is being recorded |
| `isFollowing` | `StateFlow<Boolean>` | Whether following a saved route |
| `currentRouteId` | `StateFlow<Long?>` | ID of current route (recording or following) |
| `currentRouteName` | `StateFlow<String?>` | Name of current route |
| `currentRoutePoints` | `StateFlow<List<RecordedPointEntity>>` | Points for current route (Room-backed) |
| `followingRemainingPoints` | `StateFlow<List<RecordedPointEntity>>` | Points yet to be reached (in-memory) |

## UI State Flow

```mermaid
sequenceDiagram
    participant User
    participant Main as MainActivity
    participant UIStateVM as UIStateViewModel
    participant Nav as NavigationSuiteScaffold

    User->>Nav: Tap navigation item
    Nav->>UIStateVM: updateDestination(destination)
    UIStateVM->>UIStateVM: _currentDestination.update()

    User->>Main: Leave app (Home button)
    Main->>Main: onUserLeaveHint()
    Main->>Main: enterPiPMode()
    Main->>UIStateVM: _isInPiPMode = true

    User->>Main: Tap notification stop action
    Main->>Main: handleNotificationIntent()
    Main->>UIStateVM: setPendingStopAction(action)
    UIStateVM->>Main: Process pending action
```

## Satellite Data Flow

```mermaid
sequenceDiagram
    participant GPS as GPS/GNSS Satellites
    participant Callback as GnssStatus.Callback
    participant Service as LocationService
    participant UI as RadarView

    GPS->>Callback: onSatelliteStatusChanged(GnssStatus)
    Callback->>Service: Map satellite data to SatelliteBlip
    Note over Service: angleDeg: azimuth (0-360°)<br/>radiusFraction: elevation → 0-1<br/>isLocked: usedInFix<br/>signalStrength: Cn0DbHz<br/>constellation: GPS/GLONASS/Galileo/etc.
    Service->>Service: _locationState.update(satellites = blips)
    Service-->>UI: StateFlow emits updated satellites
    UI->>UI: Draw satellite blips on radar
```

## Settings Data Flow

```mermaid
sequenceDiagram
    participant User
    participant UI as SettingsScreen
    participant VM as SettingsViewModel
    participant DS as AppSettings (DataStore)
    participant Prefs as Preferences Storage

    User->>UI: Change setting
    UI->>VM: updateSetting(value)
    VM->>DS: setSetting(value)
    DS->>Prefs: DataStore.edit()
    Prefs-->>DS: Persist complete
    DS-->>VM: StateFlow emits new value
    VM-->>UI: StateFlow emits new value
    UI->>UI: Recompose with new setting
```