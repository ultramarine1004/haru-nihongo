package com.haru.nihongo.ui.common

import org.junit.Assert.assertEquals
import org.junit.Test

class KatakanaRangeTest {

    @Test
    fun `가타카나 연속 구간을 찾는다`() {
        assertEquals(
            listOf(0..2),
            katakanaRanges("ハウス"),
        )
        assertEquals(
            listOf(0..1, 5..7),
            katakanaRanges("タムです。ハウス"),
        )
    }

    @Test
    fun `장음부호와 중점도 가타카나 구간에 포함한다`() {
        assertEquals(listOf(0..3), katakanaRanges("ラーメン"))
        assertEquals(listOf(0..2), katakanaRanges("・・・"))
    }

    @Test
    fun `한자와 히라가나는 가타카나 구간이 아니다`() {
        assertEquals(emptyList<IntRange>(), katakanaRanges("はるさんはどこですか。"))
        assertEquals(emptyList<IntRange>(), katakanaRanges("すぐ近くです。"))
        assertEquals(listOf(4..6), katakanaRanges("はるさんハウスはどこですか。"))
    }
}
