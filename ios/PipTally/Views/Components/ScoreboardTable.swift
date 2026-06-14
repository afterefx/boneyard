import SwiftUI

struct ScoreboardEntry: Identifiable, Equatable {
    var id: UUID { playerId }
    let playerId: UUID
    let playerName: String
    let colorHex: String
    let avatarIndex: Int
    let totalScore: Int
    var rank: Int
}

struct ScoreboardTable: View {
    let entries: [ScoreboardEntry]
    
    private let leaderTint = Color(hex: "#43A047")?.opacity(0.12) ?? Color.green.opacity(0.12)
    
    var body: some View {
        VStack(spacing: 0) {
            // Header Row
            HStack {
                Text("#")
                    .font(.system(size: 13, weight: .bold))
                    .foregroundColor(.secondary)
                    .frame(width: 24, alignment: .leading)
                
                Spacer()
                    .frame(width: 8)
                
                Text("Player")
                    .font(.system(size: 13, weight: .bold))
                    .foregroundColor(.secondary)
                    .frame(maxWidth: .infinity, alignment: .leading)
                
                Text("Score")
                    .font(.system(size: 13, weight: .bold))
                    .foregroundColor(.secondary)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 10)
            
            Divider()
            
            ForEach(0..<entries.count, id: \.self) { index in
                let entry = entries[index]
                let isLeader = entry.rank == 1
                
                HStack(alignment: .center) {
                    // Rank badge
                    Text("\(entry.rank)")
                        .font(.system(size: 13, weight: isLeader ? .bold : .regular))
                        .foregroundColor(isLeader ? .primary : .secondary)
                        .frame(width: 24, height: 24)
                        .background(isLeader ? Color.accentColor.opacity(0.2) : Color.appGray)
                        .clipShape(RoundedRectangle(cornerRadius: 4))
                    
                    Spacer()
                        .frame(width: 8)
                    
                    PlayerAvatar(
                        name: entry.playerName,
                        colorHex: entry.colorHex,
                        avatarIndex: entry.avatarIndex,
                        size: .small
                    )
                    
                    Spacer()
                        .frame(width: 8)
                    
                    Text(entry.playerName)
                        .font(.system(size: 15, weight: isLeader ? .semibold : .regular))
                        .foregroundColor(.primary)
                        .frame(maxWidth: .infinity, alignment: .leading)
                    
                    Text("\(entry.totalScore)")
                        .font(.system(size: 17, weight: .bold))
                        .foregroundColor(isLeader ? .accentColor : .primary)
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 10)
                .background(isLeader ? leaderTint : Color.clear)
                
                if index < entries.count - 1 {
                    Divider()
                        .padding(.horizontal, 16)
                }
            }
        }
        .background(Color.appSurface)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .shadow(color: Color.black.opacity(0.05), radius: 5, x: 0, y: 2)
    }
}
