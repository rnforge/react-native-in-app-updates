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

## Build verification

```bash
bun run build
```

Runs `bun run typecheck && bob build`, producing `lib/commonjs/`, `lib/module/`, and `lib/typescript/`.

## Example app

The `example/` directory contains a **minimal example source scaffold** — not a fully generated React Native app. It lacks native Android and iOS project files (`android/`, `ios/`, Pods, Gradle, etc.). Use `example/App.tsx` as a reference for integrating the v1 API into your own application.

> Note: `example/App.tsx` is **not included in the package `tsconfig.json`** (which only covers `src/**/*` and `nitrogen/**/*.json`). It is not typechecked by `bun run typecheck` and should be treated as illustrative source, not as a compiled package target.

## Android native / Gradle tests

Android native unit tests are **not yet available** in this repository. Future native test coverage should include:

- Play Core `AppUpdateInfo` → typed `UpdateStatus` mapping
- `Activity` / `currentActivity` resolution and null-safety
- Install source detection (Play vs. sideload)
- Fallback intent resolution for `openStorePage()`
- Listener registration, event emission, and cleanup
- Flexible update download → completion lifecycle

## Play Console validation

Real Play in-app update flow verification (immediate and flexible) requires a Google Play-distributed app, internal test track, and a physical device. This is tracked as **Issue 0007**.

See [`docs/manual-play-validation.md`](./docs/manual-play-validation.md) for the full release gate checklist.
