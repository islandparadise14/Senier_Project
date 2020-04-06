package com.example.senier_project.utils

fun List<String>.join(): String = StringBuffer().apply {
    this@join.forEach { append(it) }
}.toString()