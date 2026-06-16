package app.boneyard.domain.model

data class GamePlayer(
    val gameId: Long,
    val player: Player,
    val seatPosition: Int,
    val totalScore: Int = 0,
    val isWinner: Boolean = false
)
