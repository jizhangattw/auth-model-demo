package com.thoughtworks.data.authmodeldemo.login.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding3.widget.textChanges
import com.thoughtworks.data.authmodeldemo.R
import com.thoughtworks.data.authmodeldemo.login.helper.startPrediction
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_log_in.*
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
                startPrediction(this)
                    .subscribe {
                        logInButton.isEnabled = it
                        logInButton.text = if (it) getString(R.string.log_in_allow) else getString(R.string.log_in_disable)
                    }
                    .addTo(disposable)
            }
    }

    private fun updateProgress() {
        Observable.interval(0, 1, TimeUnit.SECONDS)
            .take(10)
            .map {
                it / 10f * 100
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
        previewTextView.text = getString(R.string.preview_text)
    }

    private fun disableLogInButton() {
        logInButton.text = getString(R.string.waiting_for_typing_start)
        logInButton.isEnabled = false
    }
}
