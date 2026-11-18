package com.haru.nihongo.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.haru.nihongo.data.repository.LearningRepository
import com.haru.nihongo.di.AppContainer
import com.haru.nihongo.domain.model.Lesson
import com.haru.nihongo.domain.model.Progress
import com.haru.nihongo.domain.model.Settings
import com.haru.nihongo.util.DateKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

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

    /** NHK 공식 강의 메타데이터를 강의 번호 순으로 노출한다. */
    val lessons: StateFlow<List<Lesson>> = MutableStateFlow(repository.lessons()).asStateFlow()

    private val _listened = MutableStateFlow(false)
    val listened: StateFlow<Boolean> = _listened.asStateFlow()

    private val _recorded = MutableStateFlow(false)
    val recorded: StateFlow<Boolean> = _recorded.asStateFlow()

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
            count = lessons.value.size.coerceAtLeast(1),
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
