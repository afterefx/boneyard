package app.piptally.domain.repository

import app.piptally.domain.model.PlayerStats
import kotlinx.coroutines.flow.Flow

interface StatsRepository {
    fun getStatsForPlayer(playerId: Long): Flow<PlayerStats?>
    fun getAllPlayerStats(): Flow<List<PlayerStats>>
}
