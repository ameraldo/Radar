# Data Flow

This document describes how data flows through the Radar application, from GPS signals to UI updates.

## Location Data Flow

```
GPS Satellite Signal
        │
        ▼
Google Play Services Location API (FusedLocationProviderClient)
        │
        ▼
LocationCallback.onLocationResult(LocationResult)
        │
        ├──► LocationService._locationState (MutableStateFlow)
        │           │
        │           ▼
        │    LocationService.handleLocationUpdate()
        │           │
        │           ├──► If Recording: handleRecordingLocationUpdate()
        │           │           │
        │           │           ▼
        │           │    RouteDao.insertPoints() → Room Database
        │           │
        │           └──► If Following: handleFollowingLocationUpdate()
        │                       │
        │                       ▼
        │                _followingRemainingPoints (remove reached points)
        │
        └► LocationViewModel.locationState (StateFlow)
                    │
                    ▼
            UI Screens observe StateFlow
```

## Route Recording Flow

```
User taps "Start Recording" on HomeScreen
        │
        ▼
LocationViewModel.startRecording()
        │
        ├──► Generate route name ("Route YYYY-MM-DD HH:MM")
        ├──► Create RouteEntity in Room database
        ├──► Insert first point at current location
        └──► Set _isRecording = true
                │
                ▼
MainActivity observes isRecording StateFlow
                │
                ▼
startLocationService() → startForegroundService(Intent)
                │
                ▼
LocationService.onStartCommand(ACTION_START_RECORDING)
                │
                ├──► _isRecording = true
                ├──► startLocationUpdates()
                ├──► startForeground(NOTIFICATION_ID, notification)
                └──► Notification shows "Recording route"
```

## Route Following Flow

```
User selects route on RoutesScreen
        │
        ▼
LocationViewModel.startFollowing(routeId, onLoaded)
        │
        ├──► Load route points from Room database
        ├──► Set _followingRemainingPoints = points.reversed()
        ├──► Set _isFollowing = true
        └──► Return callback to navigate to RadarScreen
                │
                ▼
RadarScreen displays route points
                │
                ▼
LocationService receives location updates
                │
        If within 5m of next point:
                │
                ▼
        Remove point from _followingRemainingPoints
                │
                ▼
        If all points reached → stopFollowing()
```

## StateFlow Architecture

The app uses Kotlin **StateFlow** for reactive state management:

| StateFlow | Type | Description |
|-----------|------|-------------|
| `locationState` | `LocationState` | Current GPS location, accuracy, satellites |
| `isRecording` | `Boolean` | Whether route is being recorded |
| `isFollowing` | `Boolean` | Whether following a saved route |
| `currentRouteId` | `Long?` | ID of current route (recording or following) |
| `currentRoutePoints` | `List<RecordedPointEntity>` | Points for current route |
| `followingRemainingPoints` | `List<RecordedPointEntity>` | Points yet to be reached |

## Satellite Data Flow

```
GNSS Status Callback (GnssStatus.Callback)
        │
        ▼
onSatelliteStatusChanged(GnssStatus)
        │
        ▼
Map satellite data to SatelliteBlip:
  - angleDeg: azimuth (0-360°)
  - radiusFraction: elevation mapped to 0-1
  - isLocked: usedInFix
  - signalStrength: Cn0DbHz
  - svid: satellite ID
  - constellation: GPS/GLONASS/Galileo/BeiDou/QZSS/SBAS
  - elevationDeg: elevation angle
  - hasAlmanac/hasEphemeris: orbital data status
  - carrierFrequencyMhz: L1/L5 frequency
        │
        ▼
_locationState.value = _locationState.value.copy(satellites = blips)
        │
        ▼
UI observes and displays on RadarView
```

## Settings Data Flow

```
User changes setting in SettingsScreen
        │
        ▼
SettingsViewModel.update*() methods
        │
        ▼
AppSettings.set*() → DataStore.edit()
        │
        ▼
DataStore persists to Preferences
        │
        ▼
SettingsViewModel exposes StateFlow
        │
        ▼
UI observes and updates immediately