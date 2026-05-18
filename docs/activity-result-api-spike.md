# Activity Result API Spike

Issue 0032 Phase 2F outcome: defer implementation.

## Context

RNForge currently starts Android Play Core update UI with `AppUpdateManager.startUpdateFlow(...)` from the immediate and flexible services. This returns a Task and lets the package report whether Play Core accepted the start request.

Current files:

- `android/src/main/java/dev/rnforge/inappupdates/playcore/PlayCoreImmediateUpdateService.kt`
- `android/src/main/java/dev/rnforge/inappupdates/playcore/PlayCoreFlexibleUpdateService.kt`

Google's current in-app update guide and `AppUpdateManager` reference also document `startUpdateFlowForResult(...)` variants that work with `ActivityResultLauncher`.

Source references:

- Android Developers in-app updates guide: https://developer.android.com/guide/playcore/in-app-updates/kotlin-java
- Android Developers `AppUpdateManager` reference: https://developer.android.com/reference/com/google/android/play/core/appupdate/AppUpdateManager
- Android Developers Play In-App Updates release notes: https://developer.android.com/reference/com/google/android/play/core/release-notes-in_app_updates

## Decision

Do not migrate to Activity Result API in Issue 0032.

Keep the current `startUpdateFlow(...)` implementation until there is a concrete consumer need for result-code handling beyond the existing status/result contract.

## Rationale

- The current public API returns typed `UpdateStatus` values, not Android activity result codes.
- Activity Result API requires lifecycle-owned registration before launch. RNForge currently only receives the current foreground `Activity` through `ActivityProvider`; it does not own an AndroidX lifecycle registration point.
- Adding `ActivityResultLauncher` support would likely require new lifecycle plumbing, new AndroidX activity dependencies or host integration requirements, and a changed native service contract.
- No accepted user-facing behavior currently depends on distinguishing activity result codes after the Play UI is launched.
- This is not a narrow cleanup slice; it is a separate Android lifecycle design change.

## Revisit When

Reopen this as a separate design issue if consumers need one of these behaviors:

- distinguish user cancellation from update-flow launch failure
- expose Play Core activity result codes in JavaScript
- support host-provided Activity Result registration
- align with a future Nitro lifecycle API that provides safe launcher registration

## Verification Needed If Implemented Later

- Android JVM tests for service behavior and callback mapping
- Android example `assembleDebug`
- manual Play validation for immediate and flexible update flows
- public API review for any result-code additions
