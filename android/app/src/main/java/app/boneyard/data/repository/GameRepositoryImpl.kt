package app.boneyard.data.repository

import app.boneyard.data.local.dao.GameDao
import app.boneyard.data.local.dao.GamePlayerDao
import app.boneyard.data.local.dao.PlayerDao
import app.boneyard.data.mapper.toDomain
import app.boneyard.data.mapper.toEntity
import app.boneyard.domain.model.Game
import app.boneyard.domain.model.GameStatus
import app.boneyard.domain.repository.GameRepository
import app.boneyard.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import dev.zacsweers.metro.Inject

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class GameRepositoryImpl @Inject constructor(
    private val gameDao: GameDao,
    private val gamePlayerDao: GamePlayerDao,
    private val playerDao: PlayerDao
) : GameRepository {

    override fun getActiveGames(): Flow<List<Game>> =
        gameDao.getActiveGames().map { list -> list.map { it.toDomain() } }

    override fun getCompletedGames(): Flow<List<Game>> =
        gameDao.getCompletedGames().map { list -> list.map { it.toDomain() } }

    override fun getGameById(id: Long): Flow<Game?> =
        gameDao.getGameById(id).map { it?.toDomain() }

    override suspend fun getGameByIdOnce(id: Long): Game? =
        gameDao.getGameByIdOnce(id)?.toDomain()

    override suspend fun createGame(game: Game): Long =
        gameDao.insertGame(game.toEntity())

    override suspend fun updateGame(game: Game) =
        gameDao.updateGame(game.toEntity())

    override suspend fun deleteGame(id: Long) =
        gameDao.deleteGame(id)

    override suspend fun updateStatus(gameId: Long, status: GameStatus) {
        val entity = gameDao.getGameByIdOnce(gameId) ?: return
        gameDao.updateGame(entity.copy(status = status.name))
    }

    override suspend fun updateCurrentRound(gameId: Long, roundIndex: Int) {
        val entity = gameDao.getGameByIdOnce(gameId) ?: return
        gameDao.updateGame(entity.copy(currentRoundIndex = roundIndex))
    }

    override suspend fun completeGame(gameId: Long, winnerPlayerId: Long) {
        val entity = gameDao.getGameByIdOnce(gameId) ?: return
        gameDao.updateGame(
            entity.copy(
                status = GameStatus.COMPLETED.name,
                completedAt = System.currentTimeMillis(),
                winnerPlayerId = winnerPlayerId
            )
        )
        gamePlayerDao.setWinner(gameId, winnerPlayerId, true)
    }
}
