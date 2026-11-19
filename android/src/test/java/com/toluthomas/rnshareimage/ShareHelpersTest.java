package com.toluthomas.rnshareimage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 35)
public class ShareHelpersTest {

    @Test
    public void sanitizeFilename_keepsSafeNames() {
        assertEquals("my-screenshot_1.png", RNShareImage.sanitizeFilename("my-screenshot_1.png", "fallback"));
    }

    @Test
    public void sanitizeFilename_cannotLeaveTheFolder() {
        String name = RNShareImage.sanitizeFilename("../../files/secret", "fallback");
        assertFalse(name.contains("/"));
        assertFalse(name.startsWith("."));
    }

    @Test
    public void sanitizeFilename_replacesSpacesAndSymbols() {
        assertEquals("holiday_photo__1_", RNShareImage.sanitizeFilename("holiday photo (1)", "fallback"));
    }

    @Test
    public void sanitizeFilename_usesFallbackWhenNothingIsLeft() {
        assertEquals("fallback", RNShareImage.sanitizeFilename("", "fallback"));
        assertEquals("fallback", RNShareImage.sanitizeFilename(null, "fallback"));
        assertEquals("fallback", RNShareImage.sanitizeFilename("...", "fallback"));
    }

    @Test
    public void getMimeTypeFromName_detectsCommonImageTypes() {
        assertEquals("image/jpeg", RNShareImage.getMimeTypeFromName("photo.jpg"));
        assertEquals("image/jpeg", RNShareImage.getMimeTypeFromName("photo.JPEG"));
        assertEquals("image/png", RNShareImage.getMimeTypeFromName("logo.png"));
        assertEquals("image/webp", RNShareImage.getMimeTypeFromName("banner.webp"));
    }

    @Test
    public void getMimeTypeFromName_fallsBackToAnyImage() {
        assertEquals("image/*", RNShareImage.getMimeTypeFromName("no-extension"));
        assertEquals("image/*", RNShareImage.getMimeTypeFromName("file.unknownext"));
    }

    @Test
    public void getFilenameFromUrl_dropsQueryAndFragment() {
        assertEquals("logo.png", RNShareImage.getFilenameFromUrl("https://example.com/img/logo.png?v=2#top", null));
    }

    @Test
    public void getFilenameFromUrl_addsExtensionFromContentType() {
        assertEquals("avatar.jpg", RNShareImage.getFilenameFromUrl("https://example.com/users/avatar", "image/jpeg; charset=binary"));
    }

    @Test
    public void getFilenameFromUrl_defaultsToPng() {
        assertEquals("avatar.png", RNShareImage.getFilenameFromUrl("https://example.com/users/avatar", null));
    }
}
