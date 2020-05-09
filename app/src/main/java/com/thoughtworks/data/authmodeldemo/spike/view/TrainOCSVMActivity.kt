package com.thoughtworks.data.authmodeldemo.spike.view

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.chaquo.python.Python
import com.thoughtworks.data.authmodeldemo.R
import com.thoughtworks.data.authmodeldemo.main.util.ms
import com.thoughtworks.data.authmodeldemo.spike.model.SensorData
import com.thoughtworks.data.authmodeldemo.spike.model.Vector3
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.FlowableOnSubscribe
import io.reactivex.rxjava3.functions.Function3
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.util.concurrent.TimeUnit
import kotlin.math.max

class TrainOCSVMActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_train_ocsvm)

        listenGravitySensorByObserve()
    }

    private fun listenGravitySensorByObserve() {
        collectData()
            .recordData()
            .normalized()
            .reshapeData()
            .obtainFeature(this)
            .trainOCSVMModel()
            .subscribe({
                Log.i(TAG, "success: $it")
            })
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

    private fun Flowable<SensorData>.recordData(): Flowable<Array<FloatArray>> {
        return buffer(SensorDataActivity.AVERAGE_COUNT)
            .map {
                it.reduce { acc, sensorData ->
                    acc + sensorData
                } / SensorDataActivity.AVERAGE_COUNT.toLong()
            }
            .buffer(ms(25).toInt() * ReshapeDataActivity.TIME)
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
                "scale_data", it
            )
            result.asList()
                .map { oneD ->
                    oneD.asList()
                        .map { twoD ->
                            twoD.toFloat()
                        }.toFloatArray()
                }.toTypedArray()
        }.doOnError {
            throw Exception("null null null null null")
        }
    }

    private fun Flowable<Array<FloatArray>>.reshapeData(): Flowable<Array<Array<Array<Float>>>> {
        return map {
            val python = Python.getInstance()
            val result = python.getModule("reshape").callAttr(
                "reshape", it, ReshapeDataActivity.WINDOW_SIZE,
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

    private fun Flowable<Array<Array<Array<Float>>>>.obtainFeature(context: Context): Flowable<FloatArray> {
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
                oneD.toList()
                    .flatMap { twoD ->
                        twoD.toList()
                    }
            }.toFloatArray()
            val sampleSize = 25 * 9 //sample size = 25 * 9  ->  ( 9 + 9 + ... + 9)
            val cutoff = flatData.size % sampleSize
            val inputArray = flatData.dropLast(cutoff).toFloatArray()
            for (i in inputArray.indices step sampleSize) {
                val slice = inputArray.slice(IntRange(i, i + sampleSize - 1))
                inputBuffer.loadArray(slice.toFloatArray())
            }
            interpreter.run(inputBuffer.buffer, outputBuffer.buffer.rewind())
            outputBuffer.floatArray
        }
    }

    private fun Flowable<FloatArray>.trainOCSVMModel(): Flowable<Boolean> {
        return buffer(3)
            .map {
                val python = Python.getInstance()

                python.getModule("testSklearn").callAttr(
                    "train_and_save_model", it.toTypedArray()
                )

                true
            }
    }

    companion object {
        val TAG = TrainOCSVMActivity::class.java.simpleName
    }
}
