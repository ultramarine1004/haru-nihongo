package com.haru.nihongo.data.content

import android.content.Context
import com.haru.nihongo.domain.model.Dialogue
import com.haru.nihongo.domain.model.Mode
import com.haru.nihongo.domain.model.Utterance
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** assets/content/dialogues.json 을 앱 번들에서 읽는다. 서버·네트워크 없음. */
class ContentDataSource(context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val bundle: ContentBundle by lazy {
        val text = context.assets
            .open("content/dialogues.json")
            .bufferedReader()
            .use { it.readText() }
        json.decodeFromString<ContentBundle>(text)
    }

    fun dialogues(): List<Dialogue> = bundle.dialogues.map { dto ->
        Dialogue(
            id = dto.id,
            mode = Mode.fromKey(dto.mode),
            situation = dto.situation,
            order = dto.order,
            utterances = dto.utterances.map { u ->
                Utterance(
                    speaker = u.speaker,
                    ja = u.ja,
                    ko = u.ko,
                    romaji = u.romaji,
                    polite = u.polite,
                    tip = u.tip,
                )
            },
        )
    }
}

@Serializable
private data class ContentBundle(
    val dialogues: List<DialogueDto>,
)

@Serializable
private data class DialogueDto(
    val id: String,
    val mode: String,
    val situation: String,
    val order: Int,
    val utterances: List<UtteranceDto>,
)

@Serializable
private data class UtteranceDto(
    val speaker: String,
    val ja: String,
    val ko: String,
    val romaji: String,
    val polite: Boolean,
    val tip: String = "",
)
