package com.thoughtworks.data.authmodeldemo.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.thoughtworks.data.authmodeldemo.R
import com.thoughtworks.data.authmodeldemo.spike.view.GenerateFeatureActivity
import com.thoughtworks.data.authmodeldemo.spike.view.GetFeatureActivity
import com.thoughtworks.data.authmodeldemo.spike.view.ParameterConfigActivity
import com.thoughtworks.data.authmodeldemo.spike.view.PythonCallerActivity
import com.thoughtworks.data.authmodeldemo.spike.view.ReshapeDataActivity
import com.thoughtworks.data.authmodeldemo.spike.view.SensorDataActivity
import com.thoughtworks.data.authmodeldemo.spike.view.TrainOCSVMActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupEvent()

        title = getString(R.string.app_name)
    }

    private fun setupEvent() {
        spikeSensorDataCollectionButton.setOnClickListener {
            val intent = Intent(this, SensorDataActivity::class.java)
            startActivity(intent)
        }

        featureButton.setOnClickListener {
            val intent = Intent(this, GenerateFeatureActivity::class.java)
            startActivity(intent)
        }

        callPythonButton.setOnClickListener {
            val intent = Intent(this, PythonCallerActivity::class.java)
            startActivity(intent)
        }

        reshapeDataButton.setOnClickListener {
            val intent = Intent(this, ReshapeDataActivity::class.java)
            startActivity(intent)
        }

        getFeatureButton.setOnClickListener {
            val intent = Intent(this, GetFeatureActivity::class.java)
            startActivity(intent)
        }

        getParameterButton.setOnClickListener {
            val intent = Intent(this, ParameterConfigActivity::class.java)
            startActivity(intent)
        }

        trainOCSVMButton.setOnClickListener {
            val intent = Intent(this, TrainOCSVMActivity::class.java)
            startActivity(intent)
        }
    }

}
