package com.thoughtworks.data.authmodeldemo.signup.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.thoughtworks.data.authmodeldemo.R
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        title = getString(R.string.sign_up)

        signUpButton.setOnClickListener {
            finish()
        }
    }
}
