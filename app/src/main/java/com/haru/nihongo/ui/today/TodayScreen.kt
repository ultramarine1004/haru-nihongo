package com.haru.nihongo.ui.today

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haru.nihongo.audio.LessonAudioPlayer
import com.haru.nihongo.audio.VoicePracticeController
import com.haru.nihongo.ui.common.BottomAction
import com.haru.nihongo.ui.common.BottomActionBar
import com.haru.nihongo.ui.common.DialogueCard
import com.haru.nihongo.ui.common.MenuCircleButton
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    onOpenMenu: () -> Unit,
) {
    val settings by viewModel.settings.collectAsState()
    val dialogues by viewModel.dialogues.collectAsState()

    val context = LocalContext.current
    val audioPlayer = remember { LessonAudioPlayer() }
    val recorder = remember { VoicePracticeController(context) }
    DisposableEffect(Unit) {
        onDispose {
            audioPlayer.release()
            recorder.release()
        }
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    /** 세 오디오 버튼 중 하나만 활성 — 상호 배타 상태의 단일 출처. */
    var activeAction by remember { mutableStateOf(BottomAction.NONE) }
    var playbackPlaying by remember { mutableStateOf(false) }
    var playbackPausedByUser by remember { mutableStateOf(false) }
    var playbackProgress by remember { mutableFloatStateOf(0f) }
    var showLessonPicker by remember { mutableStateOf(false) }

    LaunchedEffect(dialogues) {
        if (currentIndex >= dialogues.size) currentIndex = 0
    }
    LaunchedEffect(audioPlayer) {
        audioPlayer.onPlayingChanged = { isPlaying ->
            if (!isPlaying && activeAction == BottomAction.LISTEN) {
                activeAction = BottomAction.NONE
            }
        }
    }
    LaunchedEffect(recorder) {
        recorder.onPlaybackChanged = { isPlaying ->
            playbackPlaying = isPlaying
            if (!isPlaying && !playbackPausedByUser && activeAction == BottomAction.PLAYBACK) {
                activeAction = BottomAction.NONE
                playbackProgress = 0f
            }
        }
    }

    // 실제 MediaPlayer의 currentTime / duration 비율로 원형 링을 갱신한다.
    LaunchedEffect(activeAction, playbackPlaying) {
        if (activeAction == BottomAction.PLAYBACK && playbackPlaying) {
            while (true) {
                val duration = recorder.recordingDurationMs()
                val position = recorder.currentPositionMs()
                playbackProgress = if (duration > 0) {
                    (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                } else {
                    0f
                }
                delay(80)
            }
        }
    }

    val requestRecordPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            activeAction = if (recorder.startRecording()) {
                BottomAction.RECORD
            } else {
                BottomAction.NONE
            }
        }
    }

    /** 한 버튼을 누르면 나머지 재생·녹음을 멈춰 활성 상태가 겹치지 않게 한다. */
    fun stopOthers(keep: BottomAction) {
        if (keep != BottomAction.LISTEN) audioPlayer.stop()
        if (keep != BottomAction.RECORD) recorder.stopRecording()
        if (keep != BottomAction.PLAYBACK) recorder.stopRecordingPlayback()
    }

    fun moveTo(index: Int) {
        stopOthers(BottomAction.NONE)
        playbackPausedByUser = false
        activeAction = BottomAction.NONE
        playbackProgress = 0f
        currentIndex = index
    }

    fun toggleListen() {
        if (activeAction == BottomAction.LISTEN) {
            audioPlayer.stop()
            return
        }
        val current = dialogues.getOrNull(currentIndex) ?: return
        stopOthers(BottomAction.LISTEN)
        playbackPausedByUser = false
        // play()가 내부적으로 stop()을 호출하며 onPlayingChanged(false)를 쏘므로
        // 활성 상태는 재생 시작 후에 설정해야 한다.
        audioPlayer.play(context, current.audioAsset)
        activeAction = BottomAction.LISTEN
        viewModel.markListened()
    }

    fun toggleRecord() {
        if (activeAction == BottomAction.RECORD) {
            recorder.stopRecording()
            activeAction = BottomAction.NONE
            viewModel.setRecorded(true)
            return
        }
        if (!recorder.hasRecordPermission()) {
            requestRecordPermission.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        stopOthers(BottomAction.RECORD)
        playbackPausedByUser = false
        activeAction = if (recorder.startRecording()) {
            BottomAction.RECORD
        } else {
            BottomAction.NONE
        }
    }

    fun togglePlayback() {
        if (activeAction == BottomAction.PLAYBACK) {
            if (playbackPlaying) {
                playbackPausedByUser = true
                recorder.pauseRecordingPlayback()
            } else {
                playbackPausedByUser = false
                recorder.resumeRecordingPlayback()
            }
            return
        }
        stopOthers(BottomAction.PLAYBACK)
        playbackPausedByUser = false
        if (recorder.playRecording()) {
            activeAction = BottomAction.PLAYBACK
            playbackProgress = 0f
        } else {
            activeAction = BottomAction.NONE
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("하루 일본어 대화") },
                navigationIcon = {
                    MenuCircleButton(
                        onClick = onOpenMenu,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                },
            )
        },
        bottomBar = {
            BottomActionBar(
                activeAction = activeAction,
                playbackPlaying = playbackPlaying,
                playbackProgress = playbackProgress,
                onListenClick = { toggleListen() },
                onRecordClick = { toggleRecord() },
                onPlaybackClick = { togglePlayback() },
                onListClick = { showLessonPicker = true },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = "A와 B를 번갈아 따라 해 보세요. 통째로 익히면 쉬워요. 😊",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(20.dp))

            val current = dialogues.getOrNull(currentIndex)
            if (current == null) {
                Text("대화를 준비하고 있어요.", style = MaterialTheme.typography.bodyLarge)
            } else {
                DialogueCard(dialogue = current, fontSize = settings.fontSize)

                if (dialogues.size > 1) {
                    Spacer(Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedButton(
                            onClick = { moveTo((currentIndex - 1 + dialogues.size) % dialogues.size) },
                            modifier = Modifier.height(56.dp).weight(1f),
                        ) {
                            Text("< Prev")
                        }
                        OutlinedButton(
                            onClick = { moveTo((currentIndex + 1) % dialogues.size) },
                            modifier = Modifier.height(56.dp).weight(1f),
                        ) {
                            Text("Next >")
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showLessonPicker) {
        AlertDialog(
            onDismissRequest = { showLessonPicker = false },
            containerColor = Color.White,
            tonalElevation = 0.dp,
            title = {
                Text(
                    text = "강의 선택",
                    color = Color.Black,
                    fontSize = 18.sp,
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    dialogues.forEachIndexed { index, dialogue ->
                        TextButton(
                            onClick = {
                                moveTo(index)
                                showLessonPicker = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Black),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 12.dp,
                                top = 4.dp,
                                bottom = 4.dp,
                            ),
                        ) {
                            Text(
                                text = lessonPickerLabel(dialogue.order, dialogue.title),
                                modifier = Modifier.fillMaxWidth(),
                                fontSize = 14.sp,
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showLessonPicker = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Black),
                ) {
                    Text("닫기", fontSize = 14.sp)
                }
            },
        )
    }
}

/** "레슨1 하루상하우스는 어디예요？" → "1. 하루상 하우스는 어디예요？" */
private fun lessonPickerLabel(order: Int, rawTitle: String): String {
    val title = rawTitle
        .replace(Regex("^레슨\\d+\\s*"), "")
        .replace("하루상하우스", "하루상 하우스")
    return "$order. $title"
}
