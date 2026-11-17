package com.haru.nihongo.ui.review

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.haru.nihongo.domain.model.Progress
import com.haru.nihongo.ui.common.MenuCircleButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    recent: List<Progress>,
    onOpenMenu: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("복습") },
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
            Text("지난 7일", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                "어제도 잘했어요. 오늘은 가볍게 다시 들어볼까요?",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(20.dp))

            if (recent.isEmpty()) {
                Text("아직 기록이 없어요. 오늘 한 편의 대화부터 시작해 보세요.", style = MaterialTheme.typography.bodyLarge)
            } else {
                recent.forEach { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(item.date, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            val parts = mutableListOf("${item.completedCount}개 완료")
                            if (item.listened) parts.add("듣기")
                            if (item.recorded) parts.add("따라 말하기")
                            Text(parts.joinToString(" · "), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}
