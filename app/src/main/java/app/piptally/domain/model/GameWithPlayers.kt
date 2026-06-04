package app.piptally.domain.model

data class GameWithPlayers(
    val game: Game,
    val players: List<GamePlayer>
) {
    val sortedByScore: List<GamePlayer>
        get() = players.sortedBy { it.totalScore }

    val leader: GamePlayer?
        get() = players.minByOrNull { it.totalScore }
}
