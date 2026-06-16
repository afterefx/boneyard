package app.boneyard.ui.navigation

import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize

@Parcelize
data object HomeScreen : Screen

@Parcelize
data object PlayerListScreen : Screen

@Parcelize
data class PlayerEditScreen(val playerId: Long = -1L) : Screen

@Parcelize
data class PlayerProfileScreen(val playerId: Long) : Screen

@Parcelize
data class GameSetupScreen(
    val preselectedPlayerIds: List<Long> = emptyList(),
    val initialStep: Int = 0,
) : Screen

@Parcelize
data class ActiveGameScreen(val gameId: Long) : Screen

@Parcelize
data class GameSummaryScreen(val gameId: Long) : Screen

@Parcelize
data object GameHistoryScreen : Screen
