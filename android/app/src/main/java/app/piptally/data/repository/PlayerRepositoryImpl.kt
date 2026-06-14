package app.piptally.data.repository

import app.piptally.data.local.dao.PlayerDao
import app.piptally.data.mapper.toDomain
import app.piptally.data.mapper.toEntity
import app.piptally.domain.model.Player
import app.piptally.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

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
