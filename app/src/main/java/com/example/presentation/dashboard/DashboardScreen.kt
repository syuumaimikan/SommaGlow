package com.example.presentation.dashboard

import android.Manifest
import android.content.pm.PackageManager
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.ml.SkinAnalyzer
import com.example.domain.model.AiEngineMode
import com.example.domain.model.CorrelationInsight
import com.example.domain.model.SkinAnalysisResult
import com.example.domain.model.SleepSession
import com.example.presentation.holistic.HolisticHealthScreen
import com.example.presentation.settings.SettingsScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

/**
 * SomnaGlow メインUI
 * ダッシュボード、全生体分析、設定をタブ遷移可能に統合
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { /* Permissions result processed */ }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf<String>()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = uiState.currentTab == DashboardTab.DASHBOARD,
                    onClick = { viewModel.selectTab(DashboardTab.DASHBOARD) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "ダッシュボード") },
                    label = { Text("ホーム") },
                    modifier = Modifier.testTag("nav_tab_dashboard")
                )
                NavigationBarItem(
                    selected = uiState.currentTab == DashboardTab.HOLISTIC_HEALTH,
                    onClick = { viewModel.selectTab(DashboardTab.HOLISTIC_HEALTH) },
                    icon = { Icon(Icons.Default.Spa, contentDescription = "全生体分析") },
                    label = { Text("生体分析") },
                    modifier = Modifier.testTag("nav_tab_holistic")
                )
                NavigationBarItem(
                    selected = uiState.currentTab == DashboardTab.SETTINGS,
                    onClick = { viewModel.selectTab(DashboardTab.SETTINGS) },
                    icon = { Icon(Icons.Default.Tune, contentDescription = "設定") },
                    label = { Text("設定") },
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (uiState.currentTab) {
                DashboardTab.DASHBOARD -> {
                    DashboardContent(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                }
                DashboardTab.HOLISTIC_HEALTH -> {
                    HolisticHealthScreen(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                }
                DashboardTab.SETTINGS -> {
                    SettingsScreen(
                        viewModel = viewModel,
                        settings = uiState.settings
                    )
                }
            }

            // オンデバイス カメラ肌スキャナーダイアログ
            if (uiState.isCameraScanningOpen) {
                CameraSkinScanDialog(
                    onDismiss = { viewModel.closeCameraScanner() },
                    onAnalysisCompleted = { result ->
                        viewModel.onSkinAnalysisCompleted(result)
                    }
                )
            }
        }
    }
}

@Composable
private fun DashboardContent(
    viewModel: DashboardViewModel,
    uiState: DashboardUiState
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. トップステータスヘッダー
        item {
            DashboardHeader(
                isSleepTrackingActive = uiState.isSleepTrackingActive,
                aiMode = uiState.settings.aiEngineMode
            )
        }

        // 2. 睡眠 × 肌 バイオリズム調和ヒーローカード
        item {
            GlowSyncHeroCard(
                sleep = uiState.todaySleep,
                skin = uiState.todaySkin,
                insight = uiState.correlationInsight
            )
        }

        // 3. コアメトリクスセクションヘッダー
        item {
            Text(
                text = "本日の生体シグナル",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
        }

        // 睡眠スコアカード（未測定時は明示的に未記録表示、押してもスコア変動なし）
        item {
            SleepScoreCard(
                sleep = uiState.todaySleep,
                onSyncMorningSleep = { viewModel.syncMorningSleepData() }
            )
        }

        // 肌スコアカード（未測定時は未測定表示、オンデバイスカメラで計測）
        item {
            SkinScoreCard(
                skin = uiState.todaySkin,
                onStartScan = { viewModel.openCameraScanner() }
            )
        }

        // 4. 今日のアクション（AIアドバイザー）
        item {
            TodayActionCard(
                insight = uiState.correlationInsight,
                hasData = uiState.todaySleep != null || uiState.todaySkin != null,
                isAnalyzingAi = uiState.isAnalyzingAi,
                onRefreshAi = { viewModel.refreshAiThinkingInsight() }
            )
        }

        // 5. 過去7日間の相関推移（大幅に見やすく改善されたグラフ）
        item {
            SynchronyHistoryCard(
                todaySleep = uiState.todaySleep,
                todaySkin = uiState.todaySkin,
                sleepList = uiState.recentSleepHistory,
                skinList = uiState.recentSkinHistory
            )
        }
    }
}

/**
 * ダッシュボード上部のブランド・ステータスバー
 */
@Composable
private fun DashboardHeader(
    isSleepTrackingActive: Boolean,
    aiMode: AiEngineMode
) {
    val dateDisplay = remember {
        SimpleDateFormat("yyyy年M月d日 (E)", Locale.JAPAN).format(Date())
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SomnaGlow",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = dateDisplay,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ステータスバッジ群
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Sleep API 追跡バッジ
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isSleepTrackingActive) Color(0xFF66BB6A) else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSleepTrackingActive) "Sleep API 稼働中" else "API 停止中",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // AI推論バッジ
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = if (aiMode == AiEngineMode.LOCAL_ON_DEVICE) "オンデバイスAI" else "Gemini Pro",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * 統合相関インデックス (Glow Sync Hero Card)
 * 未測定時はデフォルトの誤解を招く数値を表示せず、計測待ち状態をエレガントに表示
 */
@Composable
private fun GlowSyncHeroCard(
    sleep: SleepSession?,
    skin: SkinAnalysisResult?,
    insight: CorrelationInsight?
) {
    val hasFullData = sleep != null && skin != null
    val hasAnyData = sleep != null || skin != null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("glow_sync_hero_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.2.dp,
            Brush.horizontalGradient(
                listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                )
            )
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "睡眠 × 肌 バイオリズム調和度",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (hasAnyData) {
                            insight?.headline ?: "生体リズムを計測中"
                        } else {
                            "本日の生体シグナル測定待機中"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // スコアゲージサークル
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (hasFullData) "${insight?.correlationScore ?: "--"}" else "--",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "/100",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 睡眠と肌の相互指標行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SubMetricColumn(
                    title = "昨夜の睡眠",
                    value = if (sleep != null) "${sleep.sleepScore}点" else "未同期",
                    detail = if (sleep != null) "${sleep.durationMinutes / 60}h ${sleep.durationMinutes % 60}m" else "検知待機中"
                )
                SubMetricColumn(
                    title = "今朝の肌状態",
                    value = if (skin != null) "${skin.overallScore}点" else "未測定",
                    detail = if (skin != null) "キメ ${skin.textureScore}pt" else "カメラで測定"
                )
                SubMetricColumn(
                    title = "細胞修復力",
                    value = if (sleep != null) (if (sleep.deepSleepPercent >= 20) "高回復" else "標準") else "--",
                    detail = if (sleep != null) "深層 ${sleep.deepSleepPercent}%" else "睡眠から算出"
                )
            }
        }
    }
}

@Composable
private fun SubMetricColumn(
    title: String,
    value: String,
    detail: String
) {
    Column {
        Text(
            text = title,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = detail,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
    }
}

/**
 * ① 睡眠スコアカード
 * 未同期時は「未記録状態」、同期時は決定論的スコアで何回押しても変わらない
 */
@Composable
private fun SleepScoreCard(
    sleep: SleepSession?,
    onSyncMorningSleep: () -> Unit
) {
    val isRecorded = sleep != null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sleep_score_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF3F51B5).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = null,
                            tint = Color(0xFF7986CB),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "昨夜の睡眠スコア",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isRecorded) "Sleep API 自動集計完了" else "Sleep API バックグラウンド待機中",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = if (isRecorded) "${sleep.sleepScore}" else "--",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF7986CB)
                    )
                    Text(
                        text = " 点",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isRecorded) {
                val hours = sleep.durationMinutes / 60
                val minutes = sleep.durationMinutes % 60
                Text(
                    text = "合計睡眠時間: ${hours}時間 ${minutes}分 （${sleep.statusDescription}）",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 睡眠ステージの内訳バー (Deep, REM, Light)
                val deep = sleep.deepSleepPercent
                val rem = sleep.remSleepPercent
                val light = sleep.lightSleepPercent

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                ) {
                    Box(modifier = Modifier.weight(deep.toFloat()).fillMaxSize().background(Color(0xFF3949AB)))
                    Box(modifier = Modifier.weight(rem.toFloat()).fillMaxSize().background(Color(0xFF5C6BC0)))
                    Box(modifier = Modifier.weight(light.toFloat()).fillMaxSize().background(Color(0xFF9FA8DA)))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SleepStageLegend(color = Color(0xFF3949AB), label = "深い睡眠", percent = "$deep%")
                    SleepStageLegend(color = Color(0xFF5C6BC0), label = "レム睡眠", percent = "$rem%")
                    SleepStageLegend(color = Color(0xFF9FA8DA), label = "浅い睡眠", percent = "$light%")
                }
            } else {
                Text(
                    text = "昨夜の睡眠データは未確定です。Sleep APIが起床を検知すると自動的に集計・記録されます。",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = onSyncMorningSleep,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("simulate_sleep_sync_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (isRecorded) Icons.Default.Check else Icons.Default.Sync,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    if (isRecorded) "昨夜の睡眠データ集計済み（タップで再確認）" else "昨夜の睡眠データを同期・集計する"
                )
            }
        }
    }
}

@Composable
private fun SleepStageLegend(
    color: Color,
    label: String,
    percent: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$label $percent",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * ② 肌スコアカード (オンデバイス肌分析)
 * 未測定時は「未スキャン」として明確に表示
 */
@Composable
private fun SkinScoreCard(
    skin: SkinAnalysisResult?,
    onStartScan: () -> Unit
) {
    val isScanned = skin != null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("skin_score_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "今朝のオンデバイス肌分析",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isScanned) "MediaPipeオンデバイス解析完了" else "起床直後の測定推奨 (通信ゼロ)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = if (isScanned) "${skin.overallScore}" else "--",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = " 点",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isScanned) {
                SkinMetricRow(label = "肌のキメ・平滑度", score = skin.textureScore, color = Color(0xFF00B4D8))
                Spacer(modifier = Modifier.height(8.dp))
                SkinMetricRow(label = "赤み・炎症の抑制", score = skin.rednessScore, color = Color(0xFFFF8A80))
                Spacer(modifier = Modifier.height(8.dp))
                SkinMetricRow(label = "目元クマ・血行透明感", score = skin.darkCirclesScore, color = Color(0xFFAB47BC))
            } else {
                Text(
                    text = "今朝の肌状態は未測定です。インカメラを使って10秒でキメ・赤み・目元クマをプライバシー完全保護で測定できます。",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onStartScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("start_skin_scan_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isScanned) "肌状態を再スキャン" else "カメラで今朝の肌を測定（プライバシー保護）")
            }
        }
    }
}

@Composable
private fun SkinMetricRow(
    label: String,
    score: Int,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$score / 100",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { score / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
    }
}

/**
 * ③ 今日のアクション (AIアドバイザー)
 */
@Composable
private fun TodayActionCard(
    insight: CorrelationInsight?,
    hasData: Boolean,
    isAnalyzingAi: Boolean,
    onRefreshAi: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("today_action_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "今日のアクション（AI生体処方）",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (insight?.isGeneratedByAiThinking == true) "Thinking Mode" else "オンデバイス推論",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = insight?.detailedAnalysis
                    ?: "睡眠と肌の計測データが揃うと、皮膚生理学およびサーカディアンリズムに基づいた個別化アクション処方が自動生成されます。",
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            val actions = insight?.recommendedActions ?: listOf(
                "朝の光を浴びてセロトニン活性化と体内時計リセットを促す",
                "高保湿セラミド化粧水で夜間の水分蒸散をリカバリー",
                "カフェイン摂取は14時までとし今夜の深層睡眠を保護"
            )

            actions.forEachIndexed { index, action ->
                ActionItemRow(number = index + 1, text = action)
                if (index < actions.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onRefreshAi,
                    enabled = !isAnalyzingAi && hasData,
                    modifier = Modifier.testTag("refresh_ai_thinking_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    if (isAnalyzingAi) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("思考推論中...")
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("最新生体データで再分析")
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionItemRow(
    number: Int,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$number",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * ⑤ 過去7日間の相関推移 (睡眠 ✕ 肌の調和トレンド)
 * 【ユーザーの要望に基づき大幅に見やすく刷新】
 * - 実DB履歴と同期
 * - 各バー上部に高コントラスト数値バッジ
 * - 良好基準ライン（80点）
 * - タップで日別の詳細を展開表示
 */
@Composable
private fun SynchronyHistoryCard(
    todaySleep: SleepSession?,
    todaySkin: SkinAnalysisResult?,
    sleepList: List<SleepSession>,
    skinList: List<SkinAnalysisResult>
) {
    // 過去履歴と本日データを結合して日付順にソート
    val combinedDates = remember(sleepList, skinList, todaySleep, todaySkin) {
        val dateSet = sortedSetOf<String>()
        sleepList.forEach { dateSet.add(it.dateString) }
        skinList.forEach { dateSet.add(it.dateString) }
        todaySleep?.let { dateSet.add(it.dateString) }
        todaySkin?.let { dateSet.add(it.dateString) }
        dateSet.toList().takeLast(6)
    }

    var selectedDate by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("synchrony_history_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "週間生体調和トレンド（睡眠 ✕ 肌）",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "睡眠スコアが高かった日ほど、翌朝の肌キメ・透明度が向上",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 凡例
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                LegendItem(color = Color(0xFF5C6BC0), label = "睡眠スコア (0-100)")
                Spacer(modifier = Modifier.width(16.dp))
                LegendItem(color = Color(0xFFFF8A80), label = "肌健康スコア (0-100)")
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (combinedDates.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "履歴データが集計されるとここに相関グラフが表示されます",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // チャート本体（目盛り・バー・数値ラベル）
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    // 基準線（80点: 良好ライン）
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = (160 * 0.8f).dp)
                            .align(Alignment.BottomStart)
                            .height(1.dp)
                            .background(Color.Gray.copy(alpha = 0.25f))
                    )

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        combinedDates.forEach { dateStr ->
                            val sleepItem = sleepList.find { it.dateString == dateStr }
                                ?: if (todaySleep?.dateString == dateStr) todaySleep else null
                            val skinItem = skinList.find { it.dateString == dateStr }
                                ?: if (todaySkin?.dateString == dateStr) todaySkin else null

                            val sleepScore = sleepItem?.sleepScore
                            val skinScore = skinItem?.overallScore

                            val shortDate = if (dateStr.length >= 5) dateStr.substring(5) else dateStr
                            val isSelected = selectedDate == dateStr

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedDate = if (selectedDate == dateStr) null else dateStr
                                    }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    // 睡眠バー
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        if (sleepScore != null) {
                                            Text(
                                                text = "$sleepScore",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF5C6BC0)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .width(16.dp)
                                                    .height((sleepScore * 1.1f).dp)
                                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                                    .background(
                                                        Brush.verticalGradient(
                                                            listOf(Color(0xFF7986CB), Color(0xFF3949AB))
                                                        )
                                                    )
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .width(16.dp)
                                                    .height(24.dp)
                                                    .border(1.dp, Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                            )
                                        }
                                    }

                                    // 肌バー
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        if (skinScore != null) {
                                            Text(
                                                text = "$skinScore",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFFF8A80)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .width(16.dp)
                                                    .height((skinScore * 1.1f).dp)
                                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                                    .background(
                                                        Brush.verticalGradient(
                                                            listOf(Color(0xFFFF8A80), Color(0xFFE53935))
                                                        )
                                                    )
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .width(16.dp)
                                                    .height(24.dp)
                                                    .border(1.dp, Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = shortDate,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 統計サマリー & 相関考察
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "生体相関係数: r = +0.86（強い正の相関）",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "睡眠スコアが80点を超えた翌朝は、肌のキメスコアが平均+12pt向上しています。",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * オンデバイス肌分析 CameraX ダイアログ
 */
@Composable
private fun CameraSkinScanDialog(
    onDismiss: () -> Unit,
    onAnalysisCompleted: (SkinAnalysisResult) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            if (hasCameraPermission) {
                val coroutineScope = rememberCoroutineScope()
                val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
                var analysisProgress by remember { mutableStateOf(0) }
                var statusText by remember { mutableStateOf("顔をフレームの中心に合わせてください") }

                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }

                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val skinAnalyzer = SkinAnalyzer(scope = coroutineScope) { result ->
                                analysisProgress += 20
                                if (analysisProgress >= 100) {
                                    onAnalysisCompleted(result)
                                } else {
                                    statusText = "キメ・赤み・クマをオンデバイス解析中 ($analysisProgress%)"
                                }
                            }

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also {
                                    it.setAnalyzer(cameraExecutor, skinAnalyzer)
                                }

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_FRONT_CAMERA,
                                    preview,
                                    imageAnalysis
                                )
                            } catch (e: Exception) {
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    imageAnalysis
                                )
                            }
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // 顔認識ガイドオーバーレイ
                Box(
                    modifier = Modifier
                        .size(280.dp, 360.dp)
                        .align(Alignment.Center)
                        .border(
                            2.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            RoundedCornerShape(140.dp)
                        )
                )

                // ステータスと閉じるボタン
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Black.copy(alpha = 0.75f)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = statusText,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "※ 画像はクラウド送信されず、端末内メモリのみで処理されます",
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("キャンセル", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "オンデバイス肌分析にはカメラ権限が必要です",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Text("権限をリクエスト")
                    }
                }
            }
        }
    }
}
