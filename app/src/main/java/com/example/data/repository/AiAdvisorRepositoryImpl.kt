package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.remote.GeminiClient
import com.example.data.remote.GeminiContent
import com.example.data.remote.GeminiGenerationConfig
import com.example.data.remote.GeminiPart
import com.example.data.remote.GeminiRequest
import com.example.data.remote.GeminiThinkingConfig
import com.example.domain.model.CorrelationInsight
import com.example.domain.model.HolisticHealthAnalysis
import com.example.domain.model.SkinAnalysisResult
import com.example.domain.model.SleepSession
import com.example.domain.repository.AiAdvisorRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * 睡眠データ・肌状態・全身生体指標の統合相関分析エンジン
 *
 * 【ハイブリッドAI設計】
 * 1. オンデバイス推論 (ローカルAI / 規定エンジン):
 *    完全オフライン・通信不要・プライバシー100%保護。皮膚生理学・概日リズム・自律神経相関ルールを端末内で即座に実行。
 * 2. Gemini 3.1 Pro (Thinking Mode High / クラウドAI):
 *    設定画面でクラウドモードが選択され、APIキーが登録されている場合のみ、数値化されたメトリクスを安全に送信し、高精度な深い思考推論を実行。
 */
class AiAdvisorRepositoryImpl : AiAdvisorRepository {

    companion object {
        private const val TAG = "AiAdvisorRepository"
    }

    override suspend fun generateCorrelationInsight(
        sleep: SleepSession?,
        skin: SkinAnalysisResult?,
        useGeminiCloud: Boolean,
        apiKey: String
    ): CorrelationInsight = withContext(Dispatchers.IO) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val sleepScore = sleep?.sleepScore ?: 80
        val deepSleep = sleep?.deepSleepPercent ?: 20
        val durationMins = sleep?.durationMinutes ?: 450
        val skinScore = skin?.overallScore ?: 82
        val texture = skin?.textureScore ?: 80
        val redness = skin?.rednessScore ?: 80
        val darkCircles = skin?.darkCirclesScore ?: 80

        val correlationScore = ((sleepScore * 0.5) + (skinScore * 0.5)).toInt().coerceIn(0, 100)

        val resolvedApiKey = if (apiKey.isNotBlank()) apiKey else {
            try {
                BuildConfig::class.java.getField("GEMINI_API_KEY").get(null) as? String ?: ""
            } catch (e: Exception) {
                ""
            }
        }

        // ユーザーがクラウドAIを選択し、有効なAPIキーが存在する場合のみ Gemini 3.1 Pro を呼び出し
        if (useGeminiCloud && resolvedApiKey.isNotBlank() && resolvedApiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = buildPrompt(
                    durationMins = durationMins,
                    sleepScore = sleepScore,
                    deepSleep = deepSleep,
                    skinScore = skinScore,
                    texture = texture,
                    redness = redness,
                    darkCircles = darkCircles,
                    correlationScore = correlationScore
                )

                val request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(GeminiPart(text = prompt))
                        )
                    ),
                    generationConfig = GeminiGenerationConfig(
                        temperature = 0.4f,
                        thinkingConfig = GeminiThinkingConfig(
                            thinkingLevel = "high"
                        )
                    )
                )

                val response = GeminiClient.service.generateWithThinking(
                    apiKey = resolvedApiKey,
                    request = request
                )

                val candidateText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!candidateText.isNullOrBlank()) {
                    return@withContext parseGeminiResponse(
                        text = candidateText,
                        dateStr = todayStr,
                        correlationScore = correlationScore
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini API with High Thinking failed, falling back to local on-device engine", e)
            }
        }

        // 完全オンデバイス（ローカル）推論エンジン
        return@withContext generateLocalCorrelationInsight(
            dateStr = todayStr,
            correlationScore = correlationScore,
            sleep = sleep,
            skin = skin
        )
    }

    override fun calculateHolisticHealthAnalysis(
        sleep: SleepSession?,
        skin: SkinAnalysisResult?,
        targetSleepHours: Float
    ): HolisticHealthAnalysis {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val sleepScore = sleep?.sleepScore ?: 75
        val durationMins = sleep?.durationMinutes ?: 420
        val deepSleep = sleep?.deepSleepPercent ?: 18
        val remSleep = sleep?.remSleepPercent ?: 20
        val lightSleep = sleep?.lightSleepPercent ?: 62

        val skinScore = skin?.overallScore ?: 78
        val texture = skin?.textureScore ?: 76
        val redness = skin?.rednessScore ?: 78
        val darkCircles = skin?.darkCirclesScore ?: 75

        // 1. 身体・細胞疲労回復度 (Physical Cellular Recovery)
        // 深層ノンレム睡眠中に成長ホルモン(hGH)が分泌され筋組織・グリコーゲン・皮膚基底膜が修復される
        val targetMins = (targetSleepHours * 60).toInt()
        val durationRatio = (durationMins.toFloat() / targetMins.toFloat()).coerceIn(0.6f, 1.2f)
        val deepFactor = (deepSleep.toFloat() / 22.0f).coerceIn(0.5f, 1.3f)
        val physicalRecoveryScore = ((sleepScore * 0.45f + (deepFactor * 45f)) * durationRatio).toInt().coerceIn(30, 99)
        val physicalStatus = when {
            physicalRecoveryScore >= 85 -> "細胞修復・成長ホルモン分泌が最大化"
            physicalRecoveryScore >= 70 -> "標準的な身体疲労回復を完了"
            else -> "深層睡眠不足により筋肉・皮膚細胞の修復遅延"
        }

        // 2. メンタル・自律神経リフレッシュ (Neurological & Mental Refresh)
        // レム睡眠による感情記憶の整理、扁桃体興奮抑制、副交感神経優位性の回復
        val remFactor = (remSleep.toFloat() / 22.0f).coerceIn(0.6f, 1.3f)
        val mentalScore = ((sleepScore * 0.5f) + (remFactor * 45f)).toInt().coerceIn(35, 98)
        val mentalStatus = when {
            mentalScore >= 85 -> "副交感神経優位・感情リセット完全完了"
            mentalScore >= 70 -> "良好な自律神経バランス"
            else -> "レム睡眠不足による神経疲労の残存リスク"
        }

        // 3. サーカディアンリズム同期度 (Circadian Alignment)
        // 就寝時刻の規則性（23:00前後の就寝ウィンドウ）
        val circadianScore = if (sleep != null) {
            val startHour = ((sleep.startTimeMillis / (1000 * 60 * 60)) % 24).toInt()
            val diffFromOptimal = abs(startHour - 23)
            (95 - (diffFromOptimal * 6)).coerceIn(50, 96)
        } else {
            75
        }
        val circadianStatus = when {
            circadianScore >= 85 -> "メラトニン分泌サイクルと完全同期"
            circadianScore >= 70 -> "軽度の体内時計ズレ（午前中の太陽光推奨）"
            else -> "ソーシャル・ジェットラグ傾向"
        }

        // 4. 免疫・サイトカイン防御力 (Immune Potential)
        // 中途覚醒が少なく連続した睡眠によってナチュラルキラー細胞やサイトカイン産生が活性化
        val immuneScore = ((physicalRecoveryScore * 0.5f) + (sleepScore * 0.5f)).toInt().coerceIn(30, 98)
        val immuneStatus = when {
            immuneScore >= 85 -> "NK細胞・抗ウイルスサイトカイン活性高水準"
            immuneScore >= 70 -> "安定した免疫防御能を維持"
            else -> "睡眠負債による一時的な免疫バリア低下"
        }

        // 5. 翌日の覚醒・集中力予測 (Daytime Alertness Readiness)
        val focusScore = ((sleepScore * 0.6f) + (mentalScore * 0.4f)).toInt().coerceIn(30, 99)
        val peakWindow = when {
            focusScore >= 85 -> "09:30 - 12:00 および 15:00 - 17:30"
            focusScore >= 70 -> "10:00 - 12:00"
            else -> "11:00 - 12:30 (午後に眠気リスクあり)"
        }

        val skinStatus = when {
            skinScore >= 85 -> "角質水分保持能・キメ密度が極めて高い"
            skinScore >= 70 -> "キメ良好・部分的な血行促進ケア推奨"
            else -> "水分蒸散亢進・微小循環のうっ血傾向"
        }

        val overallHarmony = ((sleepScore * 0.35f) + (skinScore * 0.35f) + (physicalRecoveryScore * 0.15f) + (mentalScore * 0.15f)).toInt()

        return HolisticHealthAnalysis(
            dateString = todayStr,
            skinScore = skinScore,
            skinStatus = skinStatus,
            physicalRecoveryScore = physicalRecoveryScore,
            physicalRecoveryStatus = physicalStatus,
            mentalAutonomicScore = mentalScore,
            mentalAutonomicStatus = mentalStatus,
            circadianAlignmentScore = circadianScore,
            circadianStatus = circadianStatus,
            immunePotentialScore = immuneScore,
            immuneStatus = immuneStatus,
            daytimeFocusReadiness = focusScore,
            peakFocusTimeWindow = peakWindow,
            overallBioHarmonyScore = overallHarmony
        )
    }

    private fun buildPrompt(
        durationMins: Int,
        sleepScore: Int,
        deepSleep: Int,
        skinScore: Int,
        texture: Int,
        redness: Int,
        darkCircles: Int,
        correlationScore: Int
    ): String {
        return """
あなたは一流の皮膚科学者・睡眠医学スペシャリスト・臨床神経学者です。
以下の睡眠データおよびオンデバイス肌分析データ、全身バイオリズムを深く相関分析し、ユーザーに助言を行ってください。

【昨夜の睡眠データ】
- 総睡眠時間: ${durationMins / 60}時間${durationMins % 60}分
- 睡眠総合スコア: $sleepScore / 100
- 深層睡眠（ノンレムステージ3）割合: $deepSleep%

【今朝の肌データ】
- 総合肌スコア: $skinScore / 100
- キメ・平滑度: $texture / 100
- 赤み・皮膚炎症度: $redness / 100 (100が正常)
- 目元クマ・微小循環: $darkCircles / 100 (100が透明感良好)

【出力フォーマット】
以下の形式で簡潔に出力してください（装飾記号は最小限）:
HEADLINE: [25文字以内の核心的サマリー見出し]
ANALYSIS: [深層睡眠と肌バリア機能、成長ホルモン分泌、自律神経との生理学的因果関係の解説（80〜120字程度）]
ACTION1: [今日実践すべきスキンケアアクション]
ACTION2: [日光浴や血流改善、体内時計に関するアクション]
ACTION3: [今夜の快眠リセットのための具体的なアクション]
        """.trimIndent()
    }

    private fun parseGeminiResponse(
        text: String,
        dateStr: String,
        correlationScore: Int
    ): CorrelationInsight {
        var headline = "深層睡眠による高輝度肌サイクル"
        var analysis = "深層睡眠が安定して確保されたことで、夜間の成長ホルモン分泌が正常に機能。皮膚バリア機能が修復され、目元の微小循環が整っています。"
        val actions = mutableListOf<String>()

        text.lines().forEach { rawLine ->
            val line = rawLine.trim()
            when {
                line.startsWith("HEADLINE:") -> headline = line.removePrefix("HEADLINE:").trim()
                line.startsWith("ANALYSIS:") -> analysis = line.removePrefix("ANALYSIS:").trim()
                line.startsWith("ACTION1:") -> actions.add(line.removePrefix("ACTION1:").trim())
                line.startsWith("ACTION2:") -> actions.add(line.removePrefix("ACTION2:").trim())
                line.startsWith("ACTION3:") -> actions.add(line.removePrefix("ACTION3:").trim())
            }
        }

        if (actions.isEmpty()) {
            actions.addAll(
                listOf(
                    "高浸透セラミド化粧水でバリア機能を補給",
                    "起床後1時間以内に自然光を浴びてセロトニン分泌を活性化",
                    "カフェインは14時までとし夜間の深層睡眠を保護"
                )
            )
        }

        return CorrelationInsight(
            dateString = dateStr,
            correlationScore = correlationScore,
            headline = headline,
            detailedAnalysis = analysis,
            recommendedActions = actions,
            isGeneratedByAiThinking = true
        )
    }

    private fun generateLocalCorrelationInsight(
        dateStr: String,
        correlationScore: Int,
        sleep: SleepSession?,
        skin: SkinAnalysisResult?
    ): CorrelationInsight {
        val deepSleep = sleep?.deepSleepPercent ?: 20
        val texture = skin?.textureScore ?: 80
        val redness = skin?.rednessScore ?: 80

        val headline: String
        val analysis: String
        val actions: List<String>

        if (deepSleep >= 22 && texture >= 82) {
            headline = "黄金睡眠による高輝度肌サイクル"
            analysis = "深層睡眠（ノンレムステージ3）が${deepSleep}%確保され、成長ホルモンによる表皮ターンオーバーと細胞修復が最大化。微小血管の鬱血が解消されキメが整っています。"
            actions = listOf(
                "朝のスキンケア: 高保湿セラミドで今朝の高いキメ密度を一日キープ",
                "光と血流: 起床後30分以内にカーテンを開け、セロトニン分泌を活性化",
                "快眠維持: 今夜も同様の入眠時間（23時台）を維持して体内時計を固定"
            )
        } else if (deepSleep < 18) {
            headline = "成長ホルモン不足によるキメ低下傾向"
            analysis = "深層睡眠が${deepSleep}%とやや不足しており、夜間の角層水分保持バリアの修復が十分に行われていません。日中の乾燥対策と今夜の深層睡眠確保が必要です。"
            actions = listOf(
                "朝のスキンケア: 保湿クリームを薄く重ね塗りし、日中の水分蒸散をブロック",
                "血行促進: ぬるま湯洗顔後にホットタオルを目元にあて微小循環を改善",
                "快眠リセット: 就寝90分前に40℃の入浴を行い、深部体温の降下を促す"
            )
        } else {
            headline = "安定した生体リズム調和状態"
            analysis = "睡眠と肌状態は平均以上の良好な同期を保っています。自律神経バランスを維持することで、明朝の透明感向上が期待できます。"
            actions = listOf(
                "水分補給: 起床時に常温水300mlを補給し、就寝中の脱水をリカバリー",
                "日中ケア: 紫外線対策を行い、日中の酸化ストレスから肌を守る",
                "快眠準備: カフェイン摂取は14時までとし、就寝前のブルーライトを低減"
            )
        }

        return CorrelationInsight(
            dateString = dateStr,
            correlationScore = correlationScore,
            headline = headline,
            detailedAnalysis = analysis,
            recommendedActions = actions,
            isGeneratedByAiThinking = false
        )
    }
}
