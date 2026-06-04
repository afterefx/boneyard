package app.piptally.ui.game.summary

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.piptally.data.local.dao.GamePlayerDao
import app.piptally.data.local.dao.PlayerDao
import app.piptally.domain.model.GameStatus
import app.piptally.domain.repository.GameRepository
import app.piptally.domain.repository.RoundRepository
import app.piptally.domain.repository.RoundScoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class GameSummaryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val gameRepository: GameRepository,
    private val gamePlayerDao: GamePlayerDao,
    private val playerDao: PlayerDao,
    private val roundRepository: RoundRepository,
    private val roundScoreRepository: RoundScoreRepository,
) : ViewModel() {

    private val gameId: Long = savedStateHandle.get<Long>("gameId") ?: 0L

    private val _uiState = MutableStateFlow(GameSummaryUiState(gameId = gameId, isLoading = true))
    val uiState: StateFlow<GameSummaryUiState> = _uiState.asStateFlow()

    init {
        loadSummary()
    }

    private fun loadSummary() {
        viewModelScope.launch {
            val game = gameRepository.getGameByIdOnce(gameId) ?: return@launch

            // Load players ordered by seat position
            val gamePlayers = gamePlayerDao.getPlayersForGameOnce(gameId)
                .sortedBy { it.seatPosition }

            // Resolve player entities for each seat
            val playerEntities = gamePlayers.mapNotNull { gp ->
                playerDao.getPlayerById(gp.playerId)?.let { entity -> gp to entity }
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

            // Build a map of playerId -> seat index for round winner resolution
            val playerIdToSeatIndex = playerEntities.mapIndexed { seatIndex, (gp, _) ->
                gp.playerId to seatIndex
            }.toMap()

            // Load all completed rounds ordered by roundIndex
            val rounds = roundRepository.getRoundsForGameOnce(gameId)
                .sortedBy { it.roundIndex }

            val roundSummaryRows = rounds.map { round ->
                val scores = roundScoreRepository.getScoresForRoundOnce(round.id)
                // scores is a list of (playerId, score) — arrange by seat position
                val scoresByPlayerId = scores.associateBy { it.playerId }

                // Build ordered score list by seat position
                val orderedScores = playerEntities.map { (gp, _) ->
                    scoresByPlayerId[gp.playerId]?.score ?: 0
                }

                // Round winner = player with the lowest score in this round
                val minScore = orderedScores.minOrNull() ?: 0
                val winnerSeatIndex = orderedScores.indexOfFirst { it == minScore }

                RoundSummaryRow(
                    roundIndex = round.roundIndex,
                    spinnerValue = round.spinnerValue,
                    scores = orderedScores,
                    winnerSeatIndex = winnerSeatIndex,
                )
            }

            val dateString = game.completedAt?.let { millis ->
                SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(millis))
            } ?: SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(game.createdAt))

            _uiState.update {
                it.copy(
                    gameId = gameId,
                    gameDate = dateString,
                    players = summaryPlayers,
                    rounds = roundSummaryRows,
                    isLoading = false,
                    isAbandoned = game.status == GameStatus.ABANDONED,
                    endedRoundIndex = game.currentRoundIndex
                )
            }
        }
    }
}
