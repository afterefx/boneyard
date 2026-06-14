import SwiftUI

public enum AppTheme: Int, CaseIterable, Identifiable {
    case system = 0
    case pureBlack = 1
    case deepGray = 2
    
    public var id: Int { self.rawValue }
    
    public var displayName: String {
        switch self {
        case .system: return "System"
        case .pureBlack: return "OLED Pure Black"
        case .deepGray: return "Slate Deep Gray"
        }
    }
    
    public var description: String {
        switch self {
        case .system: return "Adapts to your phone's light or dark appearance settings automatically."
        case .pureBlack: return "Deep pitch-black dark mode to maximize screen contrast and improve battery life."
        case .deepGray: return "A softer midnight-charcoal dark mode that is gentle on the eyes."
        }
    }
}

extension Color {
    // Shared failable hex color initializer
    public init?(hex: String) {
        var cString: String = hex.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()
        
        if cString.hasPrefix("#") {
            cString.remove(at: cString.startIndex)
        }
        
        if cString.count == 8 {
            var rgbValue: UInt64 = 0
            Scanner(string: cString).scanHexInt64(&rgbValue)
            
            // Format can be AARRGGBB (from Android)
            let a = CGFloat((rgbValue & 0xFF000000) >> 24) / 255.0
            let r = CGFloat((rgbValue & 0x00FF0000) >> 16) / 255.0
            let g = CGFloat((rgbValue & 0x0000FF00) >> 8) / 255.0
            let b = CGFloat(rgbValue & 0x000000FF) / 255.0
            self.init(red: Double(r), green: Double(g), blue: Double(b), opacity: Double(a))
            return
        }
        
        if cString.count != 6 {
            return nil
        }
        
        var rgbValue: UInt64 = 0
        Scanner(string: cString).scanHexInt64(&rgbValue)
        
        self.init(
            red: Double((rgbValue & 0xFF0000) >> 16) / 255.0,
            green: Double((rgbValue & 0x00FF00) >> 8) / 255.0,
            blue: Double(rgbValue & 0x0000FF) / 255.0
        )
    }
    
    public static var activeTheme: AppTheme {
        let raw = UserDefaults.standard.integer(forKey: "appTheme")
        return AppTheme(rawValue: raw) ?? .system
    }

    public static var appBackground: Color {
        switch activeTheme {
        case .system:
            #if canImport(UIKit)
            return Color(.systemGroupedBackground)
            #else
            return Color(hex: "#F8F9FF") ?? Color(white: 0.96)
            #endif
        case .pureBlack:
            return Color.black
        case .deepGray:
            return Color(hex: "#121212") ?? Color(white: 0.07)
        }
    }
    
    public static var appSurface: Color {
        switch activeTheme {
        case .system:
            #if canImport(UIKit)
            return Color(.secondarySystemGroupedBackground)
            #else
            return Color.white
            #endif
        case .pureBlack:
            return Color(hex: "#121212") ?? Color(white: 0.07)
        case .deepGray:
            return Color(hex: "#1E1E1E") ?? Color(white: 0.12)
        }
    }
    
    public static var appRowBackground: Color {
        switch activeTheme {
        case .system:
            #if canImport(UIKit)
            return Color(.systemBackground)
            #else
            return Color.white
            #endif
        case .pureBlack:
            return Color(hex: "#1C1C1E") ?? Color(white: 0.11)
        case .deepGray:
            return Color(hex: "#2C2C2E") ?? Color(white: 0.17)
        }
    }
    
    public static var appGray: Color {
        switch activeTheme {
        case .system:
            #if canImport(UIKit)
            return Color(.systemGray6)
            #else
            return Color.gray.opacity(0.15)
            #endif
        case .pureBlack:
            return Color(hex: "#2C2C2E") ?? Color.gray.opacity(0.18)
        case .deepGray:
            return Color(hex: "#3A3A3C") ?? Color.gray.opacity(0.23)
        }
    }
}

extension View {
    public func appNavigationBarTitleDisplayMode() -> some View {
        #if os(iOS)
        return self.navigationBarTitleDisplayMode(.inline)
        #else
        return self
        #endif
    }
    
    public func applyThemeColorScheme(_ appThemeRawValue: Int? = nil) -> some View {
        let raw = appThemeRawValue ?? UserDefaults.standard.integer(forKey: "appTheme")
        let theme = AppTheme(rawValue: raw) ?? .system
        
        return self.onAppear {
            ThemeManager.applyToAllWindows(theme)
        }
        .onChange(of: appThemeRawValue) { _, newValue in
            let newRaw = newValue ?? UserDefaults.standard.integer(forKey: "appTheme")
            let newTheme = AppTheme(rawValue: newRaw) ?? .system
            ThemeManager.applyToAllWindows(newTheme)
        }
    }
}

#if canImport(UIKit)
import UIKit

/// Applies theme changes at the UIKit window level, bypassing the SwiftUI
/// `preferredColorScheme` feedback loop where `@Environment(\.colorScheme)`
/// reads the already-overridden value and prevents returning to light mode.
public enum ThemeManager {
    public static func applyToAllWindows(_ theme: AppTheme) {
        let style: UIUserInterfaceStyle
        switch theme {
        case .system:
            style = .unspecified  // follows device setting
        case .pureBlack, .deepGray:
            style = .dark
        }
        
        for scene in UIApplication.shared.connectedScenes {
            guard let windowScene = scene as? UIWindowScene else { continue }
            for window in windowScene.windows {
                window.overrideUserInterfaceStyle = style
            }
        }
    }
}
#else
public enum ThemeManager {
    public static func applyToAllWindows(_ theme: AppTheme) {
        // No-op on non-UIKit platforms
    }
}
#endif
