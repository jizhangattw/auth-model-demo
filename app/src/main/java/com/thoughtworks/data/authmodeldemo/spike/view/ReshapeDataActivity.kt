package com.thoughtworks.data.authmodeldemo.spike.view

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.chaquo.python.Python
import com.thoughtworks.data.authmodeldemo.R
import com.thoughtworks.data.authmodeldemo.main.util.ms
import com.thoughtworks.data.authmodeldemo.spike.model.SensorData
import com.thoughtworks.data.authmodeldemo.spike.model.Vector3
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.FlowableOnSubscribe
import io.reactivex.rxjava3.functions.Function3
import java.util.concurrent.TimeUnit
import kotlin.math.max


class ReshapeDataActivity : AppCompatActivity() {

    private val python = Python.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reshape_data)

        listenGravitySensorByObserve()
    }

    private fun listenGravitySensorByObserve() {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val gravityFlowable = naiveObserveSensorChanged(sensorManager, gravitySensor, 100)
        val accelerometerFlowable =
            naiveObserveSensorChanged(sensorManager, accelerometerSensor, 100)
        val magneticFieldFlowable =
            naiveObserveSensorChanged(sensorManager, magneticFieldSensor, 100)

        val sensorChangedFlowable =
            Flowable.combineLatest<SensorEvent, SensorEvent, SensorEvent, SensorData>(
                gravityFlowable,
                accelerometerFlowable,
                magneticFieldFlowable,
                Function3<SensorEvent, SensorEvent, SensorEvent, SensorData> { gravitySensorEvent, accelerometerSensorEvent, magneticFieldSensorEvent ->
                    val timestamp =
                        max(gravitySensorEvent.timestamp, accelerometerSensorEvent.timestamp)
                    return@Function3 SensorData(
                        timestamp,
                        Vector3(gravitySensorEvent.values),
                        Vector3(accelerometerSensorEvent.values),
                        Vector3(magneticFieldSensorEvent.values)
                    )
                })

        sensorChangedFlowable
            .sample(ms(100), TimeUnit.MILLISECONDS)
            // calculate average value
            .buffer(SensorDataActivity.AVERAGE_COUNT)
            .map {
                it.reduce { acc, sensorData ->
                    acc + sensorData
                } / SensorDataActivity.AVERAGE_COUNT.toLong()
            }
            // collect data more
            .buffer(ms(25).toInt() * TIME)
            // to 2d float array
            .map {
                return@map it.map {
                    return@map floatArrayOf(
                        it.accelerometer.x,
                        it.accelerometer.y,
                        it.accelerometer.z,
                        it.gravity.x,
                        it.gravity.y,
                        it.gravity.z,
                        it.magneticField.x,
                        it.magneticField.y,
                        it.magneticField.z
                    )
                }.toTypedArray()
            }
            .subscribe {
                val result = python.getModule("reshape").callAttr(
                    "reshape", it, WINDOW_SIZE,
                    STEP_SIZE
                )
                Log.i(TAG, result.asList().map { it.asList().toString() }.toString())
            }
    }

    private fun naiveObserveSensorChanged(
        sensorManager: SensorManager,
        sensor: Sensor,
        samplingPeriodUs: Int
    ): Flowable<SensorEvent>? {
        return Flowable.create(FlowableOnSubscribe<SensorEvent> {
            val listener = object : SensorEventListener {
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                }

                override fun onSensorChanged(event: SensorEvent?) {
                    it.onNext(event)
                }
            }
            sensorManager.registerListener(listener, sensor, samplingPeriodUs)
        }, BackpressureStrategy.BUFFER)
    }

    companion object {
        val TAG = ReshapeDataActivity::class.java.simpleName
        const val TIME = 4
        const val WINDOW_SIZE = 25
        const val STEP_SIZE = 25
    }
}
