# Changelog

All notable changes to this project are documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project follows [Semantic Versioning](https://semver.org/).

## [Unreleased]

### 🐛 Fixes
- Android: `shareImageFromUri()` now works with local `file://` URIs. Previously Android 7+ refused them and the promise rejected with `E_SHARE_FAILED`.
- Android: shared images now carry their real type (JPEG, WebP, …) instead of always `image/png`.
- Android: the `filename` option is now used exactly as given (unsafe characters are replaced).
- Android: screenshots and downloaded images are stored in the app's cache and old ones are removed, instead of piling up in app storage.

### 📦 Other
- Android: no setup is needed any more. The library now declares its own `FileProvider` with a unique authority, so it can't conflict with other providers. If you added the `FileProvider` and `file_paths.xml` from earlier versions only for this library, you can remove them.

## [2.0.2] - 2026-11-17

### 📦 Other
- The npm package now contains only the library files, shrinking it from 143 kB to 12 kB.
- Added the MIT `LICENSE` file to the package.

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

[Unreleased]: https://github.com/ToluThomas/react-native-share-image/compare/v2.0.2...HEAD
[2.0.2]: https://github.com/ToluThomas/react-native-share-image/compare/v2.0.1...v2.0.2
[2.0.1]: https://github.com/ToluThomas/react-native-share-image/compare/v2.0.0...v2.0.1
[2.0.0]: https://github.com/ToluThomas/react-native-share-image/releases/tag/v2.0.0
