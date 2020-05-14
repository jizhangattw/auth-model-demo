package com.thoughtworks.data.authmodeldemo.login.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding3.widget.textChanges
import com.thoughtworks.data.authmodeldemo.R
import com.thoughtworks.data.authmodeldemo.login.helper.startPrediction
import com.thoughtworks.data.authmodeldemo.parameterconfig.helper.ConfigurationHelper
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_log_in.*
import kotlinx.android.synthetic.main.activity_log_in.previewTextView
import kotlinx.android.synthetic.main.activity_log_in.typingEditText
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.util.concurrent.TimeUnit

class LogInActivity : AppCompatActivity() {

    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)
        title = getString(R.string.log_in)
        disableLogInButton()
        displayPreviewByRandom()
        startPredictionWhenFirstTyping()
        backToHomeViewWhenSignUp()
    }

    private fun startPredictionWhenFirstTyping() {
        typingEditText.textChanges()
            .take(2)
            .filter {
                it.isNotEmpty()
            }
            .subscribe {
                updateProgress()
                startPrediction(this, { data -> displayResultData(data)})
                    .subscribe {
                        logInButton.isEnabled = it
                        logInButton.text = if (it) getString(R.string.log_in_allow) else getString(R.string.log_in_disable)
                    }
                    .addTo(disposable)
            }
    }

    private fun displayResultData(data: IntArray) {
        runOnUiThread {
            val failureCount = data.asList().filter { it == -1 }.count()
            val successCount = data.asList().filter { it == 1 }.count()
            val rawDataString = data.joinToString(",", "[", "]")
            predictionResultDataTextView.text = "$rawDataString\n-1: $failureCount\n1:$successCount"
        }
    }

    private fun updateProgress() {
        val detectDelay = ConfigurationHelper(this).detectDelay
        Observable.interval(0, 1, TimeUnit.SECONDS)
            .take(detectDelay.toLong())
            .map {
                it / detectDelay.toFloat() * 100
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                logInButton.text = getString(R.string.go_on_for_typing, it.toInt())
            }
            .addTo(disposable)
    }

    private fun backToHomeViewWhenSignUp() {
        logInButton.setOnClickListener {
            finish()
        }
    }

    private fun displayPreviewByRandom() {
        val englishLanguageToggle = ConfigurationHelper(this).englishLanguageToggle
        previewTextView.text =
            if (englishLanguageToggle) getString(R.string.preview_text_english) else getString(R.string.preview_text)
    }

    private fun disableLogInButton() {
        logInButton.text = getString(R.string.waiting_for_typing_start)
        logInButton.isEnabled = false
    }
}
