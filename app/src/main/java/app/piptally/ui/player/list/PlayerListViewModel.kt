package app.piptally.ui.player.list

import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.piptally.data.local.dao.GameDao
import app.piptally.data.local.dao.GamePlayerDao
import app.piptally.domain.model.Player
import app.piptally.domain.repository.PlayerRepository
import app.piptally.domain.usecase.player.DeletePlayerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerListViewModelImpl @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val deletePlayerUseCase: DeletePlayerUseCase,
    private val gamePlayerDao: GamePlayerDao,
    private val gameDao: GameDao
) : ViewModel() {

    // Keep the domain Player list alongside the UI list so we can delete by domain model
    private val playerDomainFlow = playerRepository.getAllPlayers()

    val uiState = combine(
        playerDomainFlow,
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
                gamesWon = gamesWon
            )
        }
        PlayerListUiState(players = items, isLoading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PlayerListUiState(isLoading = true)
    )

    fun deletePlayer(playerId: Long) {
        viewModelScope.launch {
            val player = playerRepository.getPlayerById(playerId) ?: return@launch
            deletePlayerUseCase(player)
        }
    }
}
