package com.example.domain.repository

import com.example.domain.model.CorrelationInsight
import com.example.domain.model.HolisticHealthAnalysis
import com.example.domain.model.SkinAnalysisResult
import com.example.domain.model.SleepClassification
import com.example.domain.model.SleepSession
import kotlinx.coroutines.flow.Flow

/**
 * 睡眠データのリポジトリインターフェース
 * Sleep API から受信したデータの永続化およびUI層へのデータストリーム提供を責務とする
 */
interface SleepRepository {
    fun observeLatestSleepSession(): Flow<SleepSession?>
    fun observeTodaySleepSession(): Flow<SleepSession?>
    fun observeRecentSleepSessions(limit: Int = 7): Flow<List<SleepSession>>

    suspend fun getTodaySleepSession(): SleepSession?
    suspend fun saveSleepSegment(startTimeMillis: Long, endTimeMillis: Long, status: Int)
    suspend fun saveSleepClassification(classification: SleepClassification)
    suspend fun recordDeterministicMorningSleepSession(targetSleepHours: Float = 8.0f): SleepSession
    suspend fun deleteTodaySleepSession()
    suspend fun clearAllSleepData()
    suspend fun ensureInitialDataSeeded()
}

/**
 * 肌分析データのリポジトリインターフェース
 * オンデバイスMLで分析された肌スコアの永続化とストリーム提供
 */
interface SkinRepository {
    fun observeLatestSkinAnalysis(): Flow<SkinAnalysisResult?>
    fun observeTodaySkinAnalysis(): Flow<SkinAnalysisResult?>
    fun observeRecentSkinAnalyses(limit: Int = 7): Flow<List<SkinAnalysisResult>>

    suspend fun getTodaySkinAnalysis(): SkinAnalysisResult?
    suspend fun evaluateTodaySkinAnalysis(sleepSession: SleepSession? = null): SkinAnalysisResult
    suspend fun saveSkinAnalysis(result: SkinAnalysisResult)
    suspend fun deleteTodaySkinAnalysis()
    suspend fun clearAllSkinData()
    suspend fun ensureInitialDataSeeded()
}

/**
 * 睡眠・生体総合相関分析およびAIアドバイザー
 */
interface AiAdvisorRepository {
    suspend fun generateCorrelationInsight(
        sleep: SleepSession?,
        skin: SkinAnalysisResult?,
        useGeminiCloud: Boolean = false,
        apiKey: String = ""
    ): CorrelationInsight

    fun calculateHolisticHealthAnalysis(
        sleep: SleepSession?,
        skin: SkinAnalysisResult?,
        targetSleepHours: Float = 8.0f
    ): HolisticHealthAnalysis
}
