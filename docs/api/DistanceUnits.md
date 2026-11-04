# DistanceUnits

> Unit systems and the source of truth for unit-dependent settings

DistanceUnits is the enum that defines the metric and imperial unit systems and
owns every unit-dependent option list and default value. It is used by
`AppSettings`, `SettingsViewModel`, `LocationService`, `LocationUtils`,
`DistanceFormatter`, and the radar/settings UI.

## Overview

**Package**: `com.ameraldo.radar.data`

**Source**: [`DistanceUnits.kt`](../../app/src/main/java/com/ameraldo/radar/data/DistanceUnits.kt)

## Enum Values

```kotlin
enum class DistanceUnits(
    val value: String,
    val distanceOptions: List<Int>,
    val defaultDistance: Int,
    val maxRangeOptions: List<Int>,
    val defaultMaxRange: Int
) {
    METRIC(...),
    IMPERIAL(...)
}
```

| Property | Description |
|----------|-------------|
| `value` | String stored in DataStore (`"metric"` / `"imperial"`) |
| `distanceOptions` | Selectable recording/following distances |
| `defaultDistance` | Default recording/following distance |
| `maxRangeOptions` | Selectable maximum radar ranges |
| `defaultMaxRange` | Default maximum radar range |

## Option Tables

### Metric (`METRIC`)

| Setting | Values | Default |
|---------|--------|---------|
| Recording/following distance | 1, 5, 10, 25, 50, 100 m | 5 m |
| Maximum radar range | 100, 500, 1000, 1500, 2000, 5000 m | 1000 m |

### Imperial (`IMPERIAL`)

| Setting | Values | Default |
|---------|--------|---------|
| Recording/following distance | 3, 15, 30, 80, 160, 320 ft | 15 ft |
| Maximum radar range | 1320, 2640, 5280, 7920, 10560, 26400 ft | 5280 ft |

Distance values are expressed in the unit system's base unit: meters for metric,
feet for imperial.

## Methods

### `normalizeDistance(stored: Int?): Int`

Returns `stored` when it is present in `distanceOptions`; otherwise returns
`defaultDistance`. This normalizes missing values and legacy values left over
from the other unit system.

### `normalizeMaxRange(stored: Int?): Int`

Returns `stored` when it is present in `maxRangeOptions`; otherwise returns
`defaultMaxRange`.

### `fromString(value: String): DistanceUnits`

Resolves a stored string to the enum, falling back to `METRIC` for unknown
values.

## Companion Constants

| Constant | Value | Meaning |
|----------|-------|---------|
| `RANGE_STEP_COUNT` | `4` | Number of radar range steps (25/50/75/100%) |
| `DEFAULT_SELECTED_RANGE_INDEX` | `1` | Default step = 50% of max range |

## Radar Range Steps

The top-level `rangeSteps(maxRange)` function converts a maximum range into the
four selectable radar ranges:

```kotlin
fun rangeSteps(maxRange: Int): List<Float> =
    (1..DistanceUnits.RANGE_STEP_COUNT)
        .map { it * maxRange.toFloat() / DistanceUnits.RANGE_STEP_COUNT }
```

Example: `rangeSteps(1000)` → `[250f, 500f, 750f, 1000f]`.

These values are exposed to the UI as `SettingsViewModel.radarRangeList` and
indexed by `SettingsViewModel.selectedRangeIndex`.

## Related Documentation

- [AppSettings](AppSettings.md) - Persists the selected units and values
- [DistanceFormatter](DistanceFormatter.md) - Formats distances for display
- [SettingsViewModel](SettingsViewModel.md) - Exposes unit-dependent state
- [Settings Screen](../screens/settings.md) - User-facing configuration
- [LocationUtils](LocationUtils.md) - Unit conversion for calculations
