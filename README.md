# Radar

Radar is an Android application for recording GPS points to create routes, saving them, and navigating back.
Designed for outdoor use where traditional navigation (Google/Apple Maps) is unavailable due to poor cellular reception.

## Features

- Route recording - capture GPS points as you move
- Route saving - store routes locally for later use
- Route following - navigate back along saved routes
- Compass heading - uses device accelerometer + magnetometer
- GPS satellite tracking - see satellite visibility
- Picture-in-Picture mode - keeps radar visible while multitasking
- Works fully offline - GPS only, no internet required

## Technical Details

- **Minimum SDK**: 35
- **Target SDK**: 36
- **Language**: Kotlin
- **Build System**: Gradle with Kotlin DSL
- **Architecture**: MVVM with Manual Dependency Injection
- **UI Framework**: Jetpack Compose
- **Database**: Room
- **Location Services**: Google Play Services Location
- **Sensors**: Accelerometer + Magnetometer (compass heading)
- **Satellite Tracking**: GPS satellite visibility display

## Screenshots
<div align="center">
  <img src="docs/assets/screenshots/home.jpeg" width="200" alt="Home Screen"/>
  <img src="docs/assets/screenshots/radar_record.jpeg" width="200" alt="Radar Recording"/>
  <img src="docs/assets/screenshots/radar_follow.jpeg" width="200" alt="Radar Following"/>
  <img src="docs/assets/screenshots/routes.jpeg" width="200" alt="Routes"/>
</div>

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

- [Architecture Overview](docs/architecture/overview.md) - System architecture and component design
- [Data Flow](docs/architecture/data-flow.md) - Data flow between layers

### API Reference

**Services**
- [LocationService](docs/api/LocationService.md) - Foreground service for GPS tracking
- [MainActivity](docs/api/MainActivity.md) - Main activity, service binding, PiP mode

**ViewModels**
- [LocationViewModel](docs/api/LocationViewModel.md) - Location state management
- [RouteViewModel](docs/api/RouteViewModel.md) - Route management
- [SensorViewModel](docs/api/SensorViewModel.md) - Compass/sensor handling
- [SettingsViewModel](docs/api/SettingsViewModel.md) - App settings
- [UIStateViewModel](docs/api/UIStateViewModel.md) - UI state (navigation, PiP)

**Data Layer**
- [RouteDao](docs/api/RouteDao.md) - Database DAO
- [AppSettings](docs/api/AppSettings.md) - DataStore preferences
- [ServiceState](docs/api/ServiceState.md) - Service state persistence
- [RouteEntity](docs/api/RouteEntity.md) - Database entities
- [LocationState](docs/api/LocationState.md) - Location data model
- [SatelliteBlip](docs/api/SatelliteBlip.md) - Satellite data model
- [LocationError](docs/api/LocationError.md) - Error types
- [LocationUtils](docs/api/LocationUtils.md) - Polar coordinate conversion

**Components**
- [RadarView](docs/api/RadarView.md) - Canvas radar visualization
- [RangeSelector](docs/api/RangeSelector.md) - Radar range selector
- [RouteCard](docs/api/RouteCard.md) - Route controls
- [CurrentLocationCard](docs/api/CurrentLocationCard.md) - Location display
- [SatellitesList](docs/api/SatellitesList.md) - Satellite details
- [RoutesList](docs/api/RoutesList.md) - Route list
- [ConfirmationDialog](docs/api/ConfirmationDialog.md) - Reusable dialog

### Screens
- [Home Screen](docs/screens/home.md) - Recording controls, satellite info
- [Radar Screen](docs/screens/radar.md) - Map visualization, navigation
- [Routes Screen](docs/screens/routes.md) - Saved routes management
- [Settings Screen](docs/screens/settings.md) - App configuration

### Guides
- [Setup Guide](docs/guides/setup.md) - Development environment setup
- [Build Instructions](docs/guides/build.md) - Building the app
- [Testing Guide](docs/guides/testing.md) - Running tests

## Setup

For detailed setup instructions, see the [Setup Guide](docs/guides/setup.md).

Quick start:
1. Clone the repository
2. Open in Android Studio (Chipmunk or later recommended)
3. Sync Gradle dependencies
4. Run on an emulator or physical device (API 35+ required)

## Permissions

The app requests the following permissions at runtime:
- `ACCESS_FINE_LOCATION`  
- `ACCESS_COARSE_LOCATION`  

These are required for the location-based features of the application.  
This app works entirely offline using only GPS - no internet or cellular connection required.  

## Building

For detailed build instructions, see the [Build Instructions](docs/guides/build.md).

To build the debug variant:
```bash
./gradlew assembleDebug
```

To build the release variant:
```bash
./gradlew assembleRelease
```

## Testing

For detailed testing instructions, see the [Testing Guide](docs/guides/testing.md).

Run unit tests:
```bash
./gradlew test
```

Run instrumented tests:
```bash
./gradlew connectedAndroidTest
```

## License

This project is licensed under the GPL License - see the [LICENSE.md](./LICENSE.md) file for details.
