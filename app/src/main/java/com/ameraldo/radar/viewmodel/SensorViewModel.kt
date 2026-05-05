package com.ameraldo.radar.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel that manages device sensors for compass heading.
 *
 * Uses accelerometer and magnetometer to calculate device heading (azimuth).
 * The heading is used to rotate the radar display.
 *
 * ## State Properties
 * - [headingDegrees]: Current compass heading in degrees (0-360°, 0 = North)
 *
 * ## Usage
 * ```
 * val sensorViewModel: SensorViewModel = viewModel()
 * val heading by sensorViewModel.headingDegrees.collectAsState()
 *
 * // Start listening when screen appears
 * sensorViewModel.startListening()
 * ```
 */
class SensorViewModel(application: Application) : AndroidViewModel(application) {
    private val sensorManager =
        application.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val _headingDegrees = MutableStateFlow(0f)

    // Sensor data arrays
    private val gravity     = FloatArray(3)    // Accelerometer: X, Y, Z (m/s²) - includes gravity
    private val geomagnetic = FloatArray(3)    // Magnetometer: X, Y, Z (μT) - earth's magnetic field

    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private var sensorsAvailable = false

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER ->
                    // Copy accelerometer values (gravity + device acceleration)
                    System.arraycopy(event.values, 0, gravity, 0, gravity.size)
                Sensor.TYPE_MAGNETIC_FIELD ->
                    // Copy magnetometer values (earth's magnetic field)
                    System.arraycopy(event.values, 0, geomagnetic, 0, geomagnetic.size)
            }

            // Rotation matrix: transforms device coordinates to world coordinates
            val r = FloatArray(9)   // 3x3 rotation matrix
            val i = FloatArray(9)   // Inclination matrix

            if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
                val orientation = FloatArray(3) // azimuth, pitch, roll
                SensorManager.getOrientation(r, orientation)

                // orientation[0] = azimuth (radians): 0=North, π/2=East, π=South, -π/2=West
                val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()

                // Normalize to 0-360° for compass display
                _headingDegrees.value = (azimuth + 360f) % 360f
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    /**
     * Current compass heading in degrees.
     *
     * Range: 0-360° where 0° = North, 90° = East, 180° = South, 270° = West
     */
    val headingDegrees: StateFlow<Float> = _headingDegrees

    init {
        sensorsAvailable = accelerometer != null && magnetometer != null
        if (!sensorsAvailable) {
            // TODO:
            // Log warning or show UI message that compass won't work
        }
    }

    /**
     * Starts receiving sensor updates for compass heading.
     * Should be called when the screen/app becomes visible.
     */
    fun startListening() {
        if (!sensorsAvailable) return

        sensorManager.registerListener(
            sensorListener,
            accelerometer!!,
            SensorManager.SENSOR_DELAY_UI  // UI rate is sufficient for compass
        )
        sensorManager.registerListener(
            sensorListener,
            magnetometer!!,
            SensorManager.SENSOR_DELAY_UI
        )
    }

    /**
     * Stops receiving sensor updates.
     * Should be called when the screen/app goes away.
     */
    fun stopListening() {
        sensorManager.unregisterListener(sensorListener)
    }

    /**
     * Cleans up sensor listeners when ViewModel is destroyed.
     * Called by the system when ViewModel is no longer in use.
     */
    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}