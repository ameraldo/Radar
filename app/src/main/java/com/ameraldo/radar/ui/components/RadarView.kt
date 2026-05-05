package com.ameraldo.radar.ui.components

import android.graphics.Paint
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.core.graphics.withSave
import com.ameraldo.radar.data.DistanceUnits
import com.ameraldo.radar.ui.theme.amber500Color
import com.ameraldo.radar.ui.theme.green500Color
import com.ameraldo.radar.ui.theme.red500Color
import com.ameraldo.radar.utils.PolarPoint
import com.ameraldo.radar.data.SatelliteBlip
import kotlin.math.cos
import kotlin.math.sin

// Arrow Direction for compass ring
private enum class ArrowDirection {
    OUTWARD, INWARD
}

/**
 * Main radar visualization component.
 *
 * Displays a radar screen with:
 * - Rotating compass ring (tracks device heading)
 * - Stationary radar grid (concentric rings, polar grid, cartesian mesh)
 * - Animated sweep line with trail
 * - Recorded points (green = start, amber = current/path)
 * - Satellite blips (color-coded by signal strength)
 * - Following indicator (dashed line to next point)
 *
 * Uses smooth compass rotation to avoid wraparound jumps at 0°/360°.
 * The sweep animation runs continuously when the component is visible.
 *
 * @param modifier Modifier for styling
 * @param headingDegrees Current compass heading (0-360°)
 * @param satelliteBlips List of satellites to display (null = show radar screen)
 * @param recordedPoints List of recorded points to display
 * @param nextPointToFollow Next point to follow (shows arrow + dashed line)
 * @param radarRange Selected radar range in meters/feet
 * @param radarDistanceUnits Current distance units
 */
@Composable
fun RadarView(
    modifier: Modifier = Modifier,
    headingDegrees: Float = 0f,
    satelliteBlips: List<SatelliteBlip>? = null,
    recordedPoints: List<PolarPoint> = emptyList(),
    nextPointToFollow: PolarPoint? = null,
    radarRange: Float = 100f,
    radarDistanceUnits: DistanceUnits = DistanceUnits.METRIC
) {
    // Color palette (Inherited by theme)
    val backgroundColor     = MaterialTheme.colorScheme.background
    val surfaceColor        = MaterialTheme.colorScheme.surface
    val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant
    val primaryColor        = MaterialTheme.colorScheme.primary
    val secondaryColor      = MaterialTheme.colorScheme.secondary
    val outlineColor        = MaterialTheme.colorScheme.outline
    val redColor            = red500Color
    val amberColor          = amber500Color
    val greenColor          = green500Color

    /* ********************** Helper functions for smooth compass behavior ********************** */

    // Track accumulated rotation to avoid wraparound jumps at 0°/360°
    // Uses shortest-path delta calculation
    var previousHeading by remember { mutableFloatStateOf(headingDegrees) }
    var accumulatedHeading by remember { mutableFloatStateOf(headingDegrees) }

    // Update accumulated heading when headingDegrees changes
    // This runs outside of composition, avoiding the mutation error
    LaunchedEffect(headingDegrees) {
        // Compute shortest-path delta
        val delta = ((headingDegrees - previousHeading + 540f) % 360f) - 180f
        accumulatedHeading += delta
        previousHeading = headingDegrees
    }

    // Smooth compass rotation
    val smoothHeading by animateFloatAsState(
        targetValue = accumulatedHeading,
        animationSpec = tween(durationMillis = 200, easing = LinearEasing),
        label = "compass_heading"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "radar_sweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep_angle"
    )

    /* ******************************* Radar Drawing (Stationary) ******************************* */

    // Colors are read from MaterialTheme to respect light/dark mode
    val windowInfo = LocalWindowInfo.current
    val density    = LocalDensity.current
    val screenPx   = with(density) {
        maxOf(
            windowInfo.containerSize.width.toFloat(),
            windowInfo.containerSize.height.toFloat()
        )
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .drawWithContent {
                clipRect(
                    left   = 0f,
                    top    = -Float.MAX_VALUE,  // bleeds upward
                    right  = size.width,
                    bottom = Float.MAX_VALUE,   // bleeds downward
                    clipOp = ClipOp.Intersect
                ) {
                    this@drawWithContent.drawContent()
                }
            }
            .padding(32.dp)
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val outerR = size.minDimension / 2f          // compass ring radius
        val radarR = outerR * 0.92f                  // inner radar radius
        // val fullR = maxOf(size.width, size.height) // extends to screen edges
        val fullR = screenPx

        /* **************************** Radar internals (stationary) **************************** */

        // 1. Background
        drawCircle(
            brush = Brush.radialGradient(
                listOf(backgroundColor, surfaceColor),
                Offset(cx, cy),
                fullR
            ),
            radius = fullR,
            center = Offset(cx, cy)
        )

        // 2. Concentric rings — spaced by radarR/4, repeating outward to fullR
        // Labels are drawn at 45° on the first 3 rings (not outer edge)
        val ringSpacingPx = radarR / 4f
        var ringDistance = radarRange / 4f
        var r = ringSpacingPx
        while (r <= fullR) {
            drawCircle(
                color  = outlineVariantColor,
                radius = r,
                center = Offset(cx, cy),
                style  = Stroke(width = 1f)
            )
            /*
             * If we're showing the radar screen (satelliteBlips == null), draw
             * distance labels on first 3 rings (not outer edge), positioned at 45°
             * to avoid crosshairs
             */
            if (r <= radarR * 0.75f && satelliteBlips == null) {
                // Set label
                val label = if (radarDistanceUnits == DistanceUnits.METRIC)
                                if (ringDistance >= 1000f)
                                    "${"%.1f".format(ringDistance / 1000f)}km"
                                else
                                    "${"%.1f".format(ringDistance)}m"
                            else
                                if (ringDistance >= 5280f)
                                    "${"%.1f".format(ringDistance / 5280f)}mi"
                                else
                                    "${"%.1f".format(ringDistance)}ft"
                // Set label angle
                val angleDeg = 45f // Draw text at 45°
                val angleRad = Math.toRadians(angleDeg.toDouble())
                val labelRadius = r + 12f  // 10px outside the ring

                val lx = cx + labelRadius * sin(angleRad).toFloat()
                val ly = cy - labelRadius * cos(angleRad).toFloat()
                // Draw
                drawContext.canvas.nativeCanvas.apply {
                    withSave {
                        rotate(angleDeg, lx, ly)
                        val paint = Paint().apply {
                            isAntiAlias = true
                            textSize = radarR * 0.05f
                            color = outlineVariantColor.toArgb()
                            textAlign = Paint.Align.CENTER
                        }
                        drawText(label, lx, ly + 8f, paint)
                    }
                }
            }

            r += ringSpacingPx
            ringDistance += radarRange / 4f
        }

        // 3. Polar grid lines every 30° (from center outward to fullR)
        for (deg in 0 until 360 step 30) {
            val rad = Math.toRadians(deg.toDouble())
            drawLine(
                color = outlineVariantColor,
                start = Offset(cx, cy),
                end = Offset(cx + fullR * sin(rad).toFloat(),
                             cy - fullR * cos(rad).toFloat()),
                strokeWidth = 1f,
                alpha = 0.6f
            )
        }

        // 4. Cartesian mesh — extends to fullR (clipped to radar radius for display)
        drawCartesianGrid(cx, cy, radarR, fullR, outlineVariantColor)

        // 5. Crosshairs (gaps at center for readability)
        val gap = radarR * 0.08f
        drawLine(outlineColor, Offset(cx - radarR, cy),
                 Offset(cx - gap, cy), strokeWidth = 1.5f)
        drawLine(outlineColor, Offset(cx + gap, cy),
                 Offset(cx + radarR, cy), strokeWidth = 1.5f)
        drawLine(outlineColor, Offset(cx, cy - radarR),
                 Offset(cx, cy - gap), strokeWidth = 1.5f)
        drawLine(outlineColor, Offset(cx, cy + gap),
                 Offset(cx, cy + radarR), strokeWidth = 1.5f)

        // 6. Tick marks on radar circle (major every 30°, minor every 10°)
        drawTicks(cx, cy, radarR, outlineColor)

        // 7. Sweep trail
        drawSweepTrail(cx, cy, radarR, sweepAngle,
                       primaryColor.copy(alpha = 0.2f)) // 20% transparency
        // 8. Sweep line (rotates continuously, 3s per revolution)
        val sweepRad = Math.toRadians(sweepAngle.toDouble())
        drawLine(
            color = primaryColor,
            start = Offset(cx, cy),
            end = Offset(
                cx + radarR * sin(sweepRad).toFloat(),
                cy - radarR * cos(sweepRad).toFloat()
            ),
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
        )

        // 9. Centre dot (marks device position)
        drawCircle(primaryColor, radius = 5f, center = Offset(cx, cy))

        // 10. Direction arrow fixed at top (points to true North, red)
        drawArrow(cx, cy, outerR, radarR, angleDeg = 0f,
            ArrowDirection.INWARD, color = outlineColor)

        // 11. Recorded point blips (green = start, amber = current/path)
        // Points are drawn with 3 layers: outer glow, middle ring, core dot
        recordedPoints.forEachIndexed { index, point ->
            val bRad    = Math.toRadians(point.angleDeg.toDouble())
            val bRadius = radarR * point.radiusFraction.coerceIn(0f, 0.95f)
            val bx      = cx + bRadius * sin(bRad).toFloat()
            val by      = cy - bRadius * cos(bRad).toFloat()

            val isFirst = index == 0
            val isLast  = index == recordedPoints.lastIndex
            val color   = when {
                isFirst -> greenColor                     // green — start
                isLast  -> amberColor                     // amber — current
                else    -> amberColor.copy(alpha = 0.4f)  // dim amber — path
            }
            val coreRadius = if (isFirst || isLast) 4f else 2.5f

            drawCircle(color.copy(alpha = 0.20f), radius = if (isFirst || isLast) 12f else 6f,
                       center = Offset(bx, by))
            drawCircle(color.copy(alpha = 0.50f), radius = if (isFirst || isLast) 6f  else 3f,
                       center = Offset(bx, by))
            drawCircle(color,                     radius = coreRadius,
                       center = Offset(bx, by))
        }

        // 12. Recorded path — line connecting all points (shows route taken)
        if (recordedPoints.size > 1) {
            val pathPoints = recordedPoints.map { point ->
                val bRad    = Math.toRadians(point.angleDeg.toDouble())
                val bRadius = radarR * point.radiusFraction.coerceIn(0f, 0.95f)
                Offset(
                    cx + bRadius * sin(bRad).toFloat(),
                    cy - bRadius * cos(bRad).toFloat()
                )
            }
            // draw connecting line
            for (i in 0 until pathPoints.size - 1) {
                drawLine(
                    color       = amberColor.copy(alpha = 0.6f),
                    start       = pathPoints[i],
                    end         = pathPoints[i + 1],
                    strokeWidth = 2f,
                    cap         = StrokeCap.Round
                )
            }
        }

        // 13. Dashed line from center to next point to follow (navigation aid)
        // Uses segments to create dashed effect
        nextPointToFollow?.let { nextPoint ->
            val bRad = Math.toRadians(nextPoint.angleDeg.toDouble())
            val bRadius = radarR * nextPoint.radiusFraction.coerceIn(0f, 0.95f)
            val targetX = cx + bRadius * sin(bRad).toFloat()
            val targetY = cy - bRadius * cos(bRad).toFloat()

            // Draw dashed line from center to target using segments
            val segments = 10
            for (i in 0 until segments step 2) {
                val startFraction = i.toFloat() / segments
                val endFraction = (i + 1).toFloat() / segments
                drawLine(
                    color = amberColor.copy(alpha = 0.6f),
                    start = Offset(
                        cx + (targetX - cx) * startFraction,
                        cy + (targetY - cy) * startFraction
                    ),
                    end = Offset(
                        cx + (targetX - cx) * endFraction,
                        cy + (targetY - cy) * endFraction
                    ),
                    strokeWidth = 2f,
                    cap = StrokeCap.Round
                )
            }

//            // Draw solid line from center to target
//            drawLine(
//                color = amberColor.copy(alpha = 0.6f),
//                start = Offset(cx, cy),
//                end = Offset(targetX, targetY),
//                strokeWidth = 2f,
//                cap = StrokeCap.Round
//            )
        }

        // 14. Satellite blips (color-coded by signal strength)
        // Green = locked (used in fix), yellow→red gradient by signal strength
        // 3 layers: outer glow, middle ring, core dot
        satelliteBlips?.forEach { blip ->
            val bRad = Math.toRadians(blip.angleDeg.toDouble())
            val bRadius = radarR * blip.radiusFraction.coerceIn(0f, 0.95f)
            val bx = cx + bRadius * sin(bRad).toFloat()
            val by = cy - bRadius * cos(bRad).toFloat()
            val core = if (blip.isLocked) {
                greenColor
            } else {
                // interpolate red → orange → yellow based on signal strength
                val t = (blip.signalStrength / 42f).coerceIn(0f, 1f)
                when {
                    t < 0.5f -> {
                        // red → orange (0–21 dB-Hz)
                        val s = t / 0.5f
                        Color(1f, 0.27f * s, 0f, 1f)
                    }

                    else -> {
                        // orange → yellow (21–42 dB-Hz)
                        val s = (t - 0.5f) / 0.5f
                        Color(1f, 0.27f + 0.73f * s, 0f, 1f)
                    }
                }
            }
            drawCircle(core.copy(alpha = 0.20f), radius = 12f,
                       center = Offset(bx, by))
            drawCircle(core.copy(alpha = 0.50f), radius = 6f,
                       center = Offset(bx, by))
            drawCircle(core, radius = 3.5f, center = Offset(bx, by))
        }

        /* ************************ Compass ring (rotates with heading) ************************* */

        rotate(degrees = -smoothHeading, pivot = Offset(cx, cy)) {
            // 1. Compass ring (rotates with heading)
            drawCompassRing(cx, cy, outerR, radarR, secondaryColor,
                           redColor, primaryColor)
            // 2. Arrow "sticks" to true north (red, outward)
            drawArrow(cx, cy, outerR, radarR, angleDeg = 0f, ArrowDirection.OUTWARD,
                       color = redColor)

            // 3. Direction to follow point arrow (yellow, inward)
            nextPointToFollow?.let { point ->
                drawArrow(cx, cy, outerR, radarR, angleDeg = point.angleDeg,
                ArrowDirection.INWARD, color = Color(0xFFFFD040)
                )
            }
        }
    }
}

/**
 * Draws the outer compass ring with tick marks and cardinal direction labels.
 *
 * Draws:
 * - Outer ring border and inner border
 * - Tick marks every 5° (longer every 45°, thick every 90°)
 * - Cardinal labels (N, E, S, W) beyond the longest tick
 *
 * This function is called inside [rotate] scope, so the ring rotates with heading.
 *
 * @param cx Center X coordinate
 * @param cy Center Y coordinate
 * @param outerR Outer radius (compass ring)
 * @param radarR Inner radius (radar circle)
 * @param compassRingColor Color for ring and major ticks
 * @param northColor Color for "N" label (highlighted)
 * @param cardinalColor Color for "E", "S", "W" labels
 */
private fun DrawScope.drawCompassRing(
    cx: Float, cy: Float,
    outerR: Float, radarR: Float,
    compassRingColor: Color,
    northColor: Color,
    cardinalColor: Color
) {
    // Outer ring border
    drawCircle(
        color  = compassRingColor,
        radius = outerR,
        center = Offset(cx, cy),
        style  = Stroke(width = 2f)
    )
    // Inner border of the compass band
    drawCircle(
        color  = compassRingColor.copy(alpha = 0.4f),
        radius = radarR,
        center = Offset(cx, cy),
        style  = Stroke(width = 1f)
    )

    val bandWidth = outerR - radarR

    // Tick marks every 5°, longer every 45° (extend OUTWARD from outerR)
    for (deg in 0 until 360 step 5) {
        val rad     = Math.toRadians(deg.toDouble())
        val sinA    = sin(rad).toFloat()
        val cosA    = cos(rad).toFloat()
        val isMajor = deg % 45 == 0
        val tickLen = when {
            deg % 90 == 0  -> (outerR - radarR) * 0.75f
            deg % 45 == 0  -> (outerR - radarR) * 0.55f
            else           -> (outerR - radarR) * 0.45f
        }
        // Draw ticks: start at outer ring, extend outward by tickLen
        drawLine(
            color       = if (isMajor) compassRingColor else compassRingColor.copy(alpha = 0.5f),
            start       = Offset(cx + outerR * sinA, cy - outerR * cosA),
            end         = Offset(cx + (outerR + tickLen) * sinA,
                                 cy - (outerR + tickLen) * cosA),
            strokeWidth = if (deg % 90 == 0) 2.5f else 1f
        )
//        // Replace the drawLine function above with the block below if you want to draw the tick
//        // lines between the inner (radar) and outer (compass) ring
//        val innerEdge = outerR - tickLen
//        drawLine(
//            color       = if (isMajor) CompassRing else CompassRing.copy(alpha = 0.5f),
//            start       = Offset(cx + innerEdge * sinA, cy - innerEdge * cosA),
//            end         = Offset(cx + outerR   * sinA, cy - outerR   * cosA),
//            strokeWidth = if (deg % 90 == 0) 2.5f else 1f
//        )
    }

    // Cardinal labels: positioned beyond the longest tick (outside compass band)
    val labelRadius = outerR + bandWidth * 1.05f

    // Replace the line above with this one if you want the cardinal labels between the inner
    // and outer ring.
    // val labelRadius = radarR + (outerR - radarR) * 0.42f

    val paint = Paint().apply {
        isAntiAlias = true
        textAlign   = Paint.Align.CENTER
    }
    val cardinals = listOf(
        Triple("N", 0.0,   northColor),
        Triple("E", 90.0,  cardinalColor),
        Triple("S", 180.0, cardinalColor),
        Triple("W", 270.0, cardinalColor)
    )
    cardinals.forEach { (label, angleDeg, color) ->
        val rad  = Math.toRadians(angleDeg)
        val lx   = cx + labelRadius * sin(rad).toFloat()
        val ly   = cy - labelRadius * cos(rad).toFloat()
        val textSize = (outerR - radarR) * 1.4f

        // Draw label: counter-rotate so text stays upright as ring spins
        // Since we're inside rotate{} scope, we offset to label position
        drawContext.canvas.nativeCanvas.apply {
            withSave {
                // We undo the parent rotate() so the text reads correctly
                // The parent rotate is -heading, so we rotate +heading here-
                // But since we're inside rotate{} scope already, we just need to
                // rotate the text around its own position to keep it upright
                rotate(0f, lx, ly)   // placeholder — see note below
                paint.color = color.toArgb()
                paint.textSize = textSize
                paint.isFakeBoldText = (label == "N")
                drawText(label, lx, ly + textSize * 0.35f, paint)
            }
        }
    }
}

/**
 * Draws the cartesian grid (horizontal and vertical lines).
 *
 * Creates a grid of cells within the radar circle.
 * Lines are clipped to [inRadius] (inner radar circle).
 * Extends outward to [exRadius] for the compass band area.
 *
 * @param cx Center X coordinate
 * @param cy Center Y coordinate
 * @param inRadius Inner radius where grid lines stop (radar circle)
 * @param exRadius Outer radius where grid lines extend to
 * @param gridColor Color for grid lines (with alpha)
 */
private fun DrawScope.drawCartesianGrid(cx: Float, cy: Float, inRadius: Float, exRadius: Float,
                                        gridColor: Color) {
    val cells    = 10
    val cellSize = (inRadius * 2f) / cells
    val cols     = (inRadius * 2f / cellSize).toInt() + 2
    val rows     = (exRadius * 2f / cellSize).toInt() + 2

    val startX = cx - (cols / 2f) * cellSize
    val startY = cy - (rows / 2f) * cellSize

    for (i in 0..cols) {
        val x = startX + i * cellSize
        drawGridSegment(cx, cy, exRadius, x1 = x, y1 = cy - exRadius, x2 = x, y2 = cy + exRadius,
                        gridColor)
    }
    for (i in 0..rows) {
        val y = startY + i * cellSize
        drawGridSegment(cx, cy, exRadius, x1 = cx - exRadius, y1 = y, x2 = cx + exRadius, y2 = y,
                        gridColor)
    }
}

/**
 * Draws a single grid segment, clipped to the radar circle.
 *
 * Draws a line segment from (x1,y1) to (x2,y2), but only the portion
 * inside the radar circle (radius) is visible.
 * Used by [drawCartesianGrid] to create the grid pattern.
 *
 * @param cx Center X coordinate
 * @param cy Center Y coordinate
 * @param radius Radar radius for clipping
 * @param x1 Start X
 * @param y1 Start Y
 * @param x2 End X
 * @param y2 End Y
 * @param gridColor Color for the visible segment
 */
private fun DrawScope.drawGridSegment(
    cx: Float, cy: Float, radius: Float,
    x1: Float, y1: Float, x2: Float, y2: Float,
    gridColor: Color
) {
    val steps = 100
    val dx = (x2 - x1) / steps
    val dy = (y2 - y1) / steps
    var drawing = false
    var startX = 0f; var startY = 0f
    for (s in 0..steps) {
        val px = x1 + s * dx; val py = y1 + s * dy
        val inside = (px - cx) * (px - cx) + (py - cy) * (py - cy) <= radius * radius
        if (inside && !drawing)  { startX = px; startY = py; drawing = true }
        else if (!inside && drawing) {
            drawLine(
                gridColor.copy(alpha = 0.3f),
                Offset(startX, startY),
                Offset(px, py), strokeWidth = 0.6f)
            drawing = false
        }
    }
    if (drawing) drawLine(
        gridColor.copy(alpha = 0.3f),
        Offset(startX, startY),
        Offset(x2, y2), strokeWidth = 0.6f)
}

/**
 * Draws tick marks on the radar circle.
 *
 * Draws ticks every 10°, with major ticks every 30° (thicker).
 * Ticks are drawn inward from the circle edge.
 *
 * @param cx Center X coordinate
 * @param cy Center Y coordinate
 * @param radius Radar circle radius
 * @param tickColor Color for tick marks
 */
private fun DrawScope.drawTicks(cx: Float, cy: Float, radius: Float, tickColor: Color) {
    for (deg in 0 until 360 step 10) {
        val rad     = Math.toRadians(deg.toDouble())
        val sinA    = sin(rad).toFloat()
        val cosA    = cos(rad).toFloat()
        val isMajor = deg % 30 == 0
        val tickLen = if (isMajor) radius * 0.07f else radius * 0.04f
        drawLine(
            color       = tickColor,
            start       = Offset(cx + (radius - tickLen) * sinA,
                                 cy - (radius - tickLen) * cosA),
            end         = Offset(cx + radius * sinA, cy - radius * cosA),
            strokeWidth = if (isMajor) 2f else 1f
        )
    }
}

/**
 * Draws the sweep trail (fading arc behind the sweep line).
 *
 * Creates a gradient arc that fades from transparent to the trail color.
 * The trail covers 80° behind the current sweep angle.
 *
 * @param cx Center X coordinate
 * @param cy Center Y coordinate
 * @param radius Radar circle radius
 * @param sweepAngle Current sweep angle in degrees
 * @param sweepTrailColor Color for the trail (with alpha)
 */
private fun DrawScope.drawSweepTrail(cx: Float, cy: Float, radius: Float, sweepAngle: Float,
                                     sweepTrailColor: Color) {
    drawArc(
        brush = Brush.sweepGradient(
            0f to Color.Transparent,
            1f to sweepTrailColor,
            center = Offset(cx, cy)
        ),
        startAngle = sweepAngle - 90f - 80f,
        sweepAngle = 80f,
        useCenter  = true,
        topLeft    = Offset(cx - radius, cy - radius),
        size       = Size(radius * 2, radius * 2)
    )
}

/**
 * Draws a directional arrow on the compass ring.
 *
 * Creates a triangular arrow pointing inward or outward from the ring.
 * Used for:
 * - North indicator (red, outward)
 * - Following direction arrow (yellow, inward)
 *
 * @param cx Center X coordinate
 * @param cy Center Y coordinate
 * @param outerR Outer radius (compass ring)
 * @param radarR Inner radius (radar circle)
 * @param angleDeg Angle in degrees where arrow should point (0° = North)
 * @param direction Whether arrow points OUTWARD or INWARD
 * @param color Arrow fill color
 */
private fun DrawScope.drawArrow(cx: Float, cy: Float, outerR: Float, radarR: Float,
                                angleDeg: Float, direction: ArrowDirection, color: Color
) {
    val bandWidth  = outerR - radarR
    val arrowH     = bandWidth * 0.75f
    val arrowW     = bandWidth * 0.45f

    val baseR: Float
    val tipR: Float

    if (direction == ArrowDirection.OUTWARD) {
        tipR  = radarR + bandWidth * 0.15f
        baseR = tipR + arrowH
    } else {
        baseR = radarR + bandWidth * 0.15f
        tipR  = baseR + arrowH
    }

    val rad  = Math.toRadians(angleDeg.toDouble())
    val sinA = sin(rad).toFloat()
    val cosA = cos(rad).toFloat()
    // perpendicular for arrow wings
    val perpX = cosA
    val perpY = sinA

    val tipX  = cx + tipR  * sinA
    val tipY  = cy - tipR  * cosA
    val baseX = cx + baseR * sinA
    val baseY = cy - baseR * cosA

    val path = Path().apply {
        moveTo(tipX, tipY)
        lineTo(baseX + arrowW / 2f * perpX, baseY + arrowW / 2f * perpY)
        lineTo(baseX - arrowW / 2f * perpX, baseY - arrowW / 2f * perpY)
        close()
    }

    // filled arrow
    drawPath(path, color = color)
    // subtle outline so it reads against the ring
    drawPath(path, color = color.copy(alpha = 0.6f), style = Stroke(width = 1.5f))
}
