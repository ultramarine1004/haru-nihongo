package com.haru.nihongo.data.repository

import com.haru.nihongo.data.content.ContentDataSource
import com.haru.nihongo.data.local.ProgressDao
import com.haru.nihongo.data.local.ProgressEntity
import com.haru.nihongo.data.preferences.SettingsStore
import com.haru.nihongo.domain.model.Dialogue
import com.haru.nihongo.domain.model.Mode
import com.haru.nihongo.domain.model.Progress
import com.haru.nihongo.domain.model.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LearningRepository(
    private val content: ContentDataSource,
    private val settingsStore: SettingsStore,
    private val progressDao: ProgressDao,
) {
    val settings: Flow<Settings> = settingsStore.settings

    val recentProgress: Flow<List<Progress>> = progressDao.recent().map { list ->
        list.map { entity ->
            Progress(
                date = entity.date,
                completedCount = entity.completedCount,
                listened = entity.listened,
                recorded = entity.recorded,
            )
        }
    }

    fun dialogues(): List<Dialogue> = content.dialogues()

    suspend fun complete(date: String, count: Int, listened: Boolean, recorded: Boolean) {
        progressDao.upsert(
            ProgressEntity(
                date = date,
                completedCount = count,
                listened = listened,
                recorded = recorded,
            ),
        )
    }

    suspend fun setMode(mode: Mode) = settingsStore.setMode(mode)

    suspend fun setDailyCount(count: Int) = settingsStore.setDailyCount(count)

    suspend fun setFontSize(size: Int) = settingsStore.setFontSize(size)

    suspend fun setAlarm(enabled: Boolean) = settingsStore.setAlarm(enabled)

    suspend fun finishOnboarding(mode: Mode, count: Int) = settingsStore.finishOnboarding(mode, count)

    suspend fun ensureFirstOpenDate() = settingsStore.ensureFirstOpenDate()
}
