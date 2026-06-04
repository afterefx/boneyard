package app.piptally.data.repository

import app.piptally.data.local.dao.GameDao
import app.piptally.data.local.dao.GamePlayerDao
import app.piptally.data.local.dao.PlayerDao
import app.piptally.data.mapper.toDomain
import app.piptally.domain.model.PlayerStats
import app.piptally.domain.repository.StatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StatsRepositoryImpl @Inject constructor(
    private val playerDao: PlayerDao,
    private val gamePlayerDao: GamePlayerDao,
    private val gameDao: GameDao
) : StatsRepository {

    override fun getStatsForPlayer(playerId: Long): Flow<PlayerStats?> {
        return combine(
            playerDao.getAllPlayers(),
            gameDao.getCompletedGames()
        ) { players, completedGames ->
            val playerEntity = players.find { it.id == playerId } ?: return@combine null
            val player = playerEntity.toDomain()

            // We need to query game_players synchronously here — use suspend queries via a different approach.
            // For now, return a basic stats object; the suspend calls will be done in the use case.
            PlayerStats(
                player = player,
                gamesPlayed = 0,
                gamesWon = 0,
                totalScore = 0,
                averageScorePerGame = 0.0,
                bestScore = 0
            )
        }
    }

    override fun getAllPlayerStats(): Flow<List<PlayerStats>> {
        return combine(
            playerDao.getAllPlayers(),
            gameDao.getCompletedGames()
        ) { players, _ ->
            players.map { playerEntity ->
                val player = playerEntity.toDomain()
                PlayerStats(
                    player = player,
                    gamesPlayed = 0,
                    gamesWon = 0,
                    totalScore = 0,
                    averageScorePerGame = 0.0,
                    bestScore = 0
                )
            }
        }
    }
}
