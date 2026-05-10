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

Android native test sources exist under `android/src/test/java/com/rnforge/inappupdates/` and use JUnit 4. The example app provides a runnable Gradle harness.

### Running from the example app

```bash
cd example/android

# Verify the library module is autolinked
./gradlew projects

# Run library unit tests (requires compilation to succeed)
./gradlew :rnforge_react-native-in-app-updates:testDebugUnitTest
```

> **Status:** The Gradle harness is functional and the library module is autolinked (`:rnforge_react-native-in-app-updates`). The library compiles and the Android native test task runs successfully: **17 tests, 4 skipped, 0 failures**. Skipped tests are explicit JUnit assumptions (unmocked Android framework in JVM), not silent passes. Future Android native coverage can add `FakeAppUpdateManager` / Mockito / Robolectric tests.

### Testable seams

Play Core services accept injectable dependencies so tests can control behavior without real Play Store availability:

- **`AppUpdateManagerProvider`** — abstracts `AppUpdateManager` creation. Production uses `PlayCoreAppUpdateManagerProvider` (shared singleton). Tests can inject a fake provider.
- **`EnvironmentChecker`** — abstracts install-source and Google Play Services checks. Production uses `DefaultEnvironmentChecker`. Tests can inject a fake to control environment state.

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

- **PlayCoreInstallStateListenerServiceTest** — Listener seam safety test sources exist:
  - Null-context path does not invoke `AppUpdateManagerProvider`
  - Adding/removing listeners is safe when context is unavailable
  - Currently skipped when NitroModules cannot load in the JVM test environment; future 0017 work should make these run under a Robolectric or instrumentation harness.

### What requires real Play Core or instrumentation

Paths that interact with `AppUpdateInfo`, `Task<AppUpdateInfo>`, or `startUpdateFlow()` need either:

- `com.google.android.play.core.appupdate.testing.FakeAppUpdateManager` (when available in the Play Core testing artifact)
- Mockito + Robolectric for mocking Play Core classes and Android `Context`
- Instrumentation tests on an emulator or physical device

These paths are covered by design via the injected seams but are not exercised by the current test sources because they require a runnable Gradle harness with Play Core on the classpath:

- `mapAppUpdateInfoToStatus()` — needs `AppUpdateInfo` (opaque Play Core object)
- `PlayCoreStatusService` success paths — needs `AppUpdateInfo` from `appUpdateInfo()` task
- `PlayCoreImmediateUpdateService` flow start — needs `Activity` and `AppUpdateInfo`
- `PlayCoreFlexibleUpdateService` flow start and `completeFlexibleUpdate()` — needs `Activity` and download state
- Install-state listener event mapping — needs real or fake `InstallState` objects

### Expanding Android test coverage

If you add Mockito and Robolectric to `android/build.gradle`, you can expand coverage to:

- Service early-return paths with a mocked `Context`
- `EnvironmentChecker` behavior with controlled `PackageManager` shadows
- `PlayCoreInstallStateListenerService` registration with a fake `AppUpdateManager`

## iOS native tests

Xcode project XCTest is not wired yet.

SwiftPM tests cover pure iOS lookup core logic:

```bash
swift test
```

The iOS lookup logic is split into seam-friendly Swift helpers in `ios/Core/AppStoreLookupCore.swift` so SwiftPM and future XCTest/Xcode coverage can target:

- lookup URL construction
- iTunes response parsing
- dotted numeric version comparison
- lookup failure/status mapping

## Play Console validation

Real Play in-app update flow verification (immediate and flexible) requires a Google Play-distributed app, internal test track, and a physical device.

See [`docs/manual-play-validation.md`](./docs/manual-play-validation.md) for the full release gate checklist.
