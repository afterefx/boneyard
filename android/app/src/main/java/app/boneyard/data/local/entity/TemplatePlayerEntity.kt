package app.boneyard.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "template_players",
    primaryKeys = ["templateId", "seatPosition"],
    foreignKeys = [
        ForeignKey(
            entity = TemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("templateId"), Index("playerId")],
)
data class TemplatePlayerEntity(
    val templateId: Long,
    val playerId: Long,
    val seatPosition: Int,
)
