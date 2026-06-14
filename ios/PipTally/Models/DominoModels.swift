import Foundation
import SwiftData

@Model
final class Player {
    @Attribute(.unique) var id: UUID
    var name: String
    var colorHex: String
    var avatarIndex: Int
    var createdAt: Date
    
    init(name: String, colorHex: String, avatarIndex: Int) {
        self.id = UUID()
        self.name = name
        self.colorHex = colorHex
        self.avatarIndex = avatarIndex
        self.createdAt = Date()
    }
}

enum GameStatus: Int, Codable {
    case active = 0
    case paused = 1
    case completed = 2
    case abandoned = 3
}

@Model
final class Game {
    @Attribute(.unique) var id: UUID
    var statusRaw: Int
    var currentRoundIndex: Int
    var createdAt: Date
    var completedAt: Date?
    var winnerPlayerId: UUID?
    
    var status: GameStatus {
        get { GameStatus(rawValue: statusRaw) ?? .active }
        set { statusRaw = newValue.rawValue }
    }
    
    @Relationship(deleteRule: .cascade)
    var gamePlayers: [GamePlayer]
    
    @Relationship(deleteRule: .cascade)
    var rounds: [Round]
    
    init(status: GameStatus = .active) {
        self.id = UUID()
        self.statusRaw = status.rawValue
        self.currentRoundIndex = 0
        self.createdAt = Date()
        self.gamePlayers = []
        self.rounds = []
    }
}

@Model
final class GamePlayer {
    @Attribute(.unique) var id: UUID
    var seatPosition: Int
    var totalScore: Int
    
    var player: Player?
    var game: Game?
    
    init(player: Player, seatPosition: Int, totalScore: Int = 0) {
        self.id = UUID()
        self.player = player
        self.seatPosition = seatPosition
        self.totalScore = totalScore
    }
}

@Model
final class Round {
    @Attribute(.unique) var id: UUID
    var roundIndex: Int
    var spinnerValue: Int
    var shakerPlayerId: UUID
    var completedAt: Date?
    
    var game: Game?
    
    @Relationship(deleteRule: .cascade)
    var scores: [RoundScore]
    
    init(roundIndex: Int, spinnerValue: Int, shakerPlayerId: UUID) {
        self.id = UUID()
        self.roundIndex = roundIndex
        self.spinnerValue = spinnerValue
        self.shakerPlayerId = shakerPlayerId
        self.completedAt = Date()
        self.scores = []
    }
}

@Model
final class RoundScore {
    @Attribute(.unique) var id: UUID
    var playerId: UUID
    var score: Int
    var isWinner: Bool
    
    var round: Round?
    
    init(playerId: UUID, score: Int, isWinner: Bool = false) {
        self.id = UUID()
        self.playerId = playerId
        self.score = score
        self.isWinner = isWinner
    }
}
