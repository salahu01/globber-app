# Globber — storyboard

Pattern-matching call blocker for Android (India-first, works worldwide). Users who get rotating spam/telemarketing calls. Design system: **Ceramic** — bone #EEEBE6, ink #141414, one signal red #E8392B for spam; light by default, dark variant in Settings. Hero: ceramic glob-star icon. Device: Pixel-style Android frame starting on a sample launcher; the punch-hole camera grows into the alert pill.

## Onboarding (≈8 s)
| # | Scene | What moves | Sound | Haptic |
|---|---|---|---|---|
| 1 | Splash | shield drops in, pulse rings, wordmark, loader, "100% on-device" | silent on first load | — |
| 2 | Onboard 1 · patterns | `+91 140*` pill; 4 numbers slide in, 3 tagged Blocked, 1 Rings | — | light on Next |
| 3 | Onboard 2 · screening role | grey shield → Android role dialog → shield turns lime and hops | allow / deny | success / soft |
| 4 | Onboard 3 · options | contacts / notify / hidden-number toggles | shieldOn chord on Start | success |

## Primary loop
| # | Screen / sheet | User sees | Hero reaction | Sound | Haptic | Alert pill |
|---|---|---|---|---|---|---|
| 5 | Home (tab) | status pill, stage, KPI bento, week chart, recent | idle; grey when not screening | — | — | "Globber is protecting you" |
| 6 | Simulate spam call | full-screen incoming call → "Blocked by Globber" seal | block bounce | ring → block | medium → heavy | "Blocked +91 140…" + View |
| 7 | Rules (tab) | search, filter chips, rule cards with toggle + hits, FAB | — | — | select | paused / active |
| 8 | Rule sheet | pattern, type chips, live tester, action, label | — | success / err | success / error + shake | "Rule added" |
| 9 | Log (tab) | skeleton → day groups, filters, search, clear | — | empty | warning → medium | "Log cleared" + Undo |
| 10 | Call detail sheet | matched rule, allow number, make rule, copy | — | allow / copy | success / light | "Number allowed" + Undo |
| 11 | Settings (tab) | screening card, contacts, hidden, notify, theme, backup | — | allow / deny / success | — | export / import |

## Demo-critical moments (fixed in seed data)
- First "Simulate spam call" is `+91 140 4482 917`, which matches rule `+91 140*` → blocked, logged, Home counters bump.
- Rule sheet test number defaults to `+91 140 4482 917`, so a new `+91 140*` rule shows "Would be rejected".
- Saving an empty pattern, a pattern with under 3 digits, or a bad regex → inline error + shake.
