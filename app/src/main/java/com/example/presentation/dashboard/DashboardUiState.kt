package com.example.presentation.dashboard

import com.example.domain.model.AppSettings
import com.example.domain.model.CorrelationInsight
import com.example.domain.model.HolisticHealthAnalysis
import com.example.domain.model.SkinAnalysisResult
import com.example.domain.model.SleepSession

enum class DashboardTab(val label: String) {
    DASHBOARD("ダッシュボード"),
    HOLISTIC_HEALTH("全生体分析"),
    SETTINGS("設定")
}

/**
 * ダッシュボード画面の UI 状態
 */
data class DashboardUiState(
    val isLoading: Boolean = true,
    val currentTab: DashboardTab = DashboardTab.DASHBOARD,
    // 本日のデータ（未記録時は null）
    val todaySleep: SleepSession? = null,
    val todaySkin: SkinAnalysisResult? = null,
    // 過去履歴
    val recentSleepHistory: List<SleepSession> = emptyList(),
    val recentSkinHistory: List<SkinAnalysisResult> = emptyList(),
    // 相関インサイト & 全生体分析
    val correlationInsight: CorrelationInsight? = null,
    val holisticHealthAnalysis: HolisticHealthAnalysis? = null,
    // アプリ設定
    val settings: AppSettings = AppSettings(),
    val isSleepTrackingActive: Boolean = true,
    val isAnalyzingAi: Boolean = false,
    val isCameraScanningOpen: Boolean = false,
    val userMessage: String? = null
)
