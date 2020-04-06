package com.example.senier_project

import android.app.Application
import com.example.senier_project.koin.module.sharedPrefModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

class SenierApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())

        startKoin {
            androidLogger()
            androidContext(this@SenierApplication)
            modules(mutableListOf(sharedPrefModule))
        }
    }
}