# AppDestinations

> Top-level navigation destinations for the app

AppDestinations defines the four tabs shown in the app's
`NavigationSuiteScaffold`: Home, Radar, Routes, and Settings.

## Overview

**Package**: `com.ameraldo.radar.navigation`

**Source**: [`AppDestinations.kt`](../../app/src/main/java/com/ameraldo/radar/navigation/AppDestinations.kt)

## Enum Values

```kotlin
enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.SatelliteAlt),
    RADAR("Radar", Icons.Default.Radar),
    ROUTES("Routes", Icons.Default.Directions),
    SETTINGS("Settings", Icons.Default.Settings)
}
```

| Value | Label | Icon | Screen |
|-------|-------|------|--------|
| `HOME` | Home | `SatelliteAlt` | [Home Screen](../screens/home.md) |
| `RADAR` | Radar | `Radar` | [Radar Screen](../screens/radar.md) |
| `ROUTES` | Routes | `Directions` | [Routes Screen](../screens/routes.md) |
| `SETTINGS` | Settings | `Settings` | [Settings Screen](../screens/settings.md) |

## Usage

[`RadarApp`](../ui/RadarApp.md) iterates over `AppDestinations.entries` to build
the navigation items:

```kotlin
AppDestinations.entries.forEach { destination ->
    item(
        icon = { Icon(destination.icon, contentDescription = destination.label) },
        label = { Text(destination.label) },
        selected = destination == currentDestination,
        onClick = { uiStateViewModel.updateDestination(destination) }
    )
}
```

The selected tab is held by
[`UIStateViewModel.currentDestination`](../api/UIStateViewModel.md), which starts
at `AppDestinations.HOME`.

Navigation is programmatic in two flows:
- Starting a recording from the Home FAB switches to `RADAR`.
- Selecting a route on the Routes screen switches to `RADAR` after calling
  `LocationViewModel.startFollowing(routeId)`.

## Related Documentation

- [RadarApp](../ui/RadarApp.md) - Renders the navigation suite and screens
- [UIStateViewModel](../api/UIStateViewModel.md) - Holds the selected destination
- [Home Screen](../screens/home.md)
- [Radar Screen](../screens/radar.md)
- [Routes Screen](../screens/routes.md)
- [Settings Screen](../screens/settings.md)
