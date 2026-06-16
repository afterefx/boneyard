package app.boneyard.data.repository

import app.boneyard.data.local.dao.GameDao
import app.boneyard.data.local.dao.GamePlayerDao
import app.boneyard.data.local.dao.PlayerDao
import app.boneyard.data.mapper.toDomain
import app.boneyard.domain.model.PlayerStats
import app.boneyard.domain.repository.StatsRepository
import app.boneyard.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import dev.zacsweers.metro.Inject

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
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
