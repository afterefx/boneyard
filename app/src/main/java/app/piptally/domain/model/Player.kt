package app.piptally.domain.model

data class Player(
    val id: Long = 0,
    val name: String,
    val color: String,
    val avatarIndex: Int,
    val createdAt: Long = System.currentTimeMillis()
)
