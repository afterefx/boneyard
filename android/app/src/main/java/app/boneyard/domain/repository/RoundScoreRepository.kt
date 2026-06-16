package app.boneyard.domain.repository

import app.boneyard.domain.model.RoundScore
import kotlinx.coroutines.flow.Flow

interface RoundScoreRepository {
    fun getScoresForRound(roundId: Long): Flow<List<RoundScore>>
    suspend fun getScoresForRoundOnce(roundId: Long): List<RoundScore>
    suspend fun getAllScoresForGame(gameId: Long): List<RoundScore>
    suspend fun saveScores(scores: List<RoundScore>)
    suspend fun deleteScoresForRound(roundId: Long)
}
