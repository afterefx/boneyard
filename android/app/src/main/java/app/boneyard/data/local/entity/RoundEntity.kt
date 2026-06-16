package app.boneyard.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rounds",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("gameId")]
)
data class RoundEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gameId: Long,
    val roundIndex: Int,
    // Number of dots the shaker must use (from ROUND_SPINNER_SEQUENCE)
    val spinnerValue: Int,
    // playerId of the shaker for this round
    val shakerPlayerId: Long,
    val completedAt: Long? = null
)
