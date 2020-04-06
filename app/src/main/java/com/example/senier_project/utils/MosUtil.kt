package com.example.senier_project.utils

import android.content.Context

fun Char.koToMosNumber(): String =
    when (this) {
        'ㄱ' -> { "1211" }
        'ㄴ' -> { "1121" }
        'ㄷ' -> { "2111" }
        'ㄹ' -> { "1112" }
        'ㅁ' -> { "22" }
        'ㅂ' -> { "122" }
        'ㅅ' -> { "221" }
        'ㅇ' -> { "212" }
        'ㅈ' -> { "1221" }
        'ㅊ' -> { "2121" }
        'ㅋ' -> { "2112" }
        'ㅌ' -> { "2211" }
        'ㅍ' -> { "222" }
        'ㅎ' -> { "1222" }
        'ㅏ' -> { "1" }
        'ㅑ' -> { "11" }
        'ㅓ' -> { "2" }
        'ㅕ' -> { "111" }
        'ㅗ' -> { "12" }
        'ㅛ' -> { "21" }
        'ㅜ' -> { "1111" }
        'ㅠ' -> { "121" }
        'ㅡ' -> { "211" }
        'ㅣ' -> { "112" }
        'ㅔ' -> { "2122" }
        'ㅐ' -> { "2212" }
        else -> { "0" }
    }

fun Char.mosToVibrate(context: Context, speed: Int) =
    when (this) {
        '1' -> { context.vibrateShort(speed) }
        '2' -> { context.vibrateLong(speed) }
        else -> {
            vibrateBlank()
        }
    }