package com.thoughtworks.data.authmodeldemo.spike.view

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.thoughtworks.data.authmodeldemo.R
import com.thoughtworks.data.authmodeldemo.main.view.MainActivity
import kotlinx.android.synthetic.main.activity_parameter_config.*


class ParameterConfigActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parameter_config)

        val sharedPref: SharedPreferences = getPreferences(Context.MODE_PRIVATE)

        setupEvent(sharedPref)
        loadCurrentParameter(sharedPref)
    }

    private fun setupEvent(sharedPref: SharedPreferences) {
        confirmConfigButton.setOnClickListener {
            saveCurrentParameter(sharedPref)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun saveCurrentParameter(sharedPref: SharedPreferences) {
        val editor = sharedPref.edit()

        val trainDelayInputEdit = findViewById<View>(R.id.train_delay_input) as EditText
        editor.putFloat("train_delay", trainDelayInputEdit.text.toString().toFloat())

        val nuInputEdit = findViewById<View>(R.id.nu_input) as EditText
        editor.putFloat("nu", nuInputEdit.text.toString().toFloat())

        val gammaInputEdit = findViewById<View>(R.id.gamma_input) as EditText
        editor.putFloat("gamma", gammaInputEdit.text.toString().toFloat())

        val detectDelayInputEdit = findViewById<View>(R.id.detect_delay_input) as EditText
        editor.putFloat("detect_delay", detectDelayInputEdit.text.toString().toFloat())

        val thresholdInputEdit = findViewById<View>(R.id.threshold_input) as EditText
        editor.putFloat("threshold", thresholdInputEdit.text.toString().toFloat())

        val languageInputEdit = findViewById<View>(R.id.language_input) as EditText
        editor.putString("language", languageInputEdit.text.toString())

        editor.apply()
    }

    private fun loadCurrentParameter(sharedPref: SharedPreferences) {
        val trainDelayInputEdit = findViewById<View>(R.id.train_delay_input) as EditText
        trainDelayInputEdit.setText(sharedPref.getFloat("train_delay", 90.0f).toString())

        val nuInputEdit = findViewById<View>(R.id.nu_input) as EditText
        nuInputEdit.setText(sharedPref.getFloat("nu", 0.092f).toString())

        val gammaInputEdit = findViewById<View>(R.id.gamma_input) as EditText
        gammaInputEdit.setText(sharedPref.getFloat("gamma", 1.151f).toString())

        val detectDelayInputEdit = findViewById<View>(R.id.detect_delay_input) as EditText
        detectDelayInputEdit.setText(sharedPref.getFloat("detect_delay", 10.0f).toString())

        val thresholdInputEdit = findViewById<View>(R.id.threshold_input) as EditText
        thresholdInputEdit.setText(sharedPref.getFloat("threshold", 10.0f).toString())

        val languageInputEdit = findViewById<View>(R.id.language_input) as EditText
        languageInputEdit.setText(sharedPref.getString("language", "Chinese"))
    }
}
