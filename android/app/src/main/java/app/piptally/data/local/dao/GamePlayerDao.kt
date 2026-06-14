package app.piptally.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import app.piptally.data.local.entity.GamePlayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GamePlayerDao {

    @Query("SELECT * FROM game_players WHERE gameId = :gameId ORDER BY seatPosition ASC")
    fun getPlayersForGame(gameId: Long): Flow<List<GamePlayerEntity>>

    @Query("SELECT * FROM game_players WHERE gameId = :gameId ORDER BY seatPosition ASC")
    suspend fun getPlayersForGameOnce(gameId: Long): List<GamePlayerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGamePlayer(gamePlayer: GamePlayerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGamePlayers(gamePlayers: List<GamePlayerEntity>)

    @Update
    suspend fun updateGamePlayer(gamePlayer: GamePlayerEntity)

    @Query("UPDATE game_players SET totalScore = totalScore + :delta WHERE gameId = :gameId AND playerId = :playerId")
    suspend fun addToScore(gameId: Long, playerId: Long, delta: Int)

    @Query("UPDATE game_players SET totalScore = :score WHERE gameId = :gameId AND playerId = :playerId")
    suspend fun setScore(gameId: Long, playerId: Long, score: Int)

    @Query("UPDATE game_players SET isWinner = :isWinner WHERE gameId = :gameId AND playerId = :playerId")
    suspend fun setWinner(gameId: Long, playerId: Long, isWinner: Boolean)

    @Query("DELETE FROM game_players WHERE gameId = :gameId")
    suspend fun deletePlayersForGame(gameId: Long)
}
