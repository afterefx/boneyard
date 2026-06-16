package app.boneyard.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import app.boneyard.data.local.entity.RoundEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoundDao {

    @Query("SELECT * FROM rounds WHERE gameId = :gameId ORDER BY roundIndex ASC")
    fun getRoundsForGame(gameId: Long): Flow<List<RoundEntity>>

    @Query("SELECT * FROM rounds WHERE gameId = :gameId ORDER BY roundIndex ASC")
    suspend fun getRoundsForGameOnce(gameId: Long): List<RoundEntity>

    @Query("SELECT * FROM rounds WHERE gameId = :gameId ORDER BY roundIndex DESC LIMIT 1")
    suspend fun getLatestRound(gameId: Long): RoundEntity?

    @Query("SELECT * FROM rounds WHERE id = :id")
    suspend fun getRoundById(id: Long): RoundEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRound(round: RoundEntity): Long

    @Update
    suspend fun updateRound(round: RoundEntity)

    @Query("DELETE FROM rounds WHERE id = :id")
    suspend fun deleteRound(id: Long)

    @Query("SELECT COUNT(*) FROM rounds WHERE gameId = :gameId")
    suspend fun countRoundsForGame(gameId: Long): Int
}
