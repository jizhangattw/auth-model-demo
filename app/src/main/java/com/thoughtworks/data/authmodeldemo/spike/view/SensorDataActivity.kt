package com.thoughtworks.data.authmodeldemo.spike.view

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.thoughtworks.data.authmodeldemo.R
import com.thoughtworks.data.authmodeldemo.common.util.hzToMillisecond
import com.thoughtworks.data.authmodeldemo.spike.model.SensorData
import com.thoughtworks.data.authmodeldemo.spike.model.Vector3
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.FlowableOnSubscribe
import io.reactivex.rxjava3.functions.Function3
import java.util.concurrent.TimeUnit
import kotlin.math.max

class SensorDataActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor_data)
        title = getString(R.string.spike_sensor_data_collection)

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
        val magneticFieldFlowable = naiveObserveSensorChanged(sensorManager, magneticFieldSensor, 100)

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
            .sample(hzToMillisecond(100L), TimeUnit.MILLISECONDS)
            .buffer(AVERAGE_COUNT)
            .subscribe {
                val sensorAverageData = it.reduce { acc, sensorData ->
                    acc + sensorData
                } / AVERAGE_COUNT.toLong()
                Log.i(TAG, sensorAverageData.toString())
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
        val TAG = SensorDataActivity::class.java.simpleName
        val AVERAGE_COUNT = 4
    }

}
