package com.haru.nihongo.ui.today

import android.Manifest
import android.content.Intent
import android.net.Uri
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
import com.haru.nihongo.ui.common.MenuCircleButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    onOpenMenu: () -> Unit,
) {
    val settings by viewModel.settings.collectAsState()
    val lessons by viewModel.lessons.collectAsState()

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
            message = "권한이 없어도 공식 음성은 들을 수 있어요. 😊"
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

    fun toggleLessonAudio() {
        val lesson = lessons.getOrNull(currentIndex) ?: return
        if (playing) {
            audioPlayer.stop()
            message = "음성을 멈췄어요. 다시 누르면 처음부터 재생합니다."
        } else {
            audioPlayer.play(lesson.audioUrl)
            viewModel.markListened()
            message = "NHK 공식 음성을 재생합니다. (인터넷 연결 필요)"
        }
    }

    fun openOfficialPage() {
        val lesson = lessons.getOrNull(currentIndex) ?: return
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(lesson.pageUrl)))
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
            val current = lessons.getOrNull(currentIndex)
            if (current == null) {
                Text("강의 목록을 준비하고 있어요.", style = MaterialTheme.typography.bodyLarge)
            } else {
                Text(
                    text = "NHK WORLD-JAPAN · 레슨 ${current.number}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = current.title,
                    fontSize = settings.fontSize.sp,
                    lineHeight = (settings.fontSize * 1.35f).sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "공식 강의 페이지에서 대본과 단어를 확인할 수 있어요.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                TextButton(onClick = { openOfficialPage() }) {
                    Text("NHK 공식 강의 보기")
                }

                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = { moveTo((currentIndex - 1 + lessons.size) % lessons.size) },
                        modifier = Modifier.height(56.dp).weight(1f),
                    ) {
                        Text("< Prev")
                    }
                    OutlinedButton(
                        onClick = { moveTo((currentIndex + 1) % lessons.size) },
                        modifier = Modifier.height(56.dp).weight(1f),
                    ) {
                        Text("Next >")
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
                    onClick = { toggleLessonAudio() },
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
                    lessons.forEachIndexed { index, lesson ->
                        TextButton(
                            onClick = {
                                moveTo(index)
                                showLessonPicker = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("레슨 ${lesson.number} · ${lesson.title}")
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
