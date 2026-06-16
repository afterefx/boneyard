package app.boneyard.domain.repository

import app.boneyard.domain.model.Player
import kotlinx.coroutines.flow.Flow

interface PlayerRepository {
    fun getAllPlayers(): Flow<List<Player>>
    suspend fun getPlayerById(id: Long): Player?
    suspend fun createPlayer(player: Player): Long
    suspend fun updatePlayer(player: Player)
    suspend fun deletePlayer(player: Player)
    suspend fun isNameTaken(name: String, excludeId: Long = 0): Boolean
}
