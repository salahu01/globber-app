# Privacy Policy

_Last updated: 17 October 2026_

**Globber** is built to protect your privacy. It runs entirely on your device.

## Summary

- **No data leaves your device.** Globber has no network/internet permission.
- **No analytics, no tracking, no ads, no third-party SDKs.**
- **No accounts.** Nothing to sign up for.

## Data Globber accesses

| Data | Why | Where it goes |
|------|-----|---------------|
| **Incoming call numbers** | Matched against your block rules via the system `CallScreeningService` | Processed on-device; a local block log is stored only on your device |
| **Allowed numbers** | Numbers you choose to always let ring | Stored only on your device |
| **Notifications** (`POST_NOTIFICATIONS`) | Tell you when a call was blocked | Stays on-device |

## Data storage

Block rules, allowed numbers and the block log are stored in a local database on your device
only. Uninstalling the app removes this data. Globber does not back this data
up to any server.

## Permissions

Globber requests only:

- `POST_NOTIFICATIONS` — block notifications

It also asks to be your **call screening app** (a system role, not a
permission), which lets Android show it the number of each incoming call.

It does **not** request contacts, internet, location, microphone, camera, or
storage permissions.

## Changes

This policy may be updated alongside releases. Material changes will be noted in
[CHANGELOG.md](CHANGELOG.md).

## Contact

Questions? Email **cvsalahu12@gmail.com** or open an issue.
