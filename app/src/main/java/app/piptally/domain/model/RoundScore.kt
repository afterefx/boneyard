package app.piptally.domain.model

data class RoundScore(
    val roundId: Long,
    val playerId: Long,
    val score: Int
)
