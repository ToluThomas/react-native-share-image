
# react-native-share-image

## Getting started

`$ npm install react-native-share-image --save`

#### Android

1. Create filepaths.xml at yourProject/android/app/src/main/res/xml/filepaths.xml (if it does not already exist)
2. Add below to filepaths.xml:
	```
	<?xml version="1.0" encoding="utf-8"?>
	<paths xmlns:android="http://schemas.android.com/apk/res/android">
		<external-path name="myexternalimages" path="Download/" />
		<root-path name="root" path="." />
		<external-path name="external_files" path="."/>
		<external-files-path name="external_files" path="." />
	</paths>
	```
3. Navigate to your app AndroidManifest.xml (likely in yourProject/android/app/src/main/AndroidManifest.xml) and add ```<provider>``` like below:
	```
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
4. If React Native version >= 0.60 you're good to go. react-native-share-image is auto-linked and set up.

##### If package is not auto-linked
`$ react-native link react-native-share-image`

##### If linking does not work still
1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNShareImagePackage;` to the imports at the top of the file
  - Add `new RNShareImagePackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-share-image'
  	project(':react-native-share-image').projectDir = new File(rootProject.projectDir,
	  '../node_modules/react-native-share-image/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-share-image')
  	```

#### iOS
Will be updated when iOS is supported ...

## Usage
```javascript
import {shareScreenshot, shareImageFromUri} from 'react-native-share-image';

// To take screenshot and share
shareScreenshot('message', 'filename');

// To take screenshot of a specific view and share
shareScreenshot('message', 'filename', 'IDofView');

// To share an image from a content uri
shareImageFromUri('someStringUri', 'message')
```
  