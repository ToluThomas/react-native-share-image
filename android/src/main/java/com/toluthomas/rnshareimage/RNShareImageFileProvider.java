package com.toluthomas.rnshareimage;

import androidx.core.content.FileProvider;

/**
 * The library's own FileProvider. It's a separate subclass with its own authority
 * ({@code <applicationId>.rnshareimage.fileprovider}) so it never conflicts with a
 * FileProvider the app or another library declares.
 */
public class RNShareImageFileProvider extends FileProvider {
}
