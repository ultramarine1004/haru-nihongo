package com.haru.nihongo.ui.common

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.haru.nihongo.R

/** 하단 세 오디오 버튼 중 현재 활성된 하나만 상태를 가진다(상호 배타). */
enum class BottomAction { NONE, LISTEN, RECORD, PLAYBACK }

private val BadgeBlue = Color(0xFF90CAF9)
private val ActiveBlue = Color(0xFF1976D2)
private val PaleBlue = Color(0xFFE3F2FD)
private val RecordRed = Color(0xFFE53935)
private val PaleRed = Color(0xFFFFEBEE)
private val RingTrack = Color(0xFFE8EAED)

/** 기존 60dp 원·72dp 외곽 박스를 30% 축소한 크기. */
private val ButtonCircle = 42.dp
private val ButtonBox = 50.dp
private val ButtonIcon = 24.dp

/**
 * 라벨 없는 아이콘 전용 하단 바.
 * 각 오디오 버튼은 toggleable로 aria-pressed(선택됨/선택 안 됨) 상태를 스크린리더에 노출한다.
 * 오른쪽 책 아이콘은 상단의 MODE를 대체하는 강의 목록 버튼이다.
 */
@Composable
fun BottomActionBar(
    activeAction: BottomAction,
    playbackPlaying: Boolean,
    playbackProgress: Float,
    onListenClick: () -> Unit,
    onRecordClick: () -> Unit,
    onPlaybackClick: () -> Unit,
    onListClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ListenButton(
                    active = activeAction == BottomAction.LISTEN,
                    onClick = onListenClick,
                )
                RecordButton(
                    active = activeAction == BottomAction.RECORD,
                    onClick = onRecordClick,
                )
                PlaybackButton(
                    active = activeAction == BottomAction.PLAYBACK,
                    playing = playbackPlaying,
                    progress = playbackProgress,
                    onClick = onPlaybackClick,
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(26.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant),
                )
                ListButton(onClick = onListClick)
            }
        }
    }
}

/** 재생 중: 연한 블루 뱃지 + 헤드폰 중앙의 3개 막대가 시차를 두고 위아래로 움직인다. */
@Composable
private fun ListenButton(
    active: Boolean,
    onClick: () -> Unit,
) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(ButtonBox)) {
        Box(
            modifier = Modifier
                .size(ButtonCircle)
                .clip(CircleShape)
                .background(if (active) PaleBlue else MaterialTheme.colorScheme.background)
                .toggleable(
                    value = active,
                    role = Role.Button,
                    onValueChange = { onClick() },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_listen_headphones),
                contentDescription = "대화 듣기",
                modifier = Modifier.size(ButtonIcon),
                tint = Color.Unspecified,
            )
            if (active) {
                val transition = rememberInfiniteTransition(label = "listen-bars")
                val bars = listOf(0, 1, 2).map { index ->
                    transition.animateFloat(
                        initialValue = 0.2f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 460, delayMillis = index * 140),
                            repeatMode = RepeatMode.Reverse,
                        ),
                        label = "bar-$index",
                    )
                }
                Canvas(modifier = Modifier.size(ButtonIcon)) {
                    val centerX = size.width / 2f
                    val centerY = size.height * 0.47f
                    val barWidth = 2.dp.toPx()
                    val minHalf = 1.dp.toPx()
                    val maxHalf = 5.dp.toPx()
                    val gap = 4.dp.toPx()
                    bars.forEachIndexed { index, bar ->
                        val half = minHalf + (maxHalf - minHalf) * bar.value
                        drawRoundRect(
                            color = ActiveBlue,
                            topLeft = Offset(
                                x = centerX + (index - 1) * gap - barWidth / 2f,
                                y = centerY - half,
                            ),
                            size = Size(barWidth, half * 2f),
                            cornerRadius = CornerRadius(barWidth / 2f),
                        )
                    }
                }
            }
        }
        if (active) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-2).dp, y = 2.dp)
                    .clip(CircleShape)
                    .background(BadgeBlue),
            )
        }
    }
}

/** 녹음 중: 코랄 레드로 전환 + 마이크 내부 채움 + 동심원 펄스. */
@Composable
private fun RecordButton(
    active: Boolean,
    onClick: () -> Unit,
) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(ButtonBox)) {
        if (active) {
            val transition = rememberInfiniteTransition(label = "record-pulse")
            val pulse = transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1300),
                    repeatMode = RepeatMode.Restart,
                ),
                label = "pulse",
            )
            Canvas(modifier = Modifier.size(ButtonBox)) {
                val stroke = 2.dp.toPx()
                val maxRadius = size.minDimension / 2f - stroke
                repeat(2) { ring ->
                    val phase = (pulse.value + ring * 0.5f) % 1f
                    drawCircle(
                        color = RecordRed.copy(alpha = 0.45f * (1f - phase)),
                        radius = maxRadius * (0.5f + 0.5f * phase),
                        style = Stroke(width = stroke),
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .size(ButtonCircle)
                .clip(CircleShape)
                .background(if (active) PaleRed else MaterialTheme.colorScheme.background)
                .toggleable(
                    value = active,
                    role = Role.Button,
                    onValueChange = { onClick() },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(
                    if (active) R.drawable.ic_speak_microphone_filled
                    else R.drawable.ic_speak_microphone,
                ),
                contentDescription = "따라 말하기",
                modifier = Modifier.size(ButtonIcon),
                tint = Color.Unspecified,
            )
        }
    }
}

/** 재생 중: ▶ → ❚❚ 로 바뀌고, 실제 currentTime/duration 비율로 원형 링이 채워진다. */
@Composable
private fun PlaybackButton(
    active: Boolean,
    playing: Boolean,
    progress: Float,
    onClick: () -> Unit,
) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(ButtonBox)) {
        Canvas(modifier = Modifier.size(ButtonBox)) {
            val stroke = 2.dp.toPx()
            val inset = stroke / 2f + 1.dp.toPx()
            drawCircle(
                color = if (active) PaleBlue else RingTrack,
                radius = size.minDimension / 2f - inset,
                style = Stroke(width = stroke),
            )
            if (active && progress > 0f) {
                drawArc(
                    color = ActiveBlue,
                    startAngle = -90f,
                    sweepAngle = 360f * progress.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - inset * 2f, size.height - inset * 2f),
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
        }
        Box(
            modifier = Modifier
                .size(ButtonCircle)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background)
                .toggleable(
                    value = active,
                    role = Role.Button,
                    onValueChange = { onClick() },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(
                    if (active && playing) R.drawable.ic_pause_recording
                    else R.drawable.ic_play_recording,
                ),
                contentDescription = "내 목소리",
                modifier = Modifier.size(ButtonIcon),
                tint = Color.Unspecified,
            )
        }
    }
}

/** 상단 MODE를 대체하는 강의 목록 버튼 — 라벨 없이 책 아이콘만 표시한다. */
@Composable
private fun ListButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(ButtonBox)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_book_index),
            contentDescription = "강의 목록",
            modifier = Modifier.size(ButtonIcon),
            tint = Color.Unspecified,
        )
    }
}
