package app.boneyard.ui.player.list

import android.graphics.Color as AndroidColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import app.boneyard.data.local.dao.GameDao
import app.boneyard.data.local.dao.GamePlayerDao
import app.boneyard.di.AppScope
import app.boneyard.domain.repository.PlayerRepository
import app.boneyard.domain.usecase.player.DeletePlayerUseCase
import app.boneyard.ui.navigation.PlayerEditScreen
import app.boneyard.ui.navigation.PlayerListScreen
import app.boneyard.ui.navigation.PlayerProfileScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import dev.zacsweers.metro.AssistedInject

class PlayerListPresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    private val playerRepository: PlayerRepository,
    private val deletePlayerUseCase: DeletePlayerUseCase,
    private val gamePlayerDao: GamePlayerDao,
    private val gameDao: GameDao,
) : Presenter<PlayerListUiState> {

    @CircuitInject(PlayerListScreen::class, AppScope::class)
    @AssistedFactory
    fun interface Factory {
        fun create(navigator: Navigator): PlayerListPresenter
    }

    @Composable
    override fun present(): PlayerListUiState {
        val scope = rememberCoroutineScope()

        val uiState by remember {
            combine(
                playerRepository.getAllPlayers(),
                gameDao.getCompletedGames()
            ) { players, completedGames ->
                val items = players.map { player ->
                    var gamesPlayed = 0
                    var gamesWon = 0
                    completedGames.forEach { game ->
                        val gp = gamePlayerDao.getPlayersForGameOnce(game.id)
                            .find { it.playerId == player.id }
                        if (gp != null) {
                            gamesPlayed++
                            if (gp.isWinner) gamesWon++
                        }
                    }
                    PlayerListItem(
                        playerId = player.id,
                        name = player.name,
                        color = runCatching {
                            Color(AndroidColor.parseColor(player.color))
                        }.getOrDefault(Color(0xFF1E88E5)),
                        avatarIndex = player.avatarIndex,
                        gamesPlayed = gamesPlayed,
                        gamesWon = gamesWon,
                    )
                }
                PlayerListUiState(players = items, isLoading = false)
            }
        }.collectAsState(PlayerListUiState(isLoading = true))

        return uiState.copy(
            eventSink = { event ->
                when (event) {
                    PlayerListEvent.Back -> navigator.pop()
                    PlayerListEvent.AddPlayer -> navigator.goTo(PlayerEditScreen())
                    is PlayerListEvent.EditPlayer -> navigator.goTo(PlayerEditScreen(event.playerId))
                    is PlayerListEvent.PlayerProfile -> navigator.goTo(PlayerProfileScreen(event.playerId))
                    is PlayerListEvent.DeletePlayer -> scope.launch {
                        val player = playerRepository.getPlayerById(event.playerId) ?: return@launch
                        deletePlayerUseCase(player)
                    }
                }
            }
        )
    }
}
