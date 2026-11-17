package com.haru.nihongo.domain.usecase

/**
 * 한자(또는 한자어)와 그 위에 붙일 후리가나 쌍.
 * kana가 null이면 그대로 출력하는 일반 텍스트 조각이다.
 */
data class FuriganaToken(
    val text: String,
    val kana: String? = null,
) {
    val hasRuby: Boolean get() = kana != null
}

/**
 * 앱에 번들된 대화에 등장하는 한자만 처리하는 소형 후리가나 사전.
 * 서버/외부 API 없이 오프라인으로 동작한다.
 */
object FuriganaTokenizer {

    private val RULES: List<Pair<String, String>> = listOf(
        "元気" to "げんき",
        "天気" to "てんき",
        "今日" to "きょう",
        "少々" to "しょうしょう",
        "来週" to "らいしゅう",
        "確認" to "かくにん",
        "大丈夫" to "だいじょうぶ",
        "三百円" to "さんびゃくえん",
        "一度" to "いちど",
        "説明" to "せつめい",
        "意味" to "いみ",
        "写真" to "しゃしん",
        "先生" to "せんせい",
        "承知" to "しょうち",
        "散歩" to "さんぽ",
        "明日" to "あした",
        "言葉" to "ことば",
        "資料" to "しりょう",
        "質問" to "しつもん",
        "駅" to "えき",
        "袋" to "ふくろ",
        "願" to "ねがい",
        "待" to "ま",
        "疲" to "つか",
        "会" to "あ",
        "何" to "なん",
        "探" to "さが",
        "来" to "き",
        "行" to "い",
        "助" to "たす",
        "帰" to "かえ",
        "持" to "も",
        "休" to "やす",
        "入" to "い",
        "呼" to "よ",
        "始" to "はじ",
        "撮" to "と",
        "気" to "き",
        "聞" to "き",
        "見" to "み",
        "遊" to "あそ",
    ).sortedByDescending { it.first.length }

    /** 긴 규칙부터 매칭해 문장을 후리가나 토큰으로 나눈다. */
    fun tokenize(text: String): List<FuriganaToken> {
        val result = mutableListOf<FuriganaToken>()
        val plain = StringBuilder()
        var index = 0

        fun flushPlain() {
            if (plain.isNotEmpty()) {
                result += FuriganaToken(plain.toString())
                plain.setLength(0)
            }
        }

        while (index < text.length) {
            val rule = RULES.firstOrNull { text.startsWith(it.first, index) }
            if (rule != null) {
                flushPlain()
                result += FuriganaToken(text = rule.first, kana = rule.second)
                index += rule.first.length
            } else {
                plain.append(text[index])
                index++
            }
        }
        flushPlain()
        return result
    }
}
