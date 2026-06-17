# @rnforge/react-native-in-app-updates

Capability-first React Native in-app updates. Android uses Google Play Core immediate and flexible update flows. iOS reports explicit unsupported status and provides an App Store fallback instead of silently pretending updates can be installed in-app.

## Docs

Full documentation lives at [rnforge.dev](https://rnforge.dev):

- [Overview](https://rnforge.dev/docs/in-app-updates)
- [Installation](https://rnforge.dev/docs/in-app-updates/install)
- [Quick Start](https://rnforge.dev/docs/in-app-updates/quickstart)
- [App Integration](https://rnforge.dev/docs/in-app-updates/app-integration)
- [API Guide](https://rnforge.dev/docs/in-app-updates/api)
- [Troubleshooting](https://rnforge.dev/docs/in-app-updates/troubleshooting)
- [LLM docs](https://rnforge.dev/docs/in-app-updates/llms.txt)
- [Full LLM docs](https://rnforge.dev/docs/in-app-updates/llms-full.txt)

## Support Matrix

| Platform | In-App Update Flow | Store Page | Install Listener |
|---|---|---|---|
| Android (Google Play) | immediate + flexible | yes | yes |
| Android (sideload / debug) | unsupported | Play Store fallback | no |
| iOS | unsupported | App Store fallback | unsupported event only |

## Requirements

- React Native version supported by `react-native-nitro-modules`
- `react-native-nitro-modules` (peer dependency)
- **Android**: `compileSdkVersion 34+`, NDK `27+`, Google Play Services
- **iOS**: Xcode `16.4+`, Swift `5.9+`

## Installation

```bash
npm install @rnforge/react-native-in-app-updates react-native-nitro-modules
```

This package includes native code. After installing, rebuild your native projects:

- **iOS:** `cd ios && pod install`, then rebuild from Xcode or your normal React Native command.
- **Android:** Rebuild from Android Studio or your normal React Native command.
- **Expo:** Use a development build. Run `npx expo prebuild`, then build and run a development app with your normal Expo workflow.

## Quick Start

```typescript
import {
  getUpdateStatus,
  startImmediateUpdate,
  startFlexibleUpdate,
  openStorePage,
  canStartImmediateUpdate,
  canStartFlexibleUpdate,
  canOpenStorePage,
} from '@rnforge/react-native-in-app-updates'

const storeOptions = {
  ios: {
    appStoreId: '1234567890',
  },
}

const status = await getUpdateStatus(storeOptions)

if (canStartImmediateUpdate(status)) {
  await startImmediateUpdate()
} else if (canStartFlexibleUpdate(status)) {
  await startFlexibleUpdate()
} else if (canOpenStorePage(status)) {
  await openStorePage(storeOptions)
}
```

For flexible update progress tracking, listener cleanup, and a full React component example, see the [App Integration](https://rnforge.dev/docs/in-app-updates/app-integration) docs.

## Testing

```bash
bun run typecheck
bun run test
```

See [`TESTING.md`](./TESTING.md) for automated test commands and local verification.

Real Android update flows (immediate and flexible) require a Play-distributed build. Debug and sideload builds are useful for testing fallback UI and store page behavior only.

## License

MIT
