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
        val tokens = FuriganaTokenizer.tokenize("はい。悠輝さんも、元気そうですね。")
        val ruby = tokens.first { it.text == "元気" }
        assertTrue(ruby.hasRuby)
        assertEquals("げんき", ruby.kana)
    }

    @Test
    fun `긴 규칙이 먼저 매칭된다`() {
        val tokens = FuriganaTokenizer.tokenize("日本語が心配です。")
        val rubies = tokens.filter { it.hasRuby }
        assertEquals("日本語", rubies[0].text)
        assertEquals("にほんご", rubies[0].kana)
        assertEquals("心配", rubies[1].text)
        assertEquals("しんぱい", rubies[1].kana)
    }

    @Test
    fun `토큰을 이어 붙이면 원문이 된다`() {
        val text = "少々お待ちください。お待たせしました。"
        val tokens = FuriganaTokenizer.tokenize(text)
        assertEquals(text, tokens.joinToString("") { it.text })
        assertTrue(tokens.any { it.text == "待" && it.kana == "ま" })
    }

    @Test
    fun `1번 대화의 近에 후리가나가 붙는다`() {
        val tokens = FuriganaTokenizer.tokenize("すぐ近くです。")
        val ruby = tokens.first { it.hasRuby }
        assertEquals("近", ruby.text)
        assertEquals("ちか", ruby.kana)
    }

    @Test
    fun `pykakasi가 잘못 읽은 한자를 바로잡는다`() {
        val cases = listOf(
            "あの的に投げてください。" to ("的" to "まと"),
            "15分くらいです。" to ("分" to "ふん"),
            "来月2日にコンサートがあるよ。" to ("日" to "ふつ"),
            "長野の人はとても親切でした。" to ("人" to "ひと"),
            "日本へ行ってみたいです。" to ("日本" to "にほん"),
            "今日は、晴れてよかったね。" to ("今日" to "きょう"),
            "小さいけどきれいですね。" to ("小" to "ちい"),
        )
        cases.forEach { (sentence, expected) ->
            val ruby = FuriganaTokenizer.tokenize(sentence).first { it.text == expected.first }
            assertEquals("'$sentence'의 ${expected.first}", expected.second, ruby.kana)
        }
    }

    @Test
    fun `뒤 글자에 따라 한자 읽기가 달라진다`() {
        fun kanaOf(sentence: String, kanji: String): String =
            FuriganaTokenizer.tokenize(sentence).first { it.text == kanji }.kana.orEmpty()

        assertEquals("い", kanaOf("私はわさびをたくさん入れてください。", "入"))
        assertEquals("はい", kanaOf("猿が温泉に入るのは 珍しいからね。", "入"))
        assertEquals("おそ", kanaOf("ごめん、遅くなって。", "遅"))
        assertEquals("おく", kanaOf("ああ、タム、遅れてごめんなさい。", "遅"))
        assertEquals("なに", kanaOf("タムさんは日本で何をしますか。", "何"))
        assertEquals("なん", kanaOf("これは何ですか。", "何"))
    }

    @Test
    fun `긴 한자어와 특수 표기도 처리한다`() {
        val cases = listOf(
            "このスペシャル海鮮丼がおすすめだよ！" to ("海鮮丼" to "かいせんどん"),
            "この卵焼き、甘くておいしいです。" to ("卵焼" to "たまごや"),
            "今度の土曜日、海斗と忍者博物館に行きます。" to ("忍者博物館" to "にんじゃはくぶつかん"),
            "少々お待ちください。" to ("少々" to "しょうしょう"),
        )
        cases.forEach { (sentence, expected) ->
            val ruby = FuriganaTokenizer.tokenize(sentence).first { it.text == expected.first }
            assertEquals("'$sentence'의 ${expected.first}", expected.second, ruby.kana)
        }
    }
}
