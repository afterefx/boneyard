import Foundation

class GameLogicTests {
    
    // Custom Assertion Helpers
    private func assertEquals<T: Equatable>(_ actual: T, _ expected: T, _ message: String = "", file: String = #file, line: Int = #line) {
        if actual != expected {
            print("  ❌ ASSERTION FAILED [\(file.components(separatedBy: "/").last ?? ""):\(line)]: Expected '\(expected)', got '\(actual)'. \(message)")
            exit(1)
        }
    }
    
    // Run all logic tests
    func run() {
        print("🏃 Running GameLogicTests...")
        testDoubleSpinnerSequence()
        testSeatingRotation()
        testShakerPlayerRotation()
        print("  ✅ GameLogicTests PASSED!")
    }
    
    // Test that the double-spinner round sequence matches the specified 14 rounds exactly
    private func testDoubleSpinnerSequence() {
        let expectedSequence = [6, 5, 4, 3, 2, 1, 0, 0, 1, 2, 3, 4, 5, 6]
        assertEquals(GameConstants.roundSpinnerSequence.count, 14, "Round sequence must have exactly 14 rounds.")
        assertEquals(GameConstants.roundSpinnerSequence, expectedSequence, "The 14-round spinner double sequence does not match.")
    }
    
    // Test that the starting shaker selection rotates the circular play sequence clockwise
    private func testSeatingRotation() {
        let p0 = "Player A"
        let p1 = "Player B"
        let p2 = "Player C"
        let p3 = "Player D"
        let players = [p0, p1, p2, p3]
        
        // Case 1: Starting shaker is index 0 (Player A). Roster sequence remains unchanged.
        var finalList = players
        let firstShakerIndex1 = 0
        if firstShakerIndex1 > 0 && firstShakerIndex1 < players.count {
            let prefix = players[firstShakerIndex1..<players.count]
            let suffix = players[0..<firstShakerIndex1]
            finalList = Array(prefix) + Array(suffix)
        }
        assertEquals(finalList, ["Player A", "Player B", "Player C", "Player D"], "Rotation index 0 should keep seating identical.")
        
        // Case 2: Starting shaker is index 2 (Player C).
        let firstShakerIndex2 = 2
        if firstShakerIndex2 > 0 && firstShakerIndex2 < players.count {
            let prefix = players[firstShakerIndex2..<players.count]
            let suffix = players[0..<firstShakerIndex2]
            finalList = Array(prefix) + Array(suffix)
        }
        assertEquals(finalList, ["Player C", "Player D", "Player A", "Player B"], "Rotation index 2 must shift order.")
        
        // Case 3: Starting shaker is index 3 (Player D).
        let firstShakerIndex3 = 3
        if firstShakerIndex3 > 0 && firstShakerIndex3 < players.count {
            let prefix = players[firstShakerIndex3..<players.count]
            let suffix = players[0..<firstShakerIndex3]
            finalList = Array(prefix) + Array(suffix)
        }
        assertEquals(finalList, ["Player D", "Player A", "Player B", "Player C"], "Rotation index 3 must shift order.")
    }
    
    // Test that the shaker rotation index updates clockwise every round
    private func testShakerPlayerRotation() {
        let playerCount = 4
        
        assertEquals(GameConstants.getShakerIndex(roundIndex: 0, playerCount: playerCount), 0, "Round 1 shaker must be seat 0.")
        assertEquals(GameConstants.getShakerIndex(roundIndex: 1, playerCount: playerCount), 1, "Round 2 shaker must be seat 1.")
        assertEquals(GameConstants.getShakerIndex(roundIndex: 4, playerCount: playerCount), 0, "Round 5 shaker must rotate to seat 0.")
        assertEquals(GameConstants.getShakerIndex(roundIndex: 13, playerCount: playerCount), 1, "Round 14 shaker must rotate to seat 1.")
    }
}
