package com.haru.nihongo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.haru.nihongo.ui.AppNavHost
import com.haru.nihongo.ui.theme.HaruNihongoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as HaruNihongoApp).container
        setContent {
            HaruNihongoTheme {
                AppNavHost(
                    container = container,
                    onExit = { finishAffinity() },
                )
            }
        }
    }
}
