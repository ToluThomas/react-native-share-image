import { NativeModules } from "react-native";

declare const global: {
  __turboModuleProxy?: unknown;
};

const CONSTANTS = {
  DEFAULT_MESSAGE: "Screenshot",
  DEFAULT_SCREENSHOT_SHARE_TITLE: "Screenshot",
  DEFAULT_IMAGE_SHARE_TITLE: "Image",
};

// Use TurboModule if available, fallback to legacy bridge
const isTurboModuleEnabled = global.__turboModuleProxy != null;

// eslint-disable-next-line @typescript-eslint/no-var-requires
const RNShareImageModule = isTurboModuleEnabled
  ? require("./NativeRNShareImage").default
  : NativeModules.RNShareImage;

const MODULE_NOT_FOUND_ERROR = new Error(
  "RNShareImage native module not found. Please make sure native module is properly linked.",
);

const getModule = (): typeof RNShareImageModule => {
  if (!RNShareImageModule) {
    throw MODULE_NOT_FOUND_ERROR;
  }
  return RNShareImageModule;
};

/**
 * Share screenshot of current app screen or specify a view to get screenshot of
 * @param id - ID for a View which is set using the nativeID prop for Views
 * @see https://reactnative.dev/docs/view#nativeid for how to set nativeID
 * @param message - Message to be shown during share
 * @param filename - name of temporary screenshot file
 * @param shareTitle - title of share modal
 * @returns Promise that resolves when share sheet is opened, rejects on error
 */
export const shareScreenshot = (
  id: string | null = null,
  message: string = CONSTANTS.DEFAULT_MESSAGE,
  filename: string = Date.now().toString(),
  shareTitle: string = CONSTANTS.DEFAULT_SCREENSHOT_SHARE_TITLE,
): Promise<void> => {
  try {
    return getModule().shareScreenshot(id, message, filename, shareTitle);
  } catch (e) {
    return Promise.reject(e);
  }
};

/**
 * Share image using uri of an image (supports both local and remote URLs)
 * @param imageUri - Local content:// URI or remote http(s):// URL
 * @param message - Message to be shown during share
 * @param shareTitle - title of share modal
 * @returns Promise that resolves when share sheet is opened, rejects on error
 */
export const shareImageFromUri = (
  imageUri: string,
  message: string = CONSTANTS.DEFAULT_MESSAGE,
  shareTitle: string = CONSTANTS.DEFAULT_IMAGE_SHARE_TITLE,
): Promise<void> => {
  try {
    return getModule().shareImageFromUri(imageUri, message, shareTitle);
  } catch (e) {
    return Promise.reject(e);
  }
};

export default {
  shareScreenshot,
  shareImageFromUri,
};
