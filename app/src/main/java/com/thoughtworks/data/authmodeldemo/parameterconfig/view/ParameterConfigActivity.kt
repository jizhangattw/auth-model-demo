package com.thoughtworks.data.authmodeldemo.parameterconfig.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import com.thoughtworks.data.authmodeldemo.R
import com.thoughtworks.data.authmodeldemo.main.view.MainActivity
import com.thoughtworks.data.authmodeldemo.parameterconfig.helper.ConfigurationHelper
import kotlinx.android.synthetic.main.activity_parameter_config.*


class ParameterConfigActivity : AppCompatActivity() {
    private lateinit var configurationHelper: ConfigurationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parameter_config)

        configurationHelper = ConfigurationHelper(this)

        setupEvent()
        loadCurrentParameter()
    }

    private fun setupEvent() {
        confirmConfigButton.setOnClickListener {
            saveCurrentParameter()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun saveCurrentParameter() {
        val trainDelayInputEdit = findViewById<View>(R.id.train_delay_input) as EditText
        configurationHelper.trainDelay = trainDelayInputEdit.text.toString().toInt()

        val nuInputEdit = findViewById<View>(R.id.nu_input) as EditText
        configurationHelper.nu = nuInputEdit.text.toString().toFloat()

        val gammaInputEdit = findViewById<View>(R.id.gamma_input) as EditText
        configurationHelper.gamma = gammaInputEdit.text.toString().toFloat()

        val detectDelayInputEdit = findViewById<View>(R.id.detect_delay_input) as EditText
        configurationHelper.detectDelay = detectDelayInputEdit.text.toString().toInt()

        val thresholdInputEdit = findViewById<View>(R.id.threshold_input) as EditText
        configurationHelper.threshold = thresholdInputEdit.text.toString().toFloat()

        val englishLanguageSwitch = findViewById<View>(R.id.englishLanguageSwitch) as Switch
        configurationHelper.englishLanguageToggle = englishLanguageSwitch.isChecked
    }

    private fun loadCurrentParameter() {
        val trainDelayInputEdit = findViewById<View>(R.id.train_delay_input) as EditText
        trainDelayInputEdit.setText(configurationHelper.trainDelay.toString())

        val nuInputEdit = findViewById<View>(R.id.nu_input) as EditText
        nuInputEdit.setText(configurationHelper.nu.toString())

        val gammaInputEdit = findViewById<View>(R.id.gamma_input) as EditText
        gammaInputEdit.setText(configurationHelper.gamma.toString())

        val detectDelayInputEdit = findViewById<View>(R.id.detect_delay_input) as EditText
        detectDelayInputEdit.setText(configurationHelper.detectDelay.toString())

        val thresholdInputEdit = findViewById<View>(R.id.threshold_input) as EditText
        thresholdInputEdit.setText(configurationHelper.threshold.toString())

        val englishLanguageSwitch = findViewById<View>(R.id.englishLanguageSwitch) as Switch
        englishLanguageSwitch.isChecked = configurationHelper.englishLanguageToggle
    }
}
