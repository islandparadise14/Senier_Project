package com.example.senier_project.feature

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.senier_project.R
import timber.log.Timber

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed(SplashHandler(), 1000)
    }

    inner class SplashHandler : Runnable {
        override fun run() {
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            overridePendingTransition(R.anim.hold, R.anim.fade_out)
            finish()
        }
    }
}
