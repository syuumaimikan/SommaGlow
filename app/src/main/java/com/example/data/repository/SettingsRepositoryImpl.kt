package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.domain.model.AiEngineMode
import com.example.domain.model.AppSettings
import com.example.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class SettingsRepositoryImpl(
    context: Context
) : SettingsRepository {

    private val prefs: SharedPreferences = context.getSharedPreferences("somnaglow_settings", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())

    private fun loadSettings(): AppSettings {
        val modeStr = prefs.getString(KEY_AI_ENGINE_MODE, AiEngineMode.LOCAL_ON_DEVICE.name)
        val mode = try {
            AiEngineMode.valueOf(modeStr ?: AiEngineMode.LOCAL_ON_DEVICE.name)
        } catch (e: Exception) {
            AiEngineMode.LOCAL_ON_DEVICE
        }

        return AppSettings(
            isBackgroundTrackingEnabled = prefs.getBoolean(KEY_BG_TRACKING, true),
            aiEngineMode = mode,
            geminiApiKey = prefs.getString(KEY_GEMINI_API_KEY, "") ?: "",
            targetSleepHours = prefs.getFloat(KEY_TARGET_SLEEP_HOURS, 8.0f),
            isSkinScanReminderEnabled = prefs.getBoolean(KEY_SKIN_REMINDER_ENABLED, true),
            skinScanReminderTime = prefs.getString(KEY_SKIN_REMINDER_TIME, "07:30") ?: "07:30",
            analyzeSkin = prefs.getBoolean(KEY_ANALYZE_SKIN, true),
            analyzePhysicalRecovery = prefs.getBoolean(KEY_ANALYZE_PHYSICAL, true),
            analyzeMentalAutonomic = prefs.getBoolean(KEY_ANALYZE_MENTAL, true),
            analyzeCircadian = prefs.getBoolean(KEY_ANALYZE_CIRCADIAN, true),
            analyzeImmune = prefs.getBoolean(KEY_ANALYZE_IMMUNE, true),
            analyzeFocusReadiness = prefs.getBoolean(KEY_ANALYZE_FOCUS, true)
        )
    }

    override fun observeSettings(): Flow<AppSettings> = _settingsFlow.asStateFlow()

    override suspend fun getSettings(): AppSettings = withContext(Dispatchers.IO) {
        loadSettings()
    }

    override suspend fun updateSettings(settings: AppSettings) = withContext(Dispatchers.IO) {
        prefs.edit()
            .putBoolean(KEY_BG_TRACKING, settings.isBackgroundTrackingEnabled)
            .putString(KEY_AI_ENGINE_MODE, settings.aiEngineMode.name)
            .putString(KEY_GEMINI_API_KEY, settings.geminiApiKey)
            .putFloat(KEY_TARGET_SLEEP_HOURS, settings.targetSleepHours)
            .putBoolean(KEY_SKIN_REMINDER_ENABLED, settings.isSkinScanReminderEnabled)
            .putString(KEY_SKIN_REMINDER_TIME, settings.skinScanReminderTime)
            .putBoolean(KEY_ANALYZE_SKIN, settings.analyzeSkin)
            .putBoolean(KEY_ANALYZE_PHYSICAL, settings.analyzePhysicalRecovery)
            .putBoolean(KEY_ANALYZE_MENTAL, settings.analyzeMentalAutonomic)
            .putBoolean(KEY_ANALYZE_CIRCADIAN, settings.analyzeCircadian)
            .putBoolean(KEY_ANALYZE_IMMUNE, settings.analyzeImmune)
            .putBoolean(KEY_ANALYZE_FOCUS, settings.analyzeFocusReadiness)
            .apply()

        _settingsFlow.value = settings
    }

    override suspend fun setBackgroundTracking(enabled: Boolean) = withContext(Dispatchers.IO) {
        prefs.edit().putBoolean(KEY_BG_TRACKING, enabled).apply()
        _settingsFlow.value = _settingsFlow.value.copy(isBackgroundTrackingEnabled = enabled)
    }

    override suspend fun setAiEngineMode(mode: AiEngineMode) = withContext(Dispatchers.IO) {
        prefs.edit().putString(KEY_AI_ENGINE_MODE, mode.name).apply()
        _settingsFlow.value = _settingsFlow.value.copy(aiEngineMode = mode)
    }

    override suspend fun setGeminiApiKey(apiKey: String) = withContext(Dispatchers.IO) {
        prefs.edit().putString(KEY_GEMINI_API_KEY, apiKey).apply()
        _settingsFlow.value = _settingsFlow.value.copy(geminiApiKey = apiKey)
    }

    override suspend fun setTargetSleepHours(hours: Float) = withContext(Dispatchers.IO) {
        prefs.edit().putFloat(KEY_TARGET_SLEEP_HOURS, hours).apply()
        _settingsFlow.value = _settingsFlow.value.copy(targetSleepHours = hours)
    }

    override suspend fun setSkinScanReminder(enabled: Boolean, time: String) = withContext(Dispatchers.IO) {
        prefs.edit()
            .putBoolean(KEY_SKIN_REMINDER_ENABLED, enabled)
            .putString(KEY_SKIN_REMINDER_TIME, time)
            .apply()
        _settingsFlow.value = _settingsFlow.value.copy(
            isSkinScanReminderEnabled = enabled,
            skinScanReminderTime = time
        )
    }

    companion object {
        private const val KEY_BG_TRACKING = "is_background_tracking_enabled"
        private const val KEY_AI_ENGINE_MODE = "ai_engine_mode"
        private const val KEY_GEMINI_API_KEY = "gemini_api_key"
        private const val KEY_TARGET_SLEEP_HOURS = "target_sleep_hours"
        private const val KEY_SKIN_REMINDER_ENABLED = "is_skin_scan_reminder_enabled"
        private const val KEY_SKIN_REMINDER_TIME = "skin_scan_reminder_time"
        private const val KEY_ANALYZE_SKIN = "analyze_skin"
        private const val KEY_ANALYZE_PHYSICAL = "analyze_physical"
        private const val KEY_ANALYZE_MENTAL = "analyze_mental"
        private const val KEY_ANALYZE_CIRCADIAN = "analyze_circadian"
        private const val KEY_ANALYZE_IMMUNE = "analyze_immune"
        private const val KEY_ANALYZE_FOCUS = "analyze_focus"
    }
}
