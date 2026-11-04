# DistanceFormatter

> Formats distances as compact, human-readable labels

DistanceFormatter provides the `DistanceUnits.formatDistance()` extension used
throughout the UI for radar ring labels, range buttons, and settings values.

## Overview

**Package**: `com.ameraldo.radar.utils`

**Source**: [`DistanceFormatter.kt`](../../app/src/main/java/com/ameraldo/radar/utils/DistanceFormatter.kt)

## Function

```kotlin
fun DistanceUnits.formatDistance(
    value: Float,
    locale: Locale = Locale.getDefault()
): String
```

`value` is expressed in the unit system's base unit: **meters** for `METRIC`
and **feet** for `IMPERIAL`. The caller does not need to convert first.

### Metric

1. Round to the nearest whole meter.
2. At 1000 m or more, show one decimal place in kilometers (`"1.0km"`).
3. Below 1000 m, show whole meters (`"250m"`).

| Input | Output |
|-------|--------|
| `250f` | `250m` |
| `999f` | `999m` |
| `1000f` | `1.0km` |
| `5000f` | `5.0km` |

### Imperial

| Condition | Formatting |
|-----------|------------|
| Below 1000 ft | Whole feet, e.g. `330ft` |
| 5280 ft or more | One decimal place in miles, e.g. `1.0mi`, `5.0mi` |
| 1000–5279 ft | Eighth-mile fractions when close, otherwise trimmed decimal miles |

Sub-mile fractions recognized (within 0.001 mi) are `1/8`, `1/4`, `3/8`,
`1/2`, `5/8`, `3/4`, and `7/8`.

| Input | Output |
|-------|--------|
| `330f` | `330ft` |
| `999f` | `999ft` |
| `1320f` | `1/4mi` |
| `1980f` | `3/8mi` |
| `2640f` | `1/2mi` |
| `5280f` | `1.0mi` |
| `7920f` | `1.5mi` |
| `1250f` | `0.24mi` |

Non-fraction values use up to two decimal places, with trailing zeros and
trailing decimal separators removed.

## Private Members

| Member | Description |
|--------|-------------|
| `FEET_PER_MILE` | `5280f` |
| `METERS_PER_KILOMETER` | `1000f` |
| `MIN_MILES_FEET` | `1000f`, threshold below which imperial stays in feet |
| `FRACTION_TOLERANCE_MILES` | `0.001f` tolerance for eighth-mile matching |
| `MILE_FRACTIONS` | Eighth-mile fraction pairs |
| `formatSubMile(miles, locale)` | Fraction/decimal formatter for 1000–5279 ft |

## Used By

- [`RadarView`](RadarView.md) — distance labels on concentric rings
- [`RangeSelector`](RangeSelector.md) — range button labels
- [Settings Screen](../screens/settings.md) — max range and distance values

## Related Documentation

- [DistanceUnits](DistanceUnits.md) - Unit systems and option lists
- [AppSettings](AppSettings.md) - Stored unit-dependent values
- [RadarView](RadarView.md) - Ring labels
- [RangeSelector](RangeSelector.md) - Range buttons
