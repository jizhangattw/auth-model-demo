package com.thoughtworks.data.authmodeldemo.signup.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding3.widget.textChanges
import com.thoughtworks.data.authmodeldemo.R
import com.thoughtworks.data.authmodeldemo.signup.helper.TrainingHelper
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.util.concurrent.TimeUnit

class SignUpActivity : AppCompatActivity() {

    private val disposable = CompositeDisposable()
    private val trainingHelper = TrainingHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        title = getString(R.string.sign_up)
        disableSignUpButton()
        displayPreviewByRandom()
        startTrainingWhenFirstTyping()
        backToHomeViewWhenSignUp()
    }

    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
    }

    private fun updateProgress() {
        Observable.interval(0, 1, TimeUnit.SECONDS)
            .take(90)
            .map {
                it / 90f * 100
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                signUpButton.text = getString(R.string.go_on_for_typing, it.toInt())
            }
            .addTo(disposable)
    }

    private fun backToHomeViewWhenSignUp() {
        signUpButton.setOnClickListener {
            finish()
        }
    }

    private fun startTrainingWhenFirstTyping() {
        typingEditText.textChanges()
            .take(2)
            .filter {
                it.isNotEmpty()
            }
            .subscribe {
                updateProgress()
                trainingHelper.start(this)
                    .subscribe {
                        signUpButton.isEnabled = true
                        signUpButton.text = getString(R.string.typing_done)
                    }
                    .addTo(disposable)
            }
    }

    private fun displayPreviewByRandom() {
        val random = (0..3).random()
        previewTextView.text = resources.getStringArray(R.array.preview_text)[random]
    }

    private fun disableSignUpButton() {
        signUpButton.text = getString(R.string.waiting_for_typing_start)
        signUpButton.isEnabled = false
    }

    companion object {
        val TAG = SignUpActivity::class.java.simpleName
    }
}
