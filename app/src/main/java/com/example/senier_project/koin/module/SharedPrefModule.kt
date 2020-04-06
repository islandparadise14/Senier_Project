package com.example.senier_project.koin.module

import com.example.senier_project.koin.repository.SharedPrefRepository
import com.example.senier_project.koin.repositoryimpl.SharedPrefRepositoryImpl
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val sharedPrefModule = module {
    single<SharedPrefRepository> { SharedPrefRepositoryImpl(androidApplication()) }
}