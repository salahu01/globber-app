# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed
- Full redesign in the new ceramic design system (bone, ink, one signal red), light and dark:
  "The filter" splash, a three-scene onboarding story, floating bottom navigation,
  and rebuilt Home, Rules, Blocked calls and Settings screens.
- Rule sheet has a live number tester and match-type / action pickers.
- Blocked-call detail sheet: always allow the number, block numbers like it, copy.

### Added
- Allowed numbers: per-number exceptions that ring regardless of rules.

## [1.0.6] - 2026-10-17

### Changed
- Target and compile against Android 16 (API 36) to meet Google Play's target API requirement.
- Application ID is now `app.globber` for the Google Play release. The source
  package (`com.salah.callblocker`) is unchanged. Not an in-place upgrade from
  earlier sideloaded builds: uninstall the old app first.

## [1.0.5] - 2026-07-24

### Fixed
- Blocking rules entered as a plain national number (e.g. `9605…`) now match
  incoming calls whose caller ID includes the country code (`+91…`). The matcher
  compares both the full E.164 form and a country-code-stripped national form, so
  **Starts with** / **Exact** rules block the number the way you see it locally.
- Add/Save button in the add-rule dialog is no longer pushed behind the keyboard,
  and no longer renders invisible (the filled button lost its background inside the
  dialog window). The dialog now keeps its actions on screen and clearly visible.

### Removed
- "Allow calls from contacts" setting. Android never routes calls from saved
  contacts to a call-screening app, so the toggle could not affect whether a
  contact was blocked and was misleading. The `READ_CONTACTS` permission is gone.

### Changed
- Bumped `versionCode` 5 → 6, `versionName` 1.0.4 → 1.0.5.

## [1.0.4] - 2026-06-29

### Changed
- Add-rule dialog now defaults the match type to **Starts with** instead of
  **Exact**. Typing a prefix like `1900` now blocks `1900190000` out of the box;
  the old default silently matched only the literal number.
- Bumped `versionCode` 4 → 5, `versionName` 1.0.3 → 1.0.4.

## [1.0.3] - 2026-06-29

### Added
- Settings screen: theme switcher, block-unknown-numbers toggle, portrait lock.
- Blocked-calls log: search field to filter screened calls.
- Open-source project hygiene: `CONTRIBUTING`, `CODE_OF_CONDUCT`, `SECURITY`,
  `PRIVACY`, issue/PR templates, CI workflow, `.editorconfig`, `.gitattributes`.

### Changed
- Bumped `versionCode` 3 → 4, `versionName` 1.0.2 → 1.0.3.
- Rebranded **Call Blocker → Globber**.
- Renamed application package `com.fegno.callblocker` → `com.salah.callblocker`.
- Updated contact email across `CODE_OF_CONDUCT`, `PRIVACY`, `SECURITY`.

## [1.0.2] - 2026-06-29

### Added
- Dashboard: pinned icon app bar with a glass (blur) backdrop above the
  call-screening banner; distinct abstract card visuals for **Most Blocked**
  (frequency histogram) and **Active Rules** (stacked rule rows).
- Blocked-calls log: phone-style sticky day headers (Today / Yesterday / date)
  with per-day counts, action filter chips (All / Reject / Silence / Voicemail)
  and Newest / Oldest sort.
- Rich, cohesive pattern-type icon family (Exact / Starts with / Contains /
  Ends with / Regex), surfaced in the rules list, editor and log.
- Empty-state illustrations for the Rules and Blocked-calls screens.
- New adaptive launcher icon: graphite background, lime shield, dark wildcard
  asterisk; refreshed README banner and brand assets to match.

### Changed
- Bumped `versionCode` 2 → 3, `versionName` 1.0.1 → 1.0.2.
- Rule editor moved to a custom dialog with IME-aware insets so the Add / Save
  buttons stay above the soft keyboard.
- Add-rule FAB is hidden on the empty Rules state (the centered call-to-action
  remains).

### Fixed
- Editor action buttons no longer hidden behind the keyboard while typing a
  pattern.

## [1.0.1] - 2026-06-29

### Added
- Release signing config sourced from a gitignored `key.properties`; the
  release APK is now signed (APK Signature Scheme v2).
- README redesign with neon-lime banner and brand assets.

### Changed
- Bumped `versionCode` 1 → 2, `versionName` 1.0 → 1.0.1.

## [1.0.0] - 2026-06-29

### Added
- Pattern-matching call blocker using Android `CallScreeningService`.
- Block rules by number pattern (prefix / suffix / wildcard).
- Optional contacts allow-list.
- Block log of screened calls.
- Dark neon-lime bento UI with a custom icon set.

[Unreleased]: https://github.com/salahu01/globber-app/compare/v1.0.4...HEAD
[1.0.4]: https://github.com/salahu01/globber-app/compare/v1.0.3...v1.0.4
[1.0.3]: https://github.com/salahu01/globber-app/compare/v1.0.2...v1.0.3
[1.0.2]: https://github.com/salahu01/globber-app/compare/v1.0.1...v1.0.2
[1.0.1]: https://github.com/salahu01/globber-app/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/salahu01/globber-app/releases/tag/v1.0.0
