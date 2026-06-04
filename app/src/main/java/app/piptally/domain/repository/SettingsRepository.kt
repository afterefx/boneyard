package app.piptally.domain.repository

import kotlinx.coroutines.flow.StateFlow

enum class AppTheme {
    SYSTEM,
    OLED,
    SLATE
}

interface SettingsRepository {
    val themeFlow: StateFlow<AppTheme>
    fun setTheme(theme: AppTheme)
    suspend fun resetAllData()
}
