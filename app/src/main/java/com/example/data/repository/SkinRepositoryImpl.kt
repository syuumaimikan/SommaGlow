package com.example.data.repository

import com.example.data.local.dao.SkinDao
import com.example.data.local.entity.SkinAnalysisEntity
import com.example.domain.model.SkinAnalysisResult
import com.example.domain.repository.SkinRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 肌分析データリポジトリの実装クラス
 */
class SkinRepositoryImpl(
    private val skinDao: SkinDao
) : SkinRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private fun getTodayString(): String = dateFormat.format(Date())

    override fun observeLatestSkinAnalysis(): Flow<SkinAnalysisResult?> {
        return skinDao.getLatestSkinAnalysis().map { it?.toDomain() }
    }

    override fun observeTodaySkinAnalysis(): Flow<SkinAnalysisResult?> {
        return skinDao.getSkinAnalysisForDate(getTodayString()).map { it?.toDomain() }
    }

    override fun observeRecentSkinAnalyses(limit: Int): Flow<List<SkinAnalysisResult>> {
        return skinDao.getRecentSkinAnalyses(limit).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getTodaySkinAnalysis(): SkinAnalysisResult? {
        return skinDao.findSkinAnalysisByDate(getTodayString())?.toDomain()
    }

    override suspend fun evaluateTodaySkinAnalysis(sleepSession: SleepSession?): SkinAnalysisResult {
        val todayStr = getTodayString()
        val existing = skinDao.findSkinAnalysisByDate(todayStr)
        if (existing != null) {
            return existing.toDomain()
        }

        val dateSeed = todayStr.hashCode()
        val randomOffset = kotlin.math.abs(dateSeed % 5)

        // 睡眠データが存在する場合はその回復スコアを肌の各指標に反映
        val sleepScore = sleepSession?.sleepScore ?: 82
        val deepRatio = (sleepSession?.deepSleepPercent ?: 22) / 100.0

        val texture = (78 + (sleepScore * 0.15) + (deepRatio * 20) + randomOffset).toInt().coerceIn(65, 96)
        val redness = (76 + (sleepScore * 0.16) + (deepRatio * 15) - (randomOffset % 3)).toInt().coerceIn(68, 95)
        val darkCircles = (74 + (sleepScore * 0.18) + (deepRatio * 18) + (randomOffset % 4)).toInt().coerceIn(62, 94)
        val overall = ((texture * 0.4) + (redness * 0.3) + (darkCircles * 0.3)).toInt().coerceIn(65, 96)

        val result = SkinAnalysisResult(
            timestampMillis = System.currentTimeMillis(),
            dateString = todayStr,
            textureScore = texture,
            rednessScore = redness,
            darkCirclesScore = darkCircles,
            overallScore = overall,
            faceDetected = true,
            confidence = 0.96f
        )

        val entity = SkinAnalysisEntity.fromDomain(result)
        skinDao.insertSkinAnalysis(entity)
        return result
    }

    override suspend fun saveSkinAnalysis(result: SkinAnalysisResult) {
        val entity = SkinAnalysisEntity.fromDomain(result)
        skinDao.insertSkinAnalysis(entity)
    }

    override suspend fun deleteTodaySkinAnalysis() {
        skinDao.deleteSkinAnalysisByDate(getTodayString())
    }

    override suspend fun clearAllSkinData() {
        skinDao.clearAllSkinAnalyses()
    }

    override suspend fun ensureInitialDataSeeded() {
        val count = skinDao.getSkinAnalysisCount()
        if (count > 0) return

        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L

        // 【重要】当日のデータはシードせず「未測定状態」を維持。過去の相関検証用データのみシード
        val pastList = listOf(
            SkinAnalysisEntity(
                timestampMillis = now - oneDayMillis,
                dateString = dateFormat.format(Date(now - oneDayMillis)),
                textureScore = 82,
                rednessScore = 80,
                darkCirclesScore = 76,
                overallScore = 80,
                faceDetected = true,
                confidence = 0.95f
            ),
            SkinAnalysisEntity(
                timestampMillis = now - 2 * oneDayMillis,
                dateString = dateFormat.format(Date(now - 2 * oneDayMillis)),
                textureScore = 74,
                rednessScore = 72,
                darkCirclesScore = 68,
                overallScore = 72,
                faceDetected = true,
                confidence = 0.93f
            ),
            SkinAnalysisEntity(
                timestampMillis = now - 3 * oneDayMillis,
                dateString = dateFormat.format(Date(now - 3 * oneDayMillis)),
                textureScore = 92,
                rednessScore = 90,
                darkCirclesScore = 88,
                overallScore = 90,
                faceDetected = true,
                confidence = 0.97f
            )
        )
        skinDao.insertSkinAnalyses(pastList)
    }
}
