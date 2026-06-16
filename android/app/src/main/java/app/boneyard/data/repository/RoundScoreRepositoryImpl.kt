package app.boneyard.data.repository

import app.boneyard.data.local.dao.RoundScoreDao
import app.boneyard.data.mapper.toDomain
import app.boneyard.data.mapper.toEntity
import app.boneyard.domain.model.RoundScore
import app.boneyard.domain.repository.RoundScoreRepository
import app.boneyard.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import dev.zacsweers.metro.Inject

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
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
