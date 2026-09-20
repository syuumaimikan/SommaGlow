package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.SkinAnalysisResult
import com.example.domain.model.SleepClassification
import com.example.domain.model.SleepSession

/**
 * 睡眠セッションのRoomエンティティ
 */
@Entity(tableName = "sleep_sessions")
data class SleepSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val durationMinutes: Int,
    val sleepScore: Int,
    val deepSleepPercent: Int,
    val remSleepPercent: Int,
    val lightSleepPercent: Int,
    val statusDescription: String,
    val dateString: String
) {
    fun toDomain(): SleepSession = SleepSession(
        id = id,
        startTimeMillis = startTimeMillis,
        endTimeMillis = endTimeMillis,
        durationMinutes = durationMinutes,
        sleepScore = sleepScore,
        deepSleepPercent = deepSleepPercent,
        remSleepPercent = remSleepPercent,
        lightSleepPercent = lightSleepPercent,
        statusDescription = statusDescription,
        dateString = dateString
    )

    companion object {
        fun fromDomain(model: SleepSession): SleepSessionEntity = SleepSessionEntity(
            id = model.id,
            startTimeMillis = model.startTimeMillis,
            endTimeMillis = model.endTimeMillis,
            durationMinutes = model.durationMinutes,
            sleepScore = model.sleepScore,
            deepSleepPercent = model.deepSleepPercent,
            remSleepPercent = model.remSleepPercent,
            lightSleepPercent = model.lightSleepPercent,
            statusDescription = model.statusDescription,
            dateString = model.dateString
        )
    }
}

/**
 * Sleep APIのリアルタイム分類エンティティ
 */
@Entity(tableName = "sleep_classifications")
data class SleepClassifyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestampMillis: Long,
    val confidence: Int,
    val motionState: Int,
    val lightLevel: Int
) {
    fun toDomain(): SleepClassification = SleepClassification(
        timestampMillis = timestampMillis,
        confidence = confidence,
        motionState = motionState,
        lightLevel = lightLevel
    )
}

/**
 * オンデバイス肌分析結果のRoomエンティティ
 */
@Entity(tableName = "skin_analyses")
data class SkinAnalysisEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestampMillis: Long,
    val dateString: String,
    val textureScore: Int,
    val rednessScore: Int,
    val darkCirclesScore: Int,
    val overallScore: Int,
    val faceDetected: Boolean,
    val confidence: Float
) {
    fun toDomain(): SkinAnalysisResult = SkinAnalysisResult(
        id = id,
        timestampMillis = timestampMillis,
        dateString = dateString,
        textureScore = textureScore,
        rednessScore = rednessScore,
        darkCirclesScore = darkCirclesScore,
        overallScore = overallScore,
        faceDetected = faceDetected,
        confidence = confidence
    )

    companion object {
        fun fromDomain(model: SkinAnalysisResult): SkinAnalysisEntity = SkinAnalysisEntity(
            id = model.id,
            timestampMillis = model.timestampMillis,
            dateString = model.dateString,
            textureScore = model.textureScore,
            rednessScore = model.rednessScore,
            darkCirclesScore = model.darkCirclesScore,
            overallScore = model.overallScore,
            faceDetected = model.faceDetected,
            confidence = model.confidence
        )
    }
}
