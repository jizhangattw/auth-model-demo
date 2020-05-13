package com.thoughtworks.data.authmodeldemo.common.helper

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.chaquo.python.Python
import com.thoughtworks.data.authmodeldemo.spike.model.SensorData
import com.thoughtworks.data.authmodeldemo.spike.model.Vector3
import com.thoughtworks.data.authmodeldemo.spike.view.SensorDataActivity
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.FlowableOnSubscribe
import io.reactivex.rxjava3.functions.Function3
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.util.concurrent.TimeUnit
import kotlin.math.max

val aiRecordAverageSize = 4
val aiWindowSize = 25

fun collectData(sampling: Int, context: Context): Flowable<SensorData> {
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
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

fun Flowable<SensorData>.recordData(seconds: Int): Flowable<Array<FloatArray>> {
    return buffer(aiRecordAverageSize)
        // average data
        .map {
            it.reduce { acc, sensorData ->
                acc + sensorData
            } / SensorDataActivity.AVERAGE_COUNT.toLong()
        }
        .buffer(aiWindowSize * seconds)
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

fun Flowable<Array<FloatArray>>.normalized(): Flowable<Array<FloatArray>> {
    return map {
        val python = Python.getInstance()
        val result = python.getModule("scale_data").callAttr(
            "scale_data", it, false, true
        )
        result.toJava(Array<FloatArray>::class.java)
    }
}

fun Flowable<Array<FloatArray>>.reshapeData(windowWidth: Int, windowHeight: Int): Flowable<Array<Array<Array<Float>>>> {
    return map {
        val python = Python.getInstance()
        val result = python.getModule("reshape").callAttr(
            "reshape", it, windowWidth,
            windowHeight
        )
        result.toJava(Array<Array<Array<Float>>>::class.java)
    }
}

fun Flowable<Array<Array<Array<Float>>>>.obtainFeature(context: Context): Flowable<Array<FloatArray>> {
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
        val sampleWidth = it[0].size
        val width = it[0][0].size
        val sampleSize = sampleWidth * width //sample size = 25 * 9  ->  ( 9 + 9 + ... + 9)
        val cutoff = flatData.size % sampleSize
        val inputArray = flatData.dropLast(cutoff).toFloatArray()

        var result = arrayOf<FloatArray>()
        for (i in inputArray.indices step sampleSize) {
            val slice = inputArray.slice(IntRange(i, i + sampleSize - 1))
            inputBuffer.loadArray(slice.toFloatArray())
            interpreter.run(inputBuffer.buffer, outputBuffer.buffer.rewind())
            result += outputBuffer.floatArray
        }
        result
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