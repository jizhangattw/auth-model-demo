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
import com.google.gson.Gson
import com.thoughtworks.data.authmodeldemo.R
import com.thoughtworks.data.authmodeldemo.spike.model.SensorData
import com.thoughtworks.data.authmodeldemo.spike.model.Vector3
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.FlowableOnSubscribe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Function3
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_train_ocsvm.*
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.io.OutputStreamWriter
import java.util.concurrent.TimeUnit
import kotlin.math.max


class TrainOCSVMActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_train_ocsvm)

        listenGravitySensorByObserve()

        Observable.interval(0, 1, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                timerTextView.text = "time: $it/60"
            }
    }

    private fun listenGravitySensorByObserve() {
        collectData(10)
            .recordData()
            .map {
                writeToFile("before_normalized.json", Gson().toJson(it), this)
                it
            }
            .normalized()
            .map {
                writeToFile("after_normalized.json", Gson().toJson(it), this)
                it
            }
            .reshapeData()
            .obtainFeature(this)
            .trainOCSVMModel()
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                resultTextView.text = "${if (it) "train success & save ocsvm success" else "train failure & save ocsvm failure"}"
                Log.i(TAG, "success: $it")
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
            sensorManager.registerListener(listener, sensor, samplingPeriodUs, samplingPeriodUs)
        }, BackpressureStrategy.BUFFER)
    }

    private fun collectData(sampling: Int): Flowable<SensorData> {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val gravityFlowable = naiveObserveSensorChanged(
            sensorManager,
            gravitySensor,
            sampling
        )
        val accelerometerFlowable =
            naiveObserveSensorChanged(
                sensorManager,
                accelerometerSensor,
                sampling
            )
        val magneticFieldFlowable =
            naiveObserveSensorChanged(
                sensorManager,
                magneticFieldSensor,
                sampling
            )

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
            .sample(sampling.toLong(), TimeUnit.MILLISECONDS)
    }

    private fun Flowable<SensorData>.recordData(): Flowable<Array<FloatArray>> {
        return buffer(100 / 25)
            // average data
            .map {
                it.reduce { acc, sensorData ->
                    acc + sensorData
                } / SensorDataActivity.AVERAGE_COUNT.toLong()
            }
            .buffer(25 * 60)
            .map { list ->
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
                }.toTypedArray()
            }
    }

    private fun Flowable<Array<FloatArray>>.normalized(): Flowable<Array<FloatArray>> {
        return map {
            val python = Python.getInstance()
            val result = python.getModule("scale_data").callAttr(
                "scale_data", it, false, true
            )
            result.toJava(Array<FloatArray>::class.java)
        }
    }

    private fun Flowable<Array<FloatArray>>.reshapeData(): Flowable<Array<Array<Array<Float>>>> {
        return map {
            val python = Python.getInstance()
            val result = python.getModule("reshape").callAttr(
                "reshape", it, ReshapeDataActivity.WINDOW_SIZE,
                ReshapeDataActivity.STEP_SIZE
            )
            result.toJava(Array<Array<Array<Float>>>::class.java)
        }
    }

    private fun Flowable<Array<Array<Array<Float>>>>.obtainFeature(context: Context): Flowable<Array<FloatArray>> {
        return map {
            val options = Interpreter
                .Options()
                .apply {
                    setNumThreads(4)
                }
            val model = FileUtil.loadMappedFile(context, "NAIVE-MINMAX-2D_model.tflite")
            val interpreter = Interpreter(model, options)
            val inputIndex = 0
            val outputIndex = 0
            val inputShape = interpreter.getInputTensor(inputIndex).shape() // {1, 25, 9, 1}
            val inputDataType =
                interpreter.getInputTensor(inputIndex).dataType()
            val featureShape = interpreter.getOutputTensor(outputIndex).shape() // {1, 64}
            val featureDataType =
                interpreter.getOutputTensor(outputIndex).dataType()
            val inputBuffer = TensorBuffer.createFixedSize(inputShape, inputDataType)
            val outputBuffer = TensorBuffer.createFixedSize(featureShape, featureDataType)
            val flatData = it.flatMap { oneD ->
                oneD.asList()
                    .flatMap { twoD ->
                        twoD.asList()
                    }
            }.toFloatArray()
            val sampleSize = 25 * 9 //sample size = 25 * 9  ->  ( 9 + 9 + ... + 9)
            val cutoff = flatData.size % sampleSize
            val inputArray = flatData.dropLast(cutoff).toFloatArray()

            var x = arrayOf<FloatArray>()
            for (i in inputArray.indices step sampleSize) {
                val slice = inputArray.slice(IntRange(i, i + sampleSize - 1))
                inputBuffer.loadArray(slice.toFloatArray())
                interpreter.run(inputBuffer.buffer, outputBuffer.buffer.rewind())
                x += outputBuffer.floatArray
            }
            x
        }
    }

    private fun Flowable<Array<FloatArray>>.trainOCSVMModel(): Flowable<Boolean> {
        return map {
            val python = Python.getInstance()
            try {
                python.getModule("testSklearn").callAttr(
                    "train_and_save_model", it
                )
                true
            } catch (throwable: Throwable) {
                false
            }
        }
    }

    private fun writeToFile(
        name: String,
        data: String,
        context: Context
    ) {
        try {
            val outputStreamWriter = OutputStreamWriter(
                context.openFileOutput(
                    name,
                    Context.MODE_PRIVATE
                )
            )
            outputStreamWriter.write(data)
            outputStreamWriter.close()
        } catch (e: IOException) {
            Log.e("Exception", "File write failed: " + e.toString())
        }
    }

    companion object {
        val TAG = TrainOCSVMActivity::class.java.simpleName
    }
}
