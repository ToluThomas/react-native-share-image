import { NativeModules } from "react-native";

declare const global: {
  __turboModuleProxy?: unknown;
};

const DEFAULTS = {
  MESSAGE: "Screenshot",
  SCREENSHOT_SHARE_TITLE: "Screenshot",
  IMAGE_SHARE_TITLE: "Image",
};

/**
 * Options for shareScreenshot()
 */
export type ShareScreenshotOptions = {
  /** ID for a View set using the nativeID prop. Pass null or omit for full screen. */
  id?: string | null;
  /** Message to be shown during share */
  message?: string;
  /** Name of temporary screenshot file */
  filename?: string;
  /** Title of share modal */
  shareTitle?: string;
};

/**
 * Options for shareImageFromUri()
 */
export type ShareImageFromUriOptions = {
  /** Local file:// or content:// URI, or remote http(s):// URL (required) */
  imageUri: string;
  /** Message to be shown during share */
  message?: string;
  /** Title of share modal */
  shareTitle?: string;
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
 * @param options - Screenshot options (all optional)
 * @returns Promise that resolves when share sheet is opened, rejects on error
 * @see https://reactnative.dev/docs/view#nativeid for how to set nativeID
 */
export const shareScreenshot = (
  options?: ShareScreenshotOptions,
): Promise<void> => {
  const {
    id = null,
    message = DEFAULTS.MESSAGE,
    filename = Date.now().toString(),
    shareTitle = DEFAULTS.SCREENSHOT_SHARE_TITLE,
  } = options ?? {};

  try {
    return getModule().shareScreenshot(id, message, filename, shareTitle);
  } catch (e) {
    return Promise.reject(e);
  }
};

/**
 * Share image using uri of an image (supports both local and remote URLs)
 * @param options - Image sharing options (imageUri is required)
 * @returns Promise that resolves when share sheet is opened, rejects on error
 */
export const shareImageFromUri = (
  options: ShareImageFromUriOptions,
): Promise<void> => {
  const {
    imageUri,
    message = DEFAULTS.MESSAGE,
    shareTitle = DEFAULTS.IMAGE_SHARE_TITLE,
  } = options;

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
