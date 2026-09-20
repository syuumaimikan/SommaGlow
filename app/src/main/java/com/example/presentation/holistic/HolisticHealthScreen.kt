package com.example.presentation.holistic

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.HolisticHealthAnalysis
import com.example.presentation.dashboard.DashboardUiState
import com.example.presentation.dashboard.DashboardViewModel

/**
 * 睡眠に関係するすべての生体指標を網羅した包括的分析画面
 * （肌・細胞疲労・自律神経・体内時計・免疫・集中力）
 */
@Composable
fun HolisticHealthScreen(
    viewModel: DashboardViewModel,
    uiState: DashboardUiState,
    modifier: Modifier = Modifier
) {
    val analysis = uiState.holisticHealthAnalysis
    val hasData = uiState.todaySleep != null || uiState.todaySkin != null

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // ヘッダー
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Spa,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "睡眠相関・全生体分析",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "睡眠は肌だけでなく、細胞修復・自律神経・免疫力・認知集中力のすべてを司る生体基盤です",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 未測定時の誘導カード
        if (!hasData) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "本日の計測データは未確定です",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "昨夜の睡眠データを同期するか、今朝の肌スキャンを行うと、睡眠医学に基づく全6指標が多角的に自動解析されます。",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 17.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { viewModel.syncMorningSleepData() },
                                modifier = Modifier.weight(1f).testTag("btn_sync_sleep_holistic"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("睡眠を同期", fontSize = 13.sp)
                            }
                            OutlinedButton(
                                onClick = { viewModel.openCameraScanner() },
                                modifier = Modifier.weight(1f).testTag("btn_scan_skin_holistic"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("肌スキャン", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // 総合バイオリズム調和度カード
        if (analysis != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "総合バイオリズム調和指数",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Bio-Harmony Index",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${analysis.overallBioHarmonyScore}",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = " / 100",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        LinearProgressIndicator(
                            progress = { analysis.overallBioHarmonyScore / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (analysis.overallBioHarmonyScore >= 80)
                                "最良の生体修復サイクルを形成中。深層睡眠・細胞ターンオーバー・神経伝達が高度に同期しています。"
                            else
                                "軽度の生体リズム乖離を検知。入眠時間の安定化と水分補給により明朝の改善が期待できます。",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 6大ドメインカード
            item {
                Text(
                    text = "睡眠関連 6大生体ドメイン詳細",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // ① 肌状態 & 表皮バリア
            if (uiState.settings.analyzeSkin) {
                item {
                    BioDomainCard(
                        title = "1. 肌健康 & 角層バリア (Skin Radiance)",
                        score = analysis.skinScore,
                        status = analysis.skinStatus,
                        icon = Icons.Default.Spa,
                        iconColor = Color(0xFFFF8A80),
                        mechanism = "夜間22時〜深夜2時の深層睡眠期に分泌される成長ホルモン(hGH)が表皮基底膜の細胞分裂を促進し、角層水分保持バリアを再生します。"
                    )
                }
            }

            // ② 身体・細胞疲労回復度
            if (uiState.settings.analyzePhysicalRecovery) {
                item {
                    BioDomainCard(
                        title = "2. 身体・細胞疲労回復 (Cellular Recovery)",
                        score = analysis.physicalRecoveryScore,
                        status = analysis.physicalRecoveryStatus,
                        icon = Icons.Default.BatteryChargingFull,
                        iconColor = Color(0xFF4CAF50),
                        mechanism = "ノンレム睡眠（深層ステージ3）において筋グリコーゲンの再充填と微小筋損傷の修復、同化ホルモンによる代謝リセットが実行されます。"
                    )
                }
            }

            // ③ メンタル & 自律神経リフレッシュ
            if (uiState.settings.analyzeMentalAutonomic) {
                item {
                    BioDomainCard(
                        title = "3. 自律神経 & 感情リセット (Mental Reset)",
                        score = analysis.mentalAutonomicScore,
                        status = analysis.mentalAutonomicStatus,
                        icon = Icons.Default.Psychology,
                        iconColor = Color(0xFF64B5F6),
                        mechanism = "レム睡眠中に感情記憶中枢（扁桃体）の過剰興奮が脱感作され、副交感神経トーンが回復。ストレス耐性と認知柔軟性を再構成します。"
                    )
                }
            }

            // ④ サーカディアンリズム同期度
            if (uiState.settings.analyzeCircadian) {
                item {
                    BioDomainCard(
                        title = "4. サーカディアンリズム同期 (Circadian Alignment)",
                        score = analysis.circadianAlignmentScore,
                        status = analysis.circadianStatus,
                        icon = Icons.Default.Schedule,
                        iconColor = Color(0xFFFFB74D),
                        mechanism = "視交叉上核（体内時計中枢）とメラトニン分泌サイクルの規則性を測定。ソーシャルジェットラグ（社会的時差ボケ）を防止します。"
                    )
                }
            }

            // ⑤ 免疫・サイトカイン防御力
            if (uiState.settings.analyzeImmune) {
                item {
                    BioDomainCard(
                        title = "5. 免疫 & サイトカイン防御力 (Immune Defense)",
                        score = analysis.immunePotentialScore,
                        status = analysis.immuneStatus,
                        icon = Icons.Default.Security,
                        iconColor = Color(0xFFBA68C8),
                        mechanism = "中途覚醒のない連続睡眠により、ナチュラルキラー(NK)細胞活性および抗ウイルス性サイトカインの産生能が高水準に保たれます。"
                    )
                }
            }

            // ⑥ 翌日の覚醒 & 集中力予測
            if (uiState.settings.analyzeFocusReadiness) {
                item {
                    BioDomainCard(
                        title = "6. 日中覚醒度 & 集中力予測 (Daytime Alertness)",
                        score = analysis.daytimeFocusReadiness,
                        status = "最高集中帯: ${analysis.peakFocusTimeWindow}",
                        icon = Icons.Default.LightMode,
                        iconColor = Color(0xFFFFD54F),
                        mechanism = "睡眠慣性（Sleep Inertia）からの脱出時間とアデノシン排出率から、本日の最大集中パフォーマンス時間帯を予測します。"
                    )
                }
            }
        }
    }
}

@Composable
private fun BioDomainCard(
    title: String,
    score: Int,
    status: String,
    icon: ImageVector,
    iconColor: Color,
    mechanism: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(iconColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = iconColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${score}点",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = iconColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "評価: $status",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = mechanism,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
