package com.haru.nihongo.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.haru.nihongo.data.repository.LearningRepository
import com.haru.nihongo.di.AppContainer
import com.haru.nihongo.domain.model.Dialogue
import com.haru.nihongo.domain.model.Mode
import com.haru.nihongo.domain.model.Progress
import com.haru.nihongo.domain.model.Settings
import com.haru.nihongo.util.DateKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodayViewModel(
    private val repository: LearningRepository,
) : ViewModel() {

    val settings: StateFlow<Settings> = repository.settings.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        Settings(),
    )

    val recent: StateFlow<List<Progress>> = repository.recentProgress.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )

    /** 하루 개수 제한 없이 선택한 모드의 모든 대화를 순서대로 노출한다. */
    val dialogues: StateFlow<List<Dialogue>> = settings.map { current ->
        repository.dialogues()
            .filter { it.mode == current.mode }
            .sortedBy { it.order }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )

    private val _listened = MutableStateFlow(false)
    val listened: StateFlow<Boolean> = _listened.asStateFlow()

    private val _recorded = MutableStateFlow(false)
    val recorded: StateFlow<Boolean> = _recorded.asStateFlow()

    fun setMode(mode: Mode) {
        viewModelScope.launch { repository.setMode(mode) }
    }

    fun markListened() {
        _listened.value = true
    }

    fun setRecorded(value: Boolean) {
        _recorded.value = value
    }

    /** 저장이 끝난 뒤 화면에서 앱을 종료할 수 있도록 suspend로 노출한다. */
    suspend fun complete() {
        repository.complete(
            date = DateKey.today(),
            count = dialogues.value.size.coerceAtLeast(1),
            listened = _listened.value,
            recorded = _recorded.value,
        )
    }
}

class TodayViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TodayViewModel(container.repository) as T
    }
}
