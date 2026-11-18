package com.haru.nihongo.domain.model

/**
 * 대화 모드. [DEFAULT]는 기획 확정 사항인 "일반인"이다.
 * 콘텐츠는 NHK 강의 대본으로 바뀌었지만 설정 값은 이전 사용자를 위해 유지한다.
 */
enum class Mode(
    val labelKo: String,
    val description: String,
    val greeting: String,
) {
    GENERAL("일반인", "이웃·길에서 나누는 가벼운 인사", "こんにちは。"),
    WORK("직장인", "출근·회의·퇴근 인사", "おはようございます。"),
    STUDENT("학생", "교실에서 쓰는 표현", "先生、こんにちは。"),
    SHOP("상점 주인·손님", "가게에서 오가는 말", "いらっしゃいませ。"),
    TRAVELER("여행자", "길 묻기·주문·사진", "すみません。"),
    FAMILY("가족·이웃", "반말과 존댓말 구분", "ただいま。"),
    ;

    companion object {
        /** 앱 첫 실행 시 선택되는 기본 모드 (기획 확정). */
        val DEFAULT: Mode = GENERAL

        fun fromKey(key: String): Mode = entries.firstOrNull { it.name == key } ?: DEFAULT
    }
}

data class Utterance(
    /** 대화 상대 구분: "A" 또는 "B". */
    val speaker: String,
    val ja: String,
    val ko: String,
    val romaji: String,
    val polite: Boolean,
    val tip: String = "",
)

data class Dialogue(
    val id: String,
    val mode: Mode,
    val title: String = "",
    val situation: String,
    val order: Int,
    val utterances: List<Utterance>,
    /** 앱 자산에 번들된 NHK 공식 MP3 경로. */
    val audioAsset: String = "",
)

data class Settings(
    val mode: Mode = Mode.DEFAULT,
    /** 첫 실행 날짜(yyyy-MM-dd). */
    val firstOpenDate: String = "",
    val dailyCount: Int = 1,
    val fontSize: Int = 22,
    val alarm: Boolean = true,
    val onboardingDone: Boolean = false,
)

data class Progress(
    val date: String,
    val completedCount: Int,
    val listened: Boolean,
    val recorded: Boolean,
)
