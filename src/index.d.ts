/**
 * Share screenshot of current app screen or specify a view to get screenshot of
 * @param id - ID for a View which is set using the nativeID prop for Views
 * @see https://reactnative.dev/docs/view#nativeid for how to set nativeID
 * @param message - Message to be shown during share
 * @param filename - name of temporary screenshot file
 * @param shareTitle - title of share modal
 */
export declare function shareScreenshot(
  id?: string | null,
  message?: string,
  filename?: string,
  shareTitle?: string
): void;

/**
 * Share image using uri of an image
 * @param imageUri - Specify a content uri for image to be shared
 * @param message - Message to be shown during share
 * @param shareTitle - title of share modal
 */
export declare function shareImageFromUri(
  imageUri: string,
  message?: string,
  shareTitle?: string
): void;

declare const _default: {
  shareScreenshot: typeof shareScreenshot;
  shareImageFromUri: typeof shareImageFromUri;
};

export default _default;
