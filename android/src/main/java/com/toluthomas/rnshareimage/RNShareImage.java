package com.toluthomas.rnshareimage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.view.View;

import androidx.core.content.FileProvider;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.uimanager.util.ReactFindViewUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import javax.annotation.Nonnull;

public class RNShareImage extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    RNShareImage(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Nonnull
    @Override
    public String getName() {
        return "RNShareImage";
    }

    // Take screenshot and share
    @ReactMethod
    public void shareScreenshot(String subject, String filename, String id) {
        Intent intent = getIntent(subject);
        try {
            File imageFile = getScreenshotFile(getTempImageFile(filename), id != null ? getPartialView(id) : this.getRootView());
            Uri imageUri = getImageUri(imageFile);
            intent.putExtra(Intent.EXTRA_STREAM, imageUri);
            Objects.requireNonNull(getCurrentActivity()).startActivity(Intent.createChooser(intent, "Share screenshot via"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Share image from content Uri
    @ReactMethod
    public void shareImageFromUri(String imageUri, String subject){
        Intent intent = getIntent(subject);
        intent.putExtra(Intent.EXTRA_STREAM, imageUri);
        Objects.requireNonNull(getCurrentActivity()).startActivity(Intent.createChooser(intent, "Share image via"));
    }

    private Intent getIntent(String subject){
        Intent intent = new Intent();
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        intent.setAction(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setType("image/png");
        return intent;
    }

    private File getScreenshotFile(File imageFile, View view) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
        getScreenshotBitmap(view).compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
        fileOutputStream.flush();
        fileOutputStream.close();
        return imageFile;
    }

    private View getRootView() {
        return Objects.requireNonNull(getCurrentActivity()).getWindow().getDecorView().findViewById(android.R.id.content);
    }

    private View getPartialView(String id){
        return ReactFindViewUtil.findView(getRootView(), id);
    }

    private Bitmap getScreenshotBitmap(View view) {
        view.setDrawingCacheEnabled(true);
        // Create bitmap from the screenshot and return it
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }


    private File getStorageDirectory() {
        return reactContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    }

    private File getTempImageFile(String filename) throws IOException {
        return File.createTempFile(filename, ".png", getStorageDirectory());
    }

    private Uri getImageUri(File imageFile) {
        return FileProvider.getUriForFile(reactContext, Objects.requireNonNull(getCurrentActivity()).getPackageName() + ".provider", imageFile);
    }
}