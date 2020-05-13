package com.thoughtworks.data.authmodeldemo.login.helper

import android.content.Context
import com.chaquo.python.Python
import com.google.gson.Gson
import com.thoughtworks.data.authmodeldemo.common.helper.backupData
import com.thoughtworks.data.authmodeldemo.common.helper.collectData
import com.thoughtworks.data.authmodeldemo.common.helper.normalized
import com.thoughtworks.data.authmodeldemo.common.helper.obtainFeature
import com.thoughtworks.data.authmodeldemo.common.helper.recordData
import com.thoughtworks.data.authmodeldemo.common.helper.reshapeData
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.schedulers.Schedulers

fun startPrediction(context: Context): Flowable<Boolean> {
    return collectData(10, context)
        .recordData(10)
        .doOnNext {
            backupData("before_normalized.json", Gson().toJson(it), context)
        }
        .normalized()
        .map {
            backupData("after_normalized.json", Gson().toJson(it), context)
            it
        }
        .reshapeData(25, 25)
        .obtainFeature(context)
        .predictOCSVMModel()
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
}

private fun Flowable<Array<FloatArray>>.predictOCSVMModel(): Flowable<Boolean> {
    return map { list ->
        val python = Python.getInstance()

        val result = python.getModule("testSklearn").callAttr(
            "predict", list
        )

        result
            .asList()
            .toTypedArray()
            .map { it.toString().toInt() }
            .filter {
                it.toInt() != -1
            }
            .average() > 0.5
    }
}