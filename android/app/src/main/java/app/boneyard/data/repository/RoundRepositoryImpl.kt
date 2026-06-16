package app.boneyard.data.repository

import app.boneyard.data.local.dao.RoundDao
import app.boneyard.data.mapper.toDomain
import app.boneyard.data.mapper.toEntity
import app.boneyard.domain.model.Round
import app.boneyard.domain.repository.RoundRepository
import app.boneyard.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import dev.zacsweers.metro.Inject

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class RoundRepositoryImpl @Inject constructor(
    private val roundDao: RoundDao
) : RoundRepository {

    override fun getRoundsForGame(gameId: Long): Flow<List<Round>> =
        roundDao.getRoundsForGame(gameId).map { list -> list.map { it.toDomain() } }

    override suspend fun getRoundsForGameOnce(gameId: Long): List<Round> =
        roundDao.getRoundsForGameOnce(gameId).map { it.toDomain() }

    override suspend fun getLatestRound(gameId: Long): Round? =
        roundDao.getLatestRound(gameId)?.toDomain()

    override suspend fun createRound(round: Round): Long =
        roundDao.insertRound(round.toEntity())

    override suspend fun completeRound(roundId: Long) {
        val entity = roundDao.getRoundById(roundId) ?: return
        roundDao.updateRound(entity.copy(completedAt = System.currentTimeMillis()))
    }

    override suspend fun deleteRound(roundId: Long) =
        roundDao.deleteRound(roundId)

    override suspend fun countRoundsForGame(gameId: Long): Int =
        roundDao.countRoundsForGame(gameId)
}
