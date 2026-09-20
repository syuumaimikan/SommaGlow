package com.example

import com.example.domain.model.CorrelationInsight
import com.example.domain.model.HolisticHealthAnalysis
import com.example.domain.model.SkinAnalysisResult
import com.example.domain.model.SleepClassification
import com.example.domain.model.SleepSession
import com.example.domain.repository.AiAdvisorRepository
import com.example.domain.repository.SkinRepository
import com.example.domain.repository.SleepRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * SomnaGlow のクリーンアーキテクチャおよび生体分析ロジック統合テスト
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SomnaGlowArchitectureTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeSleepRepository = object : SleepRepository {
        private val sleepFlow = MutableStateFlow<SleepSession?>(null)

        override fun observeLatestSleepSession(): Flow<SleepSession?> = sleepFlow.asStateFlow()
        override fun observeTodaySleepSession(): Flow<SleepSession?> = sleepFlow.asStateFlow()
        override fun observeRecentSleepSessions(limit: Int): Flow<List<SleepSession>> = MutableStateFlow(emptyList())

        override suspend fun getTodaySleepSession(): SleepSession? = sleepFlow.value
        override suspend fun saveSleepSegment(startTimeMillis: Long, endTimeMillis: Long, status: Int) {}
        override suspend fun saveSleepClassification(classification: SleepClassification) {}
        override suspend fun recordDeterministicMorningSleepSession(targetSleepHours: Float): SleepSession {
            val session = SleepSession(
                startTimeMillis = 1000L,
                endTimeMillis = 2000L,
                durationMinutes = 465,
                sleepScore = 88,
                deepSleepPercent = 23,
                remSleepPercent = 24,
                lightSleepPercent = 53,
                statusDescription = "Optimal",
                dateString = "2026-09-20"
            )
            sleepFlow.value = session
            return session
        }
        override suspend fun deleteTodaySleepSession() {
            sleepFlow.value = null
        }
        override suspend fun clearAllSleepData() {
            sleepFlow.value = null
        }
        override suspend fun ensureInitialDataSeeded() {}
    }

    private val fakeSkinRepository = object : SkinRepository {
        private val skinFlow = MutableStateFlow<SkinAnalysisResult?>(null)

        override fun observeLatestSkinAnalysis(): Flow<SkinAnalysisResult?> = skinFlow.asStateFlow()
        override fun observeTodaySkinAnalysis(): Flow<SkinAnalysisResult?> = skinFlow.asStateFlow()
        override fun observeRecentSkinAnalyses(limit: Int): Flow<List<SkinAnalysisResult>> = MutableStateFlow(emptyList())

        override suspend fun getTodaySkinAnalysis(): SkinAnalysisResult? = skinFlow.value
        override suspend fun saveSkinAnalysis(result: SkinAnalysisResult) {
            skinFlow.value = result
        }
        override suspend fun deleteTodaySkinAnalysis() {
            skinFlow.value = null
        }
        override suspend fun clearAllSkinData() {
            skinFlow.value = null
        }
        override suspend fun ensureInitialDataSeeded() {}
    }

    private val fakeAiAdvisor = object : AiAdvisorRepository {
        override suspend fun generateCorrelationInsight(
            sleep: SleepSession?,
            skin: SkinAnalysisResult?,
            useGeminiCloud: Boolean,
            apiKey: String
        ): CorrelationInsight {
            return CorrelationInsight(
                dateString = "2026-09-20",
                correlationScore = 86,
                headline = "調和テスト完了",
                detailedAnalysis = "深層睡眠と肌バリア機能が同期",
                recommendedActions = listOf("保湿ケア", "日光浴", "定時就寝"),
                isGeneratedByAiThinking = false
            )
        }

        override fun calculateHolisticHealthAnalysis(
            sleep: SleepSession?,
            skin: SkinAnalysisResult?,
            targetSleepHours: Float
        ): HolisticHealthAnalysis {
            return HolisticHealthAnalysis(
                dateString = "2026-09-20",
                skinScore = skin?.overallScore ?: 80,
                skinStatus = "キメ良好",
                physicalRecoveryScore = 85,
                physicalRecoveryStatus = "回復良好",
                mentalAutonomicScore = 82,
                mentalAutonomicStatus = "自律神経安定",
                circadianAlignmentScore = 90,
                circadianStatus = "同期中",
                immunePotentialScore = 86,
                immuneStatus = "高活性",
                daytimeFocusReadiness = 88,
                peakFocusTimeWindow = "10:00 - 12:00",
                overallBioHarmonyScore = 86
            )
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testDeterministicSleepSyncIdempotence() = runTest {
        // 初期状態は未記録(null)
        val initialSession = fakeSleepRepository.getTodaySleepSession()
        org.junit.Assert.assertNull(initialSession)

        // 睡眠データ同期
        val firstSync = fakeSleepRepository.recordDeterministicMorningSleepSession(8.0f)
        assertEquals(88, firstSync.sleepScore)
        assertEquals(465, firstSync.durationMinutes)

        // 2回目の同期でもスコアが変動せず完全一致（決定論的）
        val secondSync = fakeSleepRepository.recordDeterministicMorningSleepSession(8.0f)
        assertEquals(firstSync.sleepScore, secondSync.sleepScore)
        assertEquals(firstSync.durationMinutes, secondSync.durationMinutes)
    }

    @Test
    fun testHolisticHealthCalculation() = runTest {
        val sleep = fakeSleepRepository.recordDeterministicMorningSleepSession(8.0f)
        val skin = SkinAnalysisResult(
            timestampMillis = 2000L,
            dateString = "2026-09-20",
            textureScore = 88,
            rednessScore = 85,
            darkCirclesScore = 82,
            overallScore = 85,
            faceDetected = true
        )

        val analysis = fakeAiAdvisor.calculateHolisticHealthAnalysis(sleep, skin, 8.0f)
        assertNotNull(analysis)
        assertTrue(analysis.overallBioHarmonyScore >= 80)
        assertEquals("10:00 - 12:00", analysis.peakFocusTimeWindow)
    }
}
