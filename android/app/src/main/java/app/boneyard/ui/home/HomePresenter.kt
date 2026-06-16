package app.boneyard.ui.home

import android.graphics.Color as AndroidColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import app.boneyard.data.local.dao.GamePlayerDao
import app.boneyard.data.local.dao.PlayerDao
import app.boneyard.di.AppScope
import app.boneyard.domain.model.GameStatus
import app.boneyard.domain.repository.GameRepository
import app.boneyard.domain.repository.SettingsRepository
import app.boneyard.domain.usecase.game.GetActiveGamesUseCase
import app.boneyard.domain.usecase.template.DeleteTemplateUseCase
import app.boneyard.domain.usecase.template.GetAllTemplatesUseCase
import app.boneyard.ui.navigation.ActiveGameScreen
import app.boneyard.ui.navigation.GameHistoryScreen
import app.boneyard.ui.navigation.GameSetupScreen
import app.boneyard.ui.navigation.HomeScreen
import app.boneyard.ui.navigation.PlayerListScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.launch

class HomePresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    private val getActiveGamesUseCase: GetActiveGamesUseCase,
    private val getAllTemplatesUseCase: GetAllTemplatesUseCase,
    private val deleteTemplateUseCase: DeleteTemplateUseCase,
    private val settingsRepository: SettingsRepository,
    private val gameRepository: GameRepository,
    private val playerDao: PlayerDao,
    private val gamePlayerDao: GamePlayerDao,
) : Presenter<HomeUiState> {

    @CircuitInject(HomeScreen::class, AppScope::class)
    @AssistedFactory
    fun interface Factory {
        fun create(navigator: Navigator): HomePresenter
    }

    @Composable
    override fun present(): HomeUiState {
        val activeGames by getActiveGamesUseCase().collectAsState(emptyList())
        val allPlayers by playerDao.getAllPlayers().collectAsState(emptyList())
        val allTemplates by getAllTemplatesUseCase().collectAsState(emptyList())
        val theme by settingsRepository.themeFlow.collectAsState()
        val scope = rememberCoroutineScope()

        var summaries by remember { mutableStateOf<List<ActiveGameSummary>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }

        LaunchedEffect(activeGames, allPlayers) {
            val playerMap = allPlayers.associateBy { it.id }
            summaries = activeGames.map { game ->
                val gamePlayers = gamePlayerDao.getPlayersForGameOnce(game.id)
                val names = gamePlayers
                    .sortedBy { it.seatPosition }
                    .mapNotNull { gp -> playerMap[gp.playerId]?.name }
                ActiveGameSummary(
                    gameId = game.id,
                    playerNames = names,
                    currentRound = game.currentRoundIndex + 1,
                )
            }
            isLoading = false
        }

        val templates = allTemplates.map { template ->
            TemplateSummary(
                templateId = template.id,
                name = template.name,
                players = template.players.map { tp ->
                    TemplateSummary.PlayerInfo(
                        name = tp.player.name,
                        color = runCatching {
                            Color(AndroidColor.parseColor(tp.player.color))
                        }.getOrDefault(Color(0xFF1E88E5)),
                        avatarIndex = tp.player.avatarIndex,
                    )
                },
            )
        }

        return HomeUiState(
            activePausedGames = summaries,
            templates = templates,
            isLoading = isLoading,
            activeTheme = theme,
            eventSink = { event ->
                when (event) {
                    HomeEvent.NewGame -> navigator.goTo(GameSetupScreen())
                    HomeEvent.Players -> navigator.goTo(PlayerListScreen)
                    HomeEvent.History -> navigator.goTo(GameHistoryScreen)
                    is HomeEvent.ResumeGame -> navigator.goTo(ActiveGameScreen(event.gameId))
                    is HomeEvent.AbandonGame -> scope.launch {
                        gameRepository.updateStatus(event.gameId, GameStatus.ABANDONED)
                    }
                    is HomeEvent.DeleteGame -> scope.launch {
                        gameRepository.deleteGame(event.gameId)
                    }
                    is HomeEvent.SetTheme -> settingsRepository.setTheme(event.theme)
                    HomeEvent.ResetData -> scope.launch { settingsRepository.resetAllData() }
                    is HomeEvent.StartFromTemplate -> {
                        allTemplates.find { it.id == event.templateId }?.let { template ->
                            val playerIds = template.players.sortedBy { it.seatPosition }.map { it.player.id }
                            navigator.goTo(GameSetupScreen(preselectedPlayerIds = playerIds, initialStep = 1))
                        }
                    }
                    is HomeEvent.DeleteTemplate -> scope.launch {
                        deleteTemplateUseCase(event.templateId)
                    }
                }
            }
        )
    }
}
