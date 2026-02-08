package com.bookiibookii.bookiibookii.common

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ComErrorTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startActivity(
            ComErrorActivity.newIntent(
                this,
                ComErrorActivity.TYPE_NETWORK_ERROR
            )
        )

        finish()
    }
}