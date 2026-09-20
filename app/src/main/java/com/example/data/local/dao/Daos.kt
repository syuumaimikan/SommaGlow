package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.SkinAnalysisEntity
import com.example.data.local.entity.SleepClassifyEntity
import com.example.data.local.entity.SleepSessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * 睡眠データのDAO
 */
@Dao
interface SleepDao {
    @Query("SELECT * FROM sleep_sessions ORDER BY endTimeMillis DESC LIMIT 1")
    fun getLatestSleepSession(): Flow<SleepSessionEntity?>

    @Query("SELECT * FROM sleep_sessions WHERE dateString = :dateString LIMIT 1")
    fun getSleepSessionForDate(dateString: String): Flow<SleepSessionEntity?>

    @Query("SELECT * FROM sleep_sessions WHERE dateString = :dateString LIMIT 1")
    suspend fun findSleepSessionByDate(dateString: String): SleepSessionEntity?

    @Query("SELECT * FROM sleep_sessions ORDER BY endTimeMillis DESC LIMIT :limit")
    fun getRecentSleepSessions(limit: Int): Flow<List<SleepSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepSession(session: SleepSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepSessions(sessions: List<SleepSessionEntity>)

    @Query("SELECT COUNT(*) FROM sleep_sessions")
    suspend fun getSleepSessionCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepClassify(classify: SleepClassifyEntity): Long

    @Query("SELECT * FROM sleep_classifications ORDER BY timestampMillis DESC LIMIT 50")
    fun getRecentClassifications(): Flow<List<SleepClassifyEntity>>

    @Query("DELETE FROM sleep_sessions WHERE dateString = :dateString")
    suspend fun deleteSleepSessionByDate(dateString: String): Int

    @Query("DELETE FROM sleep_sessions")
    suspend fun clearAllSleepSessions(): Int
}

/**
 * 肌分析データのDAO
 */
@Dao
interface SkinDao {
    @Query("SELECT * FROM skin_analyses ORDER BY timestampMillis DESC LIMIT 1")
    fun getLatestSkinAnalysis(): Flow<SkinAnalysisEntity?>

    @Query("SELECT * FROM skin_analyses WHERE dateString = :dateString LIMIT 1")
    fun getSkinAnalysisForDate(dateString: String): Flow<SkinAnalysisEntity?>

    @Query("SELECT * FROM skin_analyses WHERE dateString = :dateString LIMIT 1")
    suspend fun findSkinAnalysisByDate(dateString: String): SkinAnalysisEntity?

    @Query("SELECT * FROM skin_analyses ORDER BY timestampMillis DESC LIMIT :limit")
    fun getRecentSkinAnalyses(limit: Int): Flow<List<SkinAnalysisEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkinAnalysis(analysis: SkinAnalysisEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkinAnalyses(analyses: List<SkinAnalysisEntity>)

    @Query("SELECT COUNT(*) FROM skin_analyses")
    suspend fun getSkinAnalysisCount(): Int

    @Query("DELETE FROM skin_analyses WHERE dateString = :dateString")
    suspend fun deleteSkinAnalysisByDate(dateString: String): Int

    @Query("DELETE FROM skin_analyses")
    suspend fun clearAllSkinAnalyses(): Int
}
