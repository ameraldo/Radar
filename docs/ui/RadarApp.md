# RadarApp

> Root Compose entry point: permissions, PiP, navigation, and screen wiring

RadarApp is the root composable set as the content of
[`MainActivity`](../api/MainActivity.md). It connects the view models to the
screens, converts recorded points to polar coordinates, handles the runtime
permission flow, and switches between Picture-in-Picture radar rendering and the
normal navigation UI.

## Overview

**Package**: `com.ameraldo.radar.ui`

**Source**: [`RadarApp.kt`](../../app/src/main/java/com/ameraldo/radar/ui/RadarApp.kt)

**Key Responsibilities**:
- Collect location, route, sensor, settings, and UI state flows
- Create the `SettingsViewModel` from `AppSettings`
- Convert route points to `PolarPoint` lists for the radar
- Request and re-check location/notification permissions
- Render either the PiP radar or the `NavigationSuiteScaffold`
- Route tab selection and screen callbacks

## Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `locationViewModel` | `LocationViewModel` | GPS/service state and recording/following actions |
| `sensorViewModel` | `SensorViewModel` | Compass heading |
| `uiStateViewModel` | `UIStateViewModel` | Navigation destination, PiP state, pending stop actions |
| `routesViewModel` | `RouteViewModel` | Saved routes and GPX export |

All four view models are created in `MainActivity` with the `viewModels()`
delegate and passed in.

## Observed State

| State | Source | Usage |
|-------|--------|-------|
| `headingDegrees` | `SensorViewModel` | Radar rotation |
| `locationState` | `LocationViewModel` | Coordinates, accuracy, satellites |
| `isRecording` | `LocationViewModel` | PiP render mode, Home FAB state |
| `isFollowing` | `LocationViewModel` | PiP render mode, point selection |
| `currentRoutePoints` | `LocationViewModel` | Recorded points to plot |
| `followingRemainingPoints` | `LocationViewModel` | Points still to reach |
| `currentDestination` | `UIStateViewModel` | Selected tab / screen |
| `isInPiPMode` | `UIStateViewModel` | PiP vs normal UI |
| `distanceUnits` | `SettingsViewModel` | Radar scale units |
| `selectedRange` | `SettingsViewModel` | Radar outer-ring range |

## Settings ViewModel Creation

`SettingsViewModel` is not created in `MainActivity`. RadarApp creates it with
`remember`:

```kotlin
val appSettings = remember { AppSettings(context) }
val settingsViewModel = remember { SettingsViewModel(appSettings) }
```

## Polar Point Conversion

Recorded and remaining route points are converted relative to the current GPS
position before being handed to `RadarView`:

```kotlin
val recordedPolarPoints = remember(
    currentRoutePoints, locationState.latitude, locationState.longitude,
    selectedRange, radarDistanceUnits
) {
    if (lat != null && lon != null)
        toPolarPoints(lat, lon, currentRoutePoints, selectedRange, radarDistanceUnits)
    else emptyList()
}
```

The same pattern is used for `followingRemainingPoints`.

## Permission Handling

RadarApp owns the runtime permission flow; `MainActivity` handles notification
intents and PiP instead.

- **Launcher**: `RequestMultiplePermissions` for
  `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, and `POST_NOTIFICATIONS`.
- **Granted** (either fine or coarse): `locationViewModel.startLocationUpdates()`.
- **Denied**: checks `shouldShowRequestPermissionRationale` for fine location and
  calls `onPermissionPermanentlyDenied()` when it is false, otherwise
  `onPermissionDenied()`.
- **Resume check**: a `DisposableEffect` observes `Lifecycle.Event.ON_RESUME`,
  re-checks fine/coarse permission, and restarts location updates when granted
  (covers permissions enabled from system settings).
- `onRequestPermission` is also passed to `HomeScreen` / `RadarScreen` and
  `CurrentLocationCard` for retry buttons.

## Rendering

### Picture-in-Picture

When `isInPiPMode` is true, only a zoomed radar is rendered:

```kotlin
RadarView(
    modifier = Modifier.fillMaxSize().scale(1.5f),
    headingDegrees = headingDegrees,
    recordedPoints = when {
        isFollowing -> followingPolarPoints
        isRecording -> recordedPolarPoints
        else -> emptyList()
    },
    nextPointToFollow = if (isFollowing) followingPolarPoints.firstOrNull() else null,
    radarRange = selectedRange,
    radarDistanceUnits = radarDistanceUnits
)
```

### Normal UI

Otherwise RadarApp renders `NavigationSuiteScaffold` + `Scaffold` and switches on
[`AppDestinations`](../navigation/AppDestinations.md):

| Destination | Screen | Callbacks |
|-------------|--------|-----------|
| `HOME` | `HomeScreen` | `onRecord`: starts recording if idle, then switches to `RADAR`; `onRequestPermission` |
| `RADAR` | `RadarScreen` | `onFollowingComplete`: `stopFollowing()`; `onRequestPermission` |
| `ROUTES` | `RoutesScreen` | `onFollowRoute`: `startFollowing(routeId)`, then switches to `RADAR` |
| `SETTINGS` | `SettingsScreen` | none |

## Related Documentation

- [MainActivity](../api/MainActivity.md) - Hosts this composable and binds the service
- [AppDestinations](../navigation/AppDestinations.md) - Tab definitions
- [LocationViewModel](../api/LocationViewModel.md)
- [SensorViewModel](../api/SensorViewModel.md)
- [SettingsViewModel](../api/SettingsViewModel.md)
- [UIStateViewModel](../api/UIStateViewModel.md)
- [RouteViewModel](../api/RouteViewModel.md)
- [LocationUtils](../api/LocationUtils.md) - Polar conversion
- [Home Screen](../screens/home.md)
- [Radar Screen](../screens/radar.md)
- [Routes Screen](../screens/routes.md)
- [Settings Screen](../screens/settings.md)
