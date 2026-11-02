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
    LocationVM -->|starts via intents| LocService
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
    participant UI as HomeScreen / RouteCard
    participant App as RadarApp
    participant VM as LocationViewModel
    participant Service as LocationService
    participant Room as Room Database
    participant State as ServiceState (DataStore)
    participant Notif as Notification

    User->>UI: Tap "Start Recording"
    alt From HomeScreen
        UI->>App: onRecord()
        App->>VM: startRecording()
        App->>App: updateDestination(RADAR)
    else From RouteCard
        UI->>VM: startRecording()
    end
    VM->>Service: context.startForegroundService(ACTION_START_RECORDING)
    Note over Service: onStartCommand dispatches to startRecording()
    Service->>Service: Stop following if active
    Service->>Service: _isRecording = true
    Service->>Notif: startForeground("Recording route")
    Service->>Service: generateRouteNameAndSave() (async)
    Service->>Room: insertRoute(RouteEntity)
    Service->>State: setRecordingState(true, routeId, routeName)
    Service->>Service: Insert first point if a fix is already available
    Service->>Service: startLocationUpdates()
```

## Route Following Flow

```mermaid
sequenceDiagram
    participant User
    participant UI as RoutesScreen
    participant App as RadarApp
    participant VM as LocationViewModel
    participant Service as LocationService
    participant Room as Room Database
    participant State as ServiceState (DataStore)
    participant Radar as RadarScreen

    User->>UI: Tap route to follow
    UI->>App: onFollowRoute(routeId)
    App->>VM: startFollowing(routeId)
    VM->>Service: context.startForegroundService(ACTION_START_FOLLOWING)
    App->>App: updateDestination(RADAR)
    Note over Service: onStartCommand dispatches to startFollowing()
    Service->>Service: Stop recording if active
    Service->>Service: _isFollowing = true, _currentRouteId = routeId
    Service->>Service: startForeground("Following route")
    Service->>Room: getPointsForRoute(routeId)
    Service->>Service: _followingRemainingPoints = points.reversed()
    Service->>Service: startLocationUpdates()
    Service->>State: setFollowingState(true, routeId)
    Radar->>VM: Observe currentRoutePoints / followingRemainingPoints

    loop Each location update
        alt Adaptive following enabled
            Service->>Service: Find furthest remaining point within following distance
        else Strict following
            Service->>Service: Check only the next point within following distance
        end
        alt Point(s) reached
            Service->>Service: Drop reached point(s) from _followingRemainingPoints
        end
        alt Remaining points now empty
            Note over Service: On the next location update
            Service->>Service: stopFollowing(true), clear state and stop service
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
    participant Radar as RadarScreen / RouteCard
    participant VM as LocationViewModel

    User->>Nav: Tap navigation item
    Nav->>UIStateVM: updateDestination(destination)
    UIStateVM->>UIStateVM: _currentDestination.update()

    User->>Main: Leave app (Home button)
    Main->>Main: onUserLeaveHint()
    Main->>Main: enterPiPMode()
    Main->>UIStateVM: _isInPiPMode = true

    User->>Main: Tap notification stop action
    Main->>Main: handleNotificationIntent()
    Main->>UIStateVM: setPendingStopAction(RECORDING or FOLLOWING)
    Main->>UIStateVM: updateDestination(RADAR)
    UIStateVM-->>Radar: pendingStopAction emitted
    Radar->>User: Show stop confirmation dialog

    User->>Radar: Confirm stop
    alt Recording
        Radar->>VM: stopRecording()
    else Following
        Radar->>VM: stopFollowing()
    end
    Radar->>UIStateVM: clearPendingStopAction()
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