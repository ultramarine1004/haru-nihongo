package com.haru.nihongo.data.content

import android.content.Context
import com.haru.nihongo.domain.model.Dialogue
import com.haru.nihongo.domain.model.Mode
import com.haru.nihongo.domain.model.Utterance
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** NHK 공식 페이지에서 추출해 번들한 assets/content/dialogues.json 을 읽는다. */
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

    fun dialogues(): List<Dialogue> = bundle.dialogues
        .sortedBy { it.order }
        .map { dto ->
            Dialogue(
                id = dto.id,
                mode = Mode.fromKey(dto.mode),
                title = dto.title,
                situation = dto.situation,
                order = dto.order,
                audioAsset = dto.audioAsset,
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
    val source: String = "",
    val dialogues: List<DialogueDto>,
)

@Serializable
private data class DialogueDto(
    val id: String,
    val mode: String,
    val title: String = "",
    val situation: String,
    val order: Int,
    val audioAsset: String = "",
    val utterances: List<UtteranceDto>,
)

@Serializable
private data class UtteranceDto(
    val speaker: String,
    val ja: String,
    val ko: String,
    val romaji: String = "",
    val polite: Boolean = true,
    val tip: String = "",
)
