import SwiftUI

enum AvatarSize {
    case small
    case medium
    case large
    case xlarge
    
    var size: CGFloat {
        switch self {
        case .small: return 32
        case .medium: return 48
        case .large: return 64
        case .xlarge: return 96
        }
    }
    
    var fontSize: CGFloat {
        switch self {
        case .small: return 13
        case .medium: return 18
        case .large: return 24
        case .xlarge: return 36
        }
    }
    
    var borderWidth: CGFloat {
        switch self {
        case .small: return 1.5
        case .medium: return 2
        case .large: return 2
        case .xlarge: return 3
        }
    }
    
    var shadowRadius: CGFloat {
        switch self {
        case .small: return 2
        case .medium: return 3
        case .large: return 4
        case .xlarge: return 6
        }
    }
}

struct PlayerAvatar: View {
    let name: String
    let colorHex: String
    var avatarIndex: Int = -1
    var size: AvatarSize = .medium
    
    private let avatarSymbols = [
        "die.face.6.fill",      // Dice 🎲
        "suit.spade.fill",      // Joker 🃏
        "gamecontroller.fill",  // Chess ♟️
        "trophy.fill",          // Trophy 🏆
        "target",               // Target 🎯
        "star.fill"             // Star ⭐
    ]
    
    var body: some View {
        let parsedColor = Color(hex: colorHex) ?? .blue
        let hasSymbol = avatarIndex >= 0 && avatarIndex < avatarSymbols.count
        let contentColor = isDark(color: parsedColor) ? Color.white : Color(hex: "#1A1A1A") ?? .black
        
        ZStack {
            Circle()
                .fill(parsedColor)
                .frame(width: size.size, height: size.size)
                .overlay(
                    Circle()
                        .stroke(Color.white.opacity(0.35), lineWidth: size.borderWidth)
                )
                .shadow(color: parsedColor.opacity(0.3), radius: size.shadowRadius, x: 0, y: size.shadowRadius / 2)
            
            if hasSymbol {
                Image(systemName: avatarSymbols[avatarIndex])
                    .font(.system(size: size.fontSize * 1.1, weight: .bold))
                    .foregroundColor(contentColor)
            } else {
                Text(String(name.first?.uppercased() ?? "?"))
                    .font(.system(size: size.fontSize, weight: .bold))
                    .foregroundColor(contentColor)
            }
        }
    }
    
    private func isDark(color: Color) -> Bool {
        #if canImport(UIKit)
        let uiColor = UIColor(color)
        var r: CGFloat = 0, g: CGFloat = 0, b: CGFloat = 0, a: CGFloat = 0
        if uiColor.getRed(&r, green: &g, blue: &b, alpha: &a) {
            let luminance = 0.299 * r + 0.587 * g + 0.114 * b
            return luminance < 0.5
        }
        #endif
        return true
    }
}

// Color hex parser extension
extension Color {
    
    func toHex() -> String {
        #if canImport(UIKit)
        let uiColor = UIColor(self)
        var red: CGFloat = 0
        var green: CGFloat = 0
        var blue: CGFloat = 0
        var alpha: CGFloat = 0
        if uiColor.getRed(&red, green: &green, blue: &blue, alpha: &alpha) {
            return String(format: "#%02X%02X%02X",
                          Int(round(red * 255)),
                          Int(round(green * 255)),
                          Int(round(blue * 255)))
        }
        #endif
        return "#1E88E5"
    }
}
