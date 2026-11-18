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
        player?.release()
        player = MediaPlayer().apply {
            setDataSource(file.absolutePath)
            prepare()
            start()
        }
        return true
    }

    fun release() {
        stopRecording()
        player?.release()
        player = null
    }

    private companion object {
        const val RECORD_FILE_NAME = "practice.3gp"
    }
}
