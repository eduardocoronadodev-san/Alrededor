package com.eduardo.alrededor.data.repository

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.eduardo.alrededor.domain.repository.OrientationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Brújula con el sensor de vector de rotación.
 * - Suaviza el ángulo (promedio exponencial sobre seno y coseno, para no fallar en el cruce 359° a 0°).
 * - Solo emite cuando el cambio es de al menos MIN_CHANGE_DEG, para no recomponer la lista sin necesidad.
 * - No corrige la declinación magnética (en México es de pocos grados).
 */
class SensorOrientationRepository @Inject constructor(
    @ApplicationContext context: Context
) : OrientationRepository {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    override fun azimuthUpdates(): Flow<Float> = callbackFlow {
        val sensor = rotationSensor
        if (sensor == null) {          // sin brújula: el flujo termina
            close()
            return@callbackFlow
        }

        val matrix = FloatArray(9)
        var sinAvg = 0.0
        var cosAvg = 0.0
        var initialized = false
        var lastEmitted = Float.NaN

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(matrix, event.values)

                // Con el teléfono casi plano se usa su borde superior (eje Y);
                // con el teléfono vertical, hacia donde apunta la cámara trasera (eje -Z).
                val upright = abs(matrix[8]) < UPRIGHT_THRESHOLD
                val azimuthRad = if (upright) {
                    atan2(-matrix[2].toDouble(), -matrix[5].toDouble())
                } else {
                    atan2(matrix[1].toDouble(), matrix[4].toDouble())
                }

                if (!initialized) {
                    sinAvg = sin(azimuthRad)
                    cosAvg = cos(azimuthRad)
                    initialized = true
                } else {
                    sinAvg += SMOOTHING * (sin(azimuthRad) - sinAvg)
                    cosAvg += SMOOTHING * (cos(azimuthRad) - cosAvg)
                }

                val degrees = ((Math.toDegrees(atan2(sinAvg, cosAvg)) + 360.0) % 360.0).toFloat()
                if (lastEmitted.isNaN() || angularDifference(degrees, lastEmitted) >= MIN_CHANGE_DEG) {
                    lastEmitted = degrees
                    trySend(degrees)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        awaitClose { sensorManager.unregisterListener(listener) }
    }

    private fun angularDifference(a: Float, b: Float): Float =
        abs(((a - b + 540f) % 360f) - 180f)

    private companion object {
        const val SMOOTHING = 0.15
        const val MIN_CHANGE_DEG = 3f
        const val UPRIGHT_THRESHOLD = 0.7f
    }
}
