package com.haru.nihongo.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.core.content.ContextCompat
import java.io.File
import java.util.Locale

/**
 * 듣기(TTS)와 따라 말하기(녹음 → 재생)를 담당한다.
 * 외부 발음 API를 쓰지 않아 완전 무료·오프라인 원칙을 지킨다.
 */
class VoicePracticeController(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var recordFile: File? = null

    var onTtsReady: ((Boolean) -> Unit)? = null
    var onSpeakDone: (() -> Unit)? = null

    fun initTts() {
        if (tts != null) return
        tts = TextToSpeech(context.applicationContext) { status ->
            val ready = status == TextToSpeech.SUCCESS
            if (ready) {
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) = Unit
                    override fun onDone(utteranceId: String?) { onSpeakDone?.invoke() }
                    @Suppress("DEPRECATION")
                    override fun onError(utteranceId: String?) = Unit
                    override fun onError(utteranceId: String?, errorCode: Int) = Unit
                })
            }
            onTtsReady?.invoke(ready)
        }
    }

    /** 속도 옵션을 제거하고 1배속으로 재생한다. */
    fun speak(ja: String) {
        val engine = tts ?: return
        val result = engine.setLanguage(Locale.JAPAN)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            onTtsReady?.invoke(false)
            return
        }
        engine.setSpeechRate(1.0f)
        engine.speak(ja, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
    }

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
        tts?.shutdown()
        tts = null
        stopRecording()
        player?.release()
        player = null
    }

    private companion object {
        const val UTTERANCE_ID = "haru-utterance"
        const val RECORD_FILE_NAME = "practice.3gp"
    }
}
