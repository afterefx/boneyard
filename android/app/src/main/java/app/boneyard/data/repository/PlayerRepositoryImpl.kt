package app.boneyard.data.repository

import app.boneyard.data.local.dao.PlayerDao
import app.boneyard.data.mapper.toDomain
import app.boneyard.data.mapper.toEntity
import app.boneyard.domain.model.Player
import app.boneyard.domain.repository.PlayerRepository
import app.boneyard.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import dev.zacsweers.metro.Inject

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class PlayerRepositoryImpl @Inject constructor(
    private val playerDao: PlayerDao
) : PlayerRepository {

    override fun getAllPlayers(): Flow<List<Player>> =
        playerDao.getAllPlayers().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getPlayerById(id: Long): Player? =
        playerDao.getPlayerById(id)?.toDomain()

    override suspend fun createPlayer(player: Player): Long =
        playerDao.insertPlayer(player.toEntity())

    override suspend fun updatePlayer(player: Player) =
        playerDao.updatePlayer(player.toEntity())

    override suspend fun deletePlayer(player: Player) =
        playerDao.deletePlayer(player.toEntity())

    override suspend fun isNameTaken(name: String, excludeId: Long): Boolean =
        playerDao.countByName(name.trim(), excludeId) > 0
}
