package app.boneyard.domain.repository

import app.boneyard.domain.model.Round
import kotlinx.coroutines.flow.Flow

interface RoundRepository {
    fun getRoundsForGame(gameId: Long): Flow<List<Round>>
    suspend fun getRoundsForGameOnce(gameId: Long): List<Round>
    suspend fun getLatestRound(gameId: Long): Round?
    suspend fun createRound(round: Round): Long
    suspend fun completeRound(roundId: Long)
    suspend fun deleteRound(roundId: Long)
    suspend fun countRoundsForGame(gameId: Long): Int
}
