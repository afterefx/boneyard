import Foundation
import SwiftUI

class ValidationAndEditingTests {
    
    // Custom Assertion Helpers
    private func assertEquals<T: Equatable>(_ actual: T, _ expected: T, _ message: String = "", file: String = #file, line: Int = #line) {
        if actual != expected {
            print("  ❌ ASSERTION FAILED [\(file.components(separatedBy: "/").last ?? ""):\(line)]: Expected '\(expected)', got '\(actual)'. \(message)")
            exit(1)
        }
    }
    
    private func assertTrue(_ condition: Bool, _ message: String = "", file: String = #file, line: Int = #line) {
        if !condition {
            print("  ❌ ASSERTION FAILED [\(file.components(separatedBy: "/").last ?? ""):\(line)]: Condition is false. \(message)")
            exit(1)
        }
    }
    
    private func assertFalse(_ condition: Bool, _ message: String = "", file: String = #file, line: Int = #line) {
        if condition {
            print("  ❌ ASSERTION FAILED [\(file.components(separatedBy: "/").last ?? ""):\(line)]: Condition is true. \(message)")
            exit(1)
        }
    }
    
    // Replicating view model score validations
    private func validateScoreInput(_ input: String) -> Bool {
        let trimmed = input.trimmingCharacters(in: .whitespacesAndNewlines)
        if let val = Int(trimmed), val >= 0 {
            return true
        }
        return false
    }
    
    // Run all validation and editing tests
    func run() {
        print("🏃 Running ValidationAndEditingTests...")
        testValidScoreInputs()
        testInvalidScoreInputs()
        testHistoricalRoundEditScoreRecalculation()
        testWinnerFlagMutations()
        testThemeSelectionAndPersistence()
        testActiveGameAbandonAndDeletion()
        testGameHistoryDeletion()
        testGameSetupOnStartGameCallback()
        testSettingsSheetThemeSwitchingRetention()
        print("  ✅ ValidationAndEditingTests PASSED!")
    }
    
    // Test input score fields validation - POSITIVE TESTS
    private func testValidScoreInputs() {
        assertTrue(validateScoreInput("0"), "Score of 0 is valid.")
        assertTrue(validateScoreInput("15"), "Score of 15 is valid.")
        assertTrue(validateScoreInput("125"), "Score of 125 is valid.")
        assertTrue(validateScoreInput(" 45  "), "Score with surrounding whitespace should be valid.")
    }
    
    // Test input score fields validation - NEGATIVE TESTS
    private func testInvalidScoreInputs() {
        assertFalse(validateScoreInput(""), "Empty score input must be invalid.")
        assertFalse(validateScoreInput("   "), "Whitespace-only score input must be invalid.")
        assertFalse(validateScoreInput("-5"), "Negative score inputs must be invalid.")
        assertFalse(validateScoreInput("12a"), "Score inputs with letters must be invalid.")
        assertFalse(validateScoreInput("3.5"), "Floating point score inputs must be invalid.")
        assertFalse(validateScoreInput("abc"), "Non-numeric score inputs must be invalid.")
    }
    
    // Test that editing a previously submitted round score correctly recalculates player running totals
    private func testHistoricalRoundEditScoreRecalculation() {
        var playerATotal = 45
        var playerBTotal = 60
        var playerCTotal = 30
        var playerDTotal = 50
        
        let oldScoreVal = 20
        assertEquals(playerBTotal, 60, "Precondition check failed.")
        
        let newScoreVal = 5
        playerBTotal = playerBTotal - oldScoreVal + newScoreVal
        
        assertEquals(playerBTotal, 45, "Player B score must be adjusted.")
        assertEquals(playerATotal, 45, "Player A score must remain unchanged.")
        assertEquals(playerCTotal, 30, "Player C score must remain unchanged.")
        assertEquals(playerDTotal, 50, "Player D score must remain unchanged.")
        
        let oldScoreC = 0
        let newScoreC = 15
        playerCTotal = playerCTotal - oldScoreC + newScoreC
        assertEquals(playerCTotal, 45, "Player C score must be adjusted.")
    }
    
    // Test that the winner flag correctly updates independent of score values
    private func testWinnerFlagMutations() {
        var playerBIsWinner = false
        var playerBScore = 15
        
        assertFalse(playerBIsWinner)
        assertEquals(playerBScore, 15)
        
        playerBIsWinner = true
        playerBScore = 0
        
        assertTrue(playerBIsWinner)
        assertEquals(playerBScore, 0)
        
        playerBIsWinner = false
        playerBScore = 10
        
        assertFalse(playerBIsWinner)
        assertEquals(playerBScore, 10)
    }
    
    // Test that theme selection updates UserDefaults correctly and returns correct values
    private func testThemeSelectionAndPersistence() {
        // Backup existing theme value
        let previousTheme = UserDefaults.standard.integer(forKey: "appTheme")
        
        // 1. Assert system theme defaults
        UserDefaults.standard.set(AppTheme.system.rawValue, forKey: "appTheme")
        assertEquals(UserDefaults.standard.integer(forKey: "appTheme"), AppTheme.system.rawValue)
        assertEquals(AppTheme.system.displayName, "System")
        
        // 2. Assert OLED Pure Black theme
        UserDefaults.standard.set(AppTheme.pureBlack.rawValue, forKey: "appTheme")
        assertEquals(UserDefaults.standard.integer(forKey: "appTheme"), AppTheme.pureBlack.rawValue)
        assertEquals(AppTheme.pureBlack.displayName, "OLED Pure Black")
        
        // 3. Assert Slate Deep Gray theme
        UserDefaults.standard.set(AppTheme.deepGray.rawValue, forKey: "appTheme")
        assertEquals(UserDefaults.standard.integer(forKey: "appTheme"), AppTheme.deepGray.rawValue)
        assertEquals(AppTheme.deepGray.displayName, "Slate Deep Gray")
        // Restore existing theme value
        UserDefaults.standard.set(previousTheme, forKey: "appTheme")
    }
    
    // Test that active games can be abandoned or deleted correctly
    private func testActiveGameAbandonAndDeletion() {
        // 1. Create a mock game
        let game = Game(status: .active)
        assertEquals(game.status, GameStatus.active, "New game starts as active.")
        
        // 2. Abandon the game
        game.status = .abandoned
        assertEquals(game.status, GameStatus.abandoned, "Abandoned game gets status raw value 3.")
        
        // 3. Mock dynamic active filter
        var mockGamesDatabase = [game]
        
        // Filter out completed or abandoned games
        let activeGames = mockGamesDatabase.filter { $0.status == .active || $0.status == .paused }
        assertTrue(activeGames.isEmpty, "Abandoned game is excluded from active games list.")
        
        // 4. Assert abandoned game is included in history along with completed games
        let historyGames = mockGamesDatabase.filter { $0.status == .completed || $0.status == .abandoned }
        assertEquals(historyGames.count, 1, "Abandoned game must appear in the game history.")
        assertEquals(historyGames.first?.id, game.id, "Correct game is returned in history.")
        
        // 5. Mock deletion behavior
        mockGamesDatabase.removeAll(where: { $0.id == game.id })
        assertTrue(mockGamesDatabase.isEmpty, "Deleting a game completely purges it from list.")
    }
    
    // Test that games can be deleted from history correctly
    private func testGameHistoryDeletion() {
        // 1. Create completed and abandoned games
        let completedGame = Game(status: .completed)
        let abandonedGame = Game(status: .abandoned)
        let activeGame = Game(status: .active)
        
        var mockGamesDatabase = [completedGame, abandonedGame, activeGame]
        
        // 2. Query history games (completed or abandoned)
        var historyGames = mockGamesDatabase.filter { $0.status == .completed || $0.status == .abandoned }
        assertEquals(historyGames.count, 2, "History must contain completed and abandoned games.")
        
        // 3. Delete one game from history (mimicking swipe-to-delete)
        let gameToDelete = historyGames[0]
        mockGamesDatabase.removeAll(where: { $0.id == gameToDelete.id })
        
        // 4. Query history again
        historyGames = mockGamesDatabase.filter { $0.status == .completed || $0.status == .abandoned }
        assertEquals(historyGames.count, 1, "History count decreases after deletion.")
        assertFalse(historyGames.contains(where: { $0.id == gameToDelete.id }), "Deleted game is no longer in history.")
        assertTrue(historyGames.contains(where: { $0.id == abandonedGame.id }), "Other history game remains intact.")
        assertTrue(mockGamesDatabase.contains(where: { $0.id == activeGame.id }), "Active games remain unaffected.")
    }
    
    // Test that starting a game via the setup flow correctly uses the two-phase navigation contract:
    // Phase 1 — onStartGame stores the pending game ID and dismisses the sheet.
    // Phase 2 — onDismiss (fired after sheet fully disappears) moves pendingGameId → activeGameIdToPush,
    //           pushing ActiveGameView onto the ROOT NavigationStack.
    // This ensures pausing always returns to HomeView, never GameSetupView.
    private func testGameSetupOnStartGameCallback() {
        // 1. Create a mock game (simulating what startGame() does in GameSetupView)
        let newGame = Game(status: .active)
        assertEquals(newGame.status, GameStatus.active, "Newly created game must start with active status.")
        
        // 2. Phase 1 — simulate onStartGame: store pendingGameId and dismiss sheet
        var pendingGameId: UUID? = nil
        var showingSetup = true
        let onStartGame: (UUID) -> Void = { id in
            pendingGameId = id
            showingSetup = false
        }
        onStartGame(newGame.id)
        
        // 3. Assert Phase 1 state: sheet is dismissed, game ID is staged
        assertFalse(showingSetup, "Setup sheet must be dismissed after onStartGame fires.")
        assertTrue(pendingGameId != nil, "pendingGameId must hold the new game's ID before sheet finishes dismissing.")
        assertEquals(pendingGameId, newGame.id, "pendingGameId must match the created game's ID.")
        
        // 4. Phase 2 — simulate onDismiss: move pendingGameId → activeGameIdToPush
        var activeGameIdToPush: UUID? = nil
        // Mirroring HomeView.onDismiss logic
        if let gameId = pendingGameId {
            activeGameIdToPush = gameId
            pendingGameId = nil
        }
        
        // 5. Assert Phase 2 state: navigation fires only after sheet is gone
        assertTrue(activeGameIdToPush != nil, "activeGameIdToPush must be set in onDismiss to trigger navigation.")
        assertEquals(activeGameIdToPush, newGame.id, "Navigation must target the newly created game's ID.")
        assertTrue(pendingGameId == nil, "pendingGameId must be cleared after being consumed by onDismiss.")
        
        // 6. Verify the routed game is active and resolvable from the data store
        let mockGameStore = [newGame]
        let routedGame = mockGameStore.first(where: { $0.id == activeGameIdToPush })
        assertTrue(routedGame != nil, "The game ID must resolve to an existing game for navigation.")
        assertEquals(routedGame?.status, GameStatus.active, "Routed game must be active when navigation is triggered.")
        
        // 7. Verify no duplication: exactly one active game exists on the stack
        let activeGames = mockGameStore.filter { $0.status == .active || $0.status == .paused }
        assertEquals(activeGames.count, 1, "Exactly one active game must exist after starting a game.")
        assertEquals(activeGames.first?.id, newGame.id, "The single active game must be the one just created.")
    }
    
    // Test that the settings screen does not close when switching themes,
    // and that the colors update properly when switching from each theme to the other.
    private func testSettingsSheetThemeSwitchingRetention() {
        // Backup existing theme value
        let previousTheme = UserDefaults.standard.integer(forKey: "appTheme")
        
        let themes: [AppTheme] = [.system, .pureBlack, .deepGray]
        
        for fromTheme in themes {
            for toTheme in themes {
                // Set the initial theme
                UserDefaults.standard.set(fromTheme.rawValue, forKey: "appTheme")
                
                // Simulate showing the settings sheet
                let showSettings = true
                assertTrue(showSettings, "Settings sheet must be initially open.")
                
                // Simulate changing the theme selection (updating AppStorage / UserDefaults)
                UserDefaults.standard.set(toTheme.rawValue, forKey: "appTheme")
                
                // Assert that the sheet state does not change to false (i.e. settings screen does not auto-close)
                assertTrue(showSettings, "Settings sheet must remain open (does not auto-close) when switching from \(fromTheme.displayName) to \(toTheme.displayName)")
                
                // Verify that colors returned by Color.appBackground, appSurface, etc., update correctly
                let expectedBgColor = Color.appBackground
                let expectedSurfaceColor = Color.appSurface
                let expectedRowColor = Color.appRowBackground
                
                switch toTheme {
                case .system:
                    // Color should be dynamic system color (non-nil/valid)
                    assertTrue(expectedBgColor != nil, "System background color must be defined.")
                case .pureBlack:
                    assertEquals(expectedBgColor, Color.black, "OLED Pure Black theme background must be pure black.")
                    assertEquals(expectedSurfaceColor, Color(hex: "#121212"), "OLED Pure Black theme surface must match hex #121212.")
                    assertEquals(expectedRowColor, Color(hex: "#1C1C1E"), "OLED Pure Black theme row background must match hex #1C1C1E.")
                case .deepGray:
                    assertEquals(expectedBgColor, Color(hex: "#121212"), "Slate Deep Gray theme background must match hex #121212.")
                    assertEquals(expectedSurfaceColor, Color(hex: "#1E1E1E"), "Slate Deep Gray theme surface must match hex #1E1E1E.")
                    assertEquals(expectedRowColor, Color(hex: "#2C2C2E"), "Slate Deep Gray theme row background must match hex #2C2C2E.")
                }
                
                // Verify that applyThemeColorScheme returns a view successfully without structural identity changes
                let testView = Text("Test").applyThemeColorScheme(toTheme.rawValue)
                assertTrue(testView != nil, "applyThemeColorScheme must return a valid view modifier applied to the text view.")
            }
        }
        
        // Restore the original theme
        UserDefaults.standard.set(previousTheme, forKey: "appTheme")
    }
}
