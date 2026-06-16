package app.boneyard.ui.game.summary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import app.boneyard.data.local.dao.GamePlayerDao
import app.boneyard.data.local.dao.PlayerDao
import app.boneyard.di.AppScope
import app.boneyard.domain.model.GameStatus
import app.boneyard.domain.repository.GameRepository
import app.boneyard.domain.repository.RoundRepository
import app.boneyard.domain.repository.RoundScoreRepository
import app.boneyard.ui.navigation.ActiveGameScreen
import app.boneyard.ui.navigation.GameSummaryScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import dev.zacsweers.metro.AssistedInject

class GameSummaryPresenter @AssistedInject constructor(
    @Assisted private val screen: GameSummaryScreen,
    @Assisted private val navigator: Navigator,
    private val gameRepository: GameRepository,
    private val gamePlayerDao: GamePlayerDao,
    private val playerDao: PlayerDao,
    private val roundRepository: RoundRepository,
    private val roundScoreRepository: RoundScoreRepository,
) : Presenter<GameSummaryUiState> {

    @CircuitInject(GameSummaryScreen::class, AppScope::class)
    @AssistedFactory
    fun interface Factory {
        fun create(screen: GameSummaryScreen, navigator: Navigator): GameSummaryPresenter
    }

    @Composable
    override fun present(): GameSummaryUiState {
        val gameId = screen.gameId
        var state by remember { mutableStateOf(GameSummaryUiState(gameId = gameId, isLoading = true)) }

        LaunchedEffect(gameId) {
            val game = gameRepository.getGameByIdOnce(gameId) ?: return@LaunchedEffect
            val gamePlayers = gamePlayerDao.getPlayersForGameOnce(gameId).sortedBy { it.seatPosition }
            val playerEntities = gamePlayers.mapNotNull { gp ->
                playerDao.getPlayerById(gp.playerId)?.let { gp to it }
            }

            val summaryPlayers = playerEntities.map { (gp, entity) ->
                SummaryPlayer(
                    name = entity.name,
                    color = Color(android.graphics.Color.parseColor(entity.color)),
                    avatarIndex = entity.avatarIndex,
                    totalScore = gp.totalScore,
                    isWinner = entity.id == game.winnerPlayerId,
                )
            }

            val rounds = roundRepository.getRoundsForGameOnce(gameId).sortedBy { it.roundIndex }
            val roundSummaryRows = rounds.map { round ->
                val scores = roundScoreRepository.getScoresForRoundOnce(round.id)
                val scoresByPlayerId = scores.associateBy { it.playerId }
                val orderedScores = playerEntities.map { (gp, _) ->
                    scoresByPlayerId[gp.playerId]?.score ?: 0
                }
                val minScore = orderedScores.minOrNull() ?: 0
                RoundSummaryRow(
                    roundIndex = round.roundIndex,
                    spinnerValue = round.spinnerValue,
                    scores = orderedScores,
                    winnerSeatIndex = orderedScores.indexOfFirst { it == minScore },
                )
            }

            val dateString = game.completedAt?.let { millis ->
                SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(millis))
            } ?: SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(game.createdAt))

            state = state.copy(
                gameDate = dateString,
                players = summaryPlayers,
                rounds = roundSummaryRows,
                isLoading = false,
                isAbandoned = game.status == GameStatus.ABANDONED,
                endedRoundIndex = game.currentRoundIndex,
            )
        }

        return state.copy(
            eventSink = { event ->
                when (event) {
                    GameSummaryEvent.Done -> navigator.pop()
                }
            }
        )
    }
}
