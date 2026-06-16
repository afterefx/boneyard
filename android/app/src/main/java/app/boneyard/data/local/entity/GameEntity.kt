package app.boneyard.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val status: String,
    val currentRoundIndex: Int = 0,
    val createdAt: Long,
    val completedAt: Long? = null,
    val winnerPlayerId: Long? = null
)
