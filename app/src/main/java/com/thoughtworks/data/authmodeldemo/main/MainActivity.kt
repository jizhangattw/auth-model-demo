package com.thoughtworks.data.authmodeldemo.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.thoughtworks.data.authmodeldemo.R
import com.thoughtworks.data.authmodeldemo.spike.SensorDataActivity
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
    }

}
