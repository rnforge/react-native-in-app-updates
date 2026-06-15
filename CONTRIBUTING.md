# Contributing to RNForge In-App Updates

Thanks for improving `@rnforge/react-native-in-app-updates`.

## Setup

- [bun](https://bun.sh) is required.
- Install dependencies: `bun install`
- Run tests: `bun run test`
- Build package: `bun run build`

## Before opening a PR

- `bun run typecheck` — TypeScript passes
- `bun run test` — tests pass
- `bun run build` — package builds
- If you changed exported APIs or public TSDoc, also run `bun run check:api-report`
- If you changed behavior, update the README or package docs
- If you changed release or publish config, mention it in the PR description

## Issues

- Use the **Bug report** template for runtime or package bugs.
- Use the **Docs feedback** template for README or package docs issues.

## License

By contributing, you agree that your contributions will be licensed under the MIT License.
