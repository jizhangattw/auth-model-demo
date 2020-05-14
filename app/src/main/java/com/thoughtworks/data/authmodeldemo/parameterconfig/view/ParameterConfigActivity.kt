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
        configurationHelper.trainDelay = trainDelayInputEditText.text.toString().toInt()
        configurationHelper.nu = nuInputEditText.text.toString().toFloat()
        configurationHelper.gamma = gammaInputEditText.text.toString().toFloat()
        configurationHelper.detectDelay = detectDelayInputEditText.text.toString().toInt()
        configurationHelper.threshold = thresholdInputEditText.text.toString().toFloat()
        configurationHelper.englishLanguageToggle = englishLanguageSwitch.isChecked
        configurationHelper.saveSignUpDataToggle = saveSignUpDataSwitch.isChecked
        configurationHelper.saveSignUpDataTag = saveSignUpDataTag.text.toString()
    }

    private fun loadCurrentParameter() {
        trainDelayInputEditText.setText(configurationHelper.trainDelay.toString())
        nuInputEditText.setText(configurationHelper.nu.toString())
        gammaInputEditText.setText(configurationHelper.gamma.toString())
        detectDelayInputEditText.setText(configurationHelper.detectDelay.toString())
        thresholdInputEditText.setText(configurationHelper.threshold.toString())
        englishLanguageSwitch.isChecked = configurationHelper.englishLanguageToggle
        saveSignUpDataSwitch.isChecked = configurationHelper.saveSignUpDataToggle
        saveSignUpDataTag.setText(configurationHelper.saveSignUpDataTag)
    }
}
