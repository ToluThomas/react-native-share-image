# react-native-share-image

React Native library to capture and share screenshots, as well as share images from URIs (local or remote).

## Table of Contents

- [Requirements](#requirements)
- [Installation](#installation)
- [API](#api)
- [Migration from v1.x](#migration-from-v1x)

## Requirements

- React Native >= 0.74.0
- iOS 15.1+
- Android SDK 24+

> **Note:** Version 2.x requires React Native New Architecture. For projects using the old architecture, use version 1.x.

## Installation

```sh
npm install react-native-share-image
# or
yarn add react-native-share-image
```

### iOS

```sh
cd ios && pod install
```

### Android

1. Create `file_paths.xml` at `android/app/src/main/res/xml/file_paths.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <external-files-path name="images" path="Pictures/" />
    <cache-path name="cache" path="/" />
</paths>
```

2. Add a `<provider>` to your `AndroidManifest.xml` inside the `<application>` tag:

```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.provider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

## API

| Method                                    | Return Type     | iOS | Android |
| ----------------------------------------- | --------------- | :-: | :-----: |
| [shareScreenshot()](#sharescreenshot)     | `Promise<void>` | Yes |   Yes   |
| [shareImageFromUri()](#shareimagefromuri) | `Promise<void>` | Yes |   Yes   |

---

### shareScreenshot()

Capture and share a screenshot of the entire screen or a specific view.

```typescript
import { shareScreenshot } from 'react-native-share-image';

// Share entire screen
await shareScreenshot();

// Share a specific view (requires nativeID prop on the View)
await shareScreenshot('myViewId');

// With custom message
await shareScreenshot(null, 'Check this out!');

// With custom filename
await shareScreenshot(null, 'Screenshot', 'my-screenshot');

// With custom share title
await shareScreenshot(null, 'Screenshot', 'filename', 'Share via');
```

#### Parameters

| Parameter    | Type             | Default          | Description                                                                                                    |
| ------------ | ---------------- | ---------------- | -------------------------------------------------------------------------------------------------------------- |
| `id`         | `string \| null` | `null`           | The `nativeID` of the View to capture. Pass `null` for full screen. See [nativeID docs](https://reactnative.dev/docs/view#nativeid) |
| `message`    | `string`         | `"Screenshot"`   | Message shown during share                                                                                     |
| `filename`   | `string`         | Current timestamp| Name of the temporary screenshot file                                                                          |
| `shareTitle` | `string`         | `"Screenshot"`   | Title of the share modal                                                                                       |

---

### shareImageFromUri()

Share an image from a local file path or remote URL.

```typescript
import { shareImageFromUri } from 'react-native-share-image';

// Share from local URI
await shareImageFromUri('file:///path/to/image.png');

// Share from remote URL (image will be downloaded first)
await shareImageFromUri('https://example.com/image.png');

// With custom message
await shareImageFromUri('https://example.com/image.png', 'Check this out!');

// With custom share title
await shareImageFromUri('https://example.com/image.png', 'Image', 'Share via');
```

#### Parameters

| Parameter    | Type     | Default    | Description                                           |
| ------------ | -------- | ---------- | ----------------------------------------------------- |
| `imageUri`   | `string` | (required) | Local `file://` or `content://` URI, or remote `http(s)://` URL |
| `message`    | `string` | `"Screenshot"` | Message shown during share                        |
| `shareTitle` | `string` | `"Image"`  | Title of the share modal                              |

---

## Migration from v1.x

Version 2.0 introduces several breaking changes:

1. **New Architecture Required**: v2.x only supports React Native's New Architecture (TurboModules/Fabric). Projects using the old architecture should continue using v1.x.

2. **Promise-based API**: All methods now return Promises instead of using callbacks.

   ```typescript
   // v1.x (callback style - no longer supported)
   shareScreenshot();

   // v2.x (Promise style)
   await shareScreenshot();
   // or
   shareScreenshot().then(() => console.log('Shared!')).catch(console.error);
   ```

3. **iOS Support**: Full iOS support has been added in v2.x.

4. **Remote URL Support**: `shareImageFromUri()` now supports remote `http(s)://` URLs - images are automatically downloaded before sharing.

5. **Minimum Requirements**:
   - React Native >= 0.74.0
   - iOS 15.1+
   - Android SDK 24+

## License

MIT
