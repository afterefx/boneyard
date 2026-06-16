package app.boneyard.data.repository

import app.boneyard.data.local.dao.PlayerDao
import app.boneyard.data.local.dao.TemplateDao
import app.boneyard.data.local.entity.TemplateEntity
import app.boneyard.data.local.entity.TemplatePlayerEntity
import app.boneyard.data.mapper.toDomain
import app.boneyard.di.AppScope
import app.boneyard.domain.model.Player
import app.boneyard.domain.model.Template
import app.boneyard.domain.model.TemplatePlayer
import app.boneyard.domain.repository.TemplateRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class TemplateRepositoryImpl @Inject constructor(
    private val templateDao: TemplateDao,
    private val playerDao: PlayerDao,
) : TemplateRepository {

    override fun getAllTemplates(): Flow<List<Template>> =
        combine(
            templateDao.getAllTemplatesWithPlayers(),
            playerDao.getAllPlayers(),
        ) { templatesWithPlayers, allPlayers ->
            val playerMap = allPlayers.associateBy { it.id }
            templatesWithPlayers.mapNotNull { twp ->
                val templatePlayers = twp.templatePlayers
                    .sortedBy { it.seatPosition }
                    .mapNotNull { tp ->
                        playerMap[tp.playerId]?.toDomain()?.let { player ->
                            TemplatePlayer(twp.template.id, player, tp.seatPosition)
                        }
                    }
                if (templatePlayers.size == 4) {
                    twp.template.toDomain(templatePlayers)
                } else null
            }
        }

    override suspend fun createTemplate(name: String, players: List<Player>): Long {
        require(players.size == 4) { "A template requires exactly 4 players" }
        val templateId = templateDao.insertTemplate(TemplateEntity(name = name.trim()))
        players.forEachIndexed { index, player ->
            templateDao.insertTemplatePlayer(TemplatePlayerEntity(templateId, player.id, index))
        }
        return templateId
    }

    override suspend fun deleteTemplate(templateId: Long) =
        templateDao.deleteTemplate(templateId)
}
