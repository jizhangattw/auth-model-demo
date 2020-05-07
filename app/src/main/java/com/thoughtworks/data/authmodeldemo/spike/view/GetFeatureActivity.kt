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

class GetFeatureActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_feature)

        listenGravitySensorByObserve()
    }

    private fun listenGravitySensorByObserve() {
        collectData()
            .recordData()
            .reshapeData()
            .subscribe {
                Log.i(TAG, it.toString())
            }
    }

    private fun collectData(): Flowable<SensorData> {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val gravityFlowable = naiveObserveSensorChanged(sensorManager, gravitySensor, 100)
        val accelerometerFlowable =
            naiveObserveSensorChanged(sensorManager, accelerometerSensor, 100)
        val magneticFieldFlowable =
            naiveObserveSensorChanged(sensorManager, magneticFieldSensor, 100)

        return Flowable.combineLatest<SensorEvent, SensorEvent, SensorEvent, SensorData>(
            gravityFlowable,
            accelerometerFlowable,
            magneticFieldFlowable,
            Function3<SensorEvent, SensorEvent, SensorEvent, SensorData> { gravitySensorEvent, accelerometerSensorEvent, magneticFieldSensorEvent ->
                val timestamp =
                    max(gravitySensorEvent.timestamp, accelerometerSensorEvent.timestamp)
                SensorData(
                    timestamp,
                    Vector3(gravitySensorEvent.values),
                    Vector3(accelerometerSensorEvent.values),
                    Vector3(magneticFieldSensorEvent.values)
                )
            })
            .sample(ms(100), TimeUnit.MILLISECONDS)
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

    private fun Flowable<SensorData>.recordData(): Flowable<MutableList<SensorData>> {
        return buffer(SensorDataActivity.AVERAGE_COUNT)
            .map {
                it.reduce { acc, sensorData ->
                    acc + sensorData
                } / SensorDataActivity.AVERAGE_COUNT.toLong()
            }
            .buffer(ms(25).toInt() * ReshapeDataActivity.TIME)
    }

    private fun Flowable<MutableList<SensorData>>.reshapeData(): Flowable<Array<Array<Array<Float>>>> {
        return map { list ->
            list.map { sensorData ->
                floatArrayOf(
                    sensorData.accelerometer.x,
                    sensorData.accelerometer.y,
                    sensorData.accelerometer.z,
                    sensorData.gravity.x,
                    sensorData.gravity.y,
                    sensorData.gravity.z,
                    sensorData.magneticField.x,
                    sensorData.magneticField.y,
                    sensorData.magneticField.z
                )
            }
        }
            .map {
                val python = Python.getInstance()
                val result = python.getModule("reshape").callAttr(
                    "reshape", it.toTypedArray(), ReshapeDataActivity.WINDOW_SIZE,
                    ReshapeDataActivity.STEP_SIZE
                )
                result.asList()
                    .map { oneD ->
                        oneD.asList()
                            .map { twoD ->
                                twoD.asList()
                                    .map { threeD ->
                                        threeD.toFloat()
                                    }.toTypedArray()
                            }.toTypedArray()
                    }.toTypedArray()
            }
    }

    companion object {
        val TAG = GetFeatureActivity::class.java.simpleName
    }
}
