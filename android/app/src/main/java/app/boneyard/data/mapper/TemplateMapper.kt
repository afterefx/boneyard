package app.boneyard.data.mapper

import app.boneyard.data.local.entity.TemplateEntity
import app.boneyard.domain.model.Template
import app.boneyard.domain.model.TemplatePlayer

fun TemplateEntity.toDomain(players: List<TemplatePlayer>): Template = Template(
    id = id,
    name = name,
    players = players,
    createdAt = createdAt,
)
