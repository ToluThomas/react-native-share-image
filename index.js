import {NativeModules} from 'react-native';
import CONSTANTS from './utils/constants'
const {DEFAULT_MESSAGE} = CONSTANTS;

// const DEFAULT_MESSAGE = "I'd like to share this screenshot with you";

// Show warning if native module isn't detected
if (!NativeModules.RNShareImage) {
  console.warn(
    'RNShareImage could not find native module. Please make sure native module is properly linked.',
  );
}

/**
 * Share screenshot of current app screen or specify a view to get screenshot of
 * @param {string} id - ID for a View which is set using the nativeID prop for Views
 * @link See https://reactnative.dev/docs/view#nativeid for how to set nativeID
 * @param {string} message - Message to be shown during share
 * @param {string} filename - name of temporary screenshot file
 */
export const shareScreenshot = (
  id = null,
  message = DEFAULT_MESSAGE,
  filename = new Date().getTime().toString()
) => NativeModules.RNShareImage.shareScreenshot(id, message, filename);

/**
 * Share image using uri of an image
 * @param {string} imageUri - Specify a content uri for image to be shared
 * @param {string} message - Message to be shown during share
 */
export const shareImageFromUri = (imageUri, message = DEFAULT_MESSAGE) =>
  NativeModules.RNShareImage.shareImageFromUri(imageUri, message);
