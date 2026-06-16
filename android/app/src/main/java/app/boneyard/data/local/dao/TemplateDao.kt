package app.boneyard.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import app.boneyard.data.local.entity.TemplateEntity
import app.boneyard.data.local.entity.TemplatePlayerEntity
import kotlinx.coroutines.flow.Flow

data class TemplateWithPlayers(
    @Embedded val template: TemplateEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "templateId",
    )
    val templatePlayers: List<TemplatePlayerEntity>,
)

@Dao
interface TemplateDao {

    @Transaction
    @Query("SELECT * FROM templates ORDER BY createdAt DESC")
    fun getAllTemplatesWithPlayers(): Flow<List<TemplateWithPlayers>>

    @Insert
    suspend fun insertTemplate(template: TemplateEntity): Long

    @Insert
    suspend fun insertTemplatePlayer(player: TemplatePlayerEntity)

    @Query("DELETE FROM templates WHERE id = :templateId")
    suspend fun deleteTemplate(templateId: Long)
}
