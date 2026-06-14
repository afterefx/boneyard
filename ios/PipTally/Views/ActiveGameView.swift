import SwiftUI
import SwiftData

struct ActiveGameView: View {
    @Environment(\.modelContext) private var modelContext
    @Environment(\.dismiss) private var dismiss
    
    let gameId: UUID
    
    @Query private var games: [Game]
    
    @State private var scoresText: [UUID: String] = [:]
    @State private var winnerPlayerId: UUID? = nil
    @State private var errors: [UUID: Bool] = [:]
    @State private var viewingRoundIndex = 0
    @State private var isScoreboardExpanded = true
    @State private var showingPauseAlert = false
    @State private var navigateToSummary = false
    @State private var isEditingHistoricalRound = false
    
    private let trophyGold = Color(hex: "#FFC107") ?? .yellow
    private let shakerGold = Color(hex: "#FFC107") ?? .yellow
    
    var body: some View {
        if let game = games.first(where: { $0.id == gameId }) {
            let isViewingHistory = viewingRoundIndex < game.currentRoundIndex
            let isViewingFuture = viewingRoundIndex > game.currentRoundIndex
            let isNotCurrentRound = viewingRoundIndex != game.currentRoundIndex
            let playersList = game.gamePlayers.sorted(by: { $0.seatPosition < $1.seatPosition })
            let currentSpinner = GameConstants.roundSpinnerSequence[viewingRoundIndex]
            
            // Find who shaker is for the currently viewed round
            let shakerIdx = GameConstants.getShakerIndex(roundIndex: viewingRoundIndex, playerCount: playersList.count)
            let shakerPlayer = playersList.indices.contains(shakerIdx) ? playersList[shakerIdx].player : nil
            
            VStack(spacing: 0) {
                ScrollView {
                    VStack(spacing: 16) {
                        // Round Navigation Row
                        RoundNavigationRow(
                            viewingRoundIndex: $viewingRoundIndex,
                            maxRoundIndex: GameConstants.totalRounds - 1
                        )
                        .disabled(isEditingHistoricalRound)
                        .opacity(isEditingHistoricalRound ? 0.5 : 1.0)
                        .padding(.horizontal, 4)
                        .padding(.top, 8)
                        
                        // Round Header Card (Shaker + Spinner)
                        RoundHeaderCard(
                            spinnerValue: currentSpinner,
                            shakerName: shakerPlayer?.name ?? "Unknown",
                            shakerColorHex: shakerPlayer?.colorHex ?? "#1E88E5",
                            shakerAvatarIndex: shakerPlayer?.avatarIndex ?? 0
                        )
                        .padding(.horizontal, 16)
                        
                        // Round Progress Bar (reflects actual game progress)
                        VStack(spacing: 6) {
                            ProgressView(value: Double(game.currentRoundIndex), total: 14.0)
                                .tint(.accentColor)
                            
                            HStack {
                                Text("\(game.currentRoundIndex) of 14 rounds completed")
                                    .font(.system(size: 11))
                                    .foregroundColor(.secondary)
                                Spacer()
                            }
                        }
                        .padding(.horizontal, 20)
                        
                        Divider()
                            .padding(.horizontal, 16)
                            .padding(.vertical, 4)
                        
                        // Score List Section
                        VStack(alignment: .leading, spacing: 8) {
                            HStack {
                                Text(isViewingHistory ? "Round \(viewingRoundIndex + 1) Scores" : (isViewingFuture ? "Upcoming Round" : "Enter Scores"))
                                    .font(.system(size: 15, weight: .bold))
                                    .foregroundColor(.secondary)
                                
                                if isViewingHistory {
                                    Spacer()
                                    
                                    if !isEditingHistoricalRound {
                                        Button(action: {
                                            startEditingHistoricalRound(game: game)
                                        }) {
                                            HStack(spacing: 4) {
                                                Image(systemName: "pencil")
                                                Text("Edit")
                                            }
                                            .font(.system(size: 13, weight: .semibold))
                                            .foregroundColor(.accentColor)
                                        }
                                        .buttonStyle(.plain)
                                    }
                                }
                            }
                            .padding(.horizontal, 20)
                            
                            if isViewingHistory && !isEditingHistoricalRound {
                                // History Read-Only Rows
                                let viewedRound = game.rounds.first(where: { $0.roundIndex == viewingRoundIndex })
                                
                                ForEach(playersList) { gp in
                                    if let player = gp.player {
                                        let roundScore = viewedRound?.scores.first(where: { $0.playerId == player.id })
                                        let scoreVal = roundScore?.score ?? 0
                                        let isRoundWinner = roundScore?.isWinner ?? false
                                        
                                        HStack(alignment: .center) {
                                            PlayerAvatar(
                                                name: player.name,
                                                colorHex: player.colorHex,
                                                avatarIndex: player.avatarIndex,
                                                size: .small
                                            )
                                            
                                            Spacer()
                                                .frame(width: 12)
                                            
                                            Text(player.name)
                                                .font(.system(size: 17))
                                                .foregroundColor(.secondary)
                                            
                                            Spacer()
                                            
                                            if isRoundWinner {
                                                Image(systemName: "trophy.fill")
                                                    .foregroundColor(trophyGold)
                                                    .font(.system(size: 14))
                                                Spacer()
                                                    .frame(width: 6)
                                            }
                                            
                                            Text("\(scoreVal) pts")
                                                .font(.system(size: 17, weight: .semibold))
                                                .foregroundColor(.primary)
                                        }
                                        .padding(.horizontal, 16)
                                        .padding(.vertical, 8)
                                    }
                                }
                            } else if isViewingFuture {
                                // Future Unplayed Round State
                                VStack(spacing: 12) {
                                    Image(systemName: "clock.arrow.circlepath")
                                        .font(.system(size: 40))
                                        .foregroundColor(.secondary.opacity(0.5))
                                        .padding(.bottom, 4)
                                    
                                    Text("Round \(viewingRoundIndex + 1) is Unplayed")
                                        .font(.system(size: 15, weight: .semibold))
                                        .foregroundColor(.secondary)
                                    
                                    Text("This round will become active once you complete Round \(game.currentRoundIndex + 1).")
                                        .font(.system(size: 12))
                                        .foregroundColor(.secondary.opacity(0.7))
                                        .multilineTextAlignment(.center)
                                        .padding(.horizontal, 32)
                                }
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 32)
                            } else {
                                // Live Interactive Round Score Inputs
                                ForEach(playersList) { gp in
                                    if let player = gp.player {
                                        let isWinnerBinding = Binding(
                                            get: { winnerPlayerId == player.id },
                                            set: { selected in
                                                if selected {
                                                    winnerPlayerId = player.id
                                                    scoresText[player.id] = "0"
                                                } else if winnerPlayerId == player.id {
                                                    winnerPlayerId = nil
                                                    scoresText[player.id] = ""
                                                }
                                            }
                                        )
                                        let scoreTextBinding = Binding(
                                            get: { scoresText[player.id, default: ""] },
                                            set: { scoresText[player.id] = $0 }
                                        )
                                        
                                        ScoreEntryRow(
                                            playerName: player.name,
                                            colorHex: player.colorHex,
                                            avatarIndex: player.avatarIndex,
                                            scoreText: scoreTextBinding,
                                            isWinner: isWinnerBinding,
                                            showError: errors[player.id, default: false],
                                            onWinnerToggle: {
                                                if winnerPlayerId == player.id {
                                                    winnerPlayerId = nil
                                                    scoresText[player.id] = ""
                                                } else {
                                                    winnerPlayerId = player.id
                                                    scoresText[player.id] = "0"
                                                }
                                                errors[player.id] = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        
                        Divider()
                            .padding(.horizontal, 16)
                            .padding(.vertical, 4)
                        
                        // Collapsible Scoreboard Table
                        VStack(spacing: 8) {
                            HStack {
                                Text("Standings")
                                    .font(.system(size: 15, weight: .bold))
                                    .foregroundColor(.secondary)
                                
                                Spacer()
                                
                                Button(action: {
                                    withAnimation { isScoreboardExpanded.toggle() }
                                }) {
                                    Text(isScoreboardExpanded ? "Hide" : "Show")
                                        .font(.system(size: 14, weight: .semibold))
                                }
                            }
                            .padding(.horizontal, 20)
                            
                            if isScoreboardExpanded {
                                let standings = buildScoreboardEntries(game: game)
                                ScoreboardTable(entries: standings)
                                    .padding(.horizontal, 16)
                            }
                        }
                        .padding(.bottom, 16)
                    }
                }
                
                // Bottom Bar Controls
                VStack {
                    Divider()
                    
                    HStack(spacing: 16) {
                        if isNotCurrentRound {
                            if isEditingHistoricalRound {
                                // Cancel Edit button
                                Button(action: {
                                    isEditingHistoricalRound = false
                                    errors = [:]
                                }) {
                                    Text("Cancel")
                                        .font(.system(size: 15, weight: .semibold))
                                        .foregroundColor(.red)
                                        .frame(width: 88, height: 48)
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 12)
                                                .stroke(Color.red, lineWidth: 1)
                                        )
                                }
                                
                                // Save Changes button
                                Button(action: { saveHistoricalRoundChanges(game: game) }) {
                                    Text("Save Changes")
                                        .font(.system(size: 17, weight: .bold))
                                        .foregroundColor(.white)
                                        .frame(maxWidth: .infinity)
                                        .padding(.vertical, 14)
                                        .background(Color.accentColor)
                                        .cornerRadius(12)
                                }
                            } else {
                                Button(action: {
                                    viewingRoundIndex = game.currentRoundIndex
                                }) {
                                    Text("Return to Current Round")
                                        .font(.system(size: 17, weight: .bold))
                                        .foregroundColor(.white)
                                        .frame(maxWidth: .infinity)
                                        .padding(.vertical, 14)
                                        .background(Color.accentColor)
                                        .cornerRadius(12)
                                }
                            }
                        } else {
                            // Revert Round Undo button
                            Button(action: { undoRound(game: game) }) {
                                HStack(spacing: 6) {
                                    Image(systemName: "arrow.uturn.backward")
                                    Text("Undo")
                                }
                                .font(.system(size: 15, weight: .semibold))
                                .foregroundColor(game.currentRoundIndex > 0 ? .accentColor : .secondary)
                                .frame(width: 88, height: 48)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 12)
                                        .stroke(game.currentRoundIndex > 0 ? Color.accentColor : Color.secondary.opacity(0.3), lineWidth: 1)
                                )
                            }
                            .disabled(game.currentRoundIndex == 0)
                            
                            // Submit Round scores button
                            Button(action: { submitRound(game: game) }) {
                                Text("Submit Round")
                                    .font(.system(size: 17, weight: .bold))
                                    .foregroundColor(.white)
                                    .frame(maxWidth: .infinity)
                                    .padding(.vertical, 14)
                                    .background(Color.accentColor)
                                    .cornerRadius(12)
                            }
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 16)
                    .background(Color.appRowBackground)
                }
            }
            .background(Color.appBackground)
            .navigationTitle("Round \(viewingRoundIndex + 1) of 14")
            .appNavigationBarTitleDisplayMode()
            .navigationBarBackButtonHidden(true)
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button(action: { showingPauseAlert = true }) {
                        Image(systemName: "pause.fill")
                    }
                    .disabled(isEditingHistoricalRound)
                }
            }
            .alert("Pause Game?", isPresented: $showingPauseAlert) {
                Button("Pause", role: .destructive) {
                    pauseGame(game: game)
                }
                Button("Keep Playing", role: .cancel) {}
            } message: {
                Text("Your progress is saved. Resume anytime from the home screen.")
            }
            .navigationDestination(isPresented: $navigateToSummary) {
                GameSummaryView(gameId: gameId)
            }
            .onAppear {
                if game.status == .completed {
                    dismiss()
                } else {
                    initializeInputs(game: game)
                }
            }
        } else {
            VStack {
                Text("Game not found.")
                Button("Go Home") {
                    dismiss()
                }
            }
        }
    }
    
    private func startEditingHistoricalRound(game: Game) {
        guard let viewedRound = game.rounds.first(where: { $0.roundIndex == viewingRoundIndex }) else { return }
        
        scoresText = [:]
        winnerPlayerId = nil
        errors = [:]
        
        for score in viewedRound.scores {
            scoresText[score.playerId] = "\(score.score)"
            if score.isWinner {
                winnerPlayerId = score.playerId
            }
        }
        
        isEditingHistoricalRound = true
    }
    
    private func saveHistoricalRoundChanges(game: Game) {
        // Validation: each player must have a valid non-negative integer score
        var hasError = false
        errors = [:]
        
        for gp in game.gamePlayers {
            if let player = gp.player {
                let text = scoresText[player.id, default: ""].trimmingCharacters(in: .whitespacesAndNewlines)
                if let val = Int(text), val >= 0 {
                    // Valid score
                } else {
                    errors[player.id] = true
                    hasError = true
                }
            }
        }
        
        if hasError { return }
        
        guard let viewedRound = game.rounds.first(where: { $0.roundIndex == viewingRoundIndex }) else { return }
        
        // Update scores and adjust player running totals
        for gp in game.gamePlayers {
            if let player = gp.player {
                let scoreText = scoresText[player.id, default: "0"]
                let newScoreVal = Int(scoreText) ?? 0
                let isWinner = winnerPlayerId == player.id
                
                if let oldRoundScore = viewedRound.scores.first(where: { $0.playerId == player.id }) {
                    let oldScoreVal = oldRoundScore.score
                    
                    // Update score record
                    oldRoundScore.score = newScoreVal
                    oldRoundScore.isWinner = isWinner
                    
                    // Adjust player running total
                    gp.totalScore = gp.totalScore - oldScoreVal + newScoreVal
                }
            }
        }
        
        try? modelContext.save()
        isEditingHistoricalRound = false
    }
    
    private func initializeInputs(game: Game) {
        viewingRoundIndex = game.currentRoundIndex
        winnerPlayerId = nil
        errors = [:]
        
        for gp in game.gamePlayers {
            if let player = gp.player {
                scoresText[player.id] = ""
            }
        }
    }
    
    private func buildScoreboardEntries(game: Game) -> [ScoreboardEntry] {
        let sorted = game.gamePlayers.sorted(by: { $0.totalScore < $1.totalScore })
        return sorted.enumerated().map { idx, gp in
            ScoreboardEntry(
                playerId: gp.player?.id ?? UUID(),
                playerName: gp.player?.name ?? "Unknown",
                colorHex: gp.player?.colorHex ?? "#1E88E5",
                avatarIndex: gp.player?.avatarIndex ?? 0,
                totalScore: gp.totalScore,
                rank: idx + 1
            )
        }
    }
    
    private func submitRound(game: Game) {
        // Validation: each player must have a valid non-negative integer score
        var hasError = false
        errors = [:]
        
        for gp in game.gamePlayers {
            if let player = gp.player {
                let text = scoresText[player.id, default: ""].trimmingCharacters(in: .whitespacesAndNewlines)
                if let val = Int(text), val >= 0 {
                    // Valid score
                } else {
                    errors[player.id] = true
                    hasError = true
                }
            }
        }
        
        if hasError { return }
        
        // Dynamic game logic
        let currentRound = game.currentRoundIndex
        let playersList = game.gamePlayers.sorted(by: { $0.seatPosition < $1.seatPosition })
        let shakerIdx = GameConstants.getShakerIndex(roundIndex: currentRound, playerCount: playersList.count)
        let shakerPlayerId = playersList.indices.contains(shakerIdx) ? playersList[shakerIdx].player?.id : nil
        let spinnerValue = GameConstants.roundSpinnerSequence[currentRound]
        
        // Save the round record
        let newRound = Round(roundIndex: currentRound, spinnerValue: spinnerValue, shakerPlayerId: shakerPlayerId ?? UUID())
        newRound.game = game
        modelContext.insert(newRound)
        
        // Add scores
        for gp in game.gamePlayers {
            if let player = gp.player {
                let scoreText = scoresText[player.id, default: "0"]
                let scoreVal = Int(scoreText) ?? 0
                let isWinner = winnerPlayerId == player.id
                
                let rs = RoundScore(playerId: player.id, score: scoreVal, isWinner: isWinner)
                rs.round = newRound
                newRound.scores.append(rs)
                modelContext.insert(rs)
                
                // Add to player running total
                gp.totalScore += scoreVal
            }
        }
        
        let nextRound = currentRound + 1
        if nextRound >= GameConstants.totalRounds {
            // Determine match winner (lowest total score)
            let winnerGp = game.gamePlayers.min(by: { $0.totalScore < $1.totalScore })
            game.winnerPlayerId = winnerGp?.player?.id
            game.status = .completed
            game.completedAt = Date()
            
            try? modelContext.save()
            navigateToSummary = true
        } else {
            game.currentRoundIndex = nextRound
            try? modelContext.save()
            
            // Re-initialize for next round
            initializeInputs(game: game)
        }
    }
    
    private func undoRound(game: Game) {
        guard game.currentRoundIndex > 0 else { return }
        
        let lastCompletedRoundIndex = game.currentRoundIndex - 1
        
        // Find completed round
        if let round = game.rounds.first(where: { $0.roundIndex == lastCompletedRoundIndex }) {
            // Revert player scores
            for score in round.scores {
                if let gp = game.gamePlayers.first(where: { $0.player?.id == score.playerId }) {
                    gp.totalScore -= score.score
                }
            }
            
            // Delete scores & round record
            modelContext.delete(round)
            game.currentRoundIndex = lastCompletedRoundIndex
            
            try? modelContext.save()
            initializeInputs(game: game)
        }
    }
    
    private func pauseGame(game: Game) {
        game.status = .paused
        try? modelContext.save()
        dismiss()
    }
}

// Collapsible Navigation Row for browsing completed rounds history
struct RoundNavigationRow: View {
    @Binding var viewingRoundIndex: Int
    let maxRoundIndex: Int
    
    var body: some View {
        HStack(alignment: .center) {
            Button(action: {
                if viewingRoundIndex > 0 { viewingRoundIndex -= 1 }
            }) {
                Image(systemName: "chevron.left")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(viewingRoundIndex > 0 ? .accentColor : .secondary.opacity(0.3))
                    .frame(width: 44, height: 44)
            }
            .disabled(viewingRoundIndex == 0)
            
            Spacer()
            
            Text("Round \(viewingRoundIndex + 1) of 14")
                .font(.system(size: 17, weight: .semibold))
                .foregroundColor(.primary)
            
            Spacer()
            
            Button(action: {
                if viewingRoundIndex < maxRoundIndex { viewingRoundIndex += 1 }
            }) {
                Image(systemName: "chevron.right")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(viewingRoundIndex < maxRoundIndex ? .accentColor : .secondary.opacity(0.3))
                    .frame(width: 44, height: 44)
            }
            .disabled(viewingRoundIndex == maxRoundIndex)
        }
        .padding(.horizontal, 12)
    }
}

// Visual Shaker Card + Domino Tile component
struct RoundHeaderCard: View {
    let spinnerValue: Int
    let shakerName: String
    let shakerColorHex: String
    let shakerAvatarIndex: Int
    
    private let shakerGold = Color(hex: "#FFC107") ?? .yellow
    
    var body: some View {
        HStack(alignment: .center) {
            VStack(alignment: .leading, spacing: 8) {
                HStack(spacing: 4) {
                    Image(systemName: "crown")
                        .foregroundColor(shakerGold)
                        .font(.system(size: 14))
                    
                    Text("Shaker")
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundColor(.secondary)
                }
                
                HStack(spacing: 8) {
                    PlayerAvatar(
                        name: shakerName,
                        colorHex: shakerColorHex,
                        avatarIndex: shakerAvatarIndex,
                        size: .small
                    )
                    
                    Text(shakerName)
                        .font(.system(size: 17, weight: .semibold))
                        .foregroundColor(.primary)
                }
            }
            
            Spacer()
            
            VStack(spacing: 4) {
                DominoTile(
                    topValue: spinnerValue,
                    bottomValue: spinnerValue,
                    tileWidth: 24,
                    isVertical: true
                )
                
                Text("Double-\(spinnerValue)")
                    .font(.system(size: 11))
                    .foregroundColor(.secondary)
            }
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 16)
        .background(Color.appSurface)
        .cornerRadius(12)
        .shadow(color: Color.black.opacity(0.02), radius: 3, x: 0, y: 1)
    }
}
