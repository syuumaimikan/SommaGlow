package com.example.domain.repository

import com.example.domain.model.AiEngineMode
import com.example.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<AppSettings>
    suspend fun getSettings(): AppSettings
    suspend fun updateSettings(settings: AppSettings)
    suspend fun setBackgroundTracking(enabled: Boolean)
    suspend fun setAiEngineMode(mode: AiEngineMode)
    suspend fun setGeminiApiKey(apiKey: String)
    suspend fun setTargetSleepHours(hours: Float)
    suspend fun setSkinScanReminder(enabled: Boolean, time: String)
}
