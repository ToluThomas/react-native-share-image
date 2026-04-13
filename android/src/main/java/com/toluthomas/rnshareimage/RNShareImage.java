package com.toluthomas.rnshareimage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Environment;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.uimanager.util.ReactFindViewUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class RNShareImage extends NativeRNShareImageSpec {

    public static final String NAME = "RNShareImage";
    private final ReactApplicationContext reactContext;

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
    @ReactMethod
    public void shareScreenshot(@Nullable String id, String subject, String filename, String title) {
        try {
            Intent intent = getIntent(subject);
            View view = id != null ? getPartialView(id) : this.getRootView();
            File tempImageFile = getTempImageFile(filename);
            File imageFile = getImageFile(tempImageFile, view);
            Uri imageUri = getImageUri(imageFile);
            intent.putExtra(Intent.EXTRA_STREAM, imageUri);
            Objects.requireNonNull(getCurrentActivity()).startActivity(Intent.createChooser(intent, title));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @ReactMethod
    public void shareImageFromUri(String imageUri, String message, String title) {
        new Thread(() -> {
            try {
                Uri uri;
                if (isRemoteUrl(imageUri)) {
                    File localFile = downloadImage(imageUri);
                    uri = getImageUri(localFile);
                } else {
                    uri = Uri.parse(imageUri);
                }

                Intent intent = getIntent(message);
                intent.putExtra(Intent.EXTRA_STREAM, uri);

                Objects.requireNonNull(getCurrentActivity()).runOnUiThread(() -> {
                    getCurrentActivity().startActivity(Intent.createChooser(intent, title));
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private boolean isRemoteUrl(String uri) {
        return uri != null && (uri.startsWith("http://") || uri.startsWith("https://"));
    }

    private File downloadImage(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.connect();

        String filename = getFilenameFromUrl(imageUrl);
        File outputFile = new File(getStorageDirectory(), filename);

        try (InputStream input = connection.getInputStream();
             FileOutputStream output = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
            output.flush();
        }

        return outputFile;
    }

    private String getFilenameFromUrl(String url) {
        String filename = url.substring(url.lastIndexOf('/') + 1);
        if (!filename.contains(".")) {
            filename = filename + ".png";
        }
        // Remove query parameters if present
        if (filename.contains("?")) {
            filename = filename.substring(0, filename.indexOf("?"));
        }
        return filename;
    }

    private Intent getIntent(String message) {
        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_TEXT, message);
        intent.setAction(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setType("image/png");
        return intent;
    }

    private File getImageFile(File imageFile, View view) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
        getScreenshotBitmap(view).compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
        fileOutputStream.flush();
        fileOutputStream.close();
        return imageFile;
    }

    private View getRootView() {
        return Objects.requireNonNull(getCurrentActivity()).getWindow().getDecorView()
                .findViewById(android.R.id.content);
    }

    private View getPartialView(String id) {
        return ReactFindViewUtil.findView(getRootView(), id);
    }

    private Bitmap getScreenshotBitmap(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.layout(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        view.draw(canvas);
        return bitmap;
    }

    private File getStorageDirectory() {
        return reactContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    }

    private File getTempImageFile(String filename) throws IOException {
        return File.createTempFile(filename, ".png", getStorageDirectory());
    }

    private Uri getImageUri(File imageFile) {
        return FileProvider.getUriForFile(reactContext,
                Objects.requireNonNull(getCurrentActivity()).getPackageName() + ".provider", imageFile);
    }
}
