package com.haru.nihongo.domain

import com.haru.nihongo.domain.model.Dialogue
import com.haru.nihongo.domain.model.Mode
import com.haru.nihongo.domain.model.Utterance
import com.haru.nihongo.domain.usecase.coerceCount
import com.haru.nihongo.domain.usecase.pickDialogues
import com.haru.nihongo.util.DateKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyPickerTest {

    private fun dialogue(id: String, mode: Mode, order: Int) = Dialogue(
        id = id,
        mode = mode,
        situation = "테스트 상황",
        order = order,
        utterances = listOf(
            Utterance(speaker = "A", ja = "こんにちは。", ko = "안녕하세요.", romaji = "곤니치와", polite = true),
        ),
    )

    private val sample = listOf(
        dialogue("g-1", Mode.GENERAL, 1),
        dialogue("g-2", Mode.GENERAL, 2),
        dialogue("g-3", Mode.GENERAL, 3),
        dialogue("s-1", Mode.SHOP, 1),
    )

    @Test
    fun `기본 모드는 일반인이다`() {
        assertEquals(Mode.GENERAL, Mode.DEFAULT)
    }

    @Test
    fun `하루 개수는 1에서 5로 제한된다`() {
        assertEquals(1, coerceCount(0))
        assertEquals(1, coerceCount(-3))
        assertEquals(3, coerceCount(3))
        assertEquals(5, coerceCount(99))
    }

    @Test
    fun `기본 설정이면 일반인 대화 한 개가 선택된다`() {
        val picked = pickDialogues(sample, Mode.DEFAULT, 1, 0)
        assertEquals(1, picked.size)
        assertEquals("g-1", picked.first().id)
    }

    @Test
    fun `개수를 3으로 하면 서로 다른 세 개가 선택된다`() {
        val picked = pickDialogues(sample, Mode.GENERAL, 3, 0)
        assertEquals(3, picked.size)
        assertEquals(3, picked.map { it.id }.toSet().size)
    }

    @Test
    fun `날짜 인덱스가 바뀌면 다음 대화부터 시작한다`() {
        val first = pickDialogues(sample, Mode.GENERAL, 1, 0).first().id
        val second = pickDialogues(sample, Mode.GENERAL, 1, 1).first().id
        assertTrue(first != second)
    }

    @Test
    fun `첫 실행일 기준으로 날짜 경과일을 계산한다`() {
        assertEquals(0, DateKey.daysSince("2026-11-17", "2026-11-17"))
        assertEquals(3, DateKey.daysSince("2026-11-17", "2026-11-20"))
        assertEquals(0, DateKey.daysSince("", "2026-11-20"))
        assertEquals(0, DateKey.daysSince("2026-11-25", "2026-11-20"))
    }

    @Test
    fun `선택한 모드에 콘텐츠가 없으면 빈 목록이다`() {
        val onlyGeneral = sample.filter { it.mode == Mode.GENERAL }
        assertTrue(pickDialogues(onlyGeneral, Mode.WORK, 1, 0).isEmpty())
    }
}
