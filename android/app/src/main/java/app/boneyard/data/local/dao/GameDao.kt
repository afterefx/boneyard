package app.boneyard.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import app.boneyard.data.local.entity.GameEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {

    @Query("SELECT * FROM games ORDER BY createdAt DESC")
    fun getAllGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE status IN ('ACTIVE', 'PAUSED') ORDER BY createdAt DESC")
    fun getActiveGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE status IN ('COMPLETED', 'ABANDONED') ORDER BY createdAt DESC")
    fun getCompletedGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE id = :id")
    fun getGameById(id: Long): Flow<GameEntity?>

    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun getGameByIdOnce(id: Long): GameEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertGame(game: GameEntity): Long

    @Update
    suspend fun updateGame(game: GameEntity)

    @Query("DELETE FROM games WHERE id = :id")
    suspend fun deleteGame(id: Long)
}
