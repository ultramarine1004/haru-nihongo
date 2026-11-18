package com.haru.nihongo.audio

import android.media.MediaPlayer

/**
 * NHK 공식 MP3 URL을 스트리밍하는 재생기.
 * 음성 파일을 앱에 복사하지 않으므로 재생 시 인터넷 연결이 필요하다.
 */
class LessonAudioPlayer {

    private var player: MediaPlayer? = null

    var onPlayingChanged: ((Boolean) -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    fun play(url: String) {
        stop()
        val mediaPlayer = MediaPlayer()
        player = mediaPlayer
        try {
            mediaPlayer.setDataSource(url)
            mediaPlayer.setOnPreparedListener {
                it.start()
                onPlayingChanged?.invoke(true)
            }
            mediaPlayer.setOnCompletionListener {
                onPlayingChanged?.invoke(false)
            }
            mediaPlayer.setOnErrorListener { _, _, _ ->
                onPlayingChanged?.invoke(false)
                onError?.invoke("공식 음성을 재생할 수 없어요. 인터넷 연결을 확인해 주세요.")
                true
            }
            mediaPlayer.prepareAsync()
        } catch (_: Exception) {
            release()
            onError?.invoke("공식 음성을 재생할 수 없어요. 인터넷 연결을 확인해 주세요.")
        }
    }

    fun stop() {
        player?.let { mediaPlayer ->
            try {
                if (mediaPlayer.isPlaying) mediaPlayer.stop()
            } catch (_: IllegalStateException) {
                // 이미 종료된 재생기는 무시한다.
            }
            mediaPlayer.release()
        }
        player = null
        onPlayingChanged?.invoke(false)
    }

    fun release() = stop()
}
