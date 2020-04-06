package com.example.senier_project.utils

import android.content.Context
import android.widget.Toast

fun Context.toastShort(string: String) = Toast.makeText(this, string, Toast.LENGTH_SHORT).show()

fun Context.toastLong(string: String) = Toast.makeText(this, string, Toast.LENGTH_LONG).show()