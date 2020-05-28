package com.example.senier_project.utils

import com.example.senier_project.global.SignWord

fun List<String>.join(): String = StringBuffer().apply {
    this@join.forEach { append(it) }
}.toString()

fun String.replaceSign(): String {
    var result = this
    repeat(SignWord.list.size) {
        result = result.replace(SignWord.list[it], " ")
    }
    return result
}