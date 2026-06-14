package app.piptally.util

import org.junit.Assert.assertEquals
import org.junit.Test

class GameConstantsTest {

    @Test
    fun getShakerIndex_rotatesThroughPlayers() {
        val playerCount = 4
        
        assertEquals(0, GameConstants.getShakerIndex(0, playerCount))
        assertEquals(1, GameConstants.getShakerIndex(1, playerCount))
        assertEquals(2, GameConstants.getShakerIndex(2, playerCount))
        assertEquals(3, GameConstants.getShakerIndex(3, playerCount))
        
        // Rotates back to 0
        assertEquals(0, GameConstants.getShakerIndex(4, playerCount))
        assertEquals(1, GameConstants.getShakerIndex(5, playerCount))
    }

    @Test
    fun roundSpinnerSequence_hasCorrectSize() {
        assertEquals(14, GameConstants.TOTAL_ROUNDS)
        assertEquals(14, GameConstants.ROUND_SPINNER_SEQUENCE.size)
    }

    @Test
    fun roundSpinnerSequence_hasCorrectValues() {
        val expected = listOf(6, 5, 4, 3, 2, 1, 0, 0, 1, 2, 3, 4, 5, 6)
        assertEquals(expected, GameConstants.ROUND_SPINNER_SEQUENCE)
    }
}
