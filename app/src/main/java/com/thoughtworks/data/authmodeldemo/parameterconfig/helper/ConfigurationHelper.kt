package com.thoughtworks.data.authmodeldemo.parameterconfig.helper

import android.content.Context

class ConfigurationHelper(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("parameter_configuration", Context.MODE_PRIVATE)

    var trainDelay: Int
        get() = sharedPreferences.getInt(TRAIN_DELAY, 90)
        set(value) {
            sharedPreferences.edit()
                .putInt(TRAIN_DELAY, value)
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

    var detectDelay: Int
        get() = sharedPreferences.getInt(DETECT_DELAY, 10)
        set(value) {
            sharedPreferences.edit()
                .putInt(DETECT_DELAY, value)
                .apply()
        }

    var threshold: Float
        get() = sharedPreferences.getFloat(THRESHOLD, 0.5f)
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

    var saveSignUpDataToggle: Boolean
        get() = sharedPreferences.getBoolean(SAVE_SIGN_UP_DATA_TOGGLE, false)
        set(value) {
            sharedPreferences.edit()
                .putBoolean(SAVE_SIGN_UP_DATA_TOGGLE, value)
                .apply()
        }

    var saveSignUpDataTag: String
        get() = sharedPreferences.getString(SAVE_SIGN_UP_DATA_TAG, "HouXinJian") ?: ""
        set(value) {
            sharedPreferences.edit()
                .putString(SAVE_SIGN_UP_DATA_TAG, value)
                .apply()
        }

    companion object {
        const val TRAIN_DELAY = "train_delay"
        const val NU = "nu"
        const val GAMMA = "gamma"
        const val DETECT_DELAY = "detect_delay"
        const val THRESHOLD = "threshold"
        const val ENGLISH_LANGUAGE_TOGGLE = "english_language_toggle"
        const val SAVE_SIGN_UP_DATA_TOGGLE = "save_sign_up_data_toggle"
        const val SAVE_SIGN_UP_DATA_TAG = "save_sign_up_data_tag"
    }
}