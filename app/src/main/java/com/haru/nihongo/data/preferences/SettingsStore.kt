package com.haru.nihongo.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.haru.nihongo.domain.model.Mode
import com.haru.nihongo.domain.model.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore("haru_settings")

private val KEY_MODE = stringPreferencesKey("mode")
private val KEY_FIRST_OPEN_DATE = stringPreferencesKey("first_open_date")
private val KEY_DAILY_COUNT = intPreferencesKey("daily_count")
private val KEY_FONT_SIZE = intPreferencesKey("font_size")
private val KEY_ALARM = booleanPreferencesKey("alarm")
private val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")

class SettingsStore(private val context: Context) {

    val settings: Flow<Settings> = context.settingsDataStore.data.map { prefs ->
        Settings(
            mode = Mode.fromKey(prefs[KEY_MODE] ?: Mode.DEFAULT.name),
            firstOpenDate = prefs[KEY_FIRST_OPEN_DATE] ?: "",
            dailyCount = prefs[KEY_DAILY_COUNT] ?: 1,
            // 후리가나가 잘 보이도록 이전 저장값은 최소 18sp로 올려서 불러온다.
            fontSize = (prefs[KEY_FONT_SIZE] ?: 22).coerceIn(18, 28),
            alarm = prefs[KEY_ALARM] ?: true,
            onboardingDone = prefs[KEY_ONBOARDING_DONE] ?: false,
        )
    }

    suspend fun setMode(mode: Mode) = edit { it[KEY_MODE] = mode.name }

    suspend fun setDailyCount(count: Int) = edit { it[KEY_DAILY_COUNT] = count.coerceIn(1, 5) }

    suspend fun setFontSize(size: Int) = edit { it[KEY_FONT_SIZE] = size.coerceIn(18, 28) }

    suspend fun setAlarm(enabled: Boolean) = edit { it[KEY_ALARM] = enabled }

    suspend fun finishOnboarding(mode: Mode, count: Int) {
        context.settingsDataStore.edit { prefs ->
            prefs[KEY_MODE] = mode.name
            prefs[KEY_FIRST_OPEN_DATE] = prefs[KEY_FIRST_OPEN_DATE] ?: java.time.LocalDate.now().toString()
            prefs[KEY_DAILY_COUNT] = count.coerceIn(1, 5)
            prefs[KEY_ONBOARDING_DONE] = true
        }
    }

    /** 이전 버전에서 설치했다가 온보딩만 끝낸 경우를 위한 보완. */
    suspend fun ensureFirstOpenDate() {
        context.settingsDataStore.edit { prefs ->
            if (prefs[KEY_FIRST_OPEN_DATE].isNullOrBlank()) {
                prefs[KEY_FIRST_OPEN_DATE] = java.time.LocalDate.now().toString()
            }
        }
    }

    private suspend fun edit(transform: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.settingsDataStore.edit(transform)
    }
}
