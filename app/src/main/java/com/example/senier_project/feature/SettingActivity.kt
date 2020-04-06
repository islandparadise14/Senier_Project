package com.example.senier_project.feature

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import androidx.lifecycle.Observer
import com.example.senier_project.R
import com.example.senier_project.global.Consts
import com.example.senier_project.koin.repository.SharedPrefRepository
import com.example.senier_project.utils.SystemVibratorHelper
import com.example.senier_project.utils.toastShort
import com.example.senier_project.utils.vibrateSystem
import kotlinx.android.synthetic.main.activity_setting.*
import org.koin.android.ext.android.inject

class SettingActivity : AppCompatActivity() {
    private val sharedPrefRepository: SharedPrefRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        initView()
    }

    private fun initView() {
        speedSeekBar.progress = sharedPrefRepository.getPrefsIntValue(Consts.SPEED_SETTING, 50)
        speedSeekBar.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                SystemVibratorHelper.getInstance().startVibrator(this@SettingActivity, progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        speedSeekBar.progressObserve.observe(this, Observer {
            sharedPrefRepository.writePrefs(Consts.SPEED_SETTING, it)
        })
    }
}
