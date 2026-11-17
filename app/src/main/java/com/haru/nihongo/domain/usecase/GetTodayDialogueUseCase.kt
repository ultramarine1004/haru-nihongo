package com.haru.nihongo.domain.usecase

import com.haru.nihongo.domain.model.Dialogue
import com.haru.nihongo.domain.model.Mode

/**
 * 순수 선택 로직만 감싼 UseCase.
 * Repository나 Android 프레임워크에 의존하지 않아 JVM 테스트가 가능하다.
 */
class GetTodayDialogueUseCase {
    operator fun invoke(
        dialogues: List<Dialogue>,
        mode: Mode,
        count: Int,
        dayIndex: Int,
    ): List<Dialogue> = pickDialogues(dialogues, mode, count, dayIndex)
}
