
package com.toluthomas.rnshareimage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.FileProvider;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
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
    public void shareScreenshot() {
        Intent intent = getIntent();
        try {
            File imageFile = getScreenshotFile(getTempImageFile());
            Uri imageUri = getImageUri(imageFile);
            intent.putExtra(Intent.EXTRA_STREAM, imageUri);
            Objects.requireNonNull(getCurrentActivity()).startActivity(Intent.createChooser(intent, "Share receipt using ..."));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Share image from content Uri
    @ReactMethod
    public void shareImageFromUri(String imageUri){
        Intent intent = getIntent();
        intent.putExtra(Intent.EXTRA_STREAM, imageUri);
        Objects.requireNonNull(getCurrentActivity()).startActivity(Intent.createChooser(intent, "Share receipt using ..."));
    }

    private Intent getIntent(){
        String MESSAGE_SUBJECT = "Quickteller Receipt";
        Intent intent = new Intent();
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, MESSAGE_SUBJECT);
        intent.setAction(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setType("image/png");
        return intent;
    }

    private File getScreenshotFile(File imageFile) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
        getScreenshotBitmap().compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
        fileOutputStream.flush();
        fileOutputStream.close();
        return imageFile;
    }

    private View getRootView() {
        return Objects.requireNonNull(getCurrentActivity()).findViewById(android.R.id.content);
    }

    private View getCurrentView(){
        ViewGroup rootViewGroup = (ViewGroup) getRootView();
        return rootViewGroup.getChildAt(0);
    }

    private Bitmap getScreenshotBitmap() {
        View screenView = getCurrentView();
        screenView.setDrawingCacheEnabled(true);
        // Create bitmap from the screenshot and return it
        Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
        screenView.setDrawingCacheEnabled(false);
        System.out.println("Screenshot Bitmap: " + bitmap.toString());
        return bitmap;
    }

    private String getFileName() {
        //Image file is named as a datetime and saved as a png file
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.UK).format(new Date());
        return "Quickteller_" + timeStamp + "_";
    }

    private File getStorageDirectory() {
        return reactContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    }

    private File getTempImageFile() throws IOException {
        return File.createTempFile(this.getFileName(), ".png", getStorageDirectory());
    }

    private Uri getImageUri(File imageFile) {
        return FileProvider.getUriForFile(reactContext, Objects.requireNonNull(getCurrentActivity()).getPackageName() + ".provider", imageFile);
    }
}