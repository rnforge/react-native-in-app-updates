# @rnforge/react-native-in-app-updates

A capability-first, cross-platform React Native API for in-app update flows. Built with [Nitro](https://nitro.margelo.com) for type-safe native bridging.

On **Android**, the package integrates with Google Play Core in-app updates to provide immediate and flexible update flows. On **iOS**, the package reports Play-style flows as unsupported and provides store-page helpers instead of silently pretending updates can be installed in-app.

## Support Matrix

| Platform | Real In-App Updates | Store Page | Install Listener |
|---|---|---|---|
| Android (Google Play) | immediate + flexible | yes | yes |
| Android (sideload / debug) | unsupported | yes (Play Store fallback) | no |
| iOS | unsupported | yes (App Store) | unsupported event |

## Requirements

- React Native `0.75+`
- `react-native-nitro-modules` (peer dependency)
- **Android**: `compileSdkVersion 34+`, NDK `27+`, Google Play Services
- **iOS**: Xcode `16.4+`, Swift `5.9+`

## Installation

```bash
bun add @rnforge/react-native-in-app-updates react-native-nitro-modules
# or
npm install @rnforge/react-native-in-app-updates react-native-nitro-modules
# or
yarn add @rnforge/react-native-in-app-updates react-native-nitro-modules
```

## Quick Start

```typescript
import {
  getUpdateStatus,
  startImmediateUpdate,
  startFlexibleUpdate,
  completeFlexibleUpdate,
  openStorePage,
  addInstallStateListener,
} from '@rnforge/react-native-in-app-updates'
```

The update-status and update-flow APIs (`getUpdateStatus`, `startImmediateUpdate`, `startFlexibleUpdate`, `completeFlexibleUpdate`) return typed `UpdateStatus` objects rather than throwing for expected unsupported or unavailable states. `addInstallStateListener` returns a subscription and emits `InstallStateEvent` objects. `openStorePage` returns `Promise<void>` and rejects only for invalid input or unexpected native failures.

## API Reference

### `getUpdateStatus(options?)`

Check update availability and platform capabilities. This is the recommended entry point for every update check.

```typescript
import { getUpdateStatus } from '@rnforge/react-native-in-app-updates'

const status = await getUpdateStatus({
  ios: {
    appStoreId: '1234567890',
    country: 'us',
  },
})

console.log(status.platform)          // 'android' | 'ios'
console.log(status.supported)         // boolean
console.log(status.updateAvailable)   // boolean | null
console.log(status.reason)            // e.g. 'update-available'
console.log(status.capabilities)      // { immediate, flexible, storePage, ... }
console.log(status.allowed)           // { immediate, flexible }
console.log(status.android?.playCore) // raw Play Core details (Android only)
```

**iOS options:**

| Option | Type | Default | Description |
|---|---|---|---|
| `ios.appStoreId` | `string` | — | Numeric Apple App Store ID. Required for iOS store lookup. |
| `ios.country` | `string` | — | Optional two-letter country code (e.g. `'us'`, `'gb'`) to localize the App Store lookup URL. |

**Android options:**

| Option | Type | Default | Description |
|---|---|---|---|
| `android.allowAssetPackDeletion` | `boolean` | `false` | Opt-in to allow Play Core to delete asset packs under storage pressure. Only affects Play Asset Delivery apps. |

```typescript
// Opt-in to asset-pack deletion during status check
const status = await getUpdateStatus({
  android: { allowAssetPackDeletion: true },
})
```

**Key fields:**

| Field | Description |
|---|---|
| `supported` | Whether the current platform + install source supports in-app updates |
| `updateAvailable` | `true` if a newer version is available, `false` if not, `null` if unsupported |
| `reason` | Typed reason for the result (e.g. `'update-available'`, `'unsupported-install-source'`, `'no-update-available'`) |
| `capabilities` | What flows are available on this platform regardless of current state |
| `allowed` | Whether immediate/flexible flows are currently permitted by Play policy |
| `android.playCore` | Raw Play Core details: `updateAvailability`, `availableVersionCode`, `clientVersionStalenessDays`, etc. |

### `startImmediateUpdate(options?)`

Starts an Android immediate update flow. Presents a full-screen Play Core dialog that blocks the user until they accept or decline.

```typescript
import { startImmediateUpdate } from '@rnforge/react-native-in-app-updates'

const result = await startImmediateUpdate()
console.log(result.reason) // 'update-available', 'update-not-allowed', 'unsupported-install-source', etc.
```

- On Android Play installs: triggers the Play immediate UI. App may restart if the user accepts.
- On iOS and non-Play Android installs: returns `supported: false` with a typed reason. No silent noop.

**Android options:**

| Option | Type | Default | Description |
|---|---|---|---|
| `android.allowAssetPackDeletion` | `boolean` | `false` | Opt-in to allow Play Core to delete asset packs under storage pressure during the immediate update flow. |

```typescript
// Opt-in to asset-pack deletion during immediate update
await startImmediateUpdate({ android: { allowAssetPackDeletion: true } })
```

### `startFlexibleUpdate(options?)`

Starts an Android flexible update flow. Presents a non-blocking Play Core snackbar/banner that begins a background download.

```typescript
import { startFlexibleUpdate } from '@rnforge/react-native-in-app-updates'

const result = await startFlexibleUpdate()
console.log(result.reason) // 'update-available', 'update-not-allowed', etc.
```

- On Android Play installs: triggers the Play flexible UI. Download proceeds in the background.
- On iOS and non-Play Android installs: returns `supported: false` with a typed reason.

**Android options:**

| Option | Type | Default | Description |
|---|---|---|---|
| `android.allowAssetPackDeletion` | `boolean` | `false` | Opt-in to allow Play Core to delete asset packs under storage pressure during the flexible update flow. |

```typescript
// Opt-in to asset-pack deletion during flexible update
await startFlexibleUpdate({ android: { allowAssetPackDeletion: true } })
```

### `completeFlexibleUpdate()`

Completes a downloaded flexible update. Call this after `getUpdateStatus()` returns `reason: 'flexible-update-downloaded'` or after the install-state listener reports `installStatus: 'downloaded'`.

```typescript
import { completeFlexibleUpdate } from '@rnforge/react-native-in-app-updates'

const result = await completeFlexibleUpdate()
console.log(result.reason) // 'flexible-update-downloaded' | 'update-not-allowed'
```

- If no downloaded flexible update is pending: returns `reason: 'update-not-allowed'`.
- No JavaScript exception is thrown for expected unavailable states.

### `addInstallStateListener(callback)`

Listen to Android flexible update progress and install-state changes.

```typescript
import { addInstallStateListener } from '@rnforge/react-native-in-app-updates'

const subscription = addInstallStateListener((event) => {
  console.log(event.platform)          // 'android' | 'ios'
  console.log(event.supported)         // boolean
  console.log(event.installStatus)     // 'downloading' | 'downloaded' | 'installing' | 'failed' | ...
  console.log(event.reason)            // 'download-progress' | 'install-state-changed'
  console.log(event.bytesDownloaded)   // number
  console.log(event.totalBytesToDownload) // number
  console.log(event.progress)          // 0.0 - 1.0
  console.log(event.android?.playCore) // raw Play Core details
})

// Remove listener when done
subscription.remove()
```

- On Android: events fire during flexible download/install lifecycle.
- On iOS: fires a single event with `supported: false`, `reason: 'unsupported-platform'`, then becomes a noop subscription.

### `openStorePage(options?)`

Open the platform store page for the current app.

```typescript
import { openStorePage } from '@rnforge/react-native-in-app-updates'

// iOS — requires appStoreId, optional country
await openStorePage({
  ios: {
    appStoreId: '1234567890',
    country: 'us',
  },
})

// Android — no options required
await openStorePage()
```

- **Android**: opens `market://details?id=<packageName>` with a browser fallback to the Play Store web URL.
- **iOS**: opens `https://apps.apple.com/app/id<appStoreId>`, or `https://apps.apple.com/<country>/app/id<appStoreId>` when `country` is provided. Throws `InAppUpdatesError('invalid-input')` if `appStoreId` is missing, non-digits, or `country` is invalid.

## iOS Behavior

iOS does **not** support Google Play-style immediate or flexible in-app updates. This is intentional: the package does not silently noop or pretend updates can be installed inside the app.

Instead, iOS APIs return **explicit typed status**:

`getUpdateStatus()` has two iOS modes:

- With a valid `appStoreId`, it performs an App Store lookup. Successful lookups return `supported: true`, `latestVersionLookup: true`, `updateAvailable: true/false/null`, `currentVersion`, `latestStoreVersion`, and populated `ios.appStore` metadata.
- Without `appStoreId`, or when lookup fails, it returns `supported: false` with `reason: 'missing-app-store-id'` or `'store-lookup-unavailable'`.

| API | iOS Result |
|---|---|
| `getUpdateStatus()` | App Store lookup when `appStoreId` is provided; typed unsupported lookup status otherwise |
| `startImmediateUpdate()` | `supported: false`, `reason: 'unsupported-platform'` |
| `startFlexibleUpdate()` | `supported: false`, `reason: 'unsupported-platform'` |
| `completeFlexibleUpdate()` | `supported: false`, `reason: 'unsupported-platform'` |
| `addInstallStateListener()` | One event: `supported: false`, `reason: 'unsupported-platform'`, noop subscription |
| `openStorePage()` | Opens App Store if `appStoreId` provided; throws if missing or invalid |

Use `openStorePage()` on iOS to direct users to the App Store for manual updates.

## Android `allowAssetPackDeletion` Option

The optional `android.allowAssetPackDeletion` flag controls whether Play Core may delete Play Asset Delivery asset packs when storage is insufficient during an update flow. This is **opt-in only** and defaults to `false` (current behavior unchanged).

- This option applies to `getUpdateStatus()`, `startImmediateUpdate()`, and `startFlexibleUpdate()`.
- `completeFlexibleUpdate()` does **not** accept this option.
- This is **not** general APK expansion-file support. It only affects apps that use Play Asset Delivery.
- If your app does not use Play Asset Delivery, this option has no effect.

## Android Google Play Distribution Boundary

Real in-app update flows require **all** of the following:

1. The app is installed from **Google Play** (not sideloaded, not debug builds from Android Studio).
2. Google Play Services are available and up to date.
3. The installed build is signed with the **same certificate** as the Play track build.

If any of these environment requirements are not met, the package returns `supported: false` with a typed reason:

| Condition | Reason |
|---|---|
| Sideloaded / debug install | `'unsupported-install-source'` |
| Play Core unavailable | `'play-core-unavailable'` |

When the environment supports in-app updates, the package returns `supported: true`. The update result then depends on Play availability and policy:

| Condition | Reason | updateAvailable |
|---|---|---|
| Newer version available on Play | `'update-available'` | `true` |
| Installed version is latest | `'no-update-available'` | `false` |
| Developer-triggered update in progress | `'developer-triggered-update-in-progress'` | `true` |
| Update available but not allowed by policy | `'update-not-allowed'` | varies |

The `'apk-expansion-files-unsupported'` reason is reserved in the type system for future detection. The current Android implementation does not emit it.

## Helper Predicates

Small pure JS helpers make branching on `UpdateStatus` safer and more readable:

```typescript
import {
  getUpdateStatus,
  isUpdateAvailable,
  canStartImmediateUpdate,
  canStartFlexibleUpdate,
  canCompleteFlexibleUpdate,
  canOpenStorePage,
  supportsInstallStateListener,
} from '@rnforge/react-native-in-app-updates'

const status = await getUpdateStatus()

if (canStartImmediateUpdate(status)) {
  await startImmediateUpdate()
} else if (canStartFlexibleUpdate(status)) {
  await startFlexibleUpdate()
} else if (canCompleteFlexibleUpdate(status)) {
  await completeFlexibleUpdate()
} else if (canOpenStorePage(status)) {
  await openStorePage()
}
```

| Helper | Returns `true` when |
|---|---|
| `isUpdateAvailable(status)` | `updateAvailable === true` |
| `canStartImmediateUpdate(status)` | `supported`, `capabilities.immediate`, `updateAvailable`, and `allowed.immediate` are all true |
| `canStartFlexibleUpdate(status)` | `supported`, `capabilities.flexible`, `updateAvailable`, and `allowed.flexible` are all true |
| `canCompleteFlexibleUpdate(status)` | `supported`, `capabilities.flexible`, and `installStatus === 'downloaded'` |
| `canOpenStorePage(status)` | `capabilities.storePage === true` (may be true even when `supported: false`) |
| `supportsInstallStateListener(status)` | `capabilities.installStateListener === true` |

Helpers return booleans only. If a helper returns `false`, inspect `status.reason` to understand why.

## Expected Status / Result Philosophy

This package uses a **capability-first** model:

- **Typed status objects** for expected states from update-status and update-flow APIs, not thrown errors.
- **Exceptions only** for programmer errors, invalid inputs, bridge failures, or unexpected native failures.
- Update APIs return an `UpdateStatus`-shaped result so you can branch on `supported`, `updateAvailable`, and `reason` without `try/catch` for normal conditions.

```typescript
// Capability-first branching with helpers
const status = await getUpdateStatus()

if (!status.supported) {
  // Expected unsupported state — no try/catch needed
  console.log('Unsupported:', status.reason)
  return
}

if (!isUpdateAvailable(status)) {
  console.log('No update available')
  return
}

if (canStartImmediateUpdate(status)) {
  await startImmediateUpdate()
} else if (canStartFlexibleUpdate(status)) {
  await startFlexibleUpdate()
}
```

## Testing

See [`TESTING.md`](./TESTING.md) for automated test commands and local verification.

### Automated checks

```bash
# TypeScript type checking (src + specs)
bun run typecheck

# TypeScript type checking (example/)
bun run typecheck:example

# Unit tests (Jest, mocked native layer)
bunx jest

# Build verification
bun run build
```

### Manual Play validation

Real Android Play behavior must be validated on a physical device with a Play-distributed build. See [`docs/manual-play-validation.md`](./docs/manual-play-validation.md) for the release gate checklist.

## Example

See [`example/src/App.tsx`](./example/src/App.tsx) for a minimal integration reference. The example is a runnable React Native app with Android and iOS project files, and demonstrates all v1 APIs with a simple log UI.

## Troubleshooting

### `getUpdateStatus()` returns `'unsupported-install-source'` on Android

Your app is not installed from Google Play. Play in-app updates require a Play-distributed build. Use `openStorePage()` to redirect users to the Play Store instead.

### `getUpdateStatus()` returns `'no-update-available'` even though a newer version exists

- Play Console processing may be delayed after upload.
- The installed `versionCode` may already match the latest Play track version.
- The device account may not be enrolled as an internal tester.
- The signing certificate of the installed build may not match the Play track build.

### `startImmediateUpdate()` / `startFlexibleUpdate()` returns `'update-not-allowed'`

Play policy has decided the update is not allowed at this time (e.g. too soon after last check, device constraints, or user declined previously). The app should continue normally and retry later.

### iOS `openStorePage()` throws `InAppUpdatesError('invalid-input')`

You must provide a valid `appStoreId` (digits-only) and optionally a two-letter `country` code:

```typescript
await openStorePage({ ios: { appStoreId: '1234567890', country: 'us' } })
```

### Listener events stop firing or show stale progress

Only one install-state listener is typically active per app session. If you register a new listener, remove the old one first with `subscription.remove()`.

## License

MIT
