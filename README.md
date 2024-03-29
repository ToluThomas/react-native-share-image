
# react-native-share-image

## Table of Contents

- [Getting Started](#Installation)
- [API](#API)

## Installation

`$ npm install react-native-share-image --save`

### Android

1. Create `filepaths.xml` at `android/app/src/main/res/xml/filepaths.xml` (if it does not already exist)
2. Add below to filepaths.xml:
	```xml
	<?xml version="1.0" encoding="utf-8"?>
	<paths xmlns:android="http://schemas.android.com/apk/res/android">
		<external-path name="myexternalimages" path="Download/" />
		<root-path name="root" path="." />
		<external-path name="external_files" path="."/>
		<external-files-path name="external_files" path="." />
	</paths>
	```
3. Navigate to your app `AndroidManifest.xml` (check `android/app/src/main/AndroidManifest.xml`) and add `<provider>` like below:
	```xml
	<application
        android:name=".MainApplication"
        android:label="@string/app_name"
        android:icon="@mipmap/ic_launcher"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:theme="@style/AppTheme">

        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="com.yourAppID.provider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/filepaths"/>
        </provider>
	```
4. If React Native version >= 0.60 you're good to go! **react-native-share-image** is auto-linked and set up!

#### If package is not auto-linked
`$ react-native link react-native-share-image`

### iOS
Will be updated when iOS is supported ...

## API
| Method                                                            | Return Type   |  iOS | Android |
| ----------------------------------------------------------------- | ------------- | :--: | :-----: |
| [shareScreenshot()](#shareScreenshot)                             | `void`        |  ❌  |   ✅    |
| [shareImageFromUri()](#shareImageFromUri)                         | `void`        |  ❌  |   ✅    |
---

### shareScreenshot()
```javascript
import {shareScreenshot} from 'react-native-share-image';

// To take screenshot and share
shareScreenshot(); // Shares whole screen
shareScreenshot('nativeID'); // Shares view with nativeID
shareScreenshot(null, 'customMessage'); // Shares screenshot with a message that shows when user shares image
shareScreenshot(null, null, 'filename'); // Shares screenshot with a specific filename
shareScreenshot(null, null, null, 'customTitle'); // Shares screenshot with a specific title
```
---

### shareImageFromUri()
```javascript
import {shareImageFromUri} from 'react-native-share-image';

// To share image URI
shareImageFromUri('imageURI'); // Shares image of specified imageURI
shareImageFromUri('imageURI', 'customMessage'); // Shares image with a message that shows when user shares image
shareImageFromUri('imageURI', null, 'customTitle'); // Shares image with a specific title
```
---