import { NativeModules } from 'react-native';

  // Show warning if native module isn't detected
 if (!NativeModules.RNShareImage)
   console.warn("RNShareImage could not find native module. Please make sure native module is properly linked.");

   // Share screenshot (takes screenshot and opens share options)
export const shareScreenshot = () => NativeModules.RNShareImage.shareScreenshot();

// Share image from uri (content uri for Android)
export const shareImageFromUri = (uri) => NativeModules.RNShareImage.shareImageFromUri(uri);
