# Boneyard — Domino Scorer

A 4-player domino score-tracking app for Off-the-Spinner / Double-Six games. Tracks shaker rotation, spinner rounds, and cumulative scores across all 14 rounds so you can focus on the game.

Available on [iOS](https://github.com/afterefx/boneyard-ios) and [Android](https://github.com/afterefx/boneyard-android).

## Website

The project website (landing page, game rules, privacy policy) is hosted on GitHub Pages and lives in the `site/` directory.

## Features

- **14-round games** with automatic spinner sequence `[6, 5, 4, 3, 2, 1, 0, 0, 1, 2, 3, 4, 5, 6]`
- **Shaker rotation** tracked automatically each round
- **Live scoreboard** with cumulative totals and current leader highlighted
- **Pause & resume** — games survive app restarts
- **Game history** with full round-by-round breakdown
- **Quick Start templates** — save a table of 4 players and start a game in one tap
- **Themes** — System, OLED Pure Black, Slate Deep Gray, Always Light

## iOS — Building & Running

```bash
cd ios

# Syntax check (fast, no Xcode needed)
swiftc -o /dev/null -sdk $(xcrun --show-sdk-path -sdk macosx) \
  Boneyard/BoneyardApp.swift Boneyard/Utils/GameConstants.swift \
  Boneyard/Utils/ThemeColors.swift Boneyard/Models/DominoModels.swift \
  Boneyard/Views/Components/PlayerAvatar.swift Boneyard/Views/Components/DominoTile.swift \
  Boneyard/Views/Components/ScoreboardTable.swift Boneyard/Views/Components/ScoreEntryRow.swift \
  Boneyard/Views/HomeView.swift Boneyard/Views/PlayerListView.swift \
  Boneyard/Views/PlayerEditView.swift Boneyard/Views/PlayerProfileView.swift \
  Boneyard/Views/GameSetupView.swift Boneyard/Views/ActiveGameView.swift \
  Boneyard/Views/GameSummaryView.swift Boneyard/Views/GameHistoryView.swift \
  Boneyard/Views/SettingsView.swift Boneyard/Views/SplashView.swift

# Build for simulator
xcodebuild -project Boneyard.xcodeproj -scheme Boneyard \
  -destination "platform=iOS Simulator,name=iPhone 17 Pro" \
  -configuration Debug build

# Install and launch on booted simulator
xcrun simctl install booted .build/simulator/Build/Products/Debug-iphonesimulator/Boneyard.app
xcrun simctl launch booted app.boneyard
```

## iOS — Releasing

Push a version tag to trigger the CI pipeline to TestFlight:

```bash
git tag v1.0.0 && git push --tags
```

Required GitHub secrets: `DISTRIBUTION_CERTIFICATE_P12_BASE64`, `DISTRIBUTION_CERTIFICATE_PASSWORD`, `PROVISIONING_PROFILE_BASE64`, `PROVISIONING_PROFILE_NAME`, `ASC_KEY_ID`, `ASC_ISSUER_ID`, `ASC_PRIVATE_KEY`.
