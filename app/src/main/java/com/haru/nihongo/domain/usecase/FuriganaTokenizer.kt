package com.haru.nihongo.domain.usecase

/** 한자(또는 한자어)와 그 위에 붙일 후리가나 쌍. */
data class FuriganaToken(
    val text: String,
    val kana: String? = null,
) {
    val hasRuby: Boolean get() = kana != null
}

private data class Rule(val text: String, val kana: String)
private data class ContextReading(val kanji: String, val next: String, val kana: String)

/**
 * 번들된 48개 NHK 대화에 등장하는 모든 한자를 다루는 오프라인 후리가나 사전.
 * 서버/외부 API 없이 동작하며, 긴 규칙부터 매칭한다.
 */
object FuriganaTokenizer {

    private val RULES: List<Rule> = listOf(
        Rule("忍者博物館", "にんじゃはくぶつかん"),
        Rule("旅行会社", "りょこうがいしゃ"),
        Rule("写真家", "しゃしんか"),
        Rule("北海道", "ほっかいどう"),
        Rule("博物館", "はくぶつかん"),
        Rule("土曜日", "どようび"),
        Rule("大丈夫", "だいじょうぶ"),
        Rule("大涌谷", "おおわくだに"),
        Rule("富士山", "ふじさん"),
        Rule("小学校", "しょうがっこう"),
        Rule("山手線", "やまのてせん"),
        Rule("手裏剣", "しゅりけん"),
        Rule("日曜日", "にちようび"),
        Rule("日本語", "にほんご"),
        Rule("日本食", "にほんしょく"),
        Rule("時計台", "とけいだい"),
        Rule("海鮮丼", "かいせんどん"),
        Rule("留学生", "りゅうがくせい"),
        Rule("一番", "いちばん"),
        Rule("一緒", "いっしょ"),
        Rule("上手", "じょうず"),
        Rule("中国", "ちゅうごく"),
        Rule("予約", "よやく"),
        Rule("交番", "こうばん"),
        Rule("京都", "きょうと"),
        Rule("今度", "こんど"),
        Rule("今日", "きょう"),
        Rule("似合", "にあ"),
        Rule("住人", "じゅうにん"),
        Rule("何時", "なんじ"),
        Rule("健康", "けんこう"),
        Rule("優勝", "ゆうしょう"),
        Rule("元気", "げんき"),
        Rule("写真", "しゃしん"),
        Rule("勉強", "べんきょう"),
        Rule("卒業", "そつぎょう"),
        Rule("卵焼", "たまごや"),
        Rule("去年", "きょねん"),
        Rule("友達", "ともだち"),
        Rule("地下", "ちか"),
        Rule("地震", "じしん"),
        Rule("外側", "そとがわ"),
        Rule("大事", "だいじ"),
        Rule("大人", "おとな"),
        Rule("大吉", "だいきち"),
        Rule("大変", "たいへん"),
        Rule("大好", "だいす"),
        Rule("大学", "だいがく"),
        Rule("大家", "おおや"),
        Rule("大根", "だいこん"),
        Rule("学校", "がっこう"),
        Rule("学生", "がくせい"),
        Rule("客様", "きゃくさま"),
        Rule("将来", "しょうらい"),
        Rule("少々", "しょうしょう"),
        Rule("弁当", "べんとう"),
        Rule("心配", "しんぱい"),
        Rule("忍者", "にんじゃ"),
        Rule("応援", "おうえん"),
        Rule("恋人", "こいびと"),
        Rule("悠輝", "ゆうき"),
        Rule("意味", "いみ"),
        Rule("手袋", "てぶくろ"),
        Rule("担当", "たんとう"),
        Rule("授業", "じゅぎょう"),
        Rule("料理", "りょうり"),
        Rule("旅行", "りょこう"),
        Rule("日本", "にほん"),
        Rule("普通", "ふつう"),
        Rule("有名", "ゆうめい"),
        Rule("本当", "ほんとう"),
        Rule("本物", "ほんもの"),
        Rule("来月", "らいげつ"),
        Rule("案内", "あんない"),
        Rule("楽屋", "がくや"),
        Rule("残念", "ざんねん"),
        Rule("気持", "きも"),
        Rule("池袋", "いけぶくろ"),
        Rule("沖縄", "おきなわ"),
        Rule("注文", "ちゅうもん"),
        Rule("活躍", "かつやく"),
        Rule("海外", "かいがい"),
        Rule("海斗", "かいと"),
        Rule("温泉", "おんせん"),
        Rule("演奏", "えんそう"),
        Rule("漬物", "つけもの"),
        Rule("生卵", "なまたまご"),
        Rule("番線", "ばんせん"),
        Rule("箱根", "はこね"),
        Rule("納豆", "なっとう"),
        Rule("紹介", "しょうかい"),
        Rule("縁結", "えんむす"),
        Rule("親切", "しんせつ"),
        Rule("観光", "かんこう"),
        Rule("財布", "さいふ"),
        Rule("運勢", "うんせい"),
        Rule("遠慮", "えんりょ"),
        Rule("部屋", "へや"),
        Rule("長野", "ながの"),
        Rule("電話", "でんわ"),
        Rule("電車", "でんしゃ"),
        Rule("顔色", "かおいろ"),
        Rule("風呂", "ふろ"),
        Rule("魅力", "みりょく"),
        Rule("黄色", "きいろ"),
        Rule("中", "なか"),
        Rule("久", "ひさ"),
        Rule("人", "ひと"),
        Rule("今", "いま"),
        Rule("会", "あ"),
        Rule("伝", "つた"),
        Rule("伺", "うかが"),
        Rule("何", "なん"),
        Rule("作", "つく"),
        Rule("使", "つか"),
        Rule("働", "はたら"),
        Rule("先", "さき"),
        Rule("入", "はい"),
        Rule("円", "えん"),
        Rule("出", "で"),
        Rule("分", "ふん"),
        Rule("初", "はじ"),
        Rule("助", "たす"),
        Rule("卵", "たまご"),
        Rule("外", "そと"),
        Rule("夜", "よる"),
        Rule("夢", "ゆめ"),
        Rule("大", "おお"),
        Rule("好", "す"),
        Rule("子", "こ"),
        Rule("守", "まも"),
        Rule("寒", "さむ"),
        Rule("寺", "てら"),
        Rule("小", "ちい"),
        Rule("少", "すこ"),
        Rule("左", "ひだり"),
        Rule("年", "ねん"),
        Rule("座", "すわ"),
        Rule("庭", "にわ"),
        Rule("待", "ま"),
        Rule("悪", "わる"),
        Rule("慣", "なれ"),
        Rule("投", "なげ"),
        Rule("抜", "ぬ"),
        Rule("振", "ふ"),
        Rule("揺", "ゆれ"),
        Rule("撮", "と"),
        Rule("新", "あたら"),
        Rule("日", "ふつ"),
        Rule("時", "とき"),
        Rule("晴", "は"),
        Rule("書", "か"),
        Rule("月", "がつ"),
        Rule("朝", "あさ"),
        Rule("来", "き"),
        Rule("楽", "たの"),
        Rule("欲", "ほ"),
        Rule("歌", "うた"),
        Rule("渡", "わた"),
        Rule("湯", "ゆ"),
        Rule("物", "もの"),
        Rule("猫", "ねこ"),
        Rule("猿", "さる"),
        Rule("珍", "めずら"),
        Rule("甘", "あま"),
        Rule("番", "ばん"),
        Rule("痛", "いた"),
        Rule("的", "まと"),
        Rule("着", "つ"),
        Rule("知", "し"),
        Rule("私", "わたし"),
        Rule("箱", "はこ"),
        Rule("者", "もの"),
        Rule("聴", "き"),
        Rule("花", "はな"),
        Rule("落", "お"),
        Rule("行", "い"),
        Rule("見", "み"),
        Rule("話", "はな"),
        Rule("語", "ご"),
        Rule("読", "よ"),
        Rule("買", "か"),
        Rule("赤", "あか"),
        Rule("踊", "おど"),
        Rule("近", "ちか"),
        Rule("遅", "おく"),
        Rule("道", "みち"),
        Rule("隣", "となり"),
        Rule("雪", "ゆき"),
        Rule("顔", "かお"),
        Rule("願", "ねが"),
        Rule("食", "た"),
        Rule("高", "たか"),
        Rule("黒", "くろ"),
    ).sortedByDescending { it.text.length }

    /** 뒤따르는 글자에 따라 읽기가 달라지는 경우 (예: 入れて → い, 入って → はい). */
    private val CONTEXT_READINGS: List<ContextReading> = listOf(
        ContextReading("入", "れ", "い"),
        ContextReading("遅", "く", "おそ"),
        ContextReading("何", "を", "なに"),
        ContextReading("何", "に", "なに"),
        ContextReading("何", "が", "なに"),
    )

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
            val rule = RULES.firstOrNull { text.startsWith(it.text, index) }
            if (rule != null) {
                flushPlain()
                val nextIndex = index + rule.text.length
                val contextKana = CONTEXT_READINGS.firstOrNull { c ->
                    c.kanji == rule.text && text.startsWith(c.next, nextIndex)
                }?.kana
                result += FuriganaToken(rule.text, contextKana ?: rule.kana)
                index = nextIndex
            } else {
                plain.append(text[index])
                index++
            }
        }
        flushPlain()
        return result
    }
}
