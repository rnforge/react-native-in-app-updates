# Testing

This document describes how to verify the `@rnforge/react-native-in-app-updates` package locally.

## TypeScript type checking

```bash
bun run typecheck
```

Runs `tsc --noEmit` against the full source tree (including specs and mocks).

## Unit tests

```bash
bunx jest
```

Runs the Jest suite against `src/__tests__/**/*.test.ts`. Tests mock `react-native-nitro-modules` and cover:

- `getUpdateStatus()` — Android and iOS typed results, Play Core mapping, options passthrough, bridge failure
- `startImmediateUpdate()` — Android success, iOS unsupported, unsupported-install-source, update-not-allowed, bridge failure
- `startFlexibleUpdate()` — Android success, iOS unsupported
- `completeFlexibleUpdate()` — downloaded completion, update-not-allowed when none downloaded
- `addInstallStateListener()` — iOS unsupported event, subscription remove, Android progress/status mapping
- `openStorePage()` — iOS appStoreId passthrough and validation, Android no-options and passthrough, native failure
- API export/contract tests — all v1 public APIs exported, `UpdateStatus` and `InstallStateEvent` shape, union type values
- `InAppUpdatesError` — constructor, `instanceof`, error codes, stack trace
- `buildNativeUpdateOptions` — typed Android option shaping, `allowAssetPackDeletion` passthrough

## Build verification

```bash
bun run build
```

Runs `bun run typecheck && bob build`, producing `lib/commonjs/`, `lib/module/`, and `lib/typescript/`.

## CI verification

The `.github/workflows/ci.yml` workflow runs on every push to `main` and every pull request. It covers:

- `bun run typecheck` — TypeScript type checking (src + specs)
- `bun run typecheck:example` — TypeScript type checking (example/)
- `bunx jest` — Unit tests (Jest, mocked native layer)
- `bun run build` — Build verification (bob)
- `npm pack --dry-run` — Package content sanity check

Native Android/iOS example builds are **not in CI** because they require Android SDK / Xcode and are too heavy for standard CI runners. Manual Play Console validation remains a separate release gate.

## Nitro/native release gate

Any change to the Nitro spec, `nitro.json` namespace, generated native files, or handwritten native bridge code must run both Android and iOS native build verification before publish. TypeScript, Jest, `bun run build`, `npm pack --dry-run`, and Android-only verification are not sufficient for this class of change.

Minimum required checks before publishing a release or prerelease with Nitro/native bridge changes:

```bash
bun run typecheck
bun run typecheck:example
bunx jest --runInBand
bun run build
npm pack --dry-run
```

Android native verification:

```bash
cd example/android
./gradlew :rnforge_react-native-in-app-updates:testDebugUnitTest
./gradlew assembleDebug
```

iOS native verification on macOS/Xcode:

```bash
cd example/ios
bundle install
bundle exec pod install
cd ..
bun ios
```

For release-blocking native fixes, also install the packed tarball or published `@next` package into a clean consumer app and verify it runs without any local `node_modules` patch.

## Example app

The `example/` directory is a **real runnable React Native app** generated with React Native CLI (RN 0.84.1). It includes native Android and iOS project files and demonstrates all v1 public APIs.

### Install example dependencies

```bash
# From repo root
bun install --frozen-lockfile

# Then install example dependencies
cd example
bun install
```

> `bun run typecheck:example` requires example dependencies to be installed first, because `example/tsconfig.json` extends `@react-native/typescript-config` which is a devDependency of the example app only.
>
> Bun's incremental install can hang with local `file:..` symlink dependencies when `example/node_modules` already exists. If you hit this, run `rm -rf example/node_modules` and retry. CI uses a clean checkout so it is not affected.

### Run the example

```bash
# Start Metro
cd example && bun start

# Android (requires Android SDK and emulator/device)
cd example/android && ./gradlew assembleDebug

# iOS (requires macOS and Xcode)
cd example/ios && bundle install && bundle exec pod install
# Then open the .xcworkspace in Xcode or run: cd example && bun ios
```

> Note: The `example/` source is **not included in the root package `tsconfig.json`** (which only covers `src/**/*` and `nitrogen/**/*.json`). It is typechecked separately via `bun run typecheck:example`.

### iOS verification

iOS builds require macOS and Xcode. On non-Mac environments, iOS verification is deferred until a Mac environment is available.

## Android native tests

Android native test sources exist under `android/src/test/java/dev/rnforge/inappupdates/playcore/` and use JUnit 4. The example app provides a runnable Gradle harness.

### Running from the example app

```bash
cd example/android

# Verify the library module is autolinked
./gradlew projects

# Run library unit tests
./gradlew :rnforge_react-native-in-app-updates:testDebugUnitTest

# Generate the Android JVM coverage report
./gradlew :rnforge_react-native-in-app-updates:jacocoDebugUnitTestReport
```

> **Status:** The Gradle harness runs the library module (`:rnforge_react-native-in-app-updates`). Last verified Android JVM result: 43 tests, 0 skipped, 0 failures. Last measured Android Jacoco app-package line coverage: 82.43%; generated Nitro bindings are not a useful coverage target and should be excluded from coverage interpretation.

### Testable seams

Play Core services accept injectable dependencies so tests can control behavior without real Play Store availability:

- **`AppUpdateManagerProvider`** — abstracts `AppUpdateManager` creation. Production uses `PlayCoreAppUpdateManagerProvider` (shared singleton). Tests can inject a fake provider.
- **`EnvironmentChecker`** — abstracts install-source and Google Play Services checks. Production uses `DefaultEnvironmentChecker`. Tests can inject a fake to control environment state.
- **`ActivityProvider`** — abstracts application context and current activity lookup. Production uses `DefaultActivityProvider`. Tests can inject context/activity without shadowing Nitro internals.

All services have constructor defaults, so production code (`HybridInAppUpdates`) is unchanged.

### What these test sources are intended to cover when run in a Gradle harness

- **PlayCoreMappingTest** — Mapping-focused tests that still require Android/Play Core/Nitro classes on the Gradle test classpath:
  - `mapInstallStatus()` — all known Play Core constants plus unknown fallbacks
  - `buildAppUpdateOptions()` — default, `allowAssetPackDeletion = true/false/null`
  - `encodeTaskFailure()` — regular exceptions and `InstallException` with error codes
  - `mapInstallErrorCodeLabel()` — maps only when status is `FAILED`
  - `createUnsupportedStatus()` / `createStatus()` — RNForge status object structure

- **PlayCoreEnvironmentTest** — Early-return guard behavior:
  - `checkEarlyEnvironment()` returns `update-not-allowed` when context is `null`

- **PlayCoreInstallStateListenerServiceTest** — Listener seam safety:
  - Null-context path does not invoke `AppUpdateManagerProvider`
  - Adding/removing listeners is safe when context is unavailable
- **PlayCoreStatusServiceFakeManagerTest** — FakeAppUpdateManager-backed status coverage:
  - no update available
  - update available with immediate/flexible allow flags
- **PlayCoreEnvironmentAndListenerFakeManagerTest** — Environment and listener coverage:
  - unsupported install source
  - Play Services unavailable
  - install-state listener progress and completion
  - flexible update completion after download
- **PlayCoreImmediateUpdateServiceFakeManagerTest** — Immediate flow coverage:
  - no update available
  - immediate flow starts when allowed and an Activity exists
  - update-not-allowed when Activity is missing or immediate flow is not allowed
  - unsupported install source
- **PlayCoreFlexibleUpdateServiceFakeManagerTest** — Flexible flow coverage:
  - no update available
  - flexible flow starts when allowed and an Activity exists
  - update-not-allowed when Activity is missing or flexible flow is not allowed
  - complete without downloaded update
  - Play Services unavailable
- **PlayCoreServiceFailureTest** — Task failure coverage:
  - `appUpdateInfo` failure is surfaced through the shared task-failure encoding path
- **PlayCoreStoreServiceTest** — Android store-page coverage:
  - null context failure
  - Play Store/browser intent launch with `FLAG_ACTIVITY_NEW_TASK`

### What requires real Play Core or instrumentation

The JVM suite uses `FakeAppUpdateManager`, Mockito, and Robolectric for Play Core service coverage. Real Play Store behavior still requires manual validation because Play Core update availability depends on a Play-distributed package and a physical/device environment.

## iOS native tests

Xcode project XCTest is not wired yet.

SwiftPM tests cover pure iOS lookup core logic:

```bash
swift test
```

The iOS lookup logic is split into seam-friendly Swift helpers:

- `ios/Core/AppStoreLookupCore.swift` — pure Foundation lookup logic (URL construction, iTunes response parsing, dotted version comparison)
- `ios/Support/AppStoreLookupSupport.swift` — Nitro adapter glue (bridges Core helpers to the Nitro native module)

SwiftPM and future XCTest/Xcode coverage target the Core helpers for:

- lookup URL construction
- iTunes response parsing
- dotted numeric version comparison
- lookup failure/status mapping

## Play Console validation

Real Play in-app update flow verification (immediate and flexible) requires a Google Play-distributed app, internal test track, and a physical device.

See [`docs/manual-play-validation.md`](./docs/manual-play-validation.md) for the full release gate checklist.

## Source layout

```
android/src/main/java/dev/rnforge/inappupdates/
  HybridInAppUpdates.kt          # Nitro native module (bridge)
  InAppUpdatesPackage.kt         # Nitro package registration
  ActivityProvider.kt            # Activity/context seam
  InAppUpdatesActivityProvider.kt
  EnvironmentChecker.kt          # install-source guard
  playcore/                      # Play Core implementation
    AppUpdateManagerProvider.kt
    PlayCoreAppUpdateManager.kt
    PlayCoreFlexibleUpdateService.kt
    PlayCoreImmediateUpdateService.kt
    PlayCoreInstallStateListenerService.kt
    PlayCoreMapping.kt
    PlayCoreStatusMapping.kt
    PlayCoreStatusService.kt
    PlayCoreStoreService.kt

android/src/test/java/dev/rnforge/inappupdates/playcore/
  10 test files + PlayCoreTestSupport.kt

android/src/main/cpp/cpp-adapter.cpp  # C++ bridge adapter

ios/
  Bridge.h                       # Obj-C bridge header
  HybridInAppUpdates.swift       # Nitro native module (bridge)
  Core/                          # Pure Foundation lookup logic
    AppStoreLookupCore.swift
    AppStoreLookupHTTPClient.swift
  Support/                       # Nitro adapter glue
    AppStoreLookupSupport.swift
  CoreTests/                     # SwiftPM tests
    AppStoreLookupCoreTests.swift
    AppStoreLookupHTTPClientTests.swift
```
