package de.hhn.gnsstrackingapp.ui.vrnavigation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.WindowManager
import de.hhn.gnsstrackingapp.ui.navigation.NavigationViewModel


/**
 * AzimuthCalculator computes the device's azimuth using accelerometer and magnetic field sensors,
 * updating the provided NavigationViewModel with smoothed values.
 *
 * @param context Application context for system services.
 * @param navigationViewModel ViewModel for azimuth updates.
 *
 * - Registers sensors and calculates azimuth with rotation adjustments.
 * - Smooths data using a low-pass filter.
 * - Call `unregister()` to stop sensor updates.
 */
class AzimuthCalculator(
    context: Context,
    private val navigationViewModel: NavigationViewModel
) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val adjustedRotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)

    private val accelerometerSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magneticFieldSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private var azimuth: Float = 0f
    private var lastAzimuth: Float = 0f

    init {
        accelerometerSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        magneticFieldSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, gravity, 0, event.values.size)
            }

            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, geomagnetic, 0, event.values.size)
            }
        }

        if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
            // Adjust the rotation matrix based on the device's orientation
            remapForScreenRotation()

            SensorManager.getOrientation(adjustedRotationMatrix, orientation)

            // Get azimuth in radians and convert to degrees
            azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()

            // Normalize azimuth to 0°-360°
            azimuth = (azimuth + 360) % 360

            // Smooth azimuth using a low-pass filter
            azimuth = lowPassFilter(azimuth, lastAzimuth)
            lastAzimuth = azimuth

            // Update the navigation view model with the smoothed azimuth
            navigationViewModel.updateDeviceAzimuth(azimuth)
        }
    }

    private fun remapForScreenRotation() {
        val rotation = windowManager.defaultDisplay.rotation
        when (rotation) {
            Surface.ROTATION_0 -> {
                // Portrait mode
                System.arraycopy(rotationMatrix, 0, adjustedRotationMatrix, 0, rotationMatrix.size)
            }

            Surface.ROTATION_90 -> {
                // Landscape mode, 90 degrees
                SensorManager.remapCoordinateSystem(
                    rotationMatrix,
                    SensorManager.AXIS_Y,
                    SensorManager.AXIS_MINUS_X,
                    adjustedRotationMatrix
                )
            }

            Surface.ROTATION_180 -> {
                // Reverse portrait
                SensorManager.remapCoordinateSystem(
                    rotationMatrix,
                    SensorManager.AXIS_MINUS_X,
                    SensorManager.AXIS_MINUS_Y,
                    adjustedRotationMatrix
                )
            }

            Surface.ROTATION_270 -> {
                // Landscape mode, 270 degrees
                SensorManager.remapCoordinateSystem(
                    rotationMatrix,
                    SensorManager.AXIS_MINUS_Y,
                    SensorManager.AXIS_X,
                    adjustedRotationMatrix
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle sensor accuracy changes if needed maybe....idk
    }


    fun unregister() {
        sensorManager.unregisterListener(this)
    }

    /**
     * Low-pass filter to smooth the azimuth values.
     * @param input Current azimuth value.
     * @param output Previous smoothed azimuth value.
     * @return Smoothed azimuth value.
     */
    private fun lowPassFilter(input: Float, output: Float): Float {
        val alpha = 0.25f // Smoothing factor (0 < alpha < 1)
        return output + alpha * (input - output)
    }
}