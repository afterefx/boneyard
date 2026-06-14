package app.piptally.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "players",
    indices = [Index(value = ["name"], unique = true)]
)
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: String,
    val avatarIndex: Int,
    val createdAt: Long
)
