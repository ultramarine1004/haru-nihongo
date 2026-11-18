package com.haru.nihongo.audio

import android.content.Context
import android.media.MediaPlayer

/**
 * 앱 자산에 번들된 NHK 공식 MP3를 재생하는 플레이어.
 * 인터넷 연결 없이 재생되며, 강의별 파일을 하나씩 준비해서 재생한다.
 */
class LessonAudioPlayer {

    private var player: MediaPlayer? = null

    var onPlayingChanged: ((Boolean) -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    fun play(context: Context, assetPath: String) {
        stop()
        try {
            val descriptor = context.assets.openFd(assetPath)
            val mediaPlayer = MediaPlayer()
            player = mediaPlayer
            try {
                mediaPlayer.setDataSource(
                    descriptor.fileDescriptor,
                    descriptor.startOffset,
                    descriptor.length,
                )
            } finally {
                descriptor.close()
            }
            mediaPlayer.setOnPreparedListener {
                it.start()
                onPlayingChanged?.invoke(true)
            }
            mediaPlayer.setOnCompletionListener {
                onPlayingChanged?.invoke(false)
            }
            mediaPlayer.setOnErrorListener { _, _, _ ->
                onPlayingChanged?.invoke(false)
                onError?.invoke("MP3를 재생할 수 없어요. 잠시 후 다시 눌러 주세요.")
                true
            }
            mediaPlayer.prepareAsync()
        } catch (_: Exception) {
            release()
            onError?.invoke("MP3를 찾을 수 없어요. 앱을 다시 설치해 주세요.")
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
