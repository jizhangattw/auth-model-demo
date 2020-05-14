package com.thoughtworks.data.authmodeldemo.login.helper

import android.content.Context
import com.chaquo.python.Python
import com.google.gson.Gson
import com.thoughtworks.data.authmodeldemo.common.helper.aiWindowSize
import com.thoughtworks.data.authmodeldemo.common.helper.backupData
import com.thoughtworks.data.authmodeldemo.common.helper.collectData
import com.thoughtworks.data.authmodeldemo.common.helper.normalized
import com.thoughtworks.data.authmodeldemo.common.helper.obtainFeature
import com.thoughtworks.data.authmodeldemo.common.helper.recordData
import com.thoughtworks.data.authmodeldemo.common.helper.reshapeData
import com.thoughtworks.data.authmodeldemo.common.util.hzToMillisecond
import com.thoughtworks.data.authmodeldemo.parameterconfig.helper.ConfigurationHelper
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.kotlin.toFlowable
import io.reactivex.rxjava3.schedulers.Schedulers

fun startPrediction(context: Context, predictionCallback: (IntArray) -> Unit): Flowable<Boolean> {
    val configurationHelper = ConfigurationHelper(context)
    val executeCount: Long = 1

    return collectData(hzToMillisecond(100), context)
        .recordData(configurationHelper.detectDelay)
        .doOnNext {
            backupData("before_normalized.json", it, context)
        }
        .normalized()
        .doOnNext {
            backupData("after_normalized.json", it, context)
        }
        .reshapeData(aiWindowSize, aiWindowSize)
        .obtainFeature(context)
        .predictOCSVMModel()
        .doOnNext {
            predictionCallback(it)
        }
        .analyzePredictionOCSVMModelResult(context)
        .doOnComplete {  }
        .take(executeCount)
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
}

private fun Flowable<Array<FloatArray>>.predictOCSVMModel(): Flowable<IntArray> {
    return map { list ->
        val python = Python.getInstance()

        val result = python.getModule("testSklearn").callAttr(
            "predict", list
        )

        result
            .asList()
            .toTypedArray()
            .map { it.toString().toInt() }
            .toIntArray()
    }
}

private fun Flowable<IntArray>.analyzePredictionOCSVMModelResult(context: Context): Flowable<Boolean> {
    val configurationHelper = ConfigurationHelper(context)
    return map {
        val size = it.size
        val successSize = it.filter { it != -1 }.size
        successSize.toFloat() / size > configurationHelper.threshold
    }
}