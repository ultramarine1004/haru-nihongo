package com.haru.nihongo.util

import java.time.LocalDate
import java.time.temporal.ChronoUnit

object DateKey {
    fun today(): String = LocalDate.now().toString()

    /**
     * 첫 실행일로부터 며칠 지났는지. 신규 사용자는 항상 0일차부터 시작한다.
     * 날짜 문자열이 없거나 미래인 경우에도 0을 반환한다.
     */
    fun daysSince(firstOpenDate: String, today: String = today()): Int {
        if (firstOpenDate.isBlank()) return 0
        return try {
            ChronoUnit.DAYS.between(LocalDate.parse(firstOpenDate), LocalDate.parse(today))
                .toInt()
                .coerceAtLeast(0)
        } catch (_: Exception) {
            0
        }
    }
}
