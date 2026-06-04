package app.piptally.domain.repository

import app.piptally.domain.model.Player
import kotlinx.coroutines.flow.Flow

interface PlayerRepository {
    fun getAllPlayers(): Flow<List<Player>>
    suspend fun getPlayerById(id: Long): Player?
    suspend fun createPlayer(player: Player): Long
    suspend fun updatePlayer(player: Player)
    suspend fun deletePlayer(player: Player)
    suspend fun isNameTaken(name: String, excludeId: Long = 0): Boolean
}
