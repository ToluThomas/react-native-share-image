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

if (!RNShareImageModule) {
  // eslint-disable-next-line no-console
  console.warn(
    "RNShareImage could not find native module. Please make sure native module is properly linked.",
  );
}

/**
 * Share screenshot of current app screen or specify a view to get screenshot of
 * @param id - ID for a View which is set using the nativeID prop for Views
 * @see https://reactnative.dev/docs/view#nativeid for how to set nativeID
 * @param message - Message to be shown during share
 * @param filename - name of temporary screenshot file
 * @param shareTitle - title of share modal
 */
export const shareScreenshot = (
  id: string | null = null,
  message: string = CONSTANTS.DEFAULT_MESSAGE,
  filename: string = Date.now().toString(),
  shareTitle: string = CONSTANTS.DEFAULT_SCREENSHOT_SHARE_TITLE,
): void => {
  RNShareImageModule?.shareScreenshot(id, message, filename, shareTitle);
};

/**
 * Share image using uri of an image
 * @param imageUri - Specify a content uri for image to be shared
 * @param message - Message to be shown during share
 * @param shareTitle - title of share modal
 */
export const shareImageFromUri = (
  imageUri: string,
  message: string = CONSTANTS.DEFAULT_MESSAGE,
  shareTitle: string = CONSTANTS.DEFAULT_IMAGE_SHARE_TITLE,
): void => {
  RNShareImageModule?.shareImageFromUri(imageUri, message, shareTitle);
};

export default {
  shareScreenshot,
  shareImageFromUri,
};
