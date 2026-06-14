package app.piptally.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "round_scores",
    primaryKeys = ["roundId", "playerId"],
    foreignKeys = [
        ForeignKey(
            entity = RoundEntity::class,
            parentColumns = ["id"],
            childColumns = ["roundId"],
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
        Index("roundId"),
        Index("playerId")
    ]
)
data class RoundScoreEntity(
    val roundId: Long,
    val playerId: Long,
    val score: Int
)
