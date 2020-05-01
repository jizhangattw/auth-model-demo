package com.thoughtworks.data.authmodeldemo.spike.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.chaquo.python.Python
import com.thoughtworks.data.authmodeldemo.R

class PythonCallerActivity : AppCompatActivity() {

    private val python = Python.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_python_caller)

        val callAttr = python.getModule("hello").callAttr("hi", "Jian")
        Log.i(TAG, callAttr.toString())

        python.getModule("testSklearn").callAttr("testOcsvm")
    }

    companion object {
        val TAG = PythonCallerActivity::class.java.simpleName
    }
}
