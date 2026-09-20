package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.SomnaGlowDatabase
import com.example.data.repository.AiAdvisorRepositoryImpl
import com.example.data.repository.SettingsRepositoryImpl
import com.example.data.repository.SkinRepositoryImpl
import com.example.data.repository.SleepRepositoryImpl
import com.example.data.tracking.SleepTrackingManager
import com.example.presentation.dashboard.DashboardScreen
import com.example.presentation.dashboard.DashboardViewModel
import com.example.ui.theme.MyApplicationTheme

/**
 * SomnaGlow メインアクティビティ
 * クリーンアーキテクチャの依存関係を初期化し、DashboardScreen を起動
 */
class MainActivity : ComponentActivity() {

    private lateinit var sleepTrackingManager: SleepTrackingManager

    private val dashboardViewModel: DashboardViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = SomnaGlowDatabase.getInstance(applicationContext)
                val sleepRepo = SleepRepositoryImpl(db.sleepDao())
                val skinRepo = SkinRepositoryImpl(db.skinDao())
                val settingsRepo = SettingsRepositoryImpl(applicationContext)
                val aiRepo = AiAdvisorRepositoryImpl()
                sleepTrackingManager = SleepTrackingManager(applicationContext, sleepRepo)

                return DashboardViewModel(
                    appContext = applicationContext,
                    sleepRepository = sleepRepo,
                    skinRepository = skinRepo,
                    aiAdvisorRepository = aiRepo,
                    settingsRepository = settingsRepo,
                    sleepTrackingManager = sleepTrackingManager
                ) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                DashboardScreen(viewModel = dashboardViewModel)
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun Greeting(name: String, modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier) {
    androidx.compose.material3.Text(text = "Hello $name!", modifier = modifier)
}
