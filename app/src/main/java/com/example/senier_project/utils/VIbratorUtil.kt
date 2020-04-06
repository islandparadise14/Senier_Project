package com.example.senier_project.utils

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator

fun Context.vibrateShort(speed: Int) {
    val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    vibrator.vibrate(VibrationEffect.createOneShot(100, 100))
    Thread.sleep(300 + (300 - speed.toLong() * 3))
}

fun Context.vibrateLong(speed: Int) {
    val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    vibrator.vibrate(VibrationEffect.createOneShot(300, 80))
    Thread.sleep(500 + (300 - speed.toLong() * 3))
}

fun vibrateBlank() = Thread.sleep(1000)

fun Context.vibrateSystem() {
    val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    vibrator.vibrate(VibrationEffect.createOneShot(50, 40))
    Thread.sleep(100)
}