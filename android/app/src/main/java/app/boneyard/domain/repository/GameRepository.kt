package app.boneyard.domain.repository

import app.boneyard.domain.model.Game
import app.boneyard.domain.model.GameStatus
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    fun getActiveGames(): Flow<List<Game>>
    fun getCompletedGames(): Flow<List<Game>>
    fun getGameById(id: Long): Flow<Game?>
    suspend fun getGameByIdOnce(id: Long): Game?
    suspend fun createGame(game: Game): Long
    suspend fun updateGame(game: Game)
    suspend fun deleteGame(id: Long)
    suspend fun updateStatus(gameId: Long, status: GameStatus)
    suspend fun updateCurrentRound(gameId: Long, roundIndex: Int)
    suspend fun completeGame(gameId: Long, winnerPlayerId: Long)
}
