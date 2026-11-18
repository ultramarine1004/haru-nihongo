package com.haru.nihongo.content

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** NHK 공식 링크 메타데이터가 대본/음성 없이 올바른 형식인지 검증한다. */
class LessonsContentTest {

    private val lessons = run {
        var dir = File(System.getProperty("user.dir"))
        repeat(5) {
            val candidate = File(dir, "src/main/assets/content/lessons.json")
            if (candidate.exists()) return@run candidate
            dir = dir.parentFile ?: return@run candidate
        }
        error("lessons.json not found near ${System.getProperty("user.dir") ?: "."}")
    }

    private val bundle = Json.parseToJsonElement(lessons.readText(Charsets.UTF_8)).jsonObject
    private val items = bundle.getValue("lessons").jsonArray

    @Test
    fun `48개 NHK 공식 강의 링크가Ordered되어 있다`() {
        assertEquals(48, items.size)
        val numbers = items.map { it.jsonObject.getValue("number").jsonPrimitive.content.toInt() }
        assertEquals((1..48).toList(), numbers)
    }

    @Test
    fun `각 강의는 공식 페이지와 MP3 URL만 보관한다`() {
        val page = Regex("""^https://www3\.nhk\.or\.jp/nhkworld/lesson/ko/lessons/\d{2}\.html$""")
        val mp3 = Regex("""^https://www3\.nhk\.or\.jp/nhkworld/lesson/ko/mp3/audio_lesson_\d{2}\.mp3$""")
        val forbidden = setOf("script", "transcript", "utterances", "dialogue", "text", "localPath")

        items.forEach { element ->
            val item = element.jsonObject
            val number = item.getValue("number").jsonPrimitive.content.toInt()
            assertTrue(item.getValue("title").jsonPrimitive.content.isNotBlank())
            assertTrue(page.matches(item.getValue("pageUrl").jsonPrimitive.content))
            assertTrue(mp3.matches(item.getValue("audioUrl").jsonPrimitive.content))
            assertTrue(item.getValue("pageUrl").jsonPrimitive.content.endsWith("/${"%02d".format(number)}.html"))
            assertTrue(item.getValue("audioUrl").jsonPrimitive.content.endsWith("_${"%02d".format(number)}.mp3"))
            assertTrue("대본/음성 복사 필드가 없어야 한다", forbidden.none { it in item })
        }
    }
}
