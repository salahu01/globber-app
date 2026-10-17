# Globber prototype · references & credits

## Visual references (patterns only, never copied) — browsed Dribbble 17 Oct 2026
| Ref | URL | Look | Pattern to borrow |
|---|---|---|---|
| Savi Security — Asib Iquebal | https://dribbble.com/shots/27606022 | Warm cream light, black ink, pastel quick-action tiles | "You're fully protected" status card; Safe / Scam / Risky pills on call rows |
| AI Scam Protection — Asib Iquebal | https://dribbble.com/shots/27635519 | Same family, soft pastels | Call and message rows with verdict pills |
| Data Protection — Prelab | https://dribbble.com/shots/23202108 | Dark navy/plum, violet glow | Hero "Device under protection" card with glowing lock, spammer count |
| Spam Call & SMS Blocker — Prelab | https://dribbble.com/shots/21389597 | White + blue, gauge | "Spammers blocked" gauge tile, 2-col feature bento |
| ControlPlus — Lumi studio | https://dribbble.com/shots/25688120 | Light grey, amber gauge | Protection % arc, 3-stat strip, recommended-action card |
| Caller ID — shahzaib jugwal | https://dribbble.com/shots/27350241 | White, violet→orange gradient CTA | Onboarding "Know callers before you answer"; recents list |
## Assets
- Icons: Solar by 480 Design (CC BY 4.0), @iconify-json/solar@1.2.13 — 59 icons × 2 styles.
- Photos: none (no people in this product).
- Fonts: Plus Jakarta Sans, Geist Mono (Google Fonts, OFL).
- Hero art: hand-built inline SVG shield with a glob star (`shieldSVG`).

## Invented sample data — check before a pitch
- "Today" pinned to Sat 17 Oct 2026, 9:41.
- 7 rules (`+91 140*` telemarketing, `1900*`, `*0000`, `+1 800 555*`, `7788`, regex `^\+44` paused, exact `+91 98470 12345`) and their hit counts.
- 14 blocked calls over 7 days; simulated calls `+91 140 4482 917`, `1900 410 2266`, `+91 44 2300 0000`.
- Import file `globber-rules-2026-09-02.json` with 3 rules (1 duplicate).
