package com.thoughtworks.data.authmodeldemo.parameterconfig.helper

import android.content.Context

class ConfigurationHelper(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("parameter_configuration", Context.MODE_PRIVATE)

    var trainDelay: Float
        get() = sharedPreferences.getFloat(TRAIN_DELAY, 90.0f)
        set(value) {
            sharedPreferences.edit()
                .putFloat(TRAIN_DELAY, value)
                .apply()
        }

    var nu: Float
        get() = sharedPreferences.getFloat(NU, 0.092f)
        set(value) {
            sharedPreferences.edit()
                .putFloat(NU, value)
                .apply()
        }

    var gamma: Float
        get() = sharedPreferences.getFloat(GAMMA, 1.151f)
        set(value) {
            sharedPreferences.edit()
                .putFloat(GAMMA, value)
                .apply()
        }

    var detectDelay: Float
        get() = sharedPreferences.getFloat(DETECT_DELAY, 10.0f)
        set(value) {
            sharedPreferences.edit()
                .putFloat(DETECT_DELAY, value)
                .apply()
        }

    var threshold: Float
        get() = sharedPreferences.getFloat(THRESHOLD, 10.0f)
        set(value) {
            sharedPreferences.edit()
                .putFloat(THRESHOLD, value)
                .apply()
        }

    var englishLanguageToggle: Boolean
        get() = sharedPreferences.getBoolean(ENGLISH_LANGUAGE_TOGGLE, false)
        set(value) {
            sharedPreferences.edit()
                .putBoolean(ENGLISH_LANGUAGE_TOGGLE, value)
                .apply()
        }

    companion object {
        const val TRAIN_DELAY = "train_delay"
        const val NU = "nu"
        const val GAMMA = "gamma"
        const val DETECT_DELAY = "detect_delay"
        const val THRESHOLD = "threshold"
        const val ENGLISH_LANGUAGE_TOGGLE = "english_language_toggle"
    }
}