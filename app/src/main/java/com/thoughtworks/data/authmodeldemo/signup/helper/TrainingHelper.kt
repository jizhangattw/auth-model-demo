package com.thoughtworks.data.authmodeldemo.signup.helper

import android.content.Context
import com.chaquo.python.Python
import com.google.gson.Gson
import com.thoughtworks.data.authmodeldemo.common.helper.backupData
import com.thoughtworks.data.authmodeldemo.common.helper.collectData
import com.thoughtworks.data.authmodeldemo.common.helper.normalized
import com.thoughtworks.data.authmodeldemo.common.helper.obtainFeature
import com.thoughtworks.data.authmodeldemo.common.helper.recordData
import com.thoughtworks.data.authmodeldemo.common.helper.reshapeData
import com.thoughtworks.data.authmodeldemo.parameterconfig.helper.ConfigurationHelper
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.schedulers.Schedulers

fun startTraining(context: Context): Flowable<Boolean> {
    val configurationHelper = ConfigurationHelper(context)
    return collectData(10, context)
        .recordData(90)
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
        .trainOCSVMModel()
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
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