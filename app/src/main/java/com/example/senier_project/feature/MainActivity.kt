package com.example.senier_project.feature

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.senier_project.R
import com.example.senier_project.global.Consts
import com.example.senier_project.koin.repository.SharedPrefRepository
import com.example.senier_project.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private val sharedPrefRepository: SharedPrefRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    private fun initView() {
        startMos.setOnClickListener {
            startConvertToMosVibrator()
        }
        setting.setOnClickListener {
            navigateToSetting()
        }
    }

    private fun startConvertToMosVibrator() {
        val jasoList = strokes(
            editText.text.toString(),
            false
        )
        Timber.d("분해 : $jasoList")
        Thread(Runnable {
            jasoList.map {
                Timber.d("문자당 모스신호: ${it.koToMosNumber()}")
                it.koToMosNumber()
            }.join().forEach {
                it.mosToVibrate(this, sharedPrefRepository.getPrefsIntValue(Consts.SPEED_SETTING, 50))
            }
        }).start()
    }

    private fun navigateToSetting() = startActivity(Intent(this, SettingActivity::class.java))
        .run { this@MainActivity.vibrateSystem() }
}
