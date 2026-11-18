package com.haru.nihongo.content

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** NHK 공식 페이지에서 추출해 번들한 대본과 MP3 자산을 검증한다. */
class DialoguesContentTest {

    private fun asset(relative: String): File {
        var dir = File(System.getProperty("user.dir") ?: ".")
        repeat(5) {
            val candidate = File(dir, "src/main/assets/$relative")
            if (candidate.exists()) return candidate
            dir = dir.parentFile ?: return candidate
        }
        error("asset not found: $relative")
    }

    private val dialogues = run {
        val file = asset("content/dialogues.json")
        Json.parseToJsonElement(file.readText(Charsets.UTF_8))
            .jsonObject.getValue("dialogues").jsonArray
    }

    @Test
    fun `48개 NHK 강의 대본이 강의 번호 순서로 들어 있다`() {
        assertEquals(48, dialogues.size)
        val orders = dialogues.map { it.jsonObject.getValue("order").jsonPrimitive.content.toInt() }
        assertEquals((1..48).toList(), orders)
    }

    @Test
    fun `각 대화는 말풍선 대본과 번들 MP3를 가진다`() {
        val audioPattern = Regex("""^audio/audio_lesson_\d{2}\.mp3$""")
        dialogues.forEach { element ->
            val item = element.jsonObject
            val number = item.getValue("order").jsonPrimitive.content.toInt()
            val audio = item.getValue("audioAsset").jsonPrimitive.content
            assertTrue(audioPattern.matches(audio))
            assertTrue(audio.endsWith("_${"%02d".format(number)}.mp3"))

            val mp3 = asset(audio)
            assertTrue("MP3 that should be bundled: $audio", mp3.length() > 10_000L)

            val utterances = item.getValue("utterances").jsonArray
            assertTrue("강의 $number 대본이 비어 있습니다", utterances.size >= 4)
            utterances.forEach { utterance ->
                val line = utterance.jsonObject
                assertTrue(line.getValue("speaker").jsonPrimitive.content in setOf("A", "B"))
                assertTrue(line.getValue("ja").jsonPrimitive.content.isNotBlank())
                assertTrue(line.getValue("ko").jsonPrimitive.content.isNotBlank())
            }
        }
    }
}
