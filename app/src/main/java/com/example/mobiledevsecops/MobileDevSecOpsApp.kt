package com.example.mobiledevsecops

import android.app.Application
import com.example.mobiledevsecops.di.appModule
import com.example.mobiledevsecops.di.networkModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MobileDevSecOpsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MobileDevSecOpsApp)
            modules(appModule, networkModule)
        }
    }
}
