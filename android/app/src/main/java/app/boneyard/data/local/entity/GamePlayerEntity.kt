package app.boneyard.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "game_players",
    primaryKeys = ["gameId", "playerId"],
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("gameId"),
        Index("playerId")
    ]
)
data class GamePlayerEntity(
    val gameId: Long,
    val playerId: Long,
    val seatPosition: Int,
    val totalScore: Int = 0,
    val isWinner: Boolean = false
)
