package com.thoughtworks.data.authmodeldemo.spike

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.thoughtworks.data.authmodeldemo.R
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import io.reactivex.rxjava3.functions.BiFunction

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
        val gravityObservable = naiveObserveSensorChanged(sensorManager, gravitySensor, 100)
        val accelerometerObservable =
            naiveObserveSensorChanged(sensorManager, accelerometerSensor, 100)
        val disposable = Observable.combineLatest<SensorEvent, SensorEvent, String>(
            gravityObservable,
            accelerometerObservable,
            BiFunction<SensorEvent, SensorEvent, String> { gravitySensorEvent, accelerometerSensorEvent ->
                return@BiFunction "gravity: ${gravitySensorEvent?.values?.joinToString { "$it, " }}\naccelerometer: ${accelerometerSensorEvent?.values?.joinToString { "$it, " }}"
            }).subscribe {
            Log.i(TAG, it)
        }
    }

    private fun naiveObserveSensorChanged(
        sensorManager: SensorManager,
        sensor: Sensor,
        samplingPeriodUs: Int
    ): Observable<SensorEvent>? {
        return Observable.create(ObservableOnSubscribe<SensorEvent> {
            val listener = object : SensorEventListener {
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                }

                override fun onSensorChanged(event: SensorEvent?) {
                    it.onNext(event)
                }
            }
            sensorManager.registerListener(listener, sensor, samplingPeriodUs)
        })
    }

    companion object {
        val TAG = this::class.java.simpleName
    }

}
