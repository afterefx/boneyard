package app.boneyard.data.repository

import android.content.Context
import app.boneyard.data.local.AppDatabase
import app.boneyard.di.AppScope
import app.boneyard.domain.repository.AppTheme
import app.boneyard.domain.repository.SettingsRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import dev.zacsweers.metro.Inject

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class SettingsRepositoryImpl @Inject constructor(
    private val context: Context,
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
