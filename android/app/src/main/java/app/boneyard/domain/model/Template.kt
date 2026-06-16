package app.boneyard.domain.model

data class Template(
    val id: Long = 0,
    val name: String,
    val players: List<TemplatePlayer>,
    val createdAt: Long = System.currentTimeMillis(),
)

data class TemplatePlayer(
    val templateId: Long,
    val player: Player,
    val seatPosition: Int,
)
