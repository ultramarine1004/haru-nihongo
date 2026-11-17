package com.haru.nihongo.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.haru.nihongo.di.AppContainer
import com.haru.nihongo.domain.model.Settings
import com.haru.nihongo.ui.common.AppMenuDrawer
import com.haru.nihongo.ui.onboarding.OnboardingScreen
import com.haru.nihongo.ui.review.ReviewScreen
import com.haru.nihongo.ui.settings.SettingsScreen
import com.haru.nihongo.ui.today.TodayViewModel
import com.haru.nihongo.ui.today.TodayViewModelFactory
import com.haru.nihongo.ui.today.TodayScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavHost(
    container: AppContainer,
    onExit: () -> Unit,
) {
    var loaded by remember { mutableStateOf(false) }
    var currentSettings by remember { mutableStateOf(Settings()) }

    LaunchedEffect(Unit) {
        // 이전 버전에서 온보딩만 마친 경우에도 첫 실행일을 채워 둔다.
        container.repository.ensureFirstOpenDate()
        container.repository.settings.collect { value ->
            currentSettings = value
            loaded = true
        }
    }

    if (!loaded) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val todayViewModel: TodayViewModel = viewModel(factory = TodayViewModelFactory(container))
    val recent by todayViewModel.recent.collectAsState()

    fun closeDrawer() {
        scope.launch { drawerState.close() }
    }

    fun openDrawer() {
        scope.launch { drawerState.open() }
    }

    AppMenuDrawer(
        drawerState = drawerState,
        onHome = {
            navController.popBackStack("today", false)
            closeDrawer()
        },
        onReview = {
            navController.navigate("review")
            closeDrawer()
        },
        onSettings = {
            navController.navigate("settings")
            closeDrawer()
        },
        onExit = {
            scope.launch {
                todayViewModel.complete()
                onExit()
            }
        },
    ) {
        NavHost(
            navController = navController,
            startDestination = if (currentSettings.onboardingDone) "today" else "onboarding",
        ) {
            composable("onboarding") {
                OnboardingScreen { mode, count ->
                    scope.launch {
                        container.repository.finishOnboarding(mode, count)
                        navController.navigate("today") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                }
            }

            composable("today") {
                TodayScreen(
                    viewModel = todayViewModel,
                    onOpenMenu = { openDrawer() },
                )
            }

            composable("review") {
                ReviewScreen(
                    recent = recent,
                    onOpenMenu = { openDrawer() },
                )
            }

            composable("settings") {
                SettingsScreen(
                    settings = currentSettings,
                    onMode = { scope.launch { container.repository.setMode(it) } },
                    onCount = { scope.launch { container.repository.setDailyCount(it) } },
                    onFontSize = { scope.launch { container.repository.setFontSize(it) } },
                    onAlarm = { scope.launch { container.repository.setAlarm(it) } },
                    onOpenMenu = { openDrawer() },
                )
            }
        }
    }
}
