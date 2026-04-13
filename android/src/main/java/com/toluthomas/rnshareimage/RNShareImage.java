package com.toluthomas.rnshareimage;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.PixelCopy;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.uimanager.util.ReactFindViewUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RNShareImage extends NativeRNShareImageSpec {

    public static final String NAME = "RNShareImage";
    private static final String ERROR_NO_ACTIVITY = "E_NO_ACTIVITY";
    private static final String ERROR_VIEW_NOT_FOUND = "E_VIEW_NOT_FOUND";
    private static final String ERROR_SCREENSHOT_FAILED = "E_SCREENSHOT_FAILED";
    private static final String ERROR_DOWNLOAD_FAILED = "E_DOWNLOAD_FAILED";
    private static final String ERROR_SHARE_FAILED = "E_SHARE_FAILED";

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
    public void shareScreenshot(@Nullable String id, String message, String filename, String title, Promise promise) {
        Activity activity = getCurrentActivity();
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
                            bitmap.recycle();

                            Uri imageUri = getImageUri(imageFile, activity);

                            mainHandler.post(() -> {
                                try {
                                    shareImage(imageUri, message, title, activity);
                                    promise.resolve(null);
                                } catch (Exception e) {
                                    promise.reject(ERROR_SHARE_FAILED, "Failed to share: " + e.getMessage(), e);
                                }
                            });
                        } catch (Exception e) {
                            promise.reject(ERROR_SCREENSHOT_FAILED, "Failed to save screenshot: " + e.getMessage(), e);
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
        Activity activity = getCurrentActivity();
        if (activity == null) {
            promise.reject(ERROR_NO_ACTIVITY, "No activity available");
            return;
        }

        executor.execute(() -> {
            try {
                Uri uri;
                if (isRemoteUrl(imageUri)) {
                    File localFile = downloadImage(imageUri);
                    uri = getImageUri(localFile, activity);
                } else {
                    uri = Uri.parse(imageUri);
                }

                mainHandler.post(() -> {
                    try {
                        shareImage(uri, message, title, activity);
                        promise.resolve(null);
                    } catch (Exception e) {
                        promise.reject(ERROR_SHARE_FAILED, "Failed to share: " + e.getMessage(), e);
                    }
                });
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
        File imageFile = getTempImageFile(filename);
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
        }
        return imageFile;
    }

    private void shareImage(Uri imageUri, String message, String title, Activity activity) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/png");
        intent.putExtra(Intent.EXTRA_STREAM, imageUri);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activity.startActivity(Intent.createChooser(intent, title));
    }

    private boolean isRemoteUrl(String uri) {
        return uri != null && (uri.startsWith("http://") || uri.startsWith("https://"));
    }

    private File downloadImage(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);
        connection.setDoInput(true);
        connection.connect();

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP error code: " + responseCode);
        }

        String filename = getFilenameFromUrl(imageUrl);
        File outputFile = new File(getStorageDirectory(), filename);

        try (InputStream input = connection.getInputStream();
             FileOutputStream output = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
            output.flush();
        } finally {
            connection.disconnect();
        }

        return outputFile;
    }

    private String getFilenameFromUrl(String url) {
        String filename = url.substring(url.lastIndexOf('/') + 1);
        // Remove query parameters
        int queryIndex = filename.indexOf('?');
        if (queryIndex > 0) {
            filename = filename.substring(0, queryIndex);
        }
        // Ensure file has extension
        if (!filename.contains(".")) {
            filename = filename + ".png";
        }
        return filename;
    }

    @Nullable
    private View getRootView() {
        Activity activity = getCurrentActivity();
        if (activity == null) return null;
        return activity.getWindow().getDecorView().findViewById(android.R.id.content);
    }

    @Nullable
    private View getPartialView(String id) {
        View rootView = getRootView();
        if (rootView == null) return null;
        return ReactFindViewUtil.findView(rootView, id);
    }

    private File getStorageDirectory() {
        return reactContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    }

    private File getTempImageFile(String filename) throws IOException {
        return File.createTempFile(filename, ".png", getStorageDirectory());
    }

    private Uri getImageUri(File imageFile, Activity activity) {
        return FileProvider.getUriForFile(
            reactContext,
            activity.getPackageName() + ".provider",
            imageFile
        );
    }
}
