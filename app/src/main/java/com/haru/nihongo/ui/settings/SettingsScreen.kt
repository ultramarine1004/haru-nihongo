package com.haru.nihongo.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.haru.nihongo.domain.model.Mode
import com.haru.nihongo.domain.model.Settings
import com.haru.nihongo.ui.common.MenuCircleButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: Settings,
    onMode: (Mode) -> Unit,
    onCount: (Int) -> Unit,
    onFontSize: (Int) -> Unit,
    onAlarm: (Boolean) -> Unit,
    onOpenMenu: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("설정") },
                navigationIcon = {
                    MenuCircleButton(
                        onClick = onOpenMenu,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            SectionTitle("대화 모드")
            Mode.entries.forEach { item ->
                ChoiceButton(
                    label = if (item == Mode.DEFAULT) "${item.labelKo} · 추천" else item.labelKo,
                    selected = item == settings.mode,
                    onClick = { onMode(item) },
                )
            }

            Spacer(Modifier.height(24.dp))
            SectionTitle("하루 대화 개수")
            listOf(1, 3, 5).forEach { value ->
                ChoiceButton(
                    label = if (value == 1) "1개 · 추천" else "$value 개",
                    selected = value == settings.dailyCount,
                    onClick = { onCount(value) },
                )
            }

            Spacer(Modifier.height(24.dp))
            SectionTitle("글자 크기")
            listOf(18, 22, 28).forEach { value ->
                ChoiceButton(
                    label = if (value == 22) "${value}sp · 기본" else "${value}sp",
                    selected = value == settings.fontSize,
                    onClick = { onFontSize(value) },
                )
            }

            Spacer(Modifier.height(24.dp))
            SectionTitle("아침 알림")
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("하루 한 번 알려주기", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.weight(1f))
                Switch(checked = settings.alarm, onCheckedChange = onAlarm)
            }

            Spacer(Modifier.height(32.dp))
            Text(
                "이 앱은 광고와 회원가입이 없어요. 오른쪽 아래 메뉴에서 원하는 화면으로 이동할 수 있어요. 🌿",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun ChoiceButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        colors = if (selected) {
            androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        } else {
            androidx.compose.material3.ButtonDefaults.outlinedButtonColors()
        },
    ) {
        Text(label)
    }
}
