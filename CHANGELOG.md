# Changelog

All notable changes to this project are documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project follows [Semantic Versioning](https://semver.org/).

## [Unreleased]

## [2.0.1] - 2026-11-17

### 🐛 Fixes
- Android: replaced deprecated React Native APIs. `TurboReactPackage` is now `BaseReactPackage`, `getCurrentActivity()` is now `getReactApplicationContext().getCurrentActivity()`, and the `ReactModuleInfo` constructor no longer takes the `hasConstants` argument. React Native >= 0.74 is still supported.

### 📦 Other
- Tested with React Native 0.88.1.
- Android: `compileSdk` raised from 36 to 37.

## [2.0.0] - 2026-04-13

### 🆕 Features
- Full iOS support added
- Remote URL support for `shareImageFromUri()` - images from `http(s)://` URLs are automatically downloaded
- Promise-based API - all methods now return `Promise<void>`
- Object-based API - cleaner, more flexible method signatures

### 🔴 Breaking Changes
- Requires React Native >= 0.74.0 with New Architecture enabled. For projects using the old architecture, continue using v1.x.
- Methods now use options objects instead of positional arguments:
  ```typescript
  // Old
  shareScreenshot(null, 'message', 'filename', 'title');
  shareImageFromUri('uri', 'message', 'title');

  // New
  shareScreenshot({ message: 'message', filename: 'filename', shareTitle: 'title' });
  shareImageFromUri({ imageUri: 'uri', message: 'message', shareTitle: 'title' });
  ```
- Minimum iOS version: 15.1
- Minimum Android SDK: 24

### 📦 Other
- Added example project demonstrating all features

[Unreleased]: https://github.com/ToluThomas/react-native-share-image/compare/v2.0.1...HEAD
[2.0.1]: https://github.com/ToluThomas/react-native-share-image/compare/v2.0.0...v2.0.1
[2.0.0]: https://github.com/ToluThomas/react-native-share-image/releases/tag/v2.0.0
