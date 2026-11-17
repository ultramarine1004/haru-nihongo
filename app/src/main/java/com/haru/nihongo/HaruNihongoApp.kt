package com.haru.nihongo

import android.app.Application
import com.haru.nihongo.di.AppContainer

class HaruNihongoApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
