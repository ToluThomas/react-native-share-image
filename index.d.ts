declare module 'react-native-share-image' {
    /**
     * Share screenshot of current app screen or specify a view to get screenshot of
     * @param {string} message - Message to be shown during share
     * @param {string} filename - name of temporary screenshot file
     * @param {string} id - ID for a View which is set using the nativeID prop for Views
     * @link See https://reactnative.dev/docs/view#nativeid for how to set nativeID
     */
    export function shareScreenshot(): void

    /**
     * Share image using uri of an image
     * @param {string} imageUri - Specify a content uri for image to be shared
     * @param {string} message - Message to be shown during share
     */
    export function shareImageFromUri(): void
}