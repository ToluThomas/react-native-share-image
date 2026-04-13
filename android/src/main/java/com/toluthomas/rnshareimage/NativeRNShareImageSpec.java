package com.toluthomas.rnshareimage;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.turbomodule.core.interfaces.TurboModule;

public abstract class NativeRNShareImageSpec extends ReactContextBaseJavaModule implements TurboModule {

    public NativeRNShareImageSpec(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    public abstract void shareScreenshot(@Nullable String id, String message, String filename, String shareTitle);

    public abstract void shareImageFromUri(String imageUri, String message, String shareTitle);
}
