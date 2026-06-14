import Foundation

struct GameConstants {
    /// The sequence of spinner (pip) values for each of the 14 rounds.
    static let roundSpinnerSequence = [6, 5, 4, 3, 2, 1, 0, 0, 1, 2, 3, 4, 5, 6]
    
    static var totalRounds: Int {
        return roundSpinnerSequence.count
    }
    
    /// Returns the seat-index of the player who is the shaker for a given round.
    /// Rotates through all players in order.
    static func getShakerIndex(roundIndex: Int, playerCount: Int) -> Int {
        guard playerCount > 0 else { return 0 }
        return roundIndex % playerCount
    }
}
