package app.piptally

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import app.piptally.ui.navigation.DominoNavGraph
import app.piptally.ui.theme.PipTallyTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: app.piptally.domain.repository.SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeState by settingsRepository.themeFlow.collectAsState()
            PipTallyTheme(appTheme = themeState) {
                DominoNavGraph()
            }
        }
    }
}
