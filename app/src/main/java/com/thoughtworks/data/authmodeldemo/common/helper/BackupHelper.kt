package com.thoughtworks.data.authmodeldemo.common.helper

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.thoughtworks.data.authmodeldemo.parameterconfig.helper.ConfigurationHelper
import java.io.IOException
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

fun backupData(
    name: String,
    data: Any,
    context: Context
) {
    val configurationHelper = ConfigurationHelper(context)

    if (!configurationHelper.saveSignUpDataToggle) {
        return
    }

    val time = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(
        Date()
    )
    val userTag = configurationHelper.saveSignUpDataTag

    val json = Gson().toJson(data)
    try {
        val outputStreamWriter = OutputStreamWriter(
            context.openFileOutput(
                "${userTag}_${time}_${name}",
                Context.MODE_PRIVATE
            )
        )
        outputStreamWriter.write(json)
        outputStreamWriter.close()
    } catch (e: IOException) {
        Log.e("Exception", "File write failed: $e")
    }
}