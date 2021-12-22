
# react-native-share-image

## Getting started

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

#### If linking does not work still
1. Open up `android/app/src/main/java/[...]/MainActivity.java`
2. Add `import com.toluthomas.RNShareImagePackage;` to the imports at the top of the file
3. Add `new RNShareImagePackage()` to the list returned by the `getPackages()` method
4. Append the following lines to `android/settings.gradle`:
  	```groovy
  	include ':react-native-share-image'
  	project(':react-native-share-image').projectDir = new File(rootProject.projectDir,
	  '../node_modules/react-native-share-image/android')
  	```
5. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```groovy
      compile project(':react-native-share-image')
  	```

### iOS
Will be updated when iOS is supported ...

## Usage
```javascript
import {shareScreenshot, shareImageFromUri} from 'react-native-share-image';

// To take screenshot and share
shareScreenshot();

// To take screenshot of a specific view and share
shareScreenshot('nativeID');

// To share an image from a content uri
shareImageFromUri('imageURI')
```
  