package app.piptally.domain.model

data class PlayerStats(
    val player: Player,
    val gamesPlayed: Int,
    val gamesWon: Int,
    val totalScore: Int,
    val averageScorePerGame: Double,
    val bestScore: Int,  // lowest cumulative score in a completed game (lower is better in dominoes)
    val winRate: Double = if (gamesPlayed > 0) gamesWon.toDouble() / gamesPlayed else 0.0
)
