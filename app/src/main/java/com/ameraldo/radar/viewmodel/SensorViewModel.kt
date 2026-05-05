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
    private val gravity    = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private var sensorsAvailable = false
    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER ->
                    System.arraycopy(event.values, 0, gravity, 0, gravity.size)
                Sensor.TYPE_MAGNETIC_FIELD ->
                    System.arraycopy(event.values, 0, geomagnetic, 0, geomagnetic.size)
            }

            val r = FloatArray(9)
            val i = FloatArray(9)
            if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(r, orientation)
                // orientation[0] = azimuth in radians, convert to degrees
                val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
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
            // TODO: Handle case when accelerometer and magnetometer sensors are unavailable
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
            SensorManager.SENSOR_DELAY_UI
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

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}