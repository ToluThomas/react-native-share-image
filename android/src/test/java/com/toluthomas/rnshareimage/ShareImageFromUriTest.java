package com.toluthomas.rnshareimage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Looper;

import androidx.core.content.FileProvider;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 35)
public class ShareImageFromUriTest {

    private ActivityController<Activity> controller;
    private Activity activity;
    private RNShareImage module;

    @Before
    public void setUp() throws Exception {
        clearFileProviderCache();
        controller = Robolectric.buildActivity(Activity.class).setup();
        activity = controller.get();
        // ReactApplicationContext is abstract. Its real (non-abstract) methods are all the module
        // needs here: the current activity, cache folder, content resolver and package name.
        ReactApplicationContext reactContext = mock(
            ReactApplicationContext.class,
            withSettings().useConstructor(activity.getApplication()).defaultAnswer(CALLS_REAL_METHODS));
        reactContext.onHostResume(activity);
        module = new RNShareImage(reactContext);
    }

    /**
     * FileProvider caches each authority's folder roots in a static map for the life of the JVM.
     * Robolectric gives every test a new data folder, so without this the second test would
     * look for files under the first test's folder. A real app's cache folder never moves.
     */
    private static void clearFileProviderCache() throws Exception {
        Field cache;
        try {
            cache = FileProvider.class.getDeclaredField("sCache");
        } catch (NoSuchFieldException e) {
            throw new AssertionError("FileProvider.sCache not found; update clearFileProviderCache() for this androidx.core version", e);
        }
        cache.setAccessible(true);
        synchronized (cache.get(null)) {
            ((Map<?, ?>) cache.get(null)).clear();
        }
    }

    @After
    public void tearDown() {
        module.invalidate();
        controller.destroy();
    }

    /** Runs main-thread work until the promise has been settled by the background thread. */
    private Intent shareAndGetChooser(String uri, Promise promise) {
        module.shareImageFromUri(uri, "Look at this", "Share via", promise);
        long deadline = System.currentTimeMillis() + 5000;
        Intent started = null;
        while (started == null && System.currentTimeMillis() < deadline) {
            shadowOf(Looper.getMainLooper()).idle();
            started = shadowOf(activity).getNextStartedActivity();
        }
        if (started == null) {
            fail("No share chooser was started. Promise calls: "
                + mockingDetails(promise).printInvocations());
        }
        return started;
    }

    private File writeImage(File dir, String name) throws IOException {
        File file = new File(dir, name);
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xD9});
        }
        return file;
    }

    @Test
    public void localFileUri_isSharedAsContentUriWithItsRealType() throws IOException {
        File image = writeImage(activity.getFilesDir(), "holiday photo.jpg");
        Promise promise = mock(Promise.class);

        Intent chooser = shareAndGetChooser(Uri.fromFile(image).toString(), promise);

        assertNotNull("Expected the share chooser to be started", chooser);
        assertEquals(Intent.ACTION_CHOOSER, chooser.getAction());
        assertTrue((chooser.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0);

        Intent share = chooser.getParcelableExtra(Intent.EXTRA_INTENT, Intent.class);
        assertNotNull(share);
        assertEquals(Intent.ACTION_SEND, share.getAction());
        assertEquals("image/jpeg", share.getType());
        assertEquals("Look at this", share.getStringExtra(Intent.EXTRA_TEXT));

        Uri stream = share.getParcelableExtra(Intent.EXTRA_STREAM, Uri.class);
        assertNotNull(stream);
        assertEquals("content", stream.getScheme());
        assertEquals(activity.getPackageName() + RNShareImage.FILE_PROVIDER_AUTHORITY_SUFFIX, stream.getAuthority());

        // The file was copied into the library's cache folder under a safe name.
        File copied = new File(new File(activity.getCacheDir(), "rnshareimage"), "holiday_photo.jpg");
        assertTrue("Expected a copy at " + copied, copied.isFile());

        verify(promise).resolve(null);
        verify(promise, never()).reject(anyString(), anyString(), any(Throwable.class));
    }

    @Test
    public void bareAbsolutePath_isTreatedAsLocalFile() throws IOException {
        File image = writeImage(activity.getFilesDir(), "logo.png");
        Promise promise = mock(Promise.class);

        Intent chooser = shareAndGetChooser(image.getAbsolutePath(), promise);

        assertNotNull(chooser);
        Intent share = chooser.getParcelableExtra(Intent.EXTRA_INTENT, Intent.class);
        assertEquals("image/png", share.getType());
        assertEquals("content", share.getParcelableExtra(Intent.EXTRA_STREAM, Uri.class).getScheme());
        verify(promise).resolve(null);
    }

    @Test
    public void missingLocalFile_rejectsWithDownloadError() {
        Promise promise = mock(Promise.class);

        module.shareImageFromUri("file:///does/not/exist.jpg", "msg", "title", promise);

        verify(promise, timeout(5000)).reject(eq("E_DOWNLOAD_FAILED"), anyString(), any(Throwable.class));
        verify(promise, never()).resolve(any());
    }
}
