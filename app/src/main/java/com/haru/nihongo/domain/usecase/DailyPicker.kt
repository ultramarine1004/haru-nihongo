package com.haru.nihongo.domain.usecase

import com.haru.nihongo.domain.model.Dialogue
import com.haru.nihongo.domain.model.Mode

/** 설정에서 허용하는 하루 대화 개수 범위. */
private const val MIN_COUNT = 1
private const val MAX_COUNT = 5

fun coerceCount(count: Int): Int = count.coerceIn(MIN_COUNT, MAX_COUNT)

/**
 * 선택한 모드의 표현 풀에서 오늘의 대화를 뽑는다.
 *
 * - 기본 1개, 설정에 따라 최대 5개까지 연속으로 뽑는다.
 * - 날짜가 바뀌면 다음 순번부터 시작한다 (풀의 크기로 나눈 나머지).
 * - 해당 모드에 콘텐츠가 없으면 빈 목록을 반환한다.
 */
fun pickDialogues(
    dialogues: List<Dialogue>,
    mode: Mode,
    count: Int,
    dayIndex: Int,
): List<Dialogue> {
    val pool = dialogues.filter { it.mode == mode }.sortedBy { it.order }
    if (pool.isEmpty()) return emptyList()

    val size = coerceCount(count)
    val start = dayIndex.coerceAtLeast(0)
    return List(size) { offset -> pool[(start + offset) % pool.size] }
}
