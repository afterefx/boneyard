# Boneyard — Domino Scorer

A 4-player domino score-tracking app for Mexican Train / Double-Six games. Tracks shaker rotation, spinner rounds, and cumulative scores across all 14 rounds so you can focus on the game.

Available on Android (this repo) and [iOS](https://github.com/afterefx/boneyard-ios).

## Screenshot

<img src="android/docs/screenshots/home_screen.png" width="320" alt="Home screen" />

## Features

- **14-round games** with automatic spinner sequence `[6, 5, 4, 3, 2, 1, 0, 0, 1, 2, 3, 4, 5, 6]`
- **Shaker rotation** tracked automatically each round
- **Live scoreboard** with cumulative totals and current leader highlighted
- **Pause & resume** — games survive app restarts
- **Game history** with full round-by-round breakdown
- **Quick Start templates** — save a table of 4 players and start a game in one tap
- **Themes** — System, OLED Pure Black, Slate Deep Gray, Always Light

## Tech stack

- Kotlin + Jetpack Compose + Material 3
- Circuit (Slack) for navigation and UI state
- Metro for dependency injection
- Room for local persistence
- Clean Architecture (domain / data / UI layers)

## Building

```bash
cd android
./gradlew assembleDebug        # debug APK
./gradlew installDebug         # install on connected device
./gradlew test                 # unit tests
```

## Releasing

Push a version tag to trigger the CI pipeline to Google Play Internal Testing:

```bash
git tag v1.0.0 && git push --tags
```

Required GitHub secrets: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`, `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON`.
