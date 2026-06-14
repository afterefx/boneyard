package app.piptally.ui.history

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.piptally.data.local.dao.GamePlayerDao
import app.piptally.data.local.dao.PlayerDao
import app.piptally.domain.model.GameStatus
import app.piptally.domain.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class GameHistoryViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val gamePlayerDao: GamePlayerDao,
    private val playerDao: PlayerDao,
) : ViewModel() {

    val uiState = gameRepository.getCompletedGames()
        .map { games ->
            val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

            val items = games.map { game ->
                val gamePlayers = gamePlayerDao.getPlayersForGameOnce(game.id)
                    .sortedBy { it.seatPosition }

                val playerScores = gamePlayers.mapNotNull { gp ->
                    val entity = playerDao.getPlayerById(gp.playerId) ?: return@mapNotNull null
                    entity.name to gp.totalScore
                }

                val playerNames = playerScores.joinToString(" · ") { it.first }

                val isAbandoned = game.status == GameStatus.ABANDONED

                val winnerEntity = game.winnerPlayerId?.let { winnerId ->
                    playerDao.getPlayerById(winnerId)
                }

                val dateString = (game.completedAt ?: game.createdAt).let { millis ->
                    dateFormat.format(Date(millis))
                }

                val winnerScore = gamePlayers.find { it.playerId == game.winnerPlayerId }?.totalScore ?: 0

                GameHistoryItem(
                    gameId = game.id,
                    date = dateString,
                    isAbandoned = isAbandoned,
                    roundProgress = "Round ${game.currentRoundIndex + 1} of 14",
                    winnerName = winnerEntity?.name ?: "",
                    winnerAvatarIndex = winnerEntity?.avatarIndex ?: 0,
                    winnerColor = winnerEntity?.color?.let { hex ->
                        Color(android.graphics.Color.parseColor(hex))
                    } ?: Color(0xFF1E88E5),
                    playerNames = playerNames,
                    winnerScore = winnerScore,
                    playerScores = playerScores,
                )
            }

            GameHistoryUiState(games = items, isLoading = false)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = GameHistoryUiState(isLoading = true),
        )

    fun deleteGame(gameId: Long) {
        viewModelScope.launch {
            gameRepository.deleteGame(gameId)
        }
    }
}
