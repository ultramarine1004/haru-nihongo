package com.haru.nihongo.ui.onboarding

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.haru.nihongo.domain.model.Mode

@Composable
fun OnboardingScreen(onDone: (Mode, Int) -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var mode by remember { mutableStateOf(Mode.DEFAULT) }
    var count by remember { mutableIntStateOf(1) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = "하루 일본어 대화  ·  ${step + 1} / 3",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(24.dp))

                when (step) {
                    0 -> WelcomeStep()
                    1 -> ModeStep(selected = mode, onSelect = { mode = it })
                    2 -> CountStep(selected = count, onSelect = { count = it })
                }
            }

            Button(
                onClick = {
                    if (step < 2) step++ else onDone(mode, count)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text(if (step < 2) "다음" else "시작하기")
            }
        }
    }
}

@Composable
private fun WelcomeStep() {
    Text("외우지 않으셔도 돼요.", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(16.dp))
    Text(
        text = "오늘 딱 한 편의 대화를 따라 말해 보세요.\nA와 B를 번갈아 익히면 쉬워요. 아침 2분이면 충분해요. 😊",
        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
    )
}

@Composable
private fun ModeStep(selected: Mode, onSelect: (Mode) -> Unit) {
    Text("어떤 상황을 연습할까요?", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(8.dp))
    Text(
        text = "나중에 언제든 바꿀 수 있어요. 기본은 '일반인'이에요.",
        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
    )
    Spacer(Modifier.height(20.dp))

    Mode.entries.chunked(2).forEach { pair ->
        Row(Modifier.fillMaxWidth()) {
            pair.forEach { item ->
                val isSelected = item == selected
                val label = if (item == Mode.DEFAULT) "${item.labelKo} · 추천" else item.labelKo
                val button = @Composable {
                    if (isSelected) {
                        Button(
                            onClick = { onSelect(item) },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                        ) { Text(label) }
                    } else {
                        OutlinedButton(
                            onClick = { onSelect(item) },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                        ) { Text(label) }
                    }
                }
                button()
            }
            if (pair.size == 1) Spacer(Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun CountStep(selected: Int, onSelect: (Int) -> Unit) {
    Text("하루에 몇 개 볼까요?", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(8.dp))
    Text(
        text = "처음에는 1개를 추천해요. 부담 없이 끝까지 해낼 수 있어요.",
        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
    )
    Spacer(Modifier.height(20.dp))

    listOf(1, 3, 5).forEach { value ->
        val label = if (value == 1) "1개 · 추천" else "$value 개"
        if (value == selected) {
            Button(
                onClick = { onSelect(value) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) { Text(label) }
        } else {
            OutlinedButton(
                onClick = { onSelect(value) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) { Text(label) }
        }
        Spacer(Modifier.height(12.dp))
    }

    Spacer(Modifier.height(16.dp))
    Text(
        text = "설정에서 언제든 바꿀 수 있어요.",
        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Start,
    )
}
