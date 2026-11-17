package com.haru.nihongo.domain

import com.haru.nihongo.domain.usecase.FuriganaTokenizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FuriganaTokenizerTest {

    @Test
    fun `한자가 없으면 원문 그대로 하나의 토큰이다`() {
        val tokens = FuriganaTokenizer.tokenize("おはようございます。")
        assertEquals(1, tokens.size)
        assertEquals("おはようございます。", tokens.first().text)
        assertEquals(null, tokens.first().kana)
    }

    @Test
    fun `한자어 위에 후리가나가 붙는다`() {
        val tokens = FuriganaTokenizer.tokenize("いいお天気ですね。")
        val ruby = tokens.first { it.hasRuby }
        assertEquals("天気", ruby.text)
        assertEquals("てんき", ruby.kana)
    }

    @Test
    fun `긴 규칙이 먼저 매칭된다`() {
        val tokens = FuriganaTokenizer.tokenize("来週もよろしくお願いします。")
        val rubies = tokens.filter { it.hasRuby }
        assertEquals("来週", rubies[0].text)
        assertEquals("らいしゅう", rubies[0].kana)
        assertEquals("願", rubies[1].text)
        assertEquals("ねがい", rubies[1].kana)
    }

    @Test
    fun `토큰을 이어 붙이면 원문이 된다`() {
        val text = "少々お待ちください。お待たせしました。"
        val tokens = FuriganaTokenizer.tokenize(text)
        assertEquals(text, tokens.joinToString("") { it.text })
        assertTrue(tokens.any { it.text == "待" && it.kana == "ま" })
    }
}
