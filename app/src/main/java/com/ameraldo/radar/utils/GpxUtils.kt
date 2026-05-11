package com.ameraldo.radar.utils

import com.ameraldo.radar.data.RecordedPointEntity
import com.ameraldo.radar.data.RouteEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Generates a GPX 1.1 XML string from a route and its recorded points.
 *
 * Produces a valid GPX document with metadata (route name, start time),
 * a single track containing all points in sequence order.
 * Elevation data is not included as it is not stored.
 *
 * @param route The route entity containing name and start time
 * @param points List of recorded points in sequence order (by sequenceNumber)
 * @return GPX 1.1 XML string
 */
fun generateGpx(route: RouteEntity, points: List<RecordedPointEntity>): String {
    val metadataTime = isoFormat.format(Date(route.startTime))
    val name = escapeXml(route.name)
    return buildString {
        appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
        appendLine("<gpx version=\"1.1\" creator=\"Radar\" xmlns=\"http://www.topografix.com/GPX/1/1\">")
        appendLine("  <metadata>")
        appendLine("    <name>$name</name>")
        appendLine("    <time>$metadataTime</time>")
        appendLine("  </metadata>")
        appendLine("  <trk>")
        appendLine("    <name>$name</name>")
        appendLine("    <trkseg>")
        for (point in points) {
            val pointTime = isoFormat.format(Date(point.timestamp))
            appendLine("      <trkpt lat=\"${point.latitude}\" lon=\"${point.longitude}\">")
            appendLine("        <time>$pointTime</time>")
            appendLine("      </trkpt>")
        }
        appendLine("    </trkseg>")
        appendLine("  </trk>")
        appendLine("</gpx>")
    }
}

/** ISO 8601 date formatter for GPX timestamps (UTC). */
private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

/**
 * Escapes XML special characters in a string to prevent invalid XML output.
 *
 * Replaces: & → &amp;   < → &lt;   > → &gt;   " → &quot;   ' → &apos;
 *
 * @param text Raw string potentially containing XML special characters
 * @return String with all XML special characters escaped
 */
private fun escapeXml(text: String): String {
    return text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}
