
# react-native-share-image

## Getting started

`$ npm install react-native-share-image --save`

### Mostly automatic installation

`$ react-native link react-native-share-image`

### Manual installation


#### iOS
Will be updated when iOS is supported ...

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNShareImagePackage;` to the imports at the top of the file
  - Add `new RNShareImagePackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-share-image'
  	project(':react-native-share-image').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-share-image/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-share-image')
  	```


## Usage
```javascript
import {shareScreenshot, shareImageFromUri} from 'react-native-share-image';

// To take screenshot and share
shareScreenshot();

// To share an image from a content uri
shareImageFromUri(someStringUri)
```
  