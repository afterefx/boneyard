package app.boneyard.domain.repository

import app.boneyard.domain.model.Player
import app.boneyard.domain.model.Template
import kotlinx.coroutines.flow.Flow

interface TemplateRepository {
    fun getAllTemplates(): Flow<List<Template>>
    suspend fun createTemplate(name: String, players: List<Player>): Long
    suspend fun deleteTemplate(templateId: Long)
}
