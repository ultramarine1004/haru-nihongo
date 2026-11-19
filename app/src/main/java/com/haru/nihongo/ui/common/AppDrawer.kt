package com.haru.nihongo.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/** 메인·복습·설정·종료를 한곳에 모은 팝업 사이드바. */
@Composable
fun AppMenuDrawer(
    drawerState: DrawerState,
    onHome: () -> Unit,
    onReview: () -> Unit,
    onSettings: () -> Unit,
    onExit: () -> Unit,
    content: @Composable () -> Unit,
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(28.dp))
                Text(
                    text = "하루 일본어 대화",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "오늘도 천천히, 한 대화씩",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
                Spacer(Modifier.height(18.dp))
                HorizontalDivider()

                DrawerItem(Icons.Default.Home, "메인 화면", onHome)
                DrawerItem(Icons.Default.History, "복습", onReview)
                DrawerItem(Icons.Default.Settings, "설정", onSettings)

                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                DrawerItem(Icons.AutoMirrored.Filled.ExitToApp, "학습 종료하고 앱 닫기", onExit)
                Spacer(Modifier.height(24.dp))
            }
        },
    ) {
        content()
    }
}

@Composable
private fun DrawerItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    NavigationDrawerItem(
        label = { Text(label) },
        selected = false,
        icon = { Icon(icon, contentDescription = null) },
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp),
    )
}

/**
 * 왼쪽 상단의 메뉴 버튼.
 * 흰 바탕에 회색 테두리인 48dp 원 안에 가로선 3개를 그렸다.
 */
@Composable
fun MenuCircleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(width = 1.dp, color = Color(0xFF9AA0A6), shape = CircleShape)
            .clickable(onClick = onClick)
            .semantics { contentDescription = "메뉴 열기" },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            repeat(3) { index ->
                Box(
                    Modifier
                        .size(width = 20.dp, height = 2.dp)
                        .background(Color(0xFF3C4043)),
                )
                if (index < 2) {
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

