package com.toluthomas.rnshareimage;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.PixelCopy;
import android.view.View;
import android.view.Window;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.util.ReactFindViewUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RNShareImage extends NativeRNShareImageSpec {

    public static final String NAME = "RNShareImage";
    private static final String ERROR_NO_ACTIVITY = "E_NO_ACTIVITY";
    private static final String ERROR_VIEW_NOT_FOUND = "E_VIEW_NOT_FOUND";
    private static final String ERROR_SCREENSHOT_FAILED = "E_SCREENSHOT_FAILED";
    private static final String ERROR_DOWNLOAD_FAILED = "E_DOWNLOAD_FAILED";
    private static final String ERROR_SHARE_FAILED = "E_SHARE_FAILED";

    /** Folder inside the app cache that the library's FileProvider serves. */
    private static final String SHARE_DIR = "rnshareimage";
    /** Shared files older than this are removed before a new one is written. */
    private static final long MAX_FILE_AGE_MS = 24 * 60 * 60 * 1000L;
    private static final String FALLBACK_MIME_TYPE = "image/*";
    /** Matches the authority declared in the library's AndroidManifest.xml. */
    static final String FILE_PROVIDER_AUTHORITY_SUFFIX = ".rnshareimage.fileprovider";

    private final ReactApplicationContext reactContext;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    RNShareImage(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }

    @Override
    public void invalidate() {
        executor.shutdownNow();
        super.invalidate();
    }

    @Override
    public void shareScreenshot(@Nullable String id, String message, String filename, String title, Promise promise) {
        Activity activity = reactContext.getCurrentActivity();
        if (activity == null) {
            promise.reject(ERROR_NO_ACTIVITY, "No activity available");
            return;
        }

        mainHandler.post(() -> {
            try {
                View view = id != null ? getPartialView(id) : getRootView();
                if (view == null) {
                    promise.reject(ERROR_VIEW_NOT_FOUND, "View not found" + (id != null ? " with id: " + id : ""));
                    return;
                }

                captureView(view, activity, bitmap -> {
                    executor.execute(() -> {
                        try {
                            File imageFile = saveBitmap(bitmap, filename);
                            Uri imageUri = getShareableUri(imageFile);
                            mainHandler.post(() -> shareImage(imageUri, "image/png", message, title, activity, promise));
                        } catch (Exception e) {
                            promise.reject(ERROR_SCREENSHOT_FAILED, "Failed to save screenshot: " + e.getMessage(), e);
                        } finally {
                            bitmap.recycle();
                        }
                    });
                }, error -> promise.reject(ERROR_SCREENSHOT_FAILED, error));
            } catch (Exception e) {
                promise.reject(ERROR_SCREENSHOT_FAILED, "Failed to capture screenshot: " + e.getMessage(), e);
            }
        });
    }

    @Override
    public void shareImageFromUri(String imageUri, String message, String title, Promise promise) {
        Activity activity = reactContext.getCurrentActivity();
        if (activity == null) {
            promise.reject(ERROR_NO_ACTIVITY, "No activity available");
            return;
        }

        executor.execute(() -> {
            try {
                Uri uri;
                String mimeType;
                if (isRemoteUrl(imageUri)) {
                    File localFile = downloadImage(imageUri);
                    uri = getShareableUri(localFile);
                    mimeType = getMimeTypeFromName(localFile.getName());
                } else if (isLocalFile(imageUri)) {
                    // file:// URIs can't be passed to other apps (FileUriExposedException on
                    // Android 7+), so copy the file into the shared folder and share a content:// URI.
                    File localFile = copyToShareDir(getLocalFile(imageUri));
                    uri = getShareableUri(localFile);
                    mimeType = getMimeTypeFromName(localFile.getName());
                } else {
                    uri = Uri.parse(imageUri);
                    mimeType = getMimeType(uri);
                }

                mainHandler.post(() -> shareImage(uri, mimeType, message, title, activity, promise));
            } catch (Exception e) {
                promise.reject(ERROR_DOWNLOAD_FAILED, "Failed to process image: " + e.getMessage(), e);
            }
        });
    }

    private interface CaptureCallback {
        void onSuccess(Bitmap bitmap);
    }

    private interface ErrorCallback {
        void onError(String message);
    }

    private void captureView(View view, Activity activity, CaptureCallback onSuccess, ErrorCallback onError) {
        int width = view.getWidth();
        int height = view.getHeight();

        if (width <= 0 || height <= 0) {
            onError.onError("View has invalid dimensions");
            return;
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Use PixelCopy for API 26+ (captures hardware-accelerated content)
            int[] location = new int[2];
            view.getLocationInWindow(location);

            Rect rect = new Rect(
                location[0],
                location[1],
                location[0] + width,
                location[1] + height
            );

            Window window = activity.getWindow();
            try {
                PixelCopy.request(window, rect, bitmap, result -> {
                    if (result == PixelCopy.SUCCESS) {
                        onSuccess.onSuccess(bitmap);
                    } else {
                        bitmap.recycle();
                        // Fallback to canvas method on PixelCopy failure
                        Bitmap fallbackBitmap = captureViewWithCanvas(view);
                        if (fallbackBitmap != null) {
                            onSuccess.onSuccess(fallbackBitmap);
                        } else {
                            onError.onError("PixelCopy failed with code: " + result);
                        }
                    }
                }, mainHandler);
            } catch (IllegalArgumentException e) {
                bitmap.recycle();
                // Fallback to canvas method if PixelCopy throws
                Bitmap fallbackBitmap = captureViewWithCanvas(view);
                if (fallbackBitmap != null) {
                    onSuccess.onSuccess(fallbackBitmap);
                } else {
                    onError.onError("PixelCopy not supported: " + e.getMessage());
                }
            }
        } else {
            // Fallback for older APIs
            bitmap.recycle();
            Bitmap canvasBitmap = captureViewWithCanvas(view);
            if (canvasBitmap != null) {
                onSuccess.onSuccess(canvasBitmap);
            } else {
                onError.onError("Failed to capture view with canvas");
            }
        }
    }

    @Nullable
    private Bitmap captureViewWithCanvas(View view) {
        try {
            Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    private File saveBitmap(Bitmap bitmap, String filename) throws IOException {
        File imageFile = new File(getShareDir(), sanitizeFilename(filename, "screenshot") + ".png");
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
        }
        return imageFile;
    }

    private void shareImage(Uri imageUri, String mimeType, String message, String title, Activity activity, Promise promise) {
        try {
            // ShareCompat puts the stream in ClipData too, so the read grant reaches the target app.
            Intent chooser = new ShareCompat.IntentBuilder(activity)
                .setType(mimeType)
                .setStream(imageUri)
                .setText(message)
                .setChooserTitle(title)
                .createChooserIntent()
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            activity.startActivity(chooser);
            promise.resolve(null);
        } catch (Exception e) {
            promise.reject(ERROR_SHARE_FAILED, "Failed to share: " + e.getMessage(), e);
        }
    }

    private boolean isRemoteUrl(String uri) {
        return uri != null && (uri.startsWith("http://") || uri.startsWith("https://"));
    }

    private boolean isLocalFile(String uri) {
        return uri != null && (uri.startsWith("file://") || uri.startsWith("/"));
    }

    private File getLocalFile(String uri) throws IOException {
        String path = uri.startsWith("file://") ? Uri.parse(uri).getPath() : uri;
        if (path == null) {
            throw new IOException("Invalid file URI: " + uri);
        }
        File file = new File(path);
        if (!file.isFile()) {
            throw new IOException("File not found: " + path);
        }
        return file;
    }

    private File copyToShareDir(File source) throws IOException {
        File target = new File(getShareDir(), sanitizeFilename(source.getName(), "image"));
        try (InputStream input = new FileInputStream(source);
             OutputStream output = new FileOutputStream(target)) {
            copy(input, output);
        }
        return target;
    }

    private File downloadImage(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);
        connection.setDoInput(true);

        try {
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode);
            }

            File outputFile = new File(getShareDir(), getFilenameFromUrl(imageUrl, connection.getContentType()));
            try (InputStream input = connection.getInputStream();
                 OutputStream output = new FileOutputStream(outputFile)) {
                copy(input, output);
            }
            return outputFile;
        } finally {
            connection.disconnect();
        }
    }

    private static void copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        output.flush();
    }

    static String getFilenameFromUrl(String url, @Nullable String contentType) {
        String filename = url.substring(url.lastIndexOf('/') + 1);
        int queryIndex = filename.indexOf('?');
        if (queryIndex >= 0) {
            filename = filename.substring(0, queryIndex);
        }
        int fragmentIndex = filename.indexOf('#');
        if (fragmentIndex >= 0) {
            filename = filename.substring(0, fragmentIndex);
        }
        filename = sanitizeFilename(filename, "image");
        if (!filename.contains(".")) {
            String extension = contentType != null
                ? MimeTypeMap.getSingleton().getExtensionFromMimeType(contentType.split(";")[0].trim())
                : null;
            filename = filename + "." + (extension != null ? extension : "png");
        }
        return filename;
    }

    /** Keeps letters, digits, dots, dashes and underscores, so a name can't point outside the folder. */
    static String sanitizeFilename(@Nullable String name, String fallback) {
        String cleaned = name == null ? "" : name.replaceAll("[^A-Za-z0-9._-]", "_").replaceAll("^\\.+", "");
        return cleaned.isEmpty() ? fallback : cleaned;
    }

    private String getMimeType(Uri uri) {
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            String type = reactContext.getContentResolver().getType(uri);
            if (type != null) {
                return type;
            }
        }
        String lastSegment = uri.getLastPathSegment();
        return lastSegment != null ? getMimeTypeFromName(lastSegment) : FALLBACK_MIME_TYPE;
    }

    static String getMimeTypeFromName(String name) {
        int dot = name.lastIndexOf('.');
        if (dot >= 0) {
            String extension = name.substring(dot + 1).toLowerCase(Locale.ROOT);
            String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (type != null) {
                return type;
            }
        }
        return FALLBACK_MIME_TYPE;
    }

    @Nullable
    private View getRootView() {
        Activity activity = reactContext.getCurrentActivity();
        if (activity == null) return null;
        return activity.getWindow().getDecorView().findViewById(android.R.id.content);
    }

    @Nullable
    private View getPartialView(String id) {
        View rootView = getRootView();
        if (rootView == null) return null;
        return ReactFindViewUtil.findView(rootView, id);
    }

    /** The cache folder the library's FileProvider serves. Old files are removed on each call. */
    private File getShareDir() throws IOException {
        File dir = new File(reactContext.getCacheDir(), SHARE_DIR);
        if (!dir.isDirectory() && !dir.mkdirs()) {
            throw new IOException("Could not create " + dir);
        }
        File[] files = dir.listFiles();
        if (files != null) {
            long cutoff = System.currentTimeMillis() - MAX_FILE_AGE_MS;
            for (File file : files) {
                if (file.lastModified() < cutoff) {
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
                }
            }
        }
        return dir;
    }

    private Uri getShareableUri(File file) {
        return FileProvider.getUriForFile(
            reactContext,
            reactContext.getPackageName() + FILE_PROVIDER_AUTHORITY_SUFFIX,
            file
        );
    }
}
