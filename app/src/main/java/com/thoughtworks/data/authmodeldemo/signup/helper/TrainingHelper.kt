package com.thoughtworks.data.authmodeldemo.signup.helper

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
import io.reactivex.rxjava3.schedulers.Schedulers

fun startTraining(context: Context): Flowable<Boolean> {
    val configurationHelper = ConfigurationHelper(context)
    val executeCount: Long = 1

    return collectData(hzToMillisecond(100), context)
        .recordData(configurationHelper.trainDelay)
        .doOnNext {
            backupData("before_normalized.json", Gson().toJson(it), context)
        }
        .normalized()
        .doOnNext {
            backupData("after_normalized.json", Gson().toJson(it), context)
        }
        .reshapeData(aiWindowSize, aiWindowSize)
        .obtainFeature(context)
        .trainOCSVMModel(context)
        .take(executeCount)
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
}

private fun Flowable<Array<FloatArray>>.trainOCSVMModel(context: Context): Flowable<Boolean> {
    val configurationHelper = ConfigurationHelper(context)

    return map {
        val python = Python.getInstance()
        try {
            python.getModule("testSklearn").callAttr(
                "train_and_save_model", it, configurationHelper.nu, configurationHelper.gamma
            )
            true
        } catch (throwable: Throwable) {
            false
        }
    }
}