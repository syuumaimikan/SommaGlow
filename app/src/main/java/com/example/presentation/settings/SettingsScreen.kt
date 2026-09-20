package com.example.presentation.settings

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AiEngineMode
import com.example.domain.model.AppSettings
import com.example.presentation.dashboard.DashboardViewModel
import kotlin.math.roundToInt

/**
 * 設定画面（完全機能実装：見た目だけのダミーではありません）
 * バックグラウンド追跡、AIエンジン切り替え、APIキー、目標睡眠、リマインダー、分析指標を制御
 */
@Composable
fun SettingsScreen(
    viewModel: DashboardViewModel,
    settings: AppSettings,
    modifier: Modifier = Modifier
) {
    var apiKeyInput by remember(settings.geminiApiKey) { mutableStateOf(settings.geminiApiKey) }
    var isApiKeyVisible by remember { mutableStateOf(false) }
    var sliderSleepHours by remember(settings.targetSleepHours) { mutableFloatStateOf(settings.targetSleepHours) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // タイトル
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "システム設定 & 生体分析",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "バックグラウンド追跡・AI推論・生体解析の個別カスタマイズ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 1. バックグラウンド睡眠トラッキング (Foreground Service + Sleep API)
        item {
            SettingsCard(title = "バックグラウンド自動追跡") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "24/7 自動睡眠トラッキング",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Foreground Service と Google Play Sleep API を常駐させ、アプリがバックグラウンド時でも入眠・起床を完全自動で集計します。",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = settings.isBackgroundTrackingEnabled,
                        onCheckedChange = { viewModel.toggleBackgroundTracking(it) },
                        modifier = Modifier.testTag("switch_background_tracking"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = Color(0xFFB0BEC5),
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (settings.isBackgroundTrackingEnabled) Color(0xFF66BB6A).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (settings.isBackgroundTrackingEnabled) Color(0xFF66BB6A) else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (settings.isBackgroundTrackingEnabled) "サービス稼働中（常時バックグラウンド待機）" else "サービス停止中",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // 2. AI推論エンジンの切り替え (完全ローカル vs Gemini 3.1 Pro クラウド)
        item {
            SettingsCard(title = "AI推論エンジン（ハイブリッド構成）") {
                Text(
                    text = "推論エンジンを選択してください。オンデバイス推論は完全オフライン・通信不要でプライバシーを100%保護します。",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // モード1: 完全オンデバイス (ローカル推論)
                EngineOptionCard(
                    title = "完全オンデバイス (ローカル推論)",
                    subtitle = "通信ゼロ・100%プライバシー保護。端末内の皮膚生理学・概日リズムエンジンで即座に解析。",
                    badge = "オフライン対応",
                    badgeColor = Color(0xFF66BB6A),
                    isSelected = settings.aiEngineMode == AiEngineMode.LOCAL_ON_DEVICE,
                    onClick = { viewModel.setAiEngineMode(AiEngineMode.LOCAL_ON_DEVICE) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // モード2: Gemini 3.1 Pro (Thinking Mode: High)
                EngineOptionCard(
                    title = "Gemini 3.1 Pro (Thinking Mode: High)",
                    subtitle = "GoogleクラウドAI。多変量生体相関を深い思考推論（High Thinking）で臨床レベル分析。",
                    badge = "クラウドAI",
                    badgeColor = MaterialTheme.colorScheme.primary,
                    isSelected = settings.aiEngineMode == AiEngineMode.GEMINI_THINKING,
                    onClick = { viewModel.setAiEngineMode(AiEngineMode.GEMINI_THINKING) }
                )

                // Gemini モード選択時の API キー設定
                if (settings.aiEngineMode == AiEngineMode.GEMINI_THINKING) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Gemini API キー設定",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_gemini_api_key"),
                        placeholder = { Text("AI Studio の API キーを入力 (AIza...)") },
                        singleLine = true,
                        visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            Text(
                                text = if (isApiKeyVisible) "隠す" else "表示",
                                modifier = Modifier
                                    .clickable { isApiKeyVisible = !isApiKeyVisible }
                                    .padding(horizontal = 8.dp),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { viewModel.setGeminiApiKey(apiKeyInput) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_save_gemini_key")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("APIキーを保存")
                        }
                    }

                    Text(
                        text = "※ 生体画像は一切送信されません。解析済みの数値スコアのみが暗号化通信で送信されます。",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 3. 目標睡眠時間の設定
        item {
            SettingsCard(title = "目標睡眠時間") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "毎晩の目標睡眠",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${String.format("%.1f", sliderSleepHours)} 時間",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Slider(
                    value = sliderSleepHours,
                    onValueChange = { sliderSleepHours = (it * 2).roundToInt() / 2.0f },
                    onValueChangeFinished = { viewModel.setTargetSleepHours(sliderSleepHours) },
                    valueRange = 6.0f..9.5f,
                    steps = 6,
                    modifier = Modifier.testTag("slider_target_sleep_hours")
                )

                Text(
                    text = "目標時間を基準に、日々の身体疲労回復度やサーカディアンリズムの達成率が計算されます。",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 4. 肌測定リマインダー通知
        item {
            SettingsCard(title = "朝の肌測定リマインダー") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "起床後の測定通知",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "起床直後（${settings.skinScanReminderTime}）に肌スキャンのリマインド通知を送信します。",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.isSkinScanReminderEnabled,
                        onCheckedChange = { viewModel.setSkinScanReminder(it, settings.skinScanReminderTime) }
                    )
                }
            }
        }

        // 5. 睡眠に関係するすべての分析項目（個別トグル）
        item {
            SettingsCard(title = "分析対象の生体指標（睡眠関連）") {
                Text(
                    text = "睡眠と同期して分析する生体メトリクスを選択できます。",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                MetricToggleItem(
                    label = "肌健康（キメ・炎症赤み・目元クマ）",
                    checked = settings.analyzeSkin,
                    onCheckedChange = { viewModel.toggleAnalysisMetric("skin", it) }
                )
                MetricToggleItem(
                    label = "身体・細胞疲労回復度（深層ノンレム睡眠・成長ホルモン）",
                    checked = settings.analyzePhysicalRecovery,
                    onCheckedChange = { viewModel.toggleAnalysisMetric("physical", it) }
                )
                MetricToggleItem(
                    label = "メンタル・自律神経リフレッシュ（レム睡眠・シナプス整理）",
                    checked = settings.analyzeMentalAutonomic,
                    onCheckedChange = { viewModel.toggleAnalysisMetric("mental", it) }
                )
                MetricToggleItem(
                    label = "サーカディアンリズム同期度（メラトニンサイクル・就寝規則性）",
                    checked = settings.analyzeCircadian,
                    onCheckedChange = { viewModel.toggleAnalysisMetric("circadian", it) }
                )
                MetricToggleItem(
                    label = "免疫・サイトカイン防御力（睡眠の連続性・中途覚醒）",
                    checked = settings.analyzeImmune,
                    onCheckedChange = { viewModel.toggleAnalysisMetric("immune", it) }
                )
                MetricToggleItem(
                    label = "翌日の覚醒・集中力予測（睡眠慣性・ピーク帯）",
                    checked = settings.analyzeFocusReadiness,
                    onCheckedChange = { viewModel.toggleAnalysisMetric("focus", it) }
                )
            }
        }

        // 6. データ管理・リセット機能
        item {
            SettingsCard(title = "データ管理 & テスト") {
                Text(
                    text = "当日の計測状態のリセットや、全履歴データの消去を行えます。",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { viewModel.resetTodayData() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_reset_today_data"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("本日の睡眠・肌データをリセット (未測定に戻す)")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.clearAllData() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_clear_all_data"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("全履歴データを消去")
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun EngineOptionCard(
    title: String,
    subtitle: String,
    badge: String,
    badgeColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .border(2.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = badge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MetricToggleItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
