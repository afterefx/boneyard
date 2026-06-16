package app.boneyard.domain.model

data class Game(
    val id: Long = 0,
    val status: GameStatus,
    val currentRoundIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val winnerPlayerId: Long? = null
)
