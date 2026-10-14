package com.vkasurinen.knock

import android.app.Application
import com.vkasurinen.knock.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class KnockApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@KnockApplication)
            androidLogger()
        }
    }
}