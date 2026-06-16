package app.boneyard.domain.repository

import app.boneyard.domain.model.PlayerStats
import kotlinx.coroutines.flow.Flow

interface StatsRepository {
    fun getStatsForPlayer(playerId: Long): Flow<PlayerStats?>
    fun getAllPlayerStats(): Flow<List<PlayerStats>>
}
