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

/**
 * Share screenshot of current app screen or specify a view to get screenshot of
 * @param options - Screenshot options (all optional)
 * @returns Promise that resolves when share sheet is opened, rejects on error
 * @see https://reactnative.dev/docs/view#nativeid for how to set nativeID
 */
export declare function shareScreenshot(
  options?: ShareScreenshotOptions
): Promise<void>;

/**
 * Share image using uri of an image (supports both local and remote URLs)
 * @param options - Image sharing options (imageUri is required)
 * @returns Promise that resolves when share sheet is opened, rejects on error
 */
export declare function shareImageFromUri(
  options: ShareImageFromUriOptions
): Promise<void>;

declare const _default: {
  shareScreenshot: typeof shareScreenshot;
  shareImageFromUri: typeof shareImageFromUri;
};

export default _default;
