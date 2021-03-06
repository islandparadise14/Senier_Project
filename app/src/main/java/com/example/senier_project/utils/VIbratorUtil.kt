package com.example.senier_project.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

fun Context.vibrateShort(speed: Int) {
    val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(200, 100))
    } else {
        vibrator.vibrate(200)
    }
    Thread.sleep(400 + (300 - speed.toLong() * 3))
}

fun Context.vibrateLong(speed: Int) {
    val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(500, 80))
    } else {
        vibrator.vibrate(500)
    }
    Thread.sleep(700 + (300 - speed.toLong() * 3))
}

fun vibrateBlank() = Thread.sleep(2000)

fun Context.vibrateSystem() {
    val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(50, 40))
    } else {
        vibrator.vibrate(50)
    }
    Thread.sleep(100)
}