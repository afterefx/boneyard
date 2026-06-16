package app.boneyard.ui.game.active

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import app.boneyard.data.local.dao.GamePlayerDao
import app.boneyard.data.local.dao.PlayerDao
import app.boneyard.data.local.dao.RoundDao
import app.boneyard.data.local.dao.RoundScoreDao
import app.boneyard.data.local.entity.GamePlayerEntity
import app.boneyard.data.local.entity.PlayerEntity
import app.boneyard.di.AppScope
import app.boneyard.domain.repository.GameRepository
import app.boneyard.domain.usecase.game.PauseGameUseCase
import app.boneyard.domain.usecase.round.SubmitRoundScoresUseCase
import app.boneyard.domain.usecase.round.UndoLastRoundUseCase
import app.boneyard.ui.components.ScoreboardEntry
import app.boneyard.ui.navigation.ActiveGameScreen
import app.boneyard.ui.navigation.GameSummaryScreen
import app.boneyard.util.GameConstants
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import kotlinx.coroutines.launch
import dev.zacsweers.metro.AssistedInject

class ActiveGamePresenter @AssistedInject constructor(
    @Assisted private val screen: ActiveGameScreen,
    @Assisted private val navigator: Navigator,
    private val gameRepository: GameRepository,
    private val gamePlayerDao: GamePlayerDao,
    private val playerDao: PlayerDao,
    private val roundDao: RoundDao,
    private val roundScoreDao: RoundScoreDao,
    private val submitRoundScoresUseCase: SubmitRoundScoresUseCase,
    private val undoLastRoundUseCase: UndoLastRoundUseCase,
    private val pauseGameUseCase: PauseGameUseCase,
) : Presenter<ActiveGameUiState> {

    @CircuitInject(ActiveGameScreen::class, AppScope::class)
    @AssistedFactory
    fun interface Factory {
        fun create(screen: ActiveGameScreen, navigator: Navigator): ActiveGamePresenter
    }

    @Composable
    override fun present(): ActiveGameUiState {
        val gameId = screen.gameId
        val scope = rememberCoroutineScope()

        var uiState by remember { mutableStateOf(ActiveGameUiState(gameId = gameId, isLoading = true)) }
        var playerEntriesCache by remember { mutableStateOf<List<Pair<GamePlayerEntity, PlayerEntity>>>(emptyList()) }
        var roundScoresDetailsByIndex by remember { mutableStateOf<Map<Int, Map<Long, Pair<Int, Boolean>>>>(emptyMap()) }
        var reloadTrigger by remember { mutableIntStateOf(0) }

        LaunchedEffect(reloadTrigger) {
            val game = gameRepository.getGameByIdOnce(gameId) ?: return@LaunchedEffect
            val gamePlayers = gamePlayerDao.getPlayersForGameOnce(gameId).sortedBy { it.seatPosition }
            val playerEntries = gamePlayers.mapNotNull { gp ->
                playerDao.getPlayerById(gp.playerId)?.let { gp to it }
            }
            playerEntriesCache = playerEntries

            val rounds = roundDao.getRoundsForGameOnce(gameId)
            val allScores = roundScoreDao.getAllScoresForGame(gameId)
            val scoresByRoundId = allScores.groupBy { it.roundId }
            roundScoresDetailsByIndex = rounds.associate { round ->
                round.roundIndex to (scoresByRoundId[round.id]
                    ?.associate { it.playerId to (it.score to (it.score == 0)) }
                    ?: emptyMap())
            }

            val currentRoundIndex = game.currentRoundIndex
            val spinnerValue = GameConstants.ROUND_SPINNER_SEQUENCE[currentRoundIndex]
            val shakerIndex = GameConstants.getShakerIndex(currentRoundIndex, playerEntries.size)
            val shakerEntry = playerEntries.getOrNull(shakerIndex)

            val players = playerEntries.map { (gp, entity) ->
                PlayerScoreEntry(
                    playerId = entity.id,
                    name = entity.name,
                    color = Color(android.graphics.Color.parseColor(entity.color)),
                    avatarIndex = entity.avatarIndex,
                    totalScore = gp.totalScore,
                )
            }

            uiState = uiState.copy(
                gameId = gameId,
                currentRoundIndex = currentRoundIndex,
                completedRounds = currentRoundIndex,
                viewingRoundIndex = currentRoundIndex,
                viewingRoundScores = emptyMap(),
                spinnerValue = spinnerValue,
                shakerName = shakerEntry?.second?.name ?: "",
                shakerAvatarIndex = shakerEntry?.second?.avatarIndex ?: 0,
                shakerColor = shakerEntry?.second?.color?.let { hex ->
                    Color(android.graphics.Color.parseColor(hex))
                } ?: Color(0xFF1E88E5),
                players = players,
                scoreboardEntries = buildScoreboardEntries(players),
                isLoading = false,
                isEditingHistory = false,
            )
        }

        return uiState.copy(
            eventSink = { event ->
                when (event) {
                    is ActiveGameEvent.ScoreChange -> {
                        uiState = uiState.copy(
                            players = uiState.players.map { p ->
                                if (p.playerId == event.playerId)
                                    p.copy(scoreText = event.text, showError = false)
                                else p
                            }
                        )
                    }
                    is ActiveGameEvent.WinnerToggle -> {
                        val currentWinnerId = uiState.players.firstOrNull { it.isWinner }?.playerId
                        val isDeselecting = currentWinnerId == event.playerId
                        uiState = uiState.copy(
                            players = uiState.players.map { p ->
                                when {
                                    p.playerId == event.playerId && !isDeselecting ->
                                        p.copy(isWinner = true, scoreText = "0", showError = false)
                                    p.playerId == event.playerId ->
                                        p.copy(isWinner = false, scoreText = "")
                                    else -> p.copy(isWinner = false)
                                }
                            }
                        )
                    }
                    ActiveGameEvent.SubmitRound -> {
                        val current = uiState
                        val hasError = current.players.any {
                            val parsed = it.scoreText.trim().toIntOrNull()
                            parsed == null || parsed < 0
                        }
                        if (hasError) {
                            uiState = uiState.copy(
                                players = uiState.players.map { p ->
                                    val parsed = p.scoreText.trim().toIntOrNull()
                                    p.copy(showError = parsed == null || parsed < 0)
                                }
                            )
                            return@copy
                        }
                        val scores = current.players.associate { p ->
                            p.playerId to (p.scoreText.trim().toIntOrNull() ?: 0)
                        }
                        scope.launch {
                            val isComplete = submitRoundScoresUseCase(
                                gameId = gameId,
                                currentRoundIndex = current.currentRoundIndex,
                                scores = scores,
                            )
                            if (isComplete) {
                                navigator.pop()
                                navigator.goTo(GameSummaryScreen(gameId))
                            } else {
                                reloadTrigger++
                            }
                        }
                    }
                    ActiveGameEvent.UndoRound -> {
                        scope.launch {
                            if (undoLastRoundUseCase(gameId)) reloadTrigger++
                        }
                    }
                    ActiveGameEvent.PauseGame -> {
                        scope.launch {
                            pauseGameUseCase(gameId)
                            navigator.pop()
                        }
                    }
                    ActiveGameEvent.ToggleScoreboard -> {
                        uiState = uiState.copy(isScoreboardExpanded = !uiState.isScoreboardExpanded)
                    }
                    is ActiveGameEvent.NavigateToRound -> {
                        val targetIndex = event.roundIndex
                        if (targetIndex < 0 || targetIndex > 13) return@copy
                        val shakerIndex = GameConstants.getShakerIndex(targetIndex, playerEntriesCache.size)
                        val shakerEntry = playerEntriesCache.getOrNull(shakerIndex)
                        uiState = uiState.copy(
                            viewingRoundIndex = targetIndex,
                            spinnerValue = GameConstants.ROUND_SPINNER_SEQUENCE[targetIndex],
                            shakerName = shakerEntry?.second?.name ?: "",
                            shakerAvatarIndex = shakerEntry?.second?.avatarIndex ?: 0,
                            shakerColor = shakerEntry?.second?.color?.let { hex ->
                                Color(android.graphics.Color.parseColor(hex))
                            } ?: Color(0xFF1E88E5),
                            viewingRoundScores = if (targetIndex < uiState.currentRoundIndex) {
                                roundScoresDetailsByIndex[targetIndex]?.mapValues { it.value.first } ?: emptyMap()
                            } else emptyMap(),
                            isEditingHistory = false,
                        )
                    }
                    ActiveGameEvent.StartEditingHistory -> {
                        val roundIndex = uiState.viewingRoundIndex
                        val scoresDetails = roundScoresDetailsByIndex[roundIndex] ?: emptyMap()
                        uiState = uiState.copy(
                            isEditingHistory = true,
                            players = uiState.players.map { p ->
                                val detail = scoresDetails[p.playerId]
                                p.copy(
                                    scoreText = detail?.first?.toString() ?: "",
                                    isWinner = detail?.second ?: false,
                                    showError = false,
                                )
                            }
                        )
                    }
                    ActiveGameEvent.CancelEditingHistory -> {
                        uiState = uiState.copy(isEditingHistory = false)
                        // Re-navigate to reset viewing state
                        val targetIndex = uiState.viewingRoundIndex
                        val shakerIndex = GameConstants.getShakerIndex(targetIndex, playerEntriesCache.size)
                        val shakerEntry = playerEntriesCache.getOrNull(shakerIndex)
                        uiState = uiState.copy(
                            spinnerValue = GameConstants.ROUND_SPINNER_SEQUENCE[targetIndex],
                            shakerName = shakerEntry?.second?.name ?: "",
                            shakerAvatarIndex = shakerEntry?.second?.avatarIndex ?: 0,
                            shakerColor = shakerEntry?.second?.color?.let { hex ->
                                Color(android.graphics.Color.parseColor(hex))
                            } ?: Color(0xFF1E88E5),
                            viewingRoundScores = roundScoresDetailsByIndex[targetIndex]
                                ?.mapValues { it.value.first } ?: emptyMap(),
                        )
                    }
                    ActiveGameEvent.SaveEditingHistory -> {
                        val current = uiState
                        val hasError = current.players.any {
                            val parsed = it.scoreText.trim().toIntOrNull()
                            parsed == null || parsed < 0
                        }
                        if (hasError) {
                            uiState = uiState.copy(
                                players = uiState.players.map { p ->
                                    val parsed = p.scoreText.trim().toIntOrNull()
                                    p.copy(showError = parsed == null || parsed < 0)
                                }
                            )
                            return@copy
                        }
                        val scores = current.players.associate { p ->
                            p.playerId to (p.scoreText.trim().toIntOrNull() ?: 0)
                        }
                        scope.launch {
                            submitRoundScoresUseCase(
                                gameId = gameId,
                                currentRoundIndex = current.viewingRoundIndex,
                                scores = scores,
                            )
                            uiState = uiState.copy(isEditingHistory = false)
                            reloadTrigger++
                        }
                    }
                }
            }
        )
    }

    private fun buildScoreboardEntries(players: List<PlayerScoreEntry>): List<ScoreboardEntry> =
        players.sortedBy { it.totalScore }.mapIndexed { index, player ->
            ScoreboardEntry(
                playerId = player.playerId,
                playerName = player.name,
                playerColor = player.color,
                avatarIndex = player.avatarIndex,
                totalScore = player.totalScore,
                rank = index + 1,
            )
        }
}
