package app.piptally.util

object GameConstants {
    /**
     * The sequence of spinner (pip) values for each of the 14 rounds.
     * The shaker must use a domino with exactly this many pips for their starting tile.
     */
    val ROUND_SPINNER_SEQUENCE = listOf(6, 5, 4, 3, 2, 1, 0, 0, 1, 2, 3, 4, 5, 6)

    val TOTAL_ROUNDS = ROUND_SPINNER_SEQUENCE.size  // 14

    /**
     * Returns the seat-index of the player who is the shaker for a given round.
     * Rotates through all players in order.
     */
    fun getShakerIndex(roundIndex: Int, playerCount: Int): Int =
        roundIndex % playerCount
}
