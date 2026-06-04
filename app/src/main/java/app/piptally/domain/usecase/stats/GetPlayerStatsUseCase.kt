package app.piptally.domain.usecase.stats

import app.piptally.data.local.dao.GamePlayerDao
import app.piptally.data.mapper.toDomain
import app.piptally.domain.model.PlayerStats
import app.piptally.domain.repository.GameRepository
import app.piptally.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetPlayerStatsUseCase @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val gameRepository: GameRepository,
    private val gamePlayerDao: GamePlayerDao
) {
    operator fun invoke(playerId: Long): Flow<PlayerStats?> {
        return combine(
            playerRepository.getAllPlayers(),
            gameRepository.getCompletedGames()
        ) { players, completedGames ->
            val player = players.find { it.id == playerId } ?: return@combine null

            var gamesPlayed = 0
            var gamesWon = 0
            var totalScore = 0
            val gameScores = mutableListOf<Int>()

            completedGames.forEach { game ->
                if (game.status != app.piptally.domain.model.GameStatus.COMPLETED) return@forEach
                val gamePlayers = gamePlayerDao.getPlayersForGameOnce(game.id)
                val myGamePlayer = gamePlayers.find { it.playerId == playerId }
                if (myGamePlayer != null) {
                    gamesPlayed++
                    totalScore += myGamePlayer.totalScore
                    gameScores.add(myGamePlayer.totalScore)
                    if (myGamePlayer.isWinner) gamesWon++
                }
            }

            PlayerStats(
                player = player,
                gamesPlayed = gamesPlayed,
                gamesWon = gamesWon,
                totalScore = totalScore,
                averageScorePerGame = if (gamesPlayed > 0) totalScore.toDouble() / gamesPlayed else 0.0,
                bestScore = if (gameScores.isNotEmpty()) gameScores.min() else 0
            )
        }
    }
}
