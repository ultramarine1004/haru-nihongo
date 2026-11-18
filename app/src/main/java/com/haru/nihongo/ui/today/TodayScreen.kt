package com.haru.nihongo.ui.today

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haru.nihongo.audio.LessonAudioPlayer
import com.haru.nihongo.audio.VoicePracticeController
import com.haru.nihongo.ui.common.CircleActionButton
import com.haru.nihongo.ui.common.DialogueCard
import com.haru.nihongo.ui.common.MenuCircleButton

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
    var recording by remember { mutableStateOf(false) }
    var playing by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var showLessonPicker by remember { mutableStateOf(false) }

    LaunchedEffect(dialogues) {
        if (currentIndex >= dialogues.size) currentIndex = 0
    }
    LaunchedEffect(audioPlayer) {
        audioPlayer.onPlayingChanged = { playing = it }
        audioPlayer.onError = { message = it }
    }

    val requestRecordPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            recording = recorder.startRecording()
            message = if (recording) "말해 보세요. 다시 누르면 끝나요." else "녹음을 시작할 수 없어요."
        } else {
            message = "권한이 없어도 대화 듣기는 할 수 있어요. 😊"
        }
    }

    fun moveTo(index: Int) {
        audioPlayer.stop()
        currentIndex = index
    }

    fun toggleRecording() {
        if (recording) {
            recorder.stopRecording()
            recording = false
            viewModel.setRecorded(true)
            message = "잘 했어요. 내 목소리 버튼으로 들어볼까요?"
        } else if (recorder.hasRecordPermission()) {
            recording = recorder.startRecording()
            message = if (recording) "말해 보세요. 다시 누르면 끝나요." else "녹음을 시작할 수 없어요."
        } else {
            requestRecordPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun toggleDialogueAudio() {
        val current = dialogues.getOrNull(currentIndex) ?: return
        if (playing) {
            audioPlayer.stop()
            message = "음성을 멈췄어요. 다시 누르면 처음부터 재생합니다."
        } else {
            audioPlayer.play(context, current.audioAsset)
            viewModel.markListened()
            message = "NHK 공식 MP3를 재생합니다. (번들 오디오)"
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
                actions = {
                    Text(
                        text = "MODE",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF3C4043),
                        modifier = Modifier
                            .clickable { showLessonPicker = true }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    )
                },
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircleActionButton(
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    label = if (playing) "재생 중지" else "대화 듣기",
                    onClick = { toggleDialogueAudio() },
                )
                CircleActionButton(
                    icon = if (recording) Icons.Default.Stop else Icons.Default.Mic,
                    label = if (recording) "따라 말하기 끝" else "따라 말하기",
                    onClick = { toggleRecording() },
                )
                CircleActionButton(
                    icon = Icons.Default.PlayArrow,
                    label = "내 목소리",
                    onClick = {
                        message = if (recorder.playRecording()) {
                            "내 목소리예요. 한 번 더 해 볼까요?"
                        } else {
                            "먼저 따라 말해 보세요."
                        }
                    },
                )
            }

            if (message.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Text(message, style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(Modifier.height(96.dp))
        }
    }

    if (showLessonPicker) {
        AlertDialog(
            onDismissRequest = { showLessonPicker = false },
            title = { Text("강의 선택") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    dialogues.forEachIndexed { index, dialogue ->
                        TextButton(
                            onClick = {
                                moveTo(index)
                                showLessonPicker = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("레슨 ${dialogue.order} · ${dialogue.title}")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLessonPicker = false }) { Text("닫기") }
            },
        )
    }
}
