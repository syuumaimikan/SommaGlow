package com.example.domain.model

/**
 * 睡眠セッションのドメインモデル
 * Android Sleep API (SleepSegmentEvent) から集約・計算されたデータ
 */
data class SleepSession(
    val id: Long = 0,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val durationMinutes: Int,
    val sleepScore: Int, // 0 - 100
    val deepSleepPercent: Int, // % (深層睡眠)
    val remSleepPercent: Int, // % (レム睡眠)
    val lightSleepPercent: Int, // % (浅い睡眠)
    val statusDescription: String,
    val dateString: String // YYYY-MM-DD
)

/**
 * Sleep APIのリアルタイム分類イベントデータ (SleepClassifyEvent)
 */
data class SleepClassification(
    val timestampMillis: Long,
    val confidence: Int, // 0 - 100
    val motionState: Int, // 静止度合い
    val lightLevel: Int // 周囲照度
)

/**
 * オンデバイス肌分析 (CameraX + ML) の結果ドメインモデル
 */
data class SkinAnalysisResult(
    val id: Long = 0,
    val timestampMillis: Long,
    val dateString: String,
    val textureScore: Int, // 0 - 100 (キメ・毛穴・平滑度)
    val rednessScore: Int, // 0 - 100 (赤み・炎症度: 100が健康で赤みなし)
    val darkCirclesScore: Int, // 0 - 100 (クマ・血行度: 100がクマなし良好)
    val overallScore: Int, // 総合肌スコア (0 - 100)
    val faceDetected: Boolean = true,
    val confidence: Float = 0.95f
)

/**
 * 睡眠に関係するすべての包括的生体分析モデル (Holistic Health & Bio-Analytics)
 * 睡眠・肌・身体回復・自律神経・サーカディアンリズム・免疫力を多角的に解析
 */
data class HolisticHealthAnalysis(
    val dateString: String,
    // 1. 肌健康指数 (Skin Radiance Index)
    val skinScore: Int,
    val skinStatus: String,
    // 2. 身体・細胞疲労回復度 (Physical Cellular Recovery - ノンレム深層睡眠依存)
    val physicalRecoveryScore: Int,
    val physicalRecoveryStatus: String,
    // 3. メンタル・自律神経リフレッシュ (Neurological & Mental Refresh - レム睡眠依存)
    val mentalAutonomicScore: Int,
    val mentalAutonomicStatus: String,
    // 4. サーカディアンリズム同期度 (Circadian Alignment - 就寝規則性)
    val circadianAlignmentScore: Int,
    val circadianStatus: String,
    // 5. 免疫・サイトカイン防御力 (Immune Potential - 睡眠連続性)
    val immunePotentialScore: Int,
    val immuneStatus: String,
    // 6. 翌日の覚醒・集中力予測 (Daytime Alertness Readiness)
    val daytimeFocusReadiness: Int,
    val peakFocusTimeWindow: String,
    // 総合バイオリズム調和度
    val overallBioHarmonyScore: Int
)

/**
 * 睡眠と生体相関インサイト (AIアドバイザー)
 */
data class CorrelationInsight(
    val dateString: String,
    val correlationScore: Int, // 0 - 100 (調和度)
    val headline: String,
    val detailedAnalysis: String,
    val recommendedActions: List<String>,
    val isGeneratedByAiThinking: Boolean = false
)

/**
 * AI推論エンジンのモード
 */
enum class AiEngineMode(val displayName: String, val description: String) {
    LOCAL_ON_DEVICE("完全オンデバイス (ローカルAI)", "通信ゼロ・100%プライバシー保護。端末内の生理学推論エンジンで即座に分析。"),
    GEMINI_THINKING("Gemini 3.1 Pro (Thinking Mode High)", "GoogleクラウドAI。多変量生体相関を深い思考推論で分析（APIキー必要）。")
}

/**
 * アプリケーション全体の設定モデル
 */
data class AppSettings(
    val isBackgroundTrackingEnabled: Boolean = true,
    val aiEngineMode: AiEngineMode = AiEngineMode.LOCAL_ON_DEVICE,
    val geminiApiKey: String = "",
    val targetSleepHours: Float = 8.0f,
    val isSkinScanReminderEnabled: Boolean = true,
    val skinScanReminderTime: String = "07:30",
    val analyzeSkin: Boolean = true,
    val analyzePhysicalRecovery: Boolean = true,
    val analyzeMentalAutonomic: Boolean = true,
    val analyzeCircadian: Boolean = true,
    val analyzeImmune: Boolean = true,
    val analyzeFocusReadiness: Boolean = true
)

/**
 * 睡眠・肌相関に基づくスキンケアルーティン処方モデル
 */
data class SkincareRoutine(
    val title: String,
    val summary: String,
    val morningSteps: List<SkincareStep>,
    val eveningSteps: List<SkincareStep>,
    val specialAdvice: String
)

data class SkincareStep(
    val stepNumber: Int,
    val category: String,
    val productType: String,
    val reason: String
)

/**
 * 快眠サウンドの種類
 */
enum class SleepSoundType(val displayName: String, val description: String, val iconEmoji: String) {
    RAIN("穏やかな夜雨", "高周波ノイズを遮断し副交感神経を優位にする自然環境音", "🌧️"),
    OCEAN("深海の波音", "呼吸リズムと同調する1/fゆらぎリラクゼーション音", "🌊"),
    DELTA_BINAURAL("デルタ波 (4Hz)", "深層ノンレム睡眠を強力に誘導するバイノーラルビート", "🧠"),
    PINK_NOISE("快眠ピンクノイズ", "突発音をマスクし深い睡眠サイクルを維持する低域音", "🌌")
}
