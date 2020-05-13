package com.thoughtworks.data.authmodeldemo.common.helper

import android.content.Context
import android.util.Log
import java.io.IOException
import java.io.OutputStreamWriter

fun backupData(
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
        Log.e("Exception", "File write failed: $e")
    }
}