package app.piptally.data.repository

import android.content.Context
import app.piptally.data.local.AppDatabase
import app.piptally.domain.repository.AppTheme
import app.piptally.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase
) : SettingsRepository {

    private val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    private val _themeFlow = MutableStateFlow(getSavedTheme())
    override val themeFlow: StateFlow<AppTheme> = _themeFlow.asStateFlow()

    override fun setTheme(theme: AppTheme) {
        prefs.edit().putString("app_theme", theme.name).apply()
        _themeFlow.value = theme
    }

    override suspend fun resetAllData() {
        database.clearAllTables()
    }

    private fun getSavedTheme(): AppTheme {
        val name = prefs.getString("app_theme", AppTheme.SYSTEM.name) ?: AppTheme.SYSTEM.name
        return runCatching { AppTheme.valueOf(name) }.getOrDefault(AppTheme.SYSTEM)
    }
}
