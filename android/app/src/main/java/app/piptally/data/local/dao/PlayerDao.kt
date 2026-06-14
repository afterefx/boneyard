package app.piptally.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import app.piptally.data.local.entity.PlayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {

    @Query("SELECT * FROM players ORDER BY name ASC")
    fun getAllPlayers(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE id = :id")
    suspend fun getPlayerById(id: Long): PlayerEntity?

    @Query("SELECT * FROM players WHERE id IN (:ids)")
    fun getPlayersByIds(ids: List<Long>): Flow<List<PlayerEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPlayer(player: PlayerEntity): Long

    @Update
    suspend fun updatePlayer(player: PlayerEntity)

    @Delete
    suspend fun deletePlayer(player: PlayerEntity)

    @Query("SELECT COUNT(*) FROM players WHERE name = :name AND id != :excludeId")
    suspend fun countByName(name: String, excludeId: Long = 0): Int
}
