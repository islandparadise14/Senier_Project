package com.example.senier_project.utils

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator

fun Context.vibrateShort(speed: Int) {
    val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    vibrator.vibrate(VibrationEffect.createOneShot(200, 100))
    Thread.sleep(400 + (300 - speed.toLong() * 3))
}

fun Context.vibrateLong(speed: Int) {
    val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    vibrator.vibrate(VibrationEffect.createOneShot(500, 80))
    Thread.sleep(700 + (300 - speed.toLong() * 3))
}

fun vibrateBlank() = Thread.sleep(2000)

fun Context.vibrateSystem() {
    val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    vibrator.vibrate(VibrationEffect.createOneShot(50, 40))
    Thread.sleep(100)
}