# Radar - GPS Route Tracking App

Radar is an Android application for recording GPS points to create routes, saving them locally, and navigating back along those routes. Designed for outdoor use where traditional navigation (Google/Apple Maps) is unavailable due to poor cellular reception.

## Features

- **Route Recording** - Capture GPS points as you move
- **Route Saving** - Store routes locally for later use
- **Route Following** - Navigate back along saved routes
- **Compass Heading** - Uses device accelerometer + magnetometer
- **GPS Satellite Tracking** - See satellite visibility
- **Picture-in-Picture Mode** - Keeps radar visible while multitasking
- **Works Fully Offline** - GPS only, no internet required

## Screenshots

<div align="center">
  <img src="assets/screenshots/home.jpeg" width="200" alt="Home Screen"/>
  <img src="assets/screenshots/radar_record.jpeg" width="200" alt="Radar Recording"/>
  <img src="assets/screenshots/radar_follow.jpeg" width="200" alt="Radar Following"/>
  <img src="assets/screenshots/routes.jpeg" width="200" alt="Routes"/>
</div>

## Technical Details

| Aspect | Value |
|--------|-------|
| **Minimum SDK** | 35 (Android 15) |
| **Target SDK** | 36 (Android 16) |
| **Language** | Kotlin |
| **Build System** | Gradle with Kotlin DSL |
| **Architecture** | MVVM with Manual Dependency Injection |
| **UI Framework** | Jetpack Compose |
| **Database** | Room |
| **Location Services** | Google Play Services Location |

## Project Structure

```
Radar/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com.ameraldo.radar/
│   │   │   │   ├── MainActivity     # Main activity, service binding, PiP mode
│   │   │   │   ├── service/         # LocationService (foreground service)
│   │   │   │   ├── viewmodel/       # ViewModels (Location, Route, Sensor, Settings, UIState)
│   │   │   │   ├── data/            # Room database, DAOs, entities, DataStore
│   │   │   │   ├── ui/              # Compose UI (screens, components, theme)
│   │   │   │   └── utils/           # Utility functions (LocationUtils)
│   │   │   ├── res/                 # Android resources
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                    # Unit tests
│   │   └── androidTest/             # Instrumented tests
│   └── build.gradle.kts
├── docs/                            # This documentation
├── build.gradle.kts                 # Project-level build config
├── settings.gradle.kts
└── README.md
```

## Documentation

### Architecture

- [Architecture Overview](architecture/overview.md) - System architecture and component design
- [Data Flow](architecture/data-flow.md) - Data flow between layers

### API Reference

**Services**
- [LocationService](api/LocationService.md) - Foreground service for GPS tracking
- [MainActivity](api/MainActivity.md) - Main activity, service binding, PiP mode

**ViewModels**
- [LocationViewModel](api/LocationViewModel.md) - Location state management
- [RouteViewModel](api/RouteViewModel.md) - Route management
- [SensorViewModel](api/SensorViewModel.md) - Compass/sensor handling
- [SettingsViewModel](api/SettingsViewModel.md) - App settings
- [UIStateViewModel](api/UIStateViewModel.md) - UI state (navigation, PiP)

**Data Layer**
- [RouteDao](api/RouteDao.md) - Database DAO
- [AppSettings](api/AppSettings.md) - DataStore preferences
- [ServiceState](api/ServiceState.md) - Service state persistence
- [RouteEntity](api/RouteEntity.md) - Database entities
- [LocationState](api/LocationState.md) - Location data model
- [SatelliteBlip](api/SatelliteBlip.md) - Satellite data model
- [LocationError](api/LocationError.md) - Error types
- [LocationUtils](api/LocationUtils.md) - Polar coordinate conversion

**Components**
- [RadarView](api/RadarView.md) - Canvas radar visualization
- [RangeSelector](api/RangeSelector.md) - Radar range selector
- [RouteCard](api/RouteCard.md) - Route controls
- [CurrentLocationCard](api/CurrentLocationCard.md) - Location display
- [SatellitesList](api/SatellitesList.md) - Satellite details
- [RoutesList](api/RoutesList.md) - Route list
- [ConfirmationDialog](api/ConfirmationDialog.md) - Reusable dialog

### Screens

- [Home Screen](screens/home.md) - Recording controls, satellite info
- [Radar Screen](screens/radar.md) - Map visualization, navigation
- [Routes Screen](screens/routes.md) - Saved routes management
- [Settings Screen](screens/settings.md) - App configuration

### Guides

- [Setup Guide](guides/setup.md) - Development environment setup
- [Build Instructions](guides/build.md) - Building the app
- [Testing Guide](guides/testing.md) - Running tests

## Permissions

The app requests the following permissions at runtime:
- `ACCESS_FINE_LOCATION`
- `ACCESS_COARSE_LOCATION`

These are required for the location-based features of the application. This app works entirely offline using only GPS - no internet or cellular connection required.

## License

This project is licensed under the GPL License - see the [LICENSE.md](../LICENSE.md) file for details.
