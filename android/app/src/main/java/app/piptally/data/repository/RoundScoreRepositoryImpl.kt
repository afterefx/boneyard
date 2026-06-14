package app.piptally.data.repository

import app.piptally.data.local.dao.RoundScoreDao
import app.piptally.data.mapper.toDomain
import app.piptally.data.mapper.toEntity
import app.piptally.domain.model.RoundScore
import app.piptally.domain.repository.RoundScoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoundScoreRepositoryImpl @Inject constructor(
    private val roundScoreDao: RoundScoreDao
) : RoundScoreRepository {

    override fun getScoresForRound(roundId: Long): Flow<List<RoundScore>> =
        roundScoreDao.getScoresForRound(roundId).map { list -> list.map { it.toDomain() } }

    override suspend fun getScoresForRoundOnce(roundId: Long): List<RoundScore> =
        roundScoreDao.getScoresForRoundOnce(roundId).map { it.toDomain() }

    override suspend fun getAllScoresForGame(gameId: Long): List<RoundScore> =
        roundScoreDao.getAllScoresForGame(gameId).map { it.toDomain() }

    override suspend fun saveScores(scores: List<RoundScore>) =
        roundScoreDao.insertScores(scores.map { it.toEntity() })

    override suspend fun deleteScoresForRound(roundId: Long) =
        roundScoreDao.deleteScoresForRound(roundId)
}
