# GpxUtils

> Utility functions for GPX file generation

GpxUtils provides functions for generating GPX 1.1 XML files from route data stored in the Room database.

## Overview

**Package**: `com.ameraldo.radar.utils`

**File**: `app/src/main/java/com/ameraldo/radar/utils/GpxUtils.kt`

**Key Responsibilities**:
- Generate GPX 1.1 compliant XML from route and point data
- Format GPS coordinates and timestamps per GPX schema
- Escape XML special characters in route names

## Source Documentation

For detailed API documentation, see the KDoc comments in:
- [`GpxUtils.kt`](../../app/src/main/java/com/ameraldo/radar/utils/GpxUtils.kt)

## Functions

### generateGpx()

Generates a complete GPX 1.1 document from a route and its recorded points.

**Parameters**:

| Parameter | Type | Description |
|-----------|------|-------------|
| `route` | `RouteEntity` | The route with name and start time |
| `points` | `List<RecordedPointEntity>` | Points in sequence order |

**Returns**: `String` — GPX 1.1 XML document

**GPX Structure**:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<gpx version="1.1" creator="Radar" xmlns="http://www.topografix.com/GPX/1/1">
  <metadata>
    <name>Route Name</name>
    <time>2026-05-11T12:00:00Z</time>
  </metadata>
  <trk>
    <name>Route Name</name>
    <trkseg>
        <trkpt lat="37.7749" lon="-122.4194">
            <ele>15.0</ele>
            <time>2026-05-11T12:00:00Z</time>
        </trkpt>
    </trkseg>
  </trk>
</gpx>
```

**Notes**:
- Timestamps are in ISO 8601 format (UTC)
- Elevation (`<ele>`) is included when GPS provides altitude data, omitted otherwise
- Route names are XML-escaped to prevent invalid characters
- Points are output in `sequenceNumber` order

## Private Members

| Member | Description |
|--------|-------------|
| `isoFormat` | ISO 8601 date formatter for GPX timestamps (UTC) |
| `escapeXml()` | Escapes XML special characters (`&`, `<`, `>`, `"`, `'`) |

## Usage in RouteViewModel

```kotlin
val gpx = generateGpx(route, points)
contentResolver.openOutputStream(uri)?.use {
    it.write(gpx.toByteArray(Charsets.UTF_8))
}
```

## Related Documentation

- [RouteViewModel](RouteViewModel.md) - ViewModel that calls generateGpx()
- [RouteEntity](RouteEntity.md) - Data model
- [Routes Screen](../screens/routes.md) - User-facing route management
