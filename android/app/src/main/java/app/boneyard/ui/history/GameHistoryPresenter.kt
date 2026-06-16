package app.boneyard.ui.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import app.boneyard.data.local.dao.GamePlayerDao
import app.boneyard.data.local.dao.PlayerDao
import app.boneyard.di.AppScope
import app.boneyard.domain.model.GameStatus
import app.boneyard.domain.repository.GameRepository
import app.boneyard.ui.navigation.GameHistoryScreen
import app.boneyard.ui.navigation.GameSummaryScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import dev.zacsweers.metro.AssistedInject

class GameHistoryPresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    private val gameRepository: GameRepository,
    private val gamePlayerDao: GamePlayerDao,
    private val playerDao: PlayerDao,
) : Presenter<GameHistoryUiState> {

    @CircuitInject(GameHistoryScreen::class, AppScope::class)
    @AssistedFactory
    fun interface Factory {
        fun create(navigator: Navigator): GameHistoryPresenter
    }

    @Composable
    override fun present(): GameHistoryUiState {
        val scope = rememberCoroutineScope()

        val uiState by remember {
            val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            gameRepository.getCompletedGames().map { games ->
                val items = games.map { game ->
                    val gamePlayers = gamePlayerDao.getPlayersForGameOnce(game.id)
                        .sortedBy { it.seatPosition }
                    val playerScores = gamePlayers.mapNotNull { gp ->
                        val entity = playerDao.getPlayerById(gp.playerId) ?: return@mapNotNull null
                        entity.name to gp.totalScore
                    }
                    val winnerEntity = game.winnerPlayerId?.let { playerDao.getPlayerById(it) }
                    val dateString = (game.completedAt ?: game.createdAt).let { dateFormat.format(Date(it)) }
                    val winnerScore = gamePlayers.find { it.playerId == game.winnerPlayerId }?.totalScore ?: 0

                    GameHistoryItem(
                        gameId = game.id,
                        date = dateString,
                        isAbandoned = game.status == GameStatus.ABANDONED,
                        roundProgress = "Round ${game.currentRoundIndex + 1} of 14",
                        winnerName = winnerEntity?.name ?: "",
                        winnerAvatarIndex = winnerEntity?.avatarIndex ?: 0,
                        winnerColor = winnerEntity?.color?.let { hex ->
                            Color(android.graphics.Color.parseColor(hex))
                        } ?: Color(0xFF1E88E5),
                        playerNames = playerScores.joinToString(" · ") { it.first },
                        winnerScore = winnerScore,
                        playerScores = playerScores,
                    )
                }
                GameHistoryUiState(games = items, isLoading = false)
            }
        }.collectAsState(GameHistoryUiState(isLoading = true))

        return uiState.copy(
            eventSink = { event ->
                when (event) {
                    GameHistoryEvent.Back -> navigator.pop()
                    is GameHistoryEvent.GameTapped -> navigator.goTo(GameSummaryScreen(event.gameId))
                    is GameHistoryEvent.DeleteGame -> scope.launch {
                        gameRepository.deleteGame(event.gameId)
                    }
                }
            }
        )
    }
}
