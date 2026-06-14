import SwiftUI

struct ScoreEntryRow: View {
    let playerName: String
    let colorHex: String
    let avatarIndex: Int
    @Binding var scoreText: String
    @Binding var isWinner: Bool
    var showError: Bool
    var isReadOnly: Bool = false
    var onWinnerToggle: () -> Void
    
    private let trophyGold = Color(hex: "#FFC107") ?? .yellow
    
    var body: some View {
        HStack(alignment: .center) {
            PlayerAvatar(
                name: playerName,
                colorHex: colorHex,
                avatarIndex: avatarIndex,
                size: .small
            )
            
            Spacer()
                .frame(width: 12)
            
            Text(playerName)
                .font(.system(size: 17))
                .foregroundColor(isReadOnly ? .secondary.opacity(0.5) : .primary)
                .frame(maxWidth: .infinity, alignment: .leading)
            
            Spacer()
                .frame(width: 8)
            
            // Score entry field (disabled if read-only or round winner)
            VStack(alignment: .trailing, spacing: 2) {
                TextField("0", text: $scoreText)
                    #if os(iOS)
                    .keyboardType(.numberPad)
                    #endif
                    .font(.system(size: 17, weight: .bold))
                    .multilineTextAlignment(.center)
                    .disabled(isReadOnly || isWinner)
                    .frame(width: 88, height: 40)
                    .overlay(
                        RoundedRectangle(cornerRadius: 8)
                            .stroke(showError ? Color.red : Color.gray.opacity(0.5), lineWidth: 1)
                    )
                    .onChange(of: scoreText) { oldValue, newValue in
                        // Filter to numbers only and up to 3 digits
                        let filtered = newValue.filter { $0.isNumber }
                        if filtered.count > 3 {
                            scoreText = String(filtered.prefix(3))
                        } else if scoreText != filtered {
                            scoreText = filtered
                        }
                    }
                
                // Keep space reserved for supporting error message to avoid layout jumps
                Text(showError ? "Required" : " ")
                    .font(.system(size: 11))
                    .foregroundColor(.red)
                    .padding(.trailing, 4)
            }
            
            Spacer()
                .frame(width: 4)
            
            // Winner trophy toggle button
            Button(action: onWinnerToggle) {
                Image(systemName: isWinner ? "trophy.fill" : "trophy")
                    .font(.system(size: 22))
                    .foregroundColor(isWinner ? trophyGold : .secondary)
                    .frame(width: 48, height: 48)
            }
            .disabled(isReadOnly)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 6)
    }
}
