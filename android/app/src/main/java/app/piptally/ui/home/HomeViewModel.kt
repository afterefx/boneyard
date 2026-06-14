package app.piptally.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.piptally.data.local.dao.GamePlayerDao
import app.piptally.data.local.dao.PlayerDao
import app.piptally.domain.model.GameStatus
import app.piptally.domain.repository.AppTheme
import app.piptally.domain.repository.GameRepository
import app.piptally.domain.repository.SettingsRepository
import app.piptally.domain.usecase.game.GetActiveGamesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModelImpl @Inject constructor(
    getActiveGamesUseCase: GetActiveGamesUseCase,
    private val gamePlayerDao: GamePlayerDao,
    private val playerDao: PlayerDao,
    private val settingsRepository: SettingsRepository,
    private val gameRepository: GameRepository,
) : ViewModel() {

    val themeState = settingsRepository.themeFlow

    val uiState = combine(
        getActiveGamesUseCase(),
        playerDao.getAllPlayers()
    ) { activeGames, allPlayers ->
        val playerMap = allPlayers.associateBy { it.id }
        val summaries = activeGames.map { game ->
            val gamePlayers = gamePlayerDao.getPlayersForGameOnce(game.id)
            val names = gamePlayers
                .sortedBy { it.seatPosition }
                .mapNotNull { gp -> playerMap[gp.playerId]?.name }
            ActiveGameSummary(
                gameId = game.id,
                playerNames = names,
                currentRound = game.currentRoundIndex + 1,
                totalRounds = 14
            )
        }
        HomeUiState(activePausedGames = summaries, isLoading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(isLoading = true)
    )

    fun setTheme(theme: AppTheme) {
        settingsRepository.setTheme(theme)
    }

    fun resetAllData() {
        viewModelScope.launch {
            settingsRepository.resetAllData()
        }
    }

    fun abandonGame(gameId: Long) {
        viewModelScope.launch {
            gameRepository.updateStatus(gameId, GameStatus.ABANDONED)
        }
    }

    fun deleteGame(gameId: Long) {
        viewModelScope.launch {
            gameRepository.deleteGame(gameId)
        }
    }
}
