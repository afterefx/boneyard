package app.boneyard.ui.player.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import app.boneyard.data.local.dao.GamePlayerDao
import app.boneyard.data.local.dao.PlayerDao
import app.boneyard.data.local.dao.RoundScoreDao
import app.boneyard.di.AppScope
import app.boneyard.domain.model.GameStatus
import app.boneyard.domain.repository.GameRepository
import app.boneyard.ui.navigation.PlayerEditScreen
import app.boneyard.ui.navigation.PlayerProfileScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import dev.zacsweers.metro.AssistedInject

class PlayerProfilePresenter @AssistedInject constructor(
    @Assisted private val screen: PlayerProfileScreen,
    @Assisted private val navigator: Navigator,
    private val playerDao: PlayerDao,
    private val gamePlayerDao: GamePlayerDao,
    private val roundScoreDao: RoundScoreDao,
    private val gameRepository: GameRepository,
) : Presenter<PlayerProfileUiState> {

    @CircuitInject(PlayerProfileScreen::class, AppScope::class)
    @AssistedFactory
    fun interface Factory {
        fun create(screen: PlayerProfileScreen, navigator: Navigator): PlayerProfilePresenter
    }

    @Composable
    override fun present(): PlayerProfileUiState {
        val playerId = screen.playerId
        var state by remember { mutableStateOf(PlayerProfileUiState(playerId = playerId, isLoading = true)) }

        LaunchedEffect(playerId) {
            val playerEntity = playerDao.getPlayerById(playerId) ?: return@LaunchedEffect
            val completedGames = gameRepository.getCompletedGames().first()
                .filter { it.status == GameStatus.COMPLETED }
            val playerGameData = completedGames.mapNotNull { game ->
                val gp = gamePlayerDao.getPlayersForGameOnce(game.id).firstOrNull { it.playerId == playerId }
                if (gp != null) game to gp else null
            }

            val gamesPlayed = playerGameData.size
            val gamesWon = playerGameData.count { (game, _) -> game.winnerPlayerId == playerId }
            val winPercent = if (gamesPlayed > 0) ((gamesWon.toDouble() / gamesPlayed) * 100).toInt() else 0
            val scores = playerGameData.map { (_, gp) -> gp.totalScore }
            val avgScore = if (scores.isNotEmpty()) scores.average().toInt() else 0
            val bestScore = scores.minOrNull() ?: 0
            val worstScore = scores.maxOrNull() ?: 0
            val roundsWon = roundScoreDao.getRoundsWonCount(playerId)

            val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            val allPlayersMap = playerDao.getAllPlayers().first().associateBy { it.id }

            val recentGames = playerGameData.take(10).map { (game, gp) ->
                val dateMillis = game.completedAt ?: game.createdAt
                val gamePlayers = gamePlayerDao.getPlayersForGameOnce(game.id)
                val opponents = gamePlayers
                    .filter { it.playerId != playerId }
                    .mapNotNull { allPlayersMap[it.playerId]?.name }
                    .joinToString(" · ")
                RecentGameRow(
                    gameId = game.id,
                    date = dateFormat.format(Date(dateMillis)),
                    opponents = opponents,
                    score = gp.totalScore,
                    isWin = game.winnerPlayerId == playerId,
                )
            }

            state = state.copy(
                name = playerEntity.name,
                color = Color(android.graphics.Color.parseColor(playerEntity.color)),
                avatarIndex = playerEntity.avatarIndex,
                gamesPlayed = gamesPlayed,
                gamesWon = gamesWon,
                winPercent = winPercent,
                avgScore = avgScore,
                bestScore = bestScore,
                worstScore = worstScore,
                roundsWon = roundsWon,
                recentGames = recentGames,
                isLoading = false,
            )
        }

        return state.copy(
            eventSink = { event ->
                when (event) {
                    PlayerProfileEvent.Back -> navigator.pop()
                    PlayerProfileEvent.Edit -> navigator.goTo(PlayerEditScreen(playerId))
                }
            }
        )
    }
}
