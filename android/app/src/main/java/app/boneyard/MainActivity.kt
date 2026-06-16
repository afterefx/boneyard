package app.boneyard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import app.boneyard.ui.navigation.HomeScreen
import app.boneyard.ui.theme.BoneyardTheme
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.foundation.navstack.rememberSaveableNavStack

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appGraph = (application as BoneyardApp).appGraph
        enableEdgeToEdge()
        setContent {
            val theme by appGraph.settingsRepository.themeFlow.collectAsState()
            BoneyardTheme(appTheme = theme) {
                val navStack = rememberSaveableNavStack(HomeScreen) {}
                val navigator = rememberCircuitNavigator(navStack) { finish() }
                NavigableCircuitContent(navigator, navStack, circuit = appGraph.circuit)
            }
        }
    }
}
