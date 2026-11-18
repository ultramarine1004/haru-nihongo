package com.haru.nihongo.data.content

import android.content.Context
import com.haru.nihongo.domain.model.Lesson
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** assets/content/lessons.json 을 앱 번들에서 읽는다. 대본/음성은 포함하지 않는다. */
class ContentDataSource(context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private val bundle: LessonBundle by lazy {
        val text = context.assets
            .open("content/lessons.json")
            .bufferedReader()
            .use { it.readText() }
        json.decodeFromString<LessonBundle>(text)
    }

    fun lessons(): List<Lesson> = bundle.lessons
        .sortedBy { it.number }
        .map { dto ->
            Lesson(
                id = dto.id,
                number = dto.number,
                title = dto.title,
                pageUrl = dto.pageUrl,
                audioUrl = dto.audioUrl,
            )
        }
}

@Serializable
private data class LessonBundle(
    val source: String = "",
    val lessons: List<LessonDto>,
)

@Serializable
private data class LessonDto(
    val id: String,
    val number: Int,
    val title: String,
    val pageUrl: String,
    val audioUrl: String,
)
