# 🗺️ Roadmap — PipTally

This roadmap outlines planned feature enhancements beyond the v1.0 release. Items are organized by release horizon and prioritized by user impact and feasibility.

> **Source of Truth:** This file mirrors §6 of the [Product Requirements Document](prd.md). Any design changes should be reflected in both documents.

---

## ✅ v1.0 — Shipped

The current release includes all core functionality:

- 4-player, 14-round game with standard spinner sequence (6→0→0→6)
- Player management (create, edit, delete) with avatar icons and color palette
- Game setup wizard (player selection → seating order → first shaker)
- Active gameplay with round-by-round score entry, round winner marking, and validation
- Historical round editing (navigate to any past round and correct scores)
- Live standings panel (collapsible, ranked by lowest score)
- Game summary with winner hero, confetti, and full round-by-round score grid
- Game lifecycle: active → paused → resumed → completed / abandoned
- Game history with swipe-to-delete
- Player profiles with lifetime stats (games played, won, win rate, avg score, best score)
- Three app themes (System, OLED Pure Black, Slate Deep Gray) with reactive switching
- Full data reset from Settings
- Offline-first, zero network dependency

---

## 🔜 v1.1–v1.2 — Near-Term

| Feature | Description | Priority |
|---|---|---|
| Expanded avatar icons | Add 10–15 additional icons for player personalization (e.g., animals, food, sports) | 🔴 High |
| Custom player colors | Allow users to pick any color via a color wheel, beyond the preset 8-color palette | 🟡 Medium |
| Game templates / quick start | Save a group of 4 players as a "table" for one-tap game creation | 🔴 High |
| Hidden score mode | Game creator option to hide the running standings/scoreboard until the final round is completed, adding suspense | 🔴 High |
| Haptic feedback | Tactile feedback on trophy toggle, round submit, game completion | 🟡 Medium |
| Sounds / audio cues | Optional domino shuffle sound on game start, celebration on win | 🟢 Low |

---

## 🚀 v1.3–v2.0 — Mid-Term

| Feature | Description | Priority |
|---|---|---|
| 7-round game mode | Option to play a shorter 7-round game with spinner sequence 6 → 5 → 4 → 3 → 2 → 1 → 0. Player count remains fixed at 4 (28 tiles ÷ 7 per player). | 🟡 Medium |
| Cloud sync & backup | Sync and backup game data to a first-party service provided by the app developer. **Paid subscription feature** — no iCloud, Google Drive, or third-party export. Users cannot export their data. | 🔴 High |
| Multiplayer score entry | Each player creates an account (username + email) on their own device. The game host adds friends by searching their username or email. During gameplay, the host manages round progression, but each player enters their own individual score from their own device. Upon game completion, the full game summary and stats are distributed to all participants, and the game is attached to each player's account history. **Paid subscription feature.** | 🟡 Medium |
| Player photos | Allow camera/gallery photos as avatar images in addition to icons. **Under consideration** — hosting/storage costs for user-uploaded images need to be evaluated. | 🟢 Low |

---

## 🔭 v2.0+ — Long-Term

| Feature | Description | Priority |
|---|---|---|
| Cross-platform sync | Sync data between iOS and Android via the first-party cloud service (extends the paid sync tier) | 🔴 High |
| Leaderboard & rankings | Group-based leaderboards with ELO-style rankings among players on the same sync account | 🟡 Medium |
| Game analysis & insights | Charts showing score trends, round-by-round performance, hot streaks | 🟡 Medium |
| Widget support | Home screen widgets showing active game status or player stats | 🟢 Low |

---

## 💰 Monetization Strategy

The app follows a **freemium model** with a paid subscription tier:

| Tier | Access |
|---|---|
| **Free** | Full local gameplay — all v1.x features including game setup, scoring, history, themes, and player profiles |
| **Paid Subscription** | Cloud sync & backup, multiplayer score entry, cross-platform sync, leaderboards |

> [!IMPORTANT]
> The paid tier syncs to a **first-party service** operated by the app developer. There is no integration with iCloud, Google Drive, or any third-party platform. Users cannot export their data.
