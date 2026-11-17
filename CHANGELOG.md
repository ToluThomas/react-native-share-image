# Changelog

All notable changes to this project are documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project follows [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Fixed

- Android: replaced deprecated React Native APIs. `TurboReactPackage` is now `BaseReactPackage`, `getCurrentActivity()` is now `getReactApplicationContext().getCurrentActivity()`, and the `ReactModuleInfo` constructor no longer takes the `hasConstants` argument. React Native >= 0.74 is still supported.

### Changed

- Android: `compileSdk` raised from 36 to 37.
- Development now runs against React Native 0.88.1.
- Example app upgraded from React Native 0.85.0 to 0.88.1.
- CI now tests on Node 22 and 24, and publishing runs on Node 24.

## [2.0.0] - 2026-04-13

### Features

- Full iOS support.
- `shareImageFromUri()` accepts remote `http(s)` URLs and downloads the image automatically.
- Promise-based API: every method returns `Promise<void>`.
- Methods take an options object instead of positional arguments.

### Breaking Changes

- Requires React Native >= 0.74.0 with the New Architecture enabled. Use v1.x for the old architecture.
- `shareScreenshot()` and `shareImageFromUri()` now take an options object instead of positional arguments. See the README for the new signatures.
- Minimum iOS version is 15.1.
- Minimum Android SDK is 24.

### Other

- Added an example project that demonstrates every feature.

[Unreleased]: https://github.com/ToluThomas/react-native-share-image/compare/v2.0.0...HEAD
[2.0.0]: https://github.com/ToluThomas/react-native-share-image/releases/tag/v2.0.0
