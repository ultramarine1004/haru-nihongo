package com.haru.nihongo.di

import android.content.Context
import androidx.room.Room
import com.haru.nihongo.data.content.ContentDataSource
import com.haru.nihongo.data.local.AppDatabase
import com.haru.nihongo.data.preferences.SettingsStore
import com.haru.nihongo.data.repository.LearningRepository

/** Hilt 대신 사용하는 수동 DI 컨테이너. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val contentDataSource = ContentDataSource(appContext)
    private val settingsStore = SettingsStore(appContext)
    private val database = Room.databaseBuilder(
        appContext,
        AppDatabase::class.java,
        "haru-nihongo.db",
    )
        .fallbackToDestructiveMigration()
        .build()

    val repository = LearningRepository(
        content = contentDataSource,
        settingsStore = settingsStore,
        progressDao = database.progressDao(),
    )
}
