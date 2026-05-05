# Manual Play Validation Release Gate

This document is the release gate checklist for validating real Android Play in-app update behavior before declaring the `@rnforge/react-native-in-app-updates` package release-ready.

> **Important:** Automated unit tests and TypeScript type checking do **not** prove that Play Core returns real update availability on a Play-distributed device. This checklist must be completed manually on a physical Android device with a Google Play-distributed build.

---

## 1. Purpose

The v1 API has been implemented with full mocked test coverage, but Play in-app updates depend on:

- Google Play Services on the device
- The app being installed from Google Play (not sideloaded)
- A higher `versionCode` available on Play than the one currently installed
- Proper signing certificate matching between the installed build and the Play track build

This checklist ensures that real Play behavior matches the typed API contract before release.

---

## 2. Prerequisites

Before starting validation, confirm all of the following are available:

- [ ] **Play Console app access** — account with app management rights
- [ ] **Internal testing track** — app uploaded to internal testing (not production)
- [ ] **Package name match** — the validated app uses the same `packageName` as the Play Console entry
- [ ] **Signed release/internal build** — the APK/AAB is signed with the same keystore/certificate used on Play
- [ ] **Physical Android device** — with Google Play Store and Play Services enabled
- [ ] **Tester account enrolled** — the device Google account is added as an internal tester
- [ ] **versionCode strategy** — you have two builds ready:
  - An **older** build (lower `versionCode`) to install on the device
  - A **newer** build (higher `versionCode`) uploaded to the internal testing track

---

## 3. Required Environment Facts to Record

For every validation run, record these facts before testing:

| Field | Value |
|---|---|
| Date / time | |
| Tester name / email | |
| Package name | |
| Installed versionCode | |
| Installed versionName | |
| Play track versionCode | |
| Play track versionName | |
| Device model | |
| Android API level | |
| Install source (Play / sideload / other) | |
| App build type (release / debug / internal) | |
| App signing certificate (same as Play?) | |
| Library commit hash | |

---

## 4. Validation Checklist

Run each step in order and record the observed result.

### A. No update available

**Setup:** Install the **newest** build (same `versionCode` as on Play) so there is no update differential.

**Steps:**
1. Launch the app.
2. Tap **getUpdateStatus()**.

**Expected:**
- `platform: "android"`
- `supported: true`
- `updateAvailable: false`
- `reason: "no-update-available"`
- `capabilities.immediate: true` (if Play Core is available)
- `capabilities.flexible: true` (if Play Core is available)

**Observed:**

---

### B. Update available status

**Setup:** Install the **older** build (lower `versionCode` than on Play).

**Steps:**
1. Launch the app.
2. Tap **getUpdateStatus()**.

**Expected:**
- `platform: "android"`
- `supported: true`
- `updateAvailable: true`
- `reason: "update-available"`
- `allowed.immediate: true` or `false` (depends on Play policy)
- `allowed.flexible: true` or `false` (depends on Play policy)
- `android.playCore.updateAvailability: "UPDATE_AVAILABLE"`
- `android.playCore.availableVersionCode` matches the Play track version

**Observed:**

---

### C. Immediate update flow

**Setup:** Same as B (older build installed, newer on Play). Ensure `allowed.immediate: true` from `getUpdateStatus()`.

**Steps:**
1. Tap **startImmediateUpdate() (Android Play)**.

**Expected:**
- Play Core immediate-update UI appears (full-screen blocking dialog).
- User can accept or decline.
- If accepted: app updates and restarts.
- If declined: returns `UpdateStatus` with `reason: "update-not-allowed"` or `installStatus: "canceled"`.
- No JavaScript exception thrown.

**Observed:**

---

### D. Flexible update flow

**Setup:** Same as B. Ensure `allowed.flexible: true` from `getUpdateStatus()`.

**Steps:**
1. Tap **startFlexibleUpdate() (Android Play)**.

**Expected:**
- Play Core flexible-update UI appears (non-blocking snackbar/banner).
- User can accept to start background download.
- Returns `UpdateStatus` with `installStatus: "pending"` or `"downloading"`.
- No JavaScript exception thrown.

**Observed:**

---

### E. Install-state listener and progress

**Setup:** Same as D (flexible update accepted and downloading).

**Steps:**
1. Tap **addInstallStateListener()**.
2. Watch the log for events during download.

**Expected:**
- Events fire with `platform: "android"`, `supported: true`.
- `reason: "download-progress"` during download.
- `bytesDownloaded` and `totalBytesToDownload` present.
- `progress` calculated as `bytesDownloaded / totalBytesToDownload`.
- `installStatus` transitions: `"downloading"` → `"downloaded"`.
- `android.playCore` details included in events.

**Observed:**

---

### F. Complete flexible update

**Setup:** Flexible update has fully downloaded (`installStatus: "downloaded"` observed in listener).

**Steps:**
1. Tap **completeFlexibleUpdate() (Android Play)**.

**Expected:**
- App triggers install and restarts.
- Returns `UpdateStatus` with `reason: "flexible-update-downloaded"` and `installStatus: "downloaded"`.
- No JavaScript exception thrown.

**Observed:**

---

### G. Non-Play / sideloaded unsupported behavior

**Setup:** Install the app via `adb install` or local APK (not from Play Store).

**Steps:**
1. Tap **getUpdateStatus()**.
2. Tap **startImmediateUpdate()**.
3. Tap **startFlexibleUpdate()**.

**Expected:**
- `getUpdateStatus()` returns `supported: false`, `reason: "unsupported-install-source"`.
- `startImmediateUpdate()` returns `supported: false`, `reason: "unsupported-install-source"`.
- `startFlexibleUpdate()` returns `supported: false`, `reason: "unsupported-install-source"`.
- No JavaScript exception thrown.

**Observed:**

---

### H. iOS unsupported / helper behavior smoke check

**Setup:** Run the example app on an iOS device or simulator.

**Steps:**
1. Tap **getUpdateStatus()** (without `appStoreId`).
2. Enter an `appStoreId` and tap **getUpdateStatus()** again.
3. Tap **startImmediateUpdate()**.
4. Tap **startFlexibleUpdate()**.
5. Tap **addInstallStateListener()**.
6. Tap **openStorePage()** (with `appStoreId`).

**Expected:**
- `getUpdateStatus()` without `appStoreId`: `supported: false`, `reason: "missing-app-store-id"`.
- `getUpdateStatus()` with `appStoreId`: `supported: false`, `reason: "store-lookup-unavailable"`.
- `startImmediateUpdate()`: `supported: false`, `reason: "unsupported-platform"`.
- `startFlexibleUpdate()`: `supported: false`, `reason: "unsupported-platform"`.
- `addInstallStateListener()`: fires one event with `supported: false`, `reason: "unsupported-platform"`, returns a noop subscription.
- `openStorePage()`: opens the App Store page for the given `appStoreId`.

**Observed:**

---

## 5. Expected Observations by API

| API | Expected happy-path result | Expected unsupported result |
|---|---|---|
| `getUpdateStatus()` | `supported: true`, `updateAvailable: true/false`, `android.playCore` details | `supported: false`, `reason: "unsupported-install-source"` or `"play-core-unavailable"` |
| `startImmediateUpdate()` | Play Core immediate UI shown, app may restart | `supported: false`, `reason: "unsupported-install-source"` |
| `startFlexibleUpdate()` | Play Core flexible UI shown, background download starts | `supported: false`, `reason: "unsupported-install-source"` |
| `completeFlexibleUpdate()` | App installs update and restarts | `supported: true`, `reason: "update-not-allowed"` if no download pending |
| `addInstallStateListener()` | Events fire with progress and status changes | iOS: `supported: false`, `reason: "unsupported-platform"` |
| `openStorePage()` | Platform-appropriate store page opens | Throws `InAppUpdatesError` on iOS if `appStoreId` missing |

---

## 6. Results Table

| Step | Description | Status | Notes |
|---|---|---|---|
| A | No update available | ☐ Pass ☐ Fail ☐ Blocker | |
| B | Update available status | ☐ Pass ☐ Fail ☐ Blocker | |
| C | Immediate update flow | ☐ Pass ☐ Fail ☐ Blocker | |
| D | Flexible update flow | ☐ Pass ☐ Fail ☐ Blocker | |
| E | Listener / progress | ☐ Pass ☐ Fail ☐ Blocker | |
| F | Complete flexible update | ☐ Pass ☐ Fail ☐ Blocker | |
| G | Non-Play unsupported | ☐ Pass ☐ Fail ☐ Blocker | |
| H | iOS smoke check | ☐ Pass ☐ Fail ☐ N/A | |

### Overall Result

- [ ] **Pass** — All required steps (A–G) passed. iOS smoke check (H) passed or N/A with justification.
- [ ] **Fail** — One or more steps failed; blockers must be resolved before release.
- [ ] **Blocker** — Critical issue prevents validation completion; documented below.

### Blockers

| # | Description | Impact | Proposed Resolution |
|---|---|---|---|
| 1 | | | |
| 2 | | | |

---

## 7. Release Gate Criteria

The `@rnforge/react-native-in-app-updates` v1 package is **release-ready** only when:

1. [ ] This checklist is fully completed.
2. [ ] At least one Android Play-distributed test app on a physical device has been validated.
3. [ ] The internal testing track (or higher) was used for the Play-distributed build.
4. [ ] Results are recorded in this document (or a dated copy).
5. [ ] All blockers are resolved, or explicitly accepted with documented justification.
6. [ ] Observed behavior matches the typed API contract (no undocumented exceptions, no silent failures).

---

## 8. Known Limitations

- **Real update availability depends on Play distribution state.** If Play Console processing is delayed, `getUpdateStatus()` may return `no-update-available` even when a newer version exists.
- **Local debug / sideload builds are expected to be unsupported.** Do not expect Play in-app updates to work from Android Studio debug installs.
- **APK expansion-file apps are unsupported.** This is a documented product boundary; do not test with expansion-file apps.
- **Android Gradle / native build verification is separate.** This checklist validates runtime Play behavior, not build-time compilation. Gradle build verification remains a prerequisite.
- **iOS behavior is intentionally limited.** iOS does not support Play-style in-app updates. The iOS smoke check validates that unsupported behavior is explicit and typed, not silent.
- **Listener events may vary by device and network conditions.** Download speed, interruption, and retry behavior are not deterministic.

---

## Related Documents

- [`TESTING.md`](./TESTING.md) — Automated test and local verification commands
- [`example/App.tsx`](../example/App.tsx) — Minimal example source for running the validation steps
- [Issue 0007](https://github.com/rnforge/react-native-in-app-updates/issues/7) — Tracking issue for this release gate
