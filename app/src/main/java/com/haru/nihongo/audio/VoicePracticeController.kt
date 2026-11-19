package com.haru.nihongo.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import androidx.core.content.ContextCompat
import java.io.File

/** 따라 말하기(녹음 → 내 목소리 재생)를 담당한다. */
class VoicePracticeController(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var recordFile: File? = null

    /** 내 목소리 재생 상태. false는 일시정지·종료·오류를 모두 포함한다. */
    var onPlaybackChanged: ((Boolean) -> Unit)? = null

    fun hasRecordPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    fun startRecording(): Boolean {
        if (!hasRecordPermission()) return false
        stopRecording()

        val file = File(context.cacheDir, RECORD_FILE_NAME)
        if (file.exists()) file.delete()
        recordFile = file

        val rec = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context.applicationContext)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        rec.setAudioSource(MediaRecorder.AudioSource.MIC)
        rec.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        rec.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        rec.setOutputFile(file.absolutePath)
        rec.prepare()
        rec.start()
        recorder = rec
        return true
    }

    fun stopRecording() {
        recorder?.let { rec ->
            try {
                rec.stop()
            } catch (_: RuntimeException) {
                // 녹음이 짧아 데이터가 없는 경우 무시
            } finally {
                rec.release()
            }
        }
        recorder = null
    }

    fun playRecording(): Boolean {
        val file = recordFile?.takeIf { it.exists() && it.length() > 0L } ?: return false
        stopRecordingPlayback()
        val mediaPlayer = MediaPlayer().apply {
            setDataSource(file.absolutePath)
            prepare()
            setOnCompletionListener { onPlaybackChanged?.invoke(false) }
            setOnErrorListener { _, _, _ ->
                onPlaybackChanged?.invoke(false)
                true
            }
        }
        player = mediaPlayer
        mediaPlayer.start()
        onPlaybackChanged?.invoke(true)
        return true
    }

    fun pauseRecordingPlayback() {
        val current = player ?: return
        try {
            if (current.isPlaying) current.pause()
        } catch (_: IllegalStateException) {
            // 이미 종료된 플레이어는 무시한다.
        }
        onPlaybackChanged?.invoke(false)
    }

    fun resumeRecordingPlayback() {
        val current = player ?: return
        try {
            if (!current.isPlaying) {
                current.start()
                onPlaybackChanged?.invoke(true)
            }
        } catch (_: IllegalStateException) {
            // 이미 종료된 플레이어는 무시한다.
        }
    }

    fun stopRecordingPlayback() {
        player?.let { current ->
            try {
                if (current.isPlaying) current.stop()
            } catch (_: IllegalStateException) {
                // 이미 종료된 플레이어는 무시한다.
            }
            current.release()
        }
        player = null
        onPlaybackChanged?.invoke(false)
    }

    /** 원형 프로그레스 링에 그대로 넘길 수 있는 실제 재생 위치(ms). */
    fun currentPositionMs(): Int = try {
        player?.currentPosition ?: 0
    } catch (_: IllegalStateException) {
        0
    }

    fun recordingDurationMs(): Int = try {
        player?.duration ?: 0
    } catch (_: IllegalStateException) {
        0
    }

    fun release() {
        stopRecording()
        stopRecordingPlayback()
        onPlaybackChanged = null
    }

    private companion object {
        const val RECORD_FILE_NAME = "practice.3gp"
    }
}
