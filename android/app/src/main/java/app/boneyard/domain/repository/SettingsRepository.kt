package app.boneyard.domain.repository

import kotlinx.coroutines.flow.StateFlow

enum class AppTheme {
    SYSTEM,
    OLED,
    SLATE,
    LIGHT
}

interface SettingsRepository {
    val themeFlow: StateFlow<AppTheme>
    fun setTheme(theme: AppTheme)
    suspend fun resetAllData()
}
