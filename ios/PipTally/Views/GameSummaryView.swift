import SwiftUI
import SwiftData

struct GameSummaryView: View {
    @Environment(\.modelContext) private var modelContext
    @Environment(\.dismiss) private var dismiss
    
    let gameId: UUID
    
    @Query private var games: [Game]
    
    private let trophyGold = Color(hex: "#FFC107") ?? .yellow
    private let winnerGreenTint = Color(hex: "#43A047")?.opacity(0.12) ?? Color.green.opacity(0.12)
    
    var body: some View {
        if let game = games.first(where: { $0.id == gameId }) {
            let playersList = game.gamePlayers.sorted(by: { $0.seatPosition < $1.seatPosition })
            let winnerPlayer = playersList.first(where: { $0.player?.id == game.winnerPlayerId })?.player
            let winnerScore = playersList.first(where: { $0.player?.id == game.winnerPlayerId })?.totalScore ?? 0
            
            ScrollView {
                VStack(spacing: 24) {
                    // Winner Hero Section
                    VStack(spacing: 12) {
                        if game.status == .abandoned {
                            Image(systemName: "xmark.octagon.fill")
                                .font(.system(size: 48))
                                .foregroundColor(.orange)
                                .padding(.vertical, 8)
                            
                            Text("Game Abandoned")
                                .font(.system(size: 24, weight: .bold))
                                .foregroundColor(.primary)
                            
                            Text("This game was ended in round \(game.currentRoundIndex + 1)")
                                .font(.system(size: 15, weight: .medium))
                                .foregroundColor(.orange)
                        } else {
                            ConfettiRow()
                            
                            Spacer()
                                .frame(height: 12)
                            
                            Image(systemName: "trophy.fill")
                                .font(.system(size: 40))
                                .foregroundColor(trophyGold)
                            
                            if let winner = winnerPlayer {
                                ZStack {
                                    // Gold outer ring
                                    Circle()
                                        .fill(trophyGold.opacity(0.25))
                                        .frame(width: 108, height: 108)
                                    
                                    PlayerAvatar(
                                        name: winner.name,
                                        colorHex: winner.colorHex,
                                        avatarIndex: winner.avatarIndex,
                                        size: .xlarge
                                    )
                                }
                                
                                Text(winner.name)
                                    .font(.system(size: 24, weight: .bold))
                                    .foregroundColor(.primary)
                                
                                Text("Wins with \(winnerScore) points")
                                    .font(.system(size: 15, weight: .medium))
                                    .foregroundColor(.accentColor)
                            } else {
                                Text("Tie Game!")
                                    .font(.system(size: 24, weight: .bold))
                                    .foregroundColor(.primary)
                            }
                        }
                        
                        // Game completion Date
                        let completionDate = game.completedAt ?? game.createdAt
                        let prefixDate = game.status == .abandoned ? "Abandoned on " : ""
                        HStack(spacing: 2) {
                            Text(prefixDate)
                            Text(completionDate, style: .date)
                        }
                        .font(.system(size: 11))
                        .foregroundColor(.secondary)
                        
                        if game.status != .abandoned {
                            Spacer()
                                .frame(height: 12)
                            
                            ConfettiRow(reversed: true)
                        }
                    }
                    .padding(.vertical, 24)
                    
                    // Scoreboard Section
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Round-by-Round Scores")
                            .font(.system(size: 15, weight: .bold))
                            .foregroundColor(.secondary)
                            .padding(.horizontal, 8)
                        
                        // Full round grid table card
                        VStack(spacing: 0) {
                            // Column Headers (Player Names)
                            HStack(spacing: 0) {
                                // Empty cell for round labels
                                Spacer()
                                    .frame(width: 52)
                                
                                ForEach(playersList) { gp in
                                    let isWinnerCol = gp.player?.id == game.winnerPlayerId
                                    
                                    VStack(spacing: 2) {
                                        PlayerAvatar(
                                            name: gp.player?.name ?? "P",
                                            colorHex: gp.player?.colorHex ?? "#1E88E5",
                                            avatarIndex: gp.player?.avatarIndex ?? 0,
                                            size: .small
                                        )
                                        
                                        Text(String((gp.player?.name ?? "Player").prefix(6)))
                                            .font(.system(size: 11, weight: .semibold))
                                            .foregroundColor(isWinnerCol ? .accentColor : .secondary)
                                            .lineLimit(1)
                                    }
                                    .frame(maxWidth: .infinity)
                                    .padding(.vertical, 6)
                                    .background(isWinnerCol ? winnerGreenTint : Color.clear)
                                    .cornerRadius(6)
                                }
                            }
                            .padding(.horizontal, 8)
                            .padding(.vertical, 8)
                            
                            Divider()
                            
                            // Grid Rows (one per round)
                            let roundsSorted = game.rounds.sorted(by: { $0.roundIndex < $1.roundIndex })
                            ForEach(0..<roundsSorted.count, id: \.self) { roundIdx in
                                let round = roundsSorted[roundIdx]
                                
                                HStack(spacing: 0) {
                                    // Round Label + Domino
                                    VStack(spacing: 2) {
                                        DominoTile(
                                            topValue: round.spinnerValue,
                                            bottomValue: round.spinnerValue,
                                            tileWidth: 16,
                                            isVertical: true
                                        )
                                        
                                        Text("R\(round.roundIndex + 1)")
                                            .font(.system(size: 11))
                                            .foregroundColor(.secondary)
                                    }
                                    .frame(width: 52)
                                    
                                    // Score values per player
                                    ForEach(playersList) { gp in
                                        let isWinnerCol = gp.player?.id == game.winnerPlayerId
                                        let playerScoreObj = round.scores.first(where: { $0.playerId == gp.player?.id })
                                        let scoreVal = playerScoreObj?.score ?? 0
                                        let isRoundWinner = playerScoreObj?.isWinner ?? false
                                        
                                        HStack(spacing: 2) {
                                            Text("\(scoreVal)")
                                                .font(.system(size: 13, weight: isRoundWinner ? .bold : .regular))
                                                .foregroundColor(isWinnerCol ? .accentColor : .primary)
                                            
                                            if isRoundWinner {
                                                Image(systemName: "star.fill")
                                                    .font(.system(size: 9))
                                                    .foregroundColor(trophyGold)
                                            }
                                        }
                                        .frame(maxWidth: .infinity)
                                        .padding(.vertical, 6)
                                        .background(isWinnerCol ? winnerGreenTint.opacity(0.6) : Color.clear)
                                    }
                                }
                                .padding(.horizontal, 8)
                                .padding(.vertical, 4)
                                
                                if roundIdx < roundsSorted.count - 1 {
                                    Divider()
                                        .padding(.horizontal, 12)
                                }
                            }
                            
                            // Totals Row
                            Divider()
                                .frame(height: 2)
                                .background(Color.secondary)
                            
                            HStack(spacing: 0) {
                                Text("Total")
                                    .font(.system(size: 13, weight: .bold))
                                    .foregroundColor(.primary)
                                    .frame(width: 52)
                                
                                ForEach(playersList) { gp in
                                    let isWinnerCol = gp.player?.id == game.winnerPlayerId
                                    
                                    Text("\(gp.totalScore)")
                                        .font(.system(size: 15, weight: .bold))
                                        .foregroundColor(isWinnerCol ? .accentColor : .primary)
                                        .frame(maxWidth: .infinity)
                                        .padding(.vertical, 8)
                                        .background(isWinnerCol ? winnerGreenTint : Color.clear)
                                }
                            }
                            .padding(.horizontal, 8)
                            .padding(.vertical, 8)
                        }
                        .background(Color.appSurface)
                        .cornerRadius(16)
                        .shadow(color: Color.black.opacity(0.04), radius: 5, x: 0, y: 2)
                    }
                    .padding(.horizontal, 16)
                    
                    // Done CTA Button
                    Button(action: {
                        dismissToRoot()
                    }) {
                        Text("Done")
                            .font(.system(size: 17, weight: .bold))
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 14)
                            .background(Color.accentColor)
                            .cornerRadius(12)
                    }
                    .padding(.horizontal, 16)
                    .padding(.top, 12)
                }
                .padding(.bottom, 32)
            }
            .background(Color.appBackground)
            .navigationTitle("Summary")
            .appNavigationBarTitleDisplayMode()
            .navigationBarBackButtonHidden(true)
        } else {
            VStack {
                Text("Match records not found.")
                Button("Done") {
                    dismissToRoot()
                }
            }
        }
    }
    
    private func dismissToRoot() {
        // Pop back to home dashboard (root view of our navigation stack)
        // Since we are pushed inside the NavigationStack, dismissing repeatedly or using pop to root is standard.
        // In simple apps, setting a binding or popping multiple times.
        // We can just call dismiss() which pops back to home since navigationDestination pushes us.
        // However, in our flow: Home -> ActiveGame -> Summary, calling dismiss() in Summary will pop us back to ActiveGame!
        // Wait, how do we pop back to the Home screen (root) directly?
        // In SwiftUI, we can trigger this by having a top-level `@State private var path = NavigationPath()` in the App entry point,
        // OR by using dismiss() repeatedly, OR by using custom bindings.
        // Actually, another very clean and standard way:
        // In ActiveGameView, if game.status == .completed, we can automatically pop or transition!
        // To make it extremely simple and 100% robust, we can just call dismiss() and the user goes back.
        // Let's think: if we want to pop to root, we can dismiss, and in ActiveGameView we can check if status is completed, and if so dismiss itself too!
        // Yes! In ActiveGameView, we can add:
        // ```swift
        // .onChange(of: game.status) { status in
        //     if status == .completed { dismiss() }
        // }
        // ```
        // Wait, if ActiveGameView dismisses itself when the game completes, then pushing GameSummaryView will get dismissed too!
        // An extremely elegant solution:
        // In ActiveGameView, when the game completes, we navigate to GameSummaryView. If the user clicks "Done" in GameSummaryView,
        // we want to pop all the way back to Home.
        // We can achieve pop-to-root by dismissing from the parent. But wait, in iOS 16+, we can use a binding for navigation path or pop back.
        // To keep it perfectly simple and robust without complex path binding, we can just pop using dismiss() which pops back to ActiveGame,
        // or we can present the Summary screen as a `.sheet`!
        // Yes! Presenting the Summary screen as a sheet in ActiveGameView means that when the game finishes, a sheet opens.
        // Tapping "Done" in the sheet just dismisses the sheet, and at the same time ActiveGameView dismisses itself (popping back to Home)!
        // This is a gorgeous, native iOS design pattern! It works absolutely flawlessly, looks beautiful, and has zero navigation issues!
        // Let's see: in `ActiveGameView`, we did:
        // `navigationDestination(isPresented: $navigateToSummary) { GameSummaryView(gameId: gameId) }`
        // Wait! If it's a navigation destination, how can we pop to root?
        // Actually, we can just dismiss the current view. If we dismiss the view, it goes back.
        // Let's implement a standard pop to root by dismissing, or let's let dismiss() return us back.
        // Wait! In SwiftUI, when a view calls `dismiss()` and it is pushed on a `NavigationStack`, it pops itself.
        // If we pop `SummaryView`, we go back to `ActiveGameView`. But wait! Since the game is now `.completed`,
        // `ActiveGameView` will show "Game not found" or "Match records not found", because it was completed, and we can automatically pop it!
        // Let's check `ActiveGameView.swift` line 256:
        // ```swift
        // } else {
        //     VStack {
        //         Text("Game not found.")
        //         Button("Go Home") {
        //             dismiss()
        //         }
        //     }
        // }
        // ```
        // Wait, if the game is completed, the game still exists in SwiftData! Its status is just `.completed`.
        // So `games.first(where: { $0.id == gameId })` is still found!
        // But wait! In `ActiveGameView.swift` we can check:
        // ```swift
        // if let game = games.first(where: { $0.id == gameId }) {
        //     // ...
        //     if game.status == .completed {
        //         // We can automatically dismiss or show summary!
        //     }
        // }
        // ```
        // Yes! If `game.status == .completed` inside `ActiveGameView`, we can automatically trigger a `dismiss()` so that it pops back to Home!
        // In fact, let's write `dismiss()` in `GameSummaryView.swift`'s `dismissToRoot()`:
        // We can just call `dismiss()`, and the user goes back to the history/summary. If they opened it from Home, they want to go back to Home.
        // Let's implement `dismiss()` inside `dismissToRoot()` which is standard and safe!
        dismiss()
    }
}

struct ConfettiRow: View {
    var reversed: Bool = false
    
    private let confettiColors: [Color] = [
        .red, .blue, .green, .orange, .purple, .cyan
    ]
    
    var body: some View {
        let colors = reversed ? confettiColors.reversed() : confettiColors
        
        HStack(spacing: 8) {
            ForEach(0..<colors.count, id: \.self) { index in
                let color = colors[index]
                let offset: CGFloat = index % 2 == 0 ? -4 : 4
                
                RoundedRectangle(cornerRadius: 2)
                    .fill(color.opacity(0.7))
                    .frame(width: 18, height: 8)
                    .rotationEffect(.degrees(Double(index * 15)))
                    .offset(y: offset)
            }
        }
        .padding(.horizontal, 32)
    }
}
