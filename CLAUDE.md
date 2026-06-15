# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Mandatory rules

These three rules apply to every task in this repo, without exception:

1. **Build verification** — After any code change, run the build/compile check for the affected platform before reporting the task done. Android: `./gradlew assembleDebug` from `android/`. iOS: the `swiftc` syntax check from `ios/CONTRIBUTING.md`. Both must pass cleanly.

2. **Documentation sync** — If you change architecture, add/remove dependencies, rename commands, or alter game rules, update the relevant CLAUDE.md files (`CLAUDE.md` at root and/or `android/CLAUDE.md`) in the same task. Never leave docs stale.

3. **Platform parity** — iOS and Android implement the same product. If you add a feature or fix a bug on one platform, you must apply the equivalent change to the other in the same task — or explicitly call out the gap in your response with a clear "Parity gap: …" note so it can be tracked.

4. **Deploy** — After building, deploy and launch on both platforms before reporting the task done. Android: `./gradlew installDebug` (requires a connected device or running emulator). iOS: install and launch on the booted simulator — `xcrun simctl install booted <path/to/Boneyard.app>` then `xcrun simctl launch booted app.boneyard`.

## Store release pipeline

Releases are published via Fastlane + GitHub Actions. Pushing a `v*` tag (e.g. `git tag v1.0.1 && git push --tags`) triggers both workflows automatically.

| Workflow | File | Destination |
|---|---|---|
| Android Release | `.github/workflows/android-release.yml` | Google Play Internal Testing |
| iOS Release | `.github/workflows/ios-release.yml` | TestFlight (internal) |

Both workflows can also be triggered manually from the GitHub Actions UI via `workflow_dispatch`.

### Required GitHub repository secrets

**Android:**
| Secret | How to get it |
|---|---|
| `KEYSTORE_BASE64` | `base64 -i your-key.jks` |
| `KEYSTORE_PASSWORD` | Password chosen when generating the keystore |
| `KEY_ALIAS` | Alias chosen when generating the keystore |
| `KEY_PASSWORD` | Key password (often same as store password) |
| `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` | Full JSON content of the service account key from Play Console → Setup → API access |

**iOS:**
| Secret | How to get it |
|---|---|
| `DISTRIBUTION_CERTIFICATE_P12_BASE64` | Export from Keychain Access → `base64 -i cert.p12` |
| `DISTRIBUTION_CERTIFICATE_PASSWORD` | Password set when exporting the .p12 |
| `PROVISIONING_PROFILE_BASE64` | Download from developer.apple.com → `base64 -i profile.mobileprovision` |
| `PROVISIONING_PROFILE_NAME` | The exact profile name shown in Xcode / developer portal |
| `ASC_KEY_ID` | App Store Connect → Users & Access → Integrations → Keys |
| `ASC_ISSUER_ID` | Same page as the key |
| `ASC_PRIVATE_KEY` | Contents of the downloaded `.p8` file |

---

## Repository overview

Boneyard is a 4-player domino score-tracking app available on Android and iOS. It is a monorepo with one directory per platform:

- `android/` — Kotlin/Compose app; has its own detailed `android/CLAUDE.md`
- `ios/` — Swift/SwiftUI app

The Android app has its own deeper CLAUDE.md at `android/CLAUDE.md` — read it when working in that directory.

## Android commands

Run all commands from the `android/` directory:

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew test                   # Run all unit tests
./gradlew test --tests "app.boneyard.SomeTest"  # Run a single test class
./gradlew installDebug           # Install on connected device
./gradlew lint                   # Lint check
```

CI runs `./gradlew assembleDebug` on every push/PR via `.github/workflows/android.yml`.

## iOS commands

Run all commands from the `ios/` directory:

```bash
# Syntax check against macOS SDK (fast, no Xcode project needed)
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

# Generate/update the Xcode project (required after adding/removing source files)
python3 generate_project.py

# Build for simulator
xcodebuild -project Boneyard.xcodeproj -scheme Boneyard \
  -destination "platform=iOS Simulator,name=iPhone 17 Pro" \
  -configuration Debug build

# Deploy and launch on booted simulator
open -a Simulator
xcrun simctl install booted .build/simulator/Build/Products/Debug-iphonesimulator/Boneyard.app
xcrun simctl launch booted app.boneyard

# Run logic unit tests (no Xcode needed)
cd BoneyardTests && swiftc -o run_tests main.swift GameLogicTests.swift ValidationAndEditingTests.swift \
  ../Boneyard/Utils/GameConstants.swift -sdk $(xcrun --show-sdk-path -sdk macosx) && ./run_tests
```

**iOS project file rule:** Never edit `Boneyard.xcodeproj/project.pbxproj` manually — it is regenerated by `generate_project.py`. When adding or deleting source files, update the `swift_files` array in `generate_project.py` first, then re-run the script.

## iOS architecture

MVVM backed by SwiftData (no separate repository layer):

- `Models/DominoModels.swift` — SwiftData `@Model` entities: `Player`, `Game`, `GamePlayer`, `Round`, `RoundScore`
- `Views/` — SwiftUI views; each feature is a single file; state is managed via `@State`, `@Query`, and `@Binding`
- `Utils/GameConstants.swift` — spinner sequence, shaker rotation math
- `Utils/ThemeColors.swift` — semantic color tokens (`Color.appBackground`, `Color.appSurface`, `Color.appRowBackground`, `Color.appGray`) that adapt per theme — always use these instead of platform UIKit colors
- `Views/Components/` — `PlayerAvatar`, `DominoTile`, `ScoreboardTable`, `ScoreEntryRow`

iOS platform compatibility rules (see `ios/CONTRIBUTING.md` for details):
- Wrap iOS-only modifiers in `#if os(iOS)` blocks so code compiles on macOS SDK
- Use cross-platform toolbar placements (`.primaryAction`, `.confirmationAction`) not iOS-only ones
- Use `SF Symbols` (`Image(systemName:)`) rather than Unicode emoji

## Shared domain / game rules

Both platforms implement the same rules (source of truth: `prd.md`):

| Rule | Value |
|---|---|
| Players per game | Exactly 4 |
| Rounds | 14 |
| Spinner sequence | `[6, 5, 4, 3, 2, 1, 0, 0, 1, 2, 3, 4, 5, 6]` |
| Shaker rotation | `roundIndex % playerCount` |
| Winner | Lowest cumulative score |
| Score entry | Non-negative integer, max 3 digits (0–999) |

Cascade delete: deleting a `Game` cascades to `GamePlayer`, `Round`, and `RoundScore`. Deleting a `Player` leaves historical game data intact.

## Color format

Player colors are stored as hex strings. Both platforms support 6-digit (`#RRGGBB`) and 8-digit (`#AARRGGBB`) formats.
