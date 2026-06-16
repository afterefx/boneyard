package app.boneyard.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.boneyard.data.local.entity.RoundScoreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoundScoreDao {

    @Query("SELECT * FROM round_scores WHERE roundId = :roundId")
    fun getScoresForRound(roundId: Long): Flow<List<RoundScoreEntity>>

    @Query("SELECT * FROM round_scores WHERE roundId = :roundId")
    suspend fun getScoresForRoundOnce(roundId: Long): List<RoundScoreEntity>

    @Query("""
        SELECT rs.* FROM round_scores rs
        INNER JOIN rounds r ON rs.roundId = r.id
        WHERE r.gameId = :gameId
    """)
    suspend fun getAllScoresForGame(gameId: Long): List<RoundScoreEntity>

    @Query("""
        SELECT COUNT(rs.roundId) FROM round_scores rs
        INNER JOIN rounds r ON rs.roundId = r.id
        INNER JOIN games g ON r.gameId = g.id
        WHERE rs.playerId = :playerId AND rs.score = 0 AND g.status = 'COMPLETED'
    """)
    suspend fun getRoundsWonCount(playerId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: RoundScoreEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScores(scores: List<RoundScoreEntity>)

    @Query("DELETE FROM round_scores WHERE roundId = :roundId")
    suspend fun deleteScoresForRound(roundId: Long)
}
